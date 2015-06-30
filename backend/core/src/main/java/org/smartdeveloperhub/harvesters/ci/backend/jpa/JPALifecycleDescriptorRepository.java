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
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.smartdeveloperhub.harvesters.ci.backend.integration.lifecycle.EntityId;
import org.smartdeveloperhub.harvesters.ci.backend.integration.lifecycle.LifecycleDescriptor;
import org.smartdeveloperhub.harvesters.ci.backend.integration.lifecycle.LifecycleDescriptorRepository;

public class JPALifecycleDescriptorRepository implements LifecycleDescriptorRepository {

	private EntityManagerProvider provider;

	public JPALifecycleDescriptorRepository(EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public void add(LifecycleDescriptor descriptor) {
		entityManager().persist(descriptor);
	}

	@Override
	public void remove(LifecycleDescriptor descriptor) {
		entityManager().remove(descriptor);
	}

	@Override
	public LifecycleDescriptor descriptorOfId(EntityId entityId) {
		CriteriaBuilder cb =
			entityManager().getCriteriaBuilder();

		CriteriaQuery<LifecycleDescriptor> query =
				cb.createQuery(LifecycleDescriptor.class);

		Root<LifecycleDescriptor> descriptor = query.from(LifecycleDescriptor.class);
		query.
			select(descriptor).
			where(cb.equal(descriptor.get("entityId"), entityId)).
			distinct(true);
		List<LifecycleDescriptor> results = entityManager().createQuery(query).getResultList();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

}
