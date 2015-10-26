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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class JPAPendingEnrichmentRepository implements PendingEnrichmentRepository {

	private final class CriteriaHelper<E> {

		private final CriteriaBuilder cb;
		private final Root<?> entity;
		private final List<Predicate> criteria;
		private final Map<String,Object> arguments;
		private final CriteriaQuery<E> query;


		private CriteriaHelper(final CriteriaQuery<E> query, final Root<?> entity) {
			this.cb = entityManager().getCriteriaBuilder();
			this.query = query;
			this.entity = entity;
			this.criteria=Lists.newArrayList();
			this.arguments=Maps.newLinkedHashMap();
		}

		public <T> void registerCriteria(final String parameterName, final String fieldName, final T argument) {
			if(argument!=null) {
				@SuppressWarnings("unchecked")
				final ParameterExpression<? extends T> p=this.cb.parameter((Class<? extends T>)argument.getClass(), parameterName);
				this.criteria.add(this.cb.equal(this.entity.get(fieldName),p));
				this.arguments.put(parameterName, argument);
			}
		}

		private void updateCriteria() {
			if (this.criteria.size() == 0) {
				// Nothing to do
			} else if (this.criteria.size() == 1) {
				this.query.where(this.criteria.get(0));
			} else {
				this.query.where(this.cb.and(this.criteria.toArray(new Predicate[this.criteria.size()])));
			}
		}

		public TypedQuery<E> getQuery() {
			updateCriteria();
			final TypedQuery<E> result=entityManager().createQuery(this.query);
			for(final Entry<String,Object> entry:this.arguments.entrySet()) {
				result.setParameter(entry.getKey(),entry.getValue());
			}
			return result;
		}

	}

	private final EntityManagerProvider provider;

	public JPAPendingEnrichmentRepository(final EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public void add(final PendingEnrichment pendingEnrichment) {
		entityManager().persist(pendingEnrichment);
	}

	@Override
	public void remove(final PendingEnrichment pendingEnrichment) {
		entityManager().remove(pendingEnrichment);
	}

	@Override
	public void removeAll() {
		final List<PendingEnrichment> pendingEnrichments = findPendingEnrichments(null, null, null);
		final EntityManager entityManager = entityManager();
		for(final PendingEnrichment pending:pendingEnrichments) {
			entityManager.remove(pending);
		}
	}

	@Override
	public PendingEnrichment pendingEnrichmentOfId(final long id) {
		return entityManager().find(PendingEnrichment.class,id);
	}

	@Override
	public PendingEnrichment pendingEnrichmentOfExecution(final URI target) {
		final CriteriaBuilder cb = entityManager().getCriteriaBuilder();

		final CriteriaQuery<PendingEnrichment> cq = cb.createQuery(PendingEnrichment.class);
		final Root<PendingEnrichment> entity = cq.from(PendingEnrichment.class);

		cq.
			select(entity).
			where(
				entity.
					join("executions").
						in(target));

		final List<PendingEnrichment> results =  entityManager().createQuery(cq).getResultList();
		return Iterables.getFirst(results,null);
	}

	@Override
	public List<PendingEnrichment> findPendingEnrichments(final URI repositoryLocation, final String branchName, final String commitId) {
		final CriteriaQuery<PendingEnrichment> cq =
				entityManager().
					getCriteriaBuilder().
						createQuery(PendingEnrichment.class).
						distinct(true);
		final Root<PendingEnrichment> entity = cq.from(PendingEnrichment.class);
		cq.select(entity);

		final CriteriaHelper<PendingEnrichment> helper=new CriteriaHelper<PendingEnrichment>(cq,entity);
		helper.registerCriteria("location","repositoryLocation",repositoryLocation);
		helper.registerCriteria("name","branchName",branchName);
		helper.registerCriteria("id","commitId",commitId);

		return helper.getQuery().getResultList();
	}

}
