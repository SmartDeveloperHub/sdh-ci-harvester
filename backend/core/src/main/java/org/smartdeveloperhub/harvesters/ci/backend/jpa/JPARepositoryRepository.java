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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0
 *   Bundle      : ci-backend-core-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Repository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.RepositoryRepository;

public class JPARepositoryRepository implements RepositoryRepository {

	private final EntityManagerProvider provider;

	public JPARepositoryRepository(final EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public List<URI> repositoryLocations() {
		final CriteriaQuery<URI> query =
				entityManager().
					getCriteriaBuilder().
						createQuery(URI.class);
		final Root<Repository> service = query.from(Repository.class);
		query.
			select(service.<URI>get("location")).
			distinct(true);
		return entityManager().createQuery(query).getResultList();
	}

	@Override
	public void add(final Repository repository) {
		entityManager().persist(repository);
	}

	@Override
	public void remove(final Repository repository) {
		entityManager().remove(repository);
	}

	@Override
	public Repository repositoryOfLocation(final URI location) {
		return entityManager().find(Repository.class, location);
	}

}
