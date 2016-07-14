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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.3.0
 *   Bundle      : ci-backend-api-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

public class PendingEnrichment {

	private long id;
	private URI repositoryLocation;
	private String branchName;
	private String commitId;
	private Set<URI> executions=null;

	PendingEnrichment() {
		this.executions=Sets.newLinkedHashSet();
	}

	private PendingEnrichment(final URI repositoryLocation, final String branchName, final String commitId) {
		this();
		this.repositoryLocation = repositoryLocation;
		this.branchName = branchName;
		this.commitId = commitId;
	}

	public long id() {
		return this.id;
	}

	public URI repositoryLocation() {
		return this.repositoryLocation;
	}

	public String branchName() {
		return this.branchName;
	}

	public String commitId() {
		return this.commitId;
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
						add("repositoryLocation",this.repositoryLocation).
						add("branchName",this.branchName).
						add("commitId",this.commitId).
						add("executions",this.executions).
						toString();
	}

	static PendingEnrichment newInstance(final URI repositoryLocation, final String branchName, final String commitId) {
		return new PendingEnrichment(repositoryLocation,branchName,commitId);
	}

}
