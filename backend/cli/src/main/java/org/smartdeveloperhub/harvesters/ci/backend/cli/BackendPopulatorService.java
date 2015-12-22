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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendFacade;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.jenkins.crawler.CrawlingStrategy;
import org.smartdeveloperhub.jenkins.crawler.OperationStrategy;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class BackendPopulatorService extends AbstractExecutionThreadService {

	private final class EventDumper implements EntityLifecycleEventListener {
		@Override
		public void onEvent(final EntityLifecycleEvent event) {
			Consoles.
				defaultConsole().
					printf(
						"[%s] %s %s %s%n",
						event.ocurredOn(),
						event.state(),
						event.entityType(),
						event.entityId());
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendPopulatorService.class);

	private final BackendConfig config;
	private final BackendFacade facade;
	private JenkinsIntegrationService jis;

	BackendPopulatorService(final BackendConfig config, final BackendFacade facade) {
		this.config = config;
		this.facade = facade;
	}

	@Override
	protected void run() throws Exception {
		setUp();
		execute();
		tearDown();
	}

	@Override
	protected void triggerShutdown() {
		LOGGER.info("Requested population termination...");
	}

	private void execute() throws IOException {
		LOGGER.info("Starting population...");
		this.jis.registerListener(new EventDumper());
		this.jis.connect(URI.create("https://ci.jenkins-ci.org/"));
		this.jis.awaitCrawlingCompletion();
	}

	private void tearDown() {
		try {
			LOGGER.info("Terminating population...");
			this.jis.disconnect();
			LOGGER.info("Population completed.");
		} catch (final IOException e) {
			LOGGER.error("Could not disconnect integration service. Full stacktrace follows",e);
		}
	}

	private void setUp() {
		LOGGER.info("Preparing backend...");
		final File tmpDirectory=new File(this.config.getWorkingDirectory());
		this.jis=this.facade.integrationService();
		this.jis.
			setWorkingDirectory(tmpDirectory).
			setCrawlingStrategy(
				CrawlingStrategy.
					builder().
						includeJob("jenkins_main_trunk").
						includeJob("jenkins_pom").
						includeJob("maven-interceptors").
						includeJob("tools_maven-hpi-plugin").
						includeJob("infra_extension-indexer").
						build()).
			setOperationStrategy(
				OperationStrategy.
					builder().
						run().
							once().
						build());
		LOGGER.info("Backend ready.");
	}

}
