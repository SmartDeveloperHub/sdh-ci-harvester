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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.3.0
 *   Bundle      : ci-backend-mem-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public final class InMemoryPendingEnrichmentRepository implements PendingEnrichmentRepository {

	private final CopyOnWriteArrayList<PendingEnrichment> entities;

	public InMemoryPendingEnrichmentRepository() {
		this.entities=Lists.newCopyOnWriteArrayList();
	}

	@Override
	public void add(final PendingEnrichment pendingEnrichment) {
		this.entities.add(pendingEnrichment);
	}

	@Override
	public void remove(final PendingEnrichment pendingEnrichment) {
		this.entities.remove(pendingEnrichment);
	}

	@Override
	public void removeAll() {
		this.entities.clear();
	}

	@Override
	public PendingEnrichment pendingEnrichmentOfId(final long id) {
		for(final PendingEnrichment entity:this.entities) {
			if(entity.id()==id) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public PendingEnrichment pendingEnrichmentOfExecution(final URI target) {
		for(final PendingEnrichment entity:this.entities) {
			if(entity.executions().contains(target)) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public List<PendingEnrichment> findPendingEnrichments(final URI repositoryLocation, final String branchName, final String commitId) {
		final List<PendingEnrichment> result=Lists.newArrayList();
		for(final PendingEnrichment entity:this.entities) {
			if(matches(entity, repositoryLocation, branchName, commitId)) {
				result.add(entity);
			}
		}
		return ImmutableList.copyOf(result);
	}

	private boolean matches(final PendingEnrichment entity, final URI repositoryLocation, final String branchName, final String commitId) {
		return
			matches(repositoryLocation,entity.repositoryLocation()) &&
			matches(branchName,entity.branchName()) &&
			matches(commitId,entity.commitId());
	}

	private <T> boolean matches(final T required, final T existing) {
		return required==null || required.equals(existing);
	}

}
