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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryBuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryServiceRepository;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendControllerFactory;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class BackendControllerManager {

	private static final class UnknownBackendController implements BackendController {

		private UnsupportedOperationException getFailure() {
			return new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public void connect(URI instance, EntityLifecycleEventListener listener) {
			throw getFailure();
		}

		@Override
		public void disconnect() {
			throw getFailure();
		}

		@Override
		public EntityIndex entityIndex() {
			throw getFailure();
		}

	}

	private static final class NullBackendController implements BackendController {

		private final class NullEntityIndex implements EntityIndex {
			@Override
			public Service findService(URI serviceId) {
				return service.getService(serviceId);
			}

			@Override
			public Execution findExecution(URI executionId) {
				return service.getExecution(executionId);
			}

			@Override
			public Build findBuild(URI buildId) {
				return service.getBuild(buildId);
			}
		}

		private ContinuousIntegrationService service;

		private NullBackendController() {
			this.service = new ContinuousIntegrationService(new InMemoryServiceRepository(), new InMemoryBuildRepository(), new InMemoryExecutionRepository());
		}

		@Override
		public void disconnect() {
			// NOTHING TO DO
		}

		@Override
		public void connect(URI instance, EntityLifecycleEventListener listener) throws IOException {
			// NOTHING TO DO
		}

		@Override
		public EntityIndex entityIndex() {
			return new NullEntityIndex();
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendControllerManager.class);

	private BackendControllerManager() {
	}

	public static BackendController create(String providerId) {
		if(providerId==null) {
			return new NullBackendController();
		}
		ServiceLoader<BackendControllerFactory> providers=ServiceLoader.load(BackendControllerFactory.class);
		for(BackendControllerFactory provider:providers) {
			LOGGER.debug("Trying to create backend controller {} using provider {}...",providerId,provider.getClass().getCanonicalName());
			try {
				BackendController controller = provider.create(providerId);
				if(controller!=null) {
					LOGGER.debug("Created backend controller {} via {}",providerId,provider.getClass().getCanonicalName());
					return controller;
				}
				LOGGER.debug("Could not create backend controller {} using provider {}",providerId,provider.getClass().getCanonicalName());
			} catch (Exception e) {
				LOGGER.warn("Provider {} failed while creating a backend controller {}. Full stacktrace follows",provider.getClass().getCanonicalName(),providerId,e);
			}
		}
		return new UnknownBackendController();
	}

}
