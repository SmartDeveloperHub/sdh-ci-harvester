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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.core.ApplicationRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.core.lifecycle.LifecycleDescriptorRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionManager;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ServiceRepository;

public final class JPAApplicationRegistry implements ApplicationRegistry {

	private static final Logger LOGGER=LoggerFactory.getLogger(JPAApplicationRegistry.class);

	private final class JPAEntityManagerProvider implements EntityManagerProvider {

		@Override
		public EntityManager entityManager() {
			return getManager();
		}

		@Override
		public void close() {
			disposeManager();
		}
		@Override
		public boolean isActive() {
			return isTransactionActive();
		}

	}

	private final ThreadLocal<EntityManager> manager;
	private final EntityManagerFactory emf;
	private final JPAEntityManagerProvider provider;
	private final String id;

	public JPAApplicationRegistry(EntityManagerFactory emf) {
		this.emf = emf;
		this.manager=new ThreadLocal<EntityManager>();
		this.provider = new JPAEntityManagerProvider();
		this.id = String.format("%08X",hashCode());

	}

	private void trace(String message, Object... args) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("{} - {} - {}",
				this.id,
				String.format(message,args),
				Context.getContext("org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa"));
		}
	}

	private boolean isTransactionActive() {
		boolean result = false;
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			result=entityManager.getTransaction().isActive();
		}
		return result;
	}

	private EntityManager getManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager==null) {
			entityManager = this.emf.createEntityManager();
			this.manager.set(entityManager);
			trace("Assigned manager %08X",entityManager.hashCode());
		} else {
			trace("Returned manager %08X",entityManager.hashCode());
		}
		return entityManager;
	}

	private void disposeManager() {
		EntityManager entityManager = this.manager.get();
		if(entityManager!=null) {
			entityManager.close();
			this.manager.remove();
			trace("Disposed manager %08X",entityManager.hashCode());
		} else {
			trace("Nothing to dispose");
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExecutionRepository getExecutionRepository() {
		return new JPAExecutionRepository(this.provider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BuildRepository getBuildRepository() {
		return new JPABuildRepository(this.provider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ServiceRepository getServiceRepository() {
		return new JPAServiceRepository(this.provider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public LifecycleDescriptorRepository getLifecycleDescriptorRepository() {
		return new JPALifecycleDescriptorRepository(this.provider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TransactionManager getTransactionManager() {
		return new JPATransactionManager(this.provider);
	}

	public void clear() {
		disposeManager();
	}

}
