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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.BuildRepository;

import com.google.common.collect.Maps;

public class InMemoryBuildRepository implements BuildRepository {

	private final ConcurrentMap<URI,Build> builds;

	public InMemoryBuildRepository() {
		this.builds=Maps.newConcurrentMap();
	}

	@Override
	public void add(Build build) {
		checkNotNull(build,"Build cannot be null");
		Build previous = this.builds.putIfAbsent(build.buildId(),build);
		checkArgument(previous==null,"A build identified by '%s' already exists",build.buildId());
	}

	@Override
	public void remove(Build build) {
		checkNotNull(build,"Build cannot be null");
		this.builds.remove(build.buildId(),build);
	}

	@Override
	public Build buildOfId(URI buildId) {
		checkNotNull(buildId,"Build identifier cannot be null");
		return this.builds.get(buildId);
	}

	@Override
	public <T extends Build> T buildOfId(URI buildId, Class<? extends T> clazz) {
		return clazz.cast(buildOfId(buildId));
	}

}
