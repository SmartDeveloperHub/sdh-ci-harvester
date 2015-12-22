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

import java.net.URI;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.CompletedEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CompletedEnrichmentRepository;

import com.google.common.collect.Iterables;

public class JPACompletedEnrichmentRepository implements CompletedEnrichmentRepository {

	private final EntityManagerProvider provider;

	public JPACompletedEnrichmentRepository(final EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public void add(final CompletedEnrichment completedEnrichment) {
		entityManager().persist(completedEnrichment);
	}

	@Override
	public void remove(final CompletedEnrichment completedEnrichment) {
		entityManager().remove(completedEnrichment);
	}

	@Override
	public void removeAll() {
		final List<CompletedEnrichment> completedEnrichments = findCompletedEnrichments(null, null, null);
		final EntityManager entityManager = entityManager();
		for(final CompletedEnrichment pending:completedEnrichments) {
			entityManager.remove(pending);
		}
	}

	@Override
	public CompletedEnrichment completedEnrichmentOfId(final long id) {
		return entityManager().find(CompletedEnrichment.class,id);
	}

	@Override
	public CompletedEnrichment completedEnrichmentOfExecution(final URI target) {
		final CriteriaBuilder cb = entityManager().getCriteriaBuilder();

		final CriteriaQuery<CompletedEnrichment> cq = cb.createQuery(CompletedEnrichment.class);
		final Root<CompletedEnrichment> entity = cq.from(CompletedEnrichment.class);

		cq.
			select(entity).
			where(
				entity.
					join("executions").
						in(target));

		final List<CompletedEnrichment> results =  entityManager().createQuery(cq).getResultList();
		return Iterables.getFirst(results,null);
	}

	@Override
	public List<CompletedEnrichment> findCompletedEnrichments(final URI repositoryResource, final URI branchResource, final URI commitResource) {
		final EntityManager em = entityManager();
		final CriteriaQuery<CompletedEnrichment> cq =
				em.
					getCriteriaBuilder().
						createQuery(CompletedEnrichment.class).
						distinct(true);
		final Root<CompletedEnrichment> entity = cq.from(CompletedEnrichment.class);
		cq.select(entity);

		final CriteriaHelper<CompletedEnrichment> helper=new CriteriaHelper<CompletedEnrichment>(em,cq,entity);
		helper.registerCriteria("repository","repositoryResource",repositoryResource);
		helper.registerCriteria("branch","branchResource",branchResource);
		helper.registerCriteria("commit","commitResource",commitResource);

		return helper.getQuery().getResultList();
	}

}
