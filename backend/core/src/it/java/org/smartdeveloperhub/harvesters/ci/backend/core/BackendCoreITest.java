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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa.JPAApplicationRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.core.port.jenkins.JenkinsIntegrationService;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

import com.google.common.collect.ImmutableMap;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class BackendCoreITest {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendCoreITest.class);

	private static final String JDBC_DRIVER   = "javax.persistence.jdbc.driver";
	private static final String JDBC_URL      = "javax.persistence.jdbc.url";
	private static final String JDBC_USER     = "javax.persistence.jdbc.user";
	private static final String JDBC_PASSWORD = "javax.persistence.jdbc.password";

	public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String HSQLDB_URL = "jdbc:hsqldb:mem:testdb;sql.enforce_strict_size=true;hsqldb.tx=mvcc";
	public static final String HSQLDB_USER = "sa";
	public static final String HSQLDB_PASSWORD = ""; // NOSONAR

	private static EntityManagerFactory factory;
	private static JPAApplicationRegistry persistencyFacade;

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	private ExecutionRepository executionRepository;
	private BuildRepository buildRepository;
	private ServiceRepository serviceRepository;
	private FileBasedStorage storage;
	private File tmpDirectory;

	@BeforeClass
	public static void startUp() throws IOException {
		ImmutableMap<String, String> properties =
			ImmutableMap.
				<String,String>builder().
					put(JDBC_DRIVER, HSQLDB_DRIVER).
					put(JDBC_URL, HSQLDB_URL).
					put(JDBC_USER, HSQLDB_USER).
					put(JDBC_PASSWORD, HSQLDB_PASSWORD).
					build();
		factory = Persistence.createEntityManagerFactory("jpaPersistency",properties);
		persistencyFacade = new JPAApplicationRegistry(factory);
	}

	@AfterClass
	public static void shutDown() {
		if(factory!=null) {
			factory.close();
		}
	}

	@Test
	public void smokeTest() throws Exception {
		LOGGER.info("Starting smoke test...");
		this.tmpDirectory = new File("target","jenkins"+new Date().getTime());
		ContinuousIntegrationService cis =
				new ContinuousIntegrationService(
					serviceRepository(),
					buildRepository(),
					executionRepository());
		JenkinsIntegrationService jis=
			new JenkinsIntegrationService(
				cis,
				persistencyFacade.getTransactionManager()).
				setWorkingDirectory(tmpDirectory);
		LOGGER.info("Warming up...");
		jis.connect(URI.create("http://ci.jenkins-ci.org/"));
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch(InterruptedException e) {

		}
		jis.disconnect();
		LOGGER.info("Warm up completed.");
		doVerify();
		LOGGER.info("Smoke test completed.");
	}

	private ExecutionRepository executionRepository() {
		if(this.executionRepository==null) {
			this.executionRepository=persistencyFacade.getExecutionRepository();
		}
		return this.executionRepository;
	}

	private BuildRepository buildRepository() {
		if(this.buildRepository==null) {
			this.buildRepository=persistencyFacade.getBuildRepository();
		}
		return this.buildRepository;
	}

	private ServiceRepository serviceRepository() {
		if(this.serviceRepository==null) {
			this.serviceRepository = persistencyFacade.getServiceRepository();
		}
		return this.serviceRepository;
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

	private void doVerify() throws Exception {
		LOGGER.info("Starting verification...");
		for(URI serviceId:serviceRepository().serviceIds()) {
			Service service=serviceRepository().serviceOfId(serviceId);
			LOGGER.debug("- Service {} : {}",serviceId,service);
			assertThat(service,notNullValue());
			for(URI buildId:service.builds()) {
				Build build=buildRepository().buildOfId(buildId);
				LOGGER.debug("  + Build {} : {}",buildId,build);
				Job job=storage().entityOfId(buildId,JenkinsEntityType.JOB,Job.class);
				verifyBuildMatchesJob(buildId,build,job);
				for(URI executionId:build.executions()) {
					Execution execution=executionRepository().executionOfId(executionId);
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
		assertThat("Execution and run owner should match",run.getJob(),equalTo(execution.buildId()));
	}

	private void verifyBuildMatchesJob(URI buildId, Build build, Job job) {
		assertThat(String.format("Build %s should exist",buildId),build,notNullValue());
		assertThat(String.format("Job %s should exist",buildId),job,notNullValue());
		assertThat("Build and job owner should match",job.getInstance(),equalTo(build.serviceId()));
		assertThat("Build and job title should match",job.getTitle(),equalTo(build.title()));
		assertThat("Build and job description should match",job.getDescription(),equalTo(build.description()));
	}

}
