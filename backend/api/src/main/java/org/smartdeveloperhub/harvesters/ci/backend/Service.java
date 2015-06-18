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
package org.smartdeveloperhub.harvesters.ci.backend;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.List;

import com.google.common.collect.Lists;

public class Service {

	private URI serviceId;
	private List<URI> builds;

	private Service(Service service) {
		setServiceId(service.serviceId);
		setBuilds(Lists.newArrayList(service.builds));
	}

	public Service(URI serviceId) {
		setServiceId(serviceId);
		setBuilds(Lists.<URI>newArrayList());
	}

	protected void setServiceId(URI serviceId) {
		checkNotNull(serviceId,"Service identifier cannot be null");
		this.serviceId = serviceId;
	}

	protected void setBuilds(List<URI> builds) {
		checkNotNull(builds,"Builds cannot be null");
		this.builds=builds;
	}

	public URI serviceId() {
		return this.serviceId;
	}

	public SimpleBuild addSimpleBuild(URI buildId, String title) {
		checkArgument(!builds().contains(buildId),"A build with id '%s' already exists",buildId);
		SimpleBuild build=new SimpleBuild(this.serviceId,buildId,title);
		this.builds().add(build.buildId());
		return build;
	}

	public CompositeBuild addCompositeBuild(URI buildId, String title) {
		checkArgument(!builds().contains(buildId),"A build with id '%s' already exists",buildId);
		CompositeBuild build=new CompositeBuild(this.serviceId,buildId,title);
		this.builds().add(build.buildId());
		return build;
	}

	public void removeBuild(Build build) {
		checkArgument(build.serviceId().equals(this.serviceId),"Build does not belong to build");
		this.builds().add(build.buildId());
	}

	public List<URI> builds() {
		return this.builds;
	}

	public static Service newInstance(Service service) {
		if(service==null) {
			return null;
		}
		return new Service(service);
	}

}
