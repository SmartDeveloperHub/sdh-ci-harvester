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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-integration:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-integration-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.integration;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class DefaultEntityIndex implements EntityIndex {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultEntityIndex.class);

	private final TransactionManager txManager;
	private final ContinuousIntegrationService cis;

	DefaultEntityIndex(TransactionManager txManager, ContinuousIntegrationService cis) {
		this.txManager = txManager;
		this.cis = cis;
	}

	private void commitQuietly(URI id, Transaction transaction) {
		if(transaction!=null) {
			try {
				transaction.commit();
			} catch (TransactionException e) {
				LOGGER.error("Could not commit transaction for the retrieval of {}",id,e);
			}
		}
	}

	private void logFailure(URI id, TransactionException e) {
		LOGGER.error("Could not begin transaction for retrieving {}",id,e);
	}

	@Override
	public Service findService(URI serviceId) {
		Service result=null;
		Transaction transaction = txManager.currentTransaction();
		try {
			transaction.begin();
			result=cis.getService(serviceId);
		} catch(TransactionException e) {
			logFailure(serviceId,e);
		} finally {
			commitQuietly(serviceId,transaction);
		}
		return result;
	}

	@Override
	public Execution findExecution(URI executionId) {
		Execution result=null;
		Transaction transaction = txManager.currentTransaction();
		try {
			transaction.begin();
			result=cis.getExecution(executionId);
		} catch(TransactionException e) {
			logFailure(executionId,e);
		} finally {
			commitQuietly(executionId,transaction);
		}
		return result;
	}

	@Override
	public Build findBuild(URI buildId) {
		Build result=null;
		Transaction transaction = txManager.currentTransaction();
		try {
			transaction.begin();
			result=cis.getBuild(buildId);
		} catch(TransactionException e) {
			logFailure(buildId,e);
		} finally {
			commitQuietly(buildId,transaction);
		}
		return result;
	}
}