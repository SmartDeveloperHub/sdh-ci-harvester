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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendControllerFactory;

final class BackendControllerManager {

	private static final class NullBackendController implements BackendController {

		@Override
		public void disconnect() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public ContinuousIntegrationService continuousIntegrationService() {
			throw new UnsupportedOperationException("Method not implemented yet");
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendControllerManager.class);

	private BackendControllerManager() {
	}

	public static BackendController connect(URI jenkinsInstance) {
		ServiceLoader<BackendControllerFactory> providers=ServiceLoader.load(BackendControllerFactory.class);
		for(BackendControllerFactory provider:providers) {
			LOGGER.debug("Trying to create backend controller for {} using provider {}...",jenkinsInstance,provider.getClass().getCanonicalName());
			try {
				BackendController controller = provider.create(jenkinsInstance);
				if(controller!=null) {
					LOGGER.debug("Backend controller for {} created via {}",jenkinsInstance,provider.getClass().getCanonicalName());
					return controller;
				}
				LOGGER.debug("Could not create backend controller for {} using provider {}",jenkinsInstance,provider.getClass().getCanonicalName());
			} catch (Exception e) {
				LOGGER.warn("Provider {} failed while creating a backend controller for {}. Full stacktrace follows",provider.getClass().getCanonicalName(),jenkinsInstance,e);
			}
		}
		return new NullBackendController();
	}

}
