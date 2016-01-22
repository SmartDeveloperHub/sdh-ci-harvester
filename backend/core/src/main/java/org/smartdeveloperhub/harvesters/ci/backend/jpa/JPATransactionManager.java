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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import javax.persistence.EntityTransaction;

import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;

final class JPATransactionManager implements TransactionManager {

	private final class JPATransaction implements Transaction {

		private EntityTransaction nativeTransaction() {
			return provider.entityManager().getTransaction();
		}

		@Override
		public boolean isActive() {
			return provider.isActive();
		}

		@Override
		public void begin() throws TransactionException {
			try {
				nativeTransaction().begin();
			} catch (Exception e) {
				throw new TransactionException("Begin failed",e);
			}
		}

		@Override
		public void commit() throws TransactionException {
			try {
				nativeTransaction().commit();
				provider.close();
			} catch (Exception e) {
				throw new TransactionException("Commit failed",e);
			}
		}

		@Override
		public void rollback() throws TransactionException {
			try {
				nativeTransaction().rollback();
			} catch (Exception e) {
				throw new TransactionException("Rollback failed",e);
			} finally {
				provider.close();
			}
		}

	}

	private final EntityManagerProvider provider;

	JPATransactionManager(EntityManagerProvider provider) {
		this.provider = provider;
	}

	@Override
	public Transaction currentTransaction() {
		return new JPATransaction();
	}
}