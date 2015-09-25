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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

public class SmokeTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(SmokeTest.class);

	@BeforeClass
	public static void setUpBefore() {
		File logFile = new File("target"+File.separator+"derby.log");
		System.setProperty("derby.stream.error.file", logFile.getAbsolutePath());
	}

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	private FileBasedStorage storage;
	private File tmpDirectory;

	protected void smokeTest(ContinuousIntegrationService cis, JenkinsIntegrationService jis) throws Exception {
		LOGGER.info("Starting smoke test...");
		this.tmpDirectory = new File("target","jenkins"+new Date().getTime());
		jis.setWorkingDirectory(tmpDirectory);
		LOGGER.info("Warming up...");
		jis.connect(URI.create("http://ci.jenkins-ci.org/"));
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch(InterruptedException e) {

		}
		jis.disconnect();
		LOGGER.info("Warm up completed.");
		doVerify(cis);
		LOGGER.info("Smoke test completed.");
	}


	private FileBasedStorage storage() throws IOException {
		if(this.storage==null) {
			File configFile = new File(this.tmpDirectory,"repository.xml");
			this.storage=
				FileBasedStorage.
					builder().
						withConfigFile(configFile).
						build();
		}
		return this.storage;
	}

	private void doVerify(ContinuousIntegrationService cis) throws Exception {
		LOGGER.info("Starting verification...");
		for(URI serviceId:cis.getRegisteredServices()) {
			Service service=cis.getService(serviceId);
			LOGGER.debug("- Service {} : {}",serviceId,service);
			assertThat(service,notNullValue());
			for(URI buildId:service.builds()) {
				Build build=cis.getBuild(buildId);
				LOGGER.debug("  + Build {} : {}",buildId,build);
				Job job=storage().entityOfId(buildId,JenkinsEntityType.JOB,Job.class);
				verifyBuildMatchesJob(buildId,build,job);
				for(URI executionId:build.executions()) {
					Execution execution=cis.getExecution(executionId);
					LOGGER.trace("    * Execution {} : {}",executionId,execution);
					Run run=storage().entityOfId(executionId,JenkinsEntityType.RUN,Run.class);
					verifyExecutionMatchesRun(executionId,execution,run);
				}
			}
		}
		LOGGER.info("Verification completed.");
	}

	private void verifyExecutionMatchesRun(URI executionId, Execution execution, Run run) {
		assertThat(String.format("Execution %s should exist",executionId),execution,notNullValue());
		assertThat(String.format("Run %s should exist",executionId),run,notNullValue());
		assertThat(String.format("{%s} Execution and run owner should match",executionId),run.getJob(),equalTo(execution.buildId()));
	}

	private void verifyBuildMatchesJob(URI buildId, Build build, Job job) {
		assertThat(String.format("Build %s should exist",buildId),build,notNullValue());
		assertThat(String.format("Job %s should exist",buildId),job,notNullValue());
		assertThat(String.format("{%s} Build and job owner should match",buildId),build.serviceId(),equalTo(job.getInstance()));
		assertThat(String.format("{%s} Build and job title should match",buildId),build.title(),equalTo(job.getTitle()));
		assertThat(String.format("{%s} Build and job description should match",buildId),build.description(),equalTo(job.getDescription()));
		Class<?> clazz=null;
		switch(job.getType()) {
			case FREE_STYLE_PROJECT:
				clazz=SimpleBuild.class;
				break;
			case MATRIX_CONFIGURATION:
			case MAVEN_MODULE:
				clazz=SubBuild.class;
				break;
			case MATRIX_PROJECT:
			case MAVEN_MODULE_SET:
				clazz=CompositeBuild.class;
				break;
			default:
				break;
		}
		assertThat(String.format("{%s} Build does not match job type",buildId),build,instanceOf(clazz));
	}

}
