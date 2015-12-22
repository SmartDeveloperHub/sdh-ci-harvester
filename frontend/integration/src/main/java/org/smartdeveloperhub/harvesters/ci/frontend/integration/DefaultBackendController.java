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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-integration:0.2.0
 *   Bundle      : ci-frontend-integration-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.integration;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendFacade;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ResolverService;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class DefaultBackendController implements BackendController {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultBackendController.class);

	private final BackendFacade backendFacade;

	private URI jenkinsInstance;

	private final DefaultEntityIndex index;

	private ResolverService resolver;

	DefaultBackendController(final BackendFacade backendFacade) {
		this.backendFacade = backendFacade;
		this.index=
			new DefaultEntityIndex(
				this.backendFacade.transactionManager(),
				this.backendFacade.applicationService(),
				this.backendFacade.enrichmentService());

	}

	private JenkinsIntegrationService integrationService() {
		return this.backendFacade.integrationService();
	}

	private void rollbackQuietly(final Transaction tx) {
		if(tx.isActive()) {
			try {
				tx.rollback();
			} catch (final TransactionException e) {
				LOGGER.warn("Could not discard transaction",e);
			}
		}
	}

	private boolean hasTargetService(final URI instance) {
		return
			this.backendFacade.
				applicationService().
					getRegisteredServices().
						contains(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityIndex entityIndex() {
		return this.index;
	}


	/**
	 * {@inheritDoc}
	 * @return
	 */
	@Override
	public boolean setTargetService(final URI instance) {
		checkNotNull(instance,"Target service cannot be null");
		checkState(this.jenkinsInstance==null,"Target service already defined (%s)",this.jenkinsInstance);
		LOGGER.info("Setting up target service to {}...",instance);
		final Transaction tx = this.backendFacade.transactionManager().currentTransaction();
		boolean result=false;
		try {
			tx.begin();
			try {
				if(!hasTargetService(instance)) {
					this.backendFacade.applicationService().
						registerService(
							RegisterServiceCommand.create(instance));
					tx.commit();
				}
				result=true;
				this.jenkinsInstance=instance;
			} finally {
				rollbackQuietly(tx);
			}
		} catch (final TransactionException e) {
			LOGGER.error("Could not create target service {}",instance,e);
		}
		return result;
	}

	@Override
	public void setExecutionResolver(final ResolverService resolver) {
		checkNotNull(resolver,"Execution resolver cannot be null");
		this.resolver=resolver;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(final EntityLifecycleEventListener listener) throws IOException {
		checkState(this.jenkinsInstance!=null,"No target service defined");
		checkState(this.resolver!=null,"No execution resolver defined");
		LOGGER.info("Connecting to {}...",this.jenkinsInstance);
		try {
			integrationService().registerListener(listener);
			integrationService().setResolverService(this.resolver);
			integrationService().connect(this.jenkinsInstance);
		} catch (final IOException e) {
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
		} catch (final IOException e) {
			LOGGER.error("Could not close backend properly",e);
		}
	}

}
