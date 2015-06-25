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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Phaser;

import org.smartdeveloperhub.harvesters.ci.backend.core.ApplicationRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.port.jenkins.JenkinsIntegrationService;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class BackendPopulatorService extends AbstractExecutionThreadService {

	private final BackendConfig config;
	private final ApplicationRegistry registry;
	private JenkinsIntegrationService jis;
	private Phaser phaser;

	BackendPopulatorService(BackendConfig config, ApplicationRegistry applicationRegistry) {
		this.config = config;
		this.registry = applicationRegistry;
		this.phaser = new Phaser(2);
	}

	@Override
	protected void run() throws Exception {
		File tmpDirectory=new File(this.config.getWorkingDirectory());
		ContinuousIntegrationService cis =
				new ContinuousIntegrationService(
					this.registry.getServiceRepository(),
					this.registry.getBuildRepository(),
					this.registry.getExecutionRepository());
		this.jis=
			new JenkinsIntegrationService(
				cis,
				this.registry.getTransactionManager()).
				setWorkingDirectory(tmpDirectory);
		this.jis.connect(URI.create("http://ci.jenkins-ci.org/"));
		this.phaser.arriveAndAwaitAdvance();
		try {
			this.jis.disconnect();
		} catch (IOException e) {
			Consoles.defaultConsole().printf("Could not disconnect integration service. Full stacktrace follows");
			e.printStackTrace(Consoles.defaultConsole().writer());
		}

	}

	@Override
	protected void triggerShutdown() {
		this.phaser.arrive();
	}



}
