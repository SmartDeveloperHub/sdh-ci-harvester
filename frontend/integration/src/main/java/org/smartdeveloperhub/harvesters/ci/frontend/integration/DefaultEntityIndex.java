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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-integration:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-integration-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.integration;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ExecutionEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EnrichedExecution;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class DefaultEntityIndex implements EntityIndex {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultEntityIndex.class);

	private final TransactionManager txManager;
	private final ContinuousIntegrationService cis;

	private final EnrichmentService es;

	DefaultEntityIndex(final TransactionManager txManager, final ContinuousIntegrationService cis, final EnrichmentService es) {
		this.txManager = txManager;
		this.cis = cis;
		this.es = es;
	}

	private void rollbackQuietly(final URI id, final Transaction transaction) {
		if(transaction!=null) {
			try {
				transaction.rollback();
			} catch (final TransactionException e) {
				LOGGER.error("Could not rollback transaction for the retrieval of {}",id,e);
			}
		}
	}

	private void logFailure(final URI id, final TransactionException e) {
		LOGGER.error("Could not begin transaction for retrieving {}",id,e);
	}

	@Override
	public Service findService(final URI serviceId) {
		Service result=null;
		final Transaction transaction=this.txManager.currentTransaction();
		try {
			transaction.begin();
			result=this.cis.getService(serviceId);
		} catch(final TransactionException e) {
			logFailure(serviceId,e);
		} finally {
			rollbackQuietly(serviceId,transaction);
		}
		return result;
	}

	@Override
	public Build findBuild(final URI buildId) {
		Build result=null;
		final Transaction transaction=this.txManager.currentTransaction();
		try {
			transaction.begin();
			result=this.cis.getBuild(buildId);
		} catch(final TransactionException e) {
			logFailure(buildId,e);
		} finally {
			rollbackQuietly(buildId,transaction);
		}
		return result;
	}

	@Override
	public Execution findExecution(final URI executionId) {
		Execution result=null;
		final Transaction transaction=this.txManager.currentTransaction();
		try {
			transaction.begin();
			result=this.cis.getExecution(executionId);
		} catch(final TransactionException e) {
			logFailure(executionId,e);
		} finally {
			rollbackQuietly(executionId,transaction);
		}
		return result;
	}

	@Override
	public EnrichedExecution findEnrichedExecution(final URI executionId) {
		EnrichedExecution result=null;
		final Transaction transaction=this.txManager.currentTransaction();
		try {
			transaction.begin();
			final Execution execution=this.cis.getExecution(executionId);
			final ExecutionEnrichment enrichment = this.es.getEnrichment(execution);
			result=new DelegatedEnrichedExecution(enrichment, execution);
		} catch(final TransactionException e) {
			logFailure(executionId,e);
		} finally {
			rollbackQuietly(executionId,transaction);
		}
		return result;
	}
}