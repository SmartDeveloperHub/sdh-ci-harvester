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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.smartdeveloperhub.harvesters.ci.backend.database.Database;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.BranchRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CommitRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CompletedEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.RepositoryRepository;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;

public class JPATestCase {

	private static EntityManagerFactory factory;
	private static JPAComponentRegistry persistencyFacade;

	@BeforeClass
	public static void startUp() throws Exception {
		factory = Persistence.createEntityManagerFactory("unitTests");
		persistencyFacade =
			new JPAComponentRegistry(
				new Database(){
					@Override
					public void close() throws IOException {
						if(factory!=null) {
							factory.close();
						}
					}
					@Override
					public EntityManagerFactory getEntityManagerFactory() {
						return factory;
					}
				}
			);
	}

	@AfterClass
	public static void shutDown() throws Exception {
		if(persistencyFacade!=null) {
			persistencyFacade.close();
		}
	}

	protected final TransactionManager transactionManager() {
		return persistencyFacade.getTransactionManager();
	}


	protected abstract class Operation {

		protected final RepositoryRepository repositoryRepository() {
			return persistencyFacade.getRepositoryRepository();
		}

		protected final BranchRepository branchRepository() {
			return persistencyFacade.getBranchRepository();
		}

		protected final CommitRepository commitRepository() {
			return persistencyFacade.getCommitRepository();
		}

		protected final PendingEnrichmentRepository pendingEnrichmentRepository() {
			return persistencyFacade.getPendingEnrichmentRepository();
		}

		protected final CompletedEnrichmentRepository completedEnrichmentRepository() {
			return persistencyFacade.getCompletedEnrichmentRepository();
		}

		protected abstract void execute() throws Exception;

	}

	protected final void transactional(final Operation operation) throws Exception {
		final Transaction transaction = transactionManager().currentTransaction();
		transaction.begin();
		try {
			operation.execute();
			transaction.commit();
		} catch(final Exception e) {
			if(transaction.isActive()) {
				transaction.rollback();
			}
			throw e;
		} finally {
			persistencyFacade.clear();
		}
	}

}
