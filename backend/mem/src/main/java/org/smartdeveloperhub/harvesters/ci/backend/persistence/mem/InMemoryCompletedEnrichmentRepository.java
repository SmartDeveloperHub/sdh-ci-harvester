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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-mem-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.CompletedEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CompletedEnrichmentRepository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class InMemoryCompletedEnrichmentRepository implements CompletedEnrichmentRepository {

	private final CopyOnWriteArrayList<CompletedEnrichment> entities;

	public InMemoryCompletedEnrichmentRepository() {
		this.entities=Lists.newCopyOnWriteArrayList();
	}

	@Override
	public void add(final CompletedEnrichment pendingEnrichment) {
		this.entities.add(pendingEnrichment);
	}

	@Override
	public void remove(final CompletedEnrichment pendingEnrichment) {
		this.entities.remove(pendingEnrichment);
	}

	@Override
	public void removeAll() {
		this.entities.clear();
	}

	@Override
	public CompletedEnrichment completedEnrichmentOfId(final long id) {
		for(final CompletedEnrichment entity:this.entities) {
			if(entity.id()==id) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public CompletedEnrichment completedEnrichmentOfExecution(final URI target) {
		for(final CompletedEnrichment entity:this.entities) {
			if(entity.executions().contains(target)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public List<CompletedEnrichment> findCompletedEnrichments(final URI repositoryResource, final URI branchResource, final URI commitResource) {
		final List<CompletedEnrichment> result=Lists.newArrayList();
		for(final CompletedEnrichment entity:this.entities) {
			if(matches(entity, repositoryResource, branchResource, commitResource)) {
				result.add(entity);
			}
		}
		return ImmutableList.copyOf(result);
	}

	private boolean matches(final CompletedEnrichment entity, final URI repositoryResource, final URI branchResource, final URI commitResource) {
		return
			matches(repositoryResource,entity.repositoryResource()) &&
			matches(branchResource,entity.branchResource()) &&
			matches(commitResource,entity.commitResource());
	}

	private <T> boolean matches(final T required, final T existing) {
		return required==null || required.equals(existing);
	}

}
