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

import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ServiceRepository;

public class JPAServiceRepository implements ServiceRepository {

	private EntityManagerProvider provider;

	public JPAServiceRepository(EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public void add(Service service) {
		entityManager().persist(service);
	}

	@Override
	public void remove(Service service) {
		entityManager().remove(service);
	}

	@Override
	public Service serviceOfId(URI serviceId) {
		return entityManager().find(Service.class, serviceId);
	}

	@Override
	public List<URI> serviceIds() {
		CriteriaQuery<URI> query =
			entityManager().
				getCriteriaBuilder().
					createQuery(URI.class);
		Root<Service> service = query.from(Service.class);
		query.
			select(service.<URI>get("serviceId")).
			distinct(true);
		return entityManager().createQuery(query).getResultList();
	}

}