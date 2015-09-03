/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015 Center for Open Middleware.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:0.1.0
 *   Bundle      : ci-util-bootstrap-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import java.io.File;
import java.io.FileWriter;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterables;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;


public class AbstractBootstrapTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	private File badConfig() throws Exception {
		File configYaml = yamlConfigFile();
		try(FileWriter writer=new FileWriter(configYaml)) {
			writer.append("Some bad config...");
			writer.flush();
			return configYaml;
		}
	}

	private File yamlConfig(CustomConfig config) throws Exception {
		File configYaml = yamlConfigFile();
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		mapper.writeValue(configYaml, config);
		return configYaml;
	}

	private File yamlConfigFile() throws Exception {
		return testFolder.newFile(this.testName.getMethodName()+"Config.yml");
	}

	private CustomBootstrap doBootstrap(CustomConfig config) throws Exception, BootstrapException {
		CustomBootstrap bs = new CustomBootstrap();
		bs.run(prepareConfiguration(config));
		return bs;
	}

	private String[] prepareConfiguration(CustomConfig config) throws Exception {
		File configYaml=yamlConfig(config);
		String[] args={configYaml.getAbsolutePath()};
		System.out.println(this.testName.getMethodName()+" {"+config+"} --> "+configYaml.getAbsolutePath());
		return args;
	}

	private void verifyInitializationFailure(ApplicationInitializationException e, String failure) {
		Set<String> failedServices = e.failedServices();
		assertThat(failedServices,hasSize(1));
		String failedService = Iterables.get(failedServices,0);
		assertThat(failedService,startsWith("CustomApplicationService"));
		Throwable serviceFailure = e.serviceFailure(failedService);
		assertThat(serviceFailure,notNullValue());
		assertThat(serviceFailure.getMessage(),equalTo(failure));
	}

	private void verifyShutdownFailure(ApplicationShutdownException e, String failure) {
		Set<String> failedServices = e.failedServices();
		assertThat(failedServices,hasSize(1));
		String failedService = Iterables.get(failedServices,0);
		assertThat(failedService,startsWith("CustomApplicationService"));
		Throwable serviceFailure = e.serviceFailure(failedService);
		assertThat(serviceFailure,notNullValue());
		assertThat(serviceFailure.getMessage(),equalTo(failure));
	}



	@Test
	public void testGetters() throws Exception {
		CustomBootstrap bs1 = new CustomBootstrap();
		CustomBootstrap bs2 = new CustomBootstrap();
		assertThat(bs1.applicationName(),equalTo(CustomBootstrap.NAME));
		assertThat(bs1.id(),startsWith("CustomApplication"));
		assertThat(bs2.applicationName(),equalTo(bs2.applicationName()));
		assertThat(bs2.id(),not(equalTo(bs1.id())));
	}

	@Test
	public void testBadConfig() throws Exception {
		File badConfig=badConfig();
		CustomBootstrap bs1 = new CustomBootstrap();
		try {
			bs1.run(new String[]{badConfig.getAbsolutePath()});
			fail("Should not start");
		} catch (BootstrapException e) {
			assertThat(e,not(instanceOf(ApplicationInitializationException.class)));
		}
	}

	@Test(expected=IllegalArgumentException.class)
	public void testBadArgs() throws Exception {
		new CustomBootstrap().run(new String[]{"1","2"});
	}

	@Test(expected=IllegalStateException.class)
	public void testStartOverStarted() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);

		CustomBootstrap bs = doBootstrap(config);
		bs.run(new String[]{"1"});
	}

	@Test
	public void testLifecycle() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);

		CustomBootstrap bs = doBootstrap(config);

		CustomApplicationService controller = bs.service();
		try {
			assertThat(controller.isRunning(),equalTo(true));
			assertThat(controller.config(),equalTo(config));
		} finally {
			try {
				bs.terminate();
			} finally {
				assertThat(controller.isRunning(),equalTo(false));
			}
		}
	}

	@Test
	public void testDelayInitialization() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setDelayInitialization(true);
		try {
			doBootstrap(config);
			fail("Should not start");
		} catch (ApplicationInitializationException e) {
			Set<String> failedServices = e.failedServices();
			assertThat(failedServices,hasSize(0));
		}
	}

	@Test
	public void testFailBootstrapInitialization() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setFailBootstrapStartUp(true);
		try {
			doBootstrap(config);
			fail("Should not start");
		} catch (ApplicationInitializationException e) {
			Set<String> failedServices = e.failedServices();
			assertThat(failedServices,hasSize(0));
		}
	}

	@Test
	public void testFailServiceInitialization() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setFailServiceStartUp(true);
		try {
			doBootstrap(config);
			fail("Should not start");
		} catch (ApplicationInitializationException e) {
			e.printStackTrace();
			verifyInitializationFailure(e,CustomConfig.FAILED_SERVICE_START_UP_MESSAGE);
		}
	}

	@Test
	public void testFailServiceInitializationWithBootstrapShutdownFailure() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setFailServiceStartUp(true);
		config.setFailBootstrapShutdown(true);

		try {
			doBootstrap(config);
			fail("Should not start");
		} catch (ApplicationInitializationException e) {
			e.printStackTrace();
			verifyInitializationFailure(e,CustomConfig.FAILED_SERVICE_START_UP_MESSAGE);
		}
	}

	@Test
	public void testFailBootstrapShutdown() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setFailBootstrapShutdown(true);

		CustomBootstrap bs;
		try {
			bs = doBootstrap(config);
			bs.terminate();
			fail("Should not terminate");
		} catch (ApplicationShutdownException e) {
			Set<String> failedServices = e.failedServices();
			assertThat(failedServices,hasSize(0));
		}
	}

	@Test
	public void testFailServiceShutdown() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setFailServiceShutdown(true);

		CustomBootstrap bs;
		try {
			bs = doBootstrap(config);
			bs.terminate();
			fail("Should not terminate");
		} catch (ApplicationShutdownException e) {
			verifyShutdownFailure(e,CustomConfig.FAILED_SERVICE_SHUTDOWN_MESSAGE);
		}
	}

	@Test
	public void testFailFullShutdown() throws Exception {
		CustomConfig config = new CustomConfig();
		config.setSetting(3);
		config.setFailBootstrapShutdown(true);
		config.setFailServiceShutdown(true);

		CustomBootstrap bs;
		try {
			bs = doBootstrap(config);
			bs.terminate();
			fail("Should not terminate");
		} catch (ApplicationShutdownException e) {
			verifyShutdownFailure(e, "failedServiceShutdown");
		}
	}

}
