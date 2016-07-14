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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.4.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

public class CompletedEnrichment {

	private long id;
	private URI repositoryResource;
	private URI branchResource;
	private URI commitResource;
	private Set<URI> executions=null;

	CompletedEnrichment() {
		this.executions=Sets.newLinkedHashSet();
	}

	private CompletedEnrichment(final URI repositoryResource, final URI branchResource, final URI commitResource) {
		this();
		this.repositoryResource = repositoryResource;
		this.branchResource = branchResource;
		this.commitResource = commitResource;
	}

	public long id() {
		return this.id;
	}

	public URI repositoryResource() {
		return this.repositoryResource;
	}

	public URI branchResource() {
		return this.branchResource;
	}

	public URI commitResource() {
		return this.commitResource;
	}

	public Set<URI> executions() {
		return this.executions;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
						add("id",this.id).
						add("repositoryResource",this.repositoryResource).
						add("branchResource",this.branchResource).
						add("commitResource",this.commitResource).
						add("executions",this.executions).
						toString();
	}

	static CompletedEnrichment newInstance(final URI repositoryResource, final URI branchResource, final URI commitResource) {
		return new CompletedEnrichment(repositoryResource,branchResource,commitResource);
	}

}
