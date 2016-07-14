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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.3.0
 *   Bundle      : ci-frontend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.io.IOException;
import java.net.URI;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendConfig;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ResolverService;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryBuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryServiceRepository;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendControllerFactory;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EnrichedExecution;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

import com.google.common.base.Optional;

final class BackendControllerManager {

	private static final class UnknownBackendController implements BackendController {

		private UnsupportedOperationException getFailure() {
			return new UnsupportedOperationException("Method not implemented yet");
		}

		@Override
		public boolean setTargetService(final URI instance) {
			throw getFailure();
		}

		@Override
		public void setExecutionResolver(final ResolverService resolver) {
			throw getFailure();
		}

		@Override
		public void connect(final EntityLifecycleEventListener listener) {
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
			private final class NullEnrichedExecution implements
					EnrichedExecution {
				private final Execution target;

				private NullEnrichedExecution(final Execution target) {
					this.target = target;
				}

				@Override
				public Execution target() {
					return this.target;
				}

				@Override
				public Optional<URI> repositoryResource() {
					return Optional.absent();
				}

				@Override
				public Optional<URI> branchResource() {
					return Optional.absent();
				}

				@Override
				public Optional<URI> commitResource() {
					return Optional.absent();
				}
			}

			@Override
			public Service findService(final URI serviceId) {
				return NullBackendController.this.service.getService(serviceId);
			}

			@Override
			public Execution findExecution(final URI executionId) {
				return NullBackendController.this.service.getExecution(executionId);
			}

			@Override
			public Build findBuild(final URI buildId) {
				return NullBackendController.this.service.getBuild(buildId);
			}

			@Override
			public EnrichedExecution findEnrichedExecution(final URI executionId) {
				return new NullEnrichedExecution(findExecution(executionId));
			}
		}

		private final ContinuousIntegrationService service;

		private NullBackendController() {
			this.service = new ContinuousIntegrationService(new InMemoryServiceRepository(), new InMemoryBuildRepository(), new InMemoryExecutionRepository());
		}

		@Override
		public boolean setTargetService(final URI instance) {
			return false;
		}

		@Override
		public void setExecutionResolver(final ResolverService resolver) {
			// NOTHING TO DO
		}

		@Override
		public void connect(final EntityLifecycleEventListener listener) throws IOException {
			// NOTHING TO DO
		}

		@Override
		public EntityIndex entityIndex() {
			return new NullEntityIndex();
		}

		@Override
		public void disconnect() {
			// NOTHING TO DO
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendControllerManager.class);

	private BackendControllerManager() {
	}

	public static BackendController create(final String providerId, final BackendConfig config) {
		BackendController result = new NullBackendController();
		if(providerId!=null && config!=null) {
			result=loadBackend(providerId,config);
		}
		return result;
	}

	private static BackendController loadBackend(final String providerId, final BackendConfig cfg) {
		final ServiceLoader<BackendControllerFactory> providers=ServiceLoader.load(BackendControllerFactory.class);
		for(final BackendControllerFactory provider:providers) {
			LOGGER.debug("Trying to create backend controller {} using provider {}...",providerId,provider.getClass().getCanonicalName());
			try {
				final BackendController controller = provider.create(providerId,cfg);
				if(controller!=null) {
					LOGGER.debug("Created backend controller {} via {}",providerId,provider.getClass().getCanonicalName());
					return controller;
				}
				LOGGER.debug("Could not create backend controller {} using provider {}",providerId,provider.getClass().getCanonicalName());
			} catch (final Exception e) {
				LOGGER.warn("Provider {} failed while creating a backend controller {}. Full stacktrace follows",provider.getClass().getCanonicalName(),providerId,e);
			}
		}
		return new UnknownBackendController();
	}

}