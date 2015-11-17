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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendFacade;
import org.smartdeveloperhub.util.bootstrap.AbstractBootstrap;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.Service;

public class BackendPopulator extends AbstractBootstrap<BackendConfig> {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendPopulator.class);

 	private static final String NAME = "BackendPopulator";

	private BackendPopulatorService service;

	private BackendConfig config;

	private BackendFacade facade;

	public BackendPopulator() {
		super(NAME,BackendConfig.class);
	}

	public static void main(final String[] args) throws Exception {
		final BackendPopulator bs = new BackendPopulator();
		bs.run(args);
		bs.terminate();
	}

	@Override
	protected void shutdown() {
		LOGGER.info("Shutting down application...");
		try {
			this.facade.close();
			Consoles.defaultConsole().printf("Retrieved data available at %s.",this.config.getDatabase().getLocation());
		} catch (final Exception e) {
			Consoles.defaultConsole().printf("Failed to shutdown the populator");
			LOGGER.error("Failed to shutdown the application. Full stacktrace follows",e);
		}
	}

	@Override
	protected Iterable<Service> getServices(final BackendConfig config) {
		Consoles.
			defaultConsole().
				printf("Using '%s' as working directory%n",config.getWorkingDirectory()).
				printf("Persisting retrieved data in '%s'%n",config.getDatabase().getLocation());
		this.config=config;
		final org.smartdeveloperhub.harvesters.ci.backend.BackendConfig backendConfig=new org.smartdeveloperhub.harvesters.ci.backend.BackendConfig();
		backendConfig.setDatabase(config.getDatabase());
		backendConfig.setEnrichment(config.getEnrichment());
		this.facade=BackendFacade.create(backendConfig);
		this.service=new BackendPopulatorService(config,this.facade);
		return Collections.<Service>singleton(this.service);
	}

}