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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-mem-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ExecutionRepository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class InMemoryExecutionRepository implements ExecutionRepository {

	private final ConcurrentMap<URI,Execution> executions;

	public InMemoryExecutionRepository() {
		this.executions=Maps.newConcurrentMap();
	}

	@Override
	public void add(final Execution entity) {
		checkNotNull(entity,"Execution cannot be null");
		final Execution previous = this.executions.putIfAbsent(entity.executionId(),entity);
		checkArgument(previous==null,"An execution identified by '%s' already exists",entity.executionId());
	}

	@Override
	public void remove(final Execution entity) {
		checkNotNull(entity,"Execution cannot be null");
		this.executions.remove(entity.executionId(),entity);
	}

	@Override
	public Execution executionOfId(final URI executionId) {
		checkNotNull(executionId,"Execution identifier cannot be null");
		return this.executions.get(executionId);
	}

	@Override
	public List<URI> executionIds() {
		return ImmutableList.copyOf(this.executions.keySet());
	}

}
