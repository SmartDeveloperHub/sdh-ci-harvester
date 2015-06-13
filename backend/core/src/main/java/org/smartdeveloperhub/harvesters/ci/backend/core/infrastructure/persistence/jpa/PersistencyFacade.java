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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;

public final class PersistencyFacade {

	private final ThreadLocal<EntityManager> manager;
	private final EntityManagerFactory emf;

	interface EntityManagerProvider {

		EntityManager entityManager();

	}

	public PersistencyFacade(EntityManagerFactory emf) {
		this.emf = emf;
		this.manager=new ThreadLocal<EntityManager>();
	}

	private EntityManager getManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager==null) {
			entityManager = this.emf.createEntityManager();
			this.manager.set(entityManager);
		}
		return entityManager;
	}

	public ExecutionRepository getExecutionRepository() {
		return
			new JPAExecutionRepository(
				new EntityManagerProvider(){
					@Override
					public EntityManager entityManager() {
						return getManager();
					}
				}
			);
	}

	public BuildRepository getBuildRepository() {
		return
			new JPABuildRepository(
				new EntityManagerProvider(){
					@Override
					public EntityManager entityManager() {
						return getManager();
					}
				}
			);
	}

	public final void beginTransaction() {
		getManager().getTransaction().begin();
	}

	public final void commitTransaction() {
		getManager().getTransaction().commit();
	}

	public final void rollbackTransaction() {
		EntityTransaction transaction = getManager().getTransaction();
		if(transaction.isActive()) {
			transaction.rollback();
		}
	}

	public void disposeManagers() {
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			this.manager.set(null);
			entityManager.close();
		}
	}

}
