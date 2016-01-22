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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-mem-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Repository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.RepositoryRepository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class InMemoryRepositoryRepository implements RepositoryRepository {

	private final ConcurrentMap<URI,Repository> repositories;

	public InMemoryRepositoryRepository() {
		this.repositories=Maps.newConcurrentMap();
	}

	@Override
	public List<URI> repositoryLocations() {
		return ImmutableList.copyOf(this.repositories.keySet());
	}

	@Override
	public void add(final Repository repository) {
		checkNotNull(repository,"Repository cannot be null");
		final Repository previous = this.repositories.putIfAbsent(repository.location(),repository);
		checkArgument(previous==null,"A repository located at '%s' already exists",repository.location());
	}

	@Override
	public void remove(final Repository repository) {
		checkNotNull(repository,"Repository cannot be null");
		this.repositories.remove(repository.location(),repository);
	}

	@Override
	public Repository repositoryOfLocation(final URI location) {
		checkNotNull(location,"Repository location cannot be null");
		return this.repositories.get(location);
	}

}
