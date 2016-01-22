/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.backend.domain.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

import com.google.common.collect.Sets;

public class SmokeTest {

	private static final String DEFAULT_TARGET = "https://ci.jenkins-ci.org/";

	private static final Logger LOGGER=LoggerFactory.getLogger(SmokeTest.class);

	@BeforeClass
	public static void setUpBefore() {
		final File logFile = new File("target"+File.separator+"derby.log");
		System.setProperty("derby.stream.error.file", logFile.getAbsolutePath());
	}

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	private FileBasedStorage storage;
	private File tmpDirectory;

	protected void smokeTest(final ContinuousIntegrationService cis, final JenkinsIntegrationService jis, final EnrichmentService es) throws Exception {
		LOGGER.info("Starting smoke test...");
		this.tmpDirectory = new File("target","jenkins"+new Date().getTime());
		jis.setWorkingDirectory(this.tmpDirectory);
		LOGGER.info("Warming up...");
		// Local: http://vps164.cesvima.upm.es:8000/
		jis.connect(URI.create(DEFAULT_TARGET));
		try {
			TimeUnit.SECONDS.sleep(60);
		} catch(final InterruptedException e) {
		}
		jis.disconnect();
		LOGGER.info("Warm up completed.");
		doVerify(cis);
		checkPendingEnrichments(cis,es);
		LOGGER.info("Smoke test completed.");
	}


	private void checkPendingEnrichments(final ContinuousIntegrationService cis, final EnrichmentService es) {
		LOGGER.info("Checking pending enrichments...");
		final Set<URI> executions=Sets.newLinkedHashSet();
		for(final PendingEnrichment pending:es.pendingEnrichments()) {
			LOGGER.debug("- [{}] <{},{},{}> {{}}",pending.id(),pending.repositoryLocation(),pending.branchName(),pending.commitId(),pending.executions().size());
			for(final URI id:pending.executions()) {
				final Execution execution = cis.getExecution(id);
				LOGGER.debug("  + Awaiting execution: {}",execution);
				if(execution.codebase().location()!=null) {
	 				assertThat("Repository location does not match "+pending+" vs. "+execution,pending.repositoryLocation(),equalTo(execution.codebase().location()));
				}
				if(execution.codebase().branchName()!=null) {
					assertThat("Branch name does not match "+pending+" vs. "+execution,pending.branchName(),equalTo(execution.codebase().branchName()));
				}
				if(execution.commitId()!=null) {
					assertThat("Commit id does not match "+pending+" vs. "+execution,pending.commitId(),equalTo(execution.commitId()));
				}
				assertThat(executions.add(id),equalTo(true));
			}
		}
		LOGGER.info("Pending enrichment check completed.");
	}


	private void doVerify(final ContinuousIntegrationService cis) throws Exception {
		LOGGER.info("Starting verification...");
		for(final URI serviceId:cis.getRegisteredServices()) {
			final Service service=cis.getService(serviceId);
			LOGGER.debug("- Service {} : {}",serviceId,service);
			assertThat(service,notNullValue());
			for(final URI buildId:service.builds()) {
				final Build build=cis.getBuild(buildId);
				LOGGER.debug("  + Build {} : {}",buildId,build);
				final Job job=storage().entityOfId(buildId,JenkinsEntityType.JOB,Job.class);
				verifyBuildMatchesJob(buildId,build,job);
				for(final URI executionId:build.executions()) {
					final Execution execution=cis.getExecution(executionId);
					LOGGER.debug("    * Execution {} : {}",executionId,execution);
					final Run run=storage().entityOfId(executionId,JenkinsEntityType.RUN,Run.class);
					verifyExecutionMatchesRun(executionId,execution,run);
				}
			}
		}
		LOGGER.info("Verification completed.");
	}

	private void verifyExecutionMatchesRun(final URI executionId, final Execution execution, final Run run) {
		assertThat(String.format("Execution %s should exist",executionId),execution,notNullValue());
		assertThat(String.format("Run %s should exist",executionId),run,notNullValue());
		assertThat(String.format("{%s} Execution and run owner should match",executionId),run.getJob(),equalTo(execution.buildId()));
	}

	private void verifyBuildMatchesJob(final URI buildId, final Build build, final Job job) {
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


	private FileBasedStorage storage() throws IOException {
		if(this.storage==null) {
			final File configFile = new File(this.tmpDirectory,"repository.xml");
			this.storage=
				FileBasedStorage.
					builder().
						withConfigFile(configFile).
						build();
		}
		return this.storage;
	}

}
