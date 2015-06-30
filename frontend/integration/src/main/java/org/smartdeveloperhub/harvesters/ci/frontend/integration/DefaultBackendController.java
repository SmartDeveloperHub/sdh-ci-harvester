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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-integration:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-integration-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.integration;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendFacade;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;

final class DefaultBackendController implements BackendController {

	private final static Logger LOGGER=LoggerFactory.getLogger(DefaultBackendController.class);

	private final URI jenkinsInstance;

	private final BackendFacade backendFacade;

	DefaultBackendController(URI jenkinsInstance, BackendFacade backendFacade) {
		this.jenkinsInstance = jenkinsInstance;
		this.backendFacade = backendFacade;
		LOGGER.info("Connecting to {}...",this.jenkinsInstance);
		ContinuousIntegrationService cis = backendFacade.applicationService();
		List<URI> availableServices = cis.getRegisteredServices();
		if(!availableServices.contains(jenkinsInstance)) {
			throw new RuntimeException("Could not find instance "+jenkinsInstance+" ("+availableServices+")");
		}
		LOGGER.info("Connected.",this.jenkinsInstance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContinuousIntegrationService continuousIntegrationService() {
		return backendFacade.applicationService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		LOGGER.info("Disconnecting from {}...",this.jenkinsInstance);
		try {
			this.backendFacade.close();
		} catch (IOException e) {
			LOGGER.error("Could not close backend properly",e);
		} finally {
			LOGGER.info("Disconnected.",this.jenkinsInstance);
		}
	}

}
