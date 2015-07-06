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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendFacade;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;

final class DefaultBackendController implements BackendController {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultBackendController.class);

	private final BackendFacade backendFacade;

	private URI jenkinsInstance;

	DefaultBackendController(BackendFacade backendFacade) {
		this.backendFacade = backendFacade;
	}

	private JenkinsIntegrationService integrationService() {
		return this.backendFacade.integrationService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContinuousIntegrationService continuousIntegrationService() {
		return this.backendFacade.applicationService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(URI instance, EntityLifecycleEventListener listener) throws IOException {
		checkState(this.jenkinsInstance==null,"Already connected");
		this.jenkinsInstance=instance;
		LOGGER.info("Connecting to {}...",this.jenkinsInstance);
		checkState(hasInstance(instance,this.backendFacade),"Could not connect to %s",instance);
		try {
			integrationService().registerListener(listener);
			integrationService().connect(instance);
		} catch (IOException e) {
			LOGGER.info("Could not connect to {}. Full stacktrace follows",this.jenkinsInstance,e);
			this.jenkinsInstance=null;
			throw e;
		}
		LOGGER.info("Connected to {}.",this.jenkinsInstance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		try {
			if(integrationService().isConnected()) {
				LOGGER.info("Disconnecting from {}...",this.jenkinsInstance);
				integrationService().disconnect();
				LOGGER.info("Disconnected from {}.",this.jenkinsInstance);
			}
			this.backendFacade.close();
		} catch (IOException e) {
			LOGGER.error("Could not close backend properly",e);
		}
	}

	private static boolean hasInstance(URI instance, BackendFacade backend) {
		return hasInstance(instance, backend, true);
	}

	private static boolean hasInstance(URI jenkinsInstance, BackendFacade backend, boolean create) {
		List<URI> availableServices=
			backend.
				applicationService().
					getRegisteredServices();
		boolean contains = availableServices.contains(jenkinsInstance);
		if(!contains) {
			if(create) {
				backend.
					applicationService().
						registerService(RegisterServiceCommand.create(jenkinsInstance));
				contains=hasInstance(jenkinsInstance,backend,false);
			} else {
				LOGGER.warn("Could not find instance {} ({})",jenkinsInstance,availableServices);
			}
		}
		return contains;
	}

}
