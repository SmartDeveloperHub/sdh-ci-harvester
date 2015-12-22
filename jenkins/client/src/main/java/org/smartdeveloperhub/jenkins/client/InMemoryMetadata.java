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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.2.0
 *   Bundle      : ci-jenkins-client-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import java.util.Date;
import java.util.List;

import org.smartdeveloperhub.jenkins.JenkinsResource.Metadata;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

final class InMemoryMetadata implements Metadata {

	private final Date retrievedOn;
	private final InMemoryResponseExcerpt response;
	private List<Filter> filters;
	private String serverVersion;

	InMemoryMetadata(final Date retrievedOn) {
		this.filters = ImmutableList.of();
		this.response = new InMemoryResponseExcerpt();
		this.retrievedOn = retrievedOn;
	}

	InMemoryMetadata withFilters(final List<Filter> filters) {
		this.filters=ImmutableList.copyOf(filters);
		return this;
	}

	InMemoryMetadata withServerVersion(final String serverVersion) {
		this.serverVersion = serverVersion;
		return this;
	}

	@Override
	public List<Filter> filters() {
		return this.filters;
	}

	@Override
	public String serverVersion() {
		return this.serverVersion;
	}

	@Override
	public Date retrievedOn() {
		return this.retrievedOn;
	}

	@Override
	public InMemoryResponseExcerpt response() {
		return this.response;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("filters",this.filters).
					add("retrievedOn",this.retrievedOn).
					add("serverVersion",this.serverVersion).
					add("response",this.response).
					toString();
	}

}