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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0
 *   Bundle      : ci-backend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class CriteriaHelper<E> {

	private final EntityManager entityManager;
	private final CriteriaBuilder cb;
	private final Root<?> entity;
	private final List<Predicate> criteria;
	private final Map<String,Object> arguments;
	private final CriteriaQuery<E> query;

	CriteriaHelper(final EntityManager entityManager, final CriteriaQuery<E> query, final Root<?> entity) {
		this.entityManager = entityManager;
		this.cb = entityManager.getCriteriaBuilder();
		this.query = query;
		this.entity = entity;
		this.criteria=Lists.newArrayList();
		this.arguments=Maps.newLinkedHashMap();
	}

	private EntityManager entityManager() {
		return this.entityManager;
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
		if (this.criteria.isEmpty()) {
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