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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ServiceRepository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

public class InMemoryServiceRepository implements ServiceRepository {

	private final ConcurrentMap<URI,Service> services;

	public InMemoryServiceRepository() {
		this.services=Maps.newConcurrentMap();
	}

	@Override
	public List<URI> serviceIds() {
		return ImmutableList.copyOf(this.services.keySet());
	}

	@Override
	public void add(Service service) {
		checkNotNull(service,"Service cannot be null");
		Service previous = this.services.putIfAbsent(service.serviceId(),service);
		checkArgument(previous==null,"A service identified by '%s' already exists",service.serviceId());
	}

	@Override
	public void remove(Service service) {
		checkNotNull(service,"Service cannot be null");
		this.services.remove(service.serviceId(),service);
	}

	@Override
	public Service serviceOfId(URI serviceId) {
		checkNotNull(serviceId,"Service identifier cannot be null");
		return this.services.get(serviceId);
	}

}
