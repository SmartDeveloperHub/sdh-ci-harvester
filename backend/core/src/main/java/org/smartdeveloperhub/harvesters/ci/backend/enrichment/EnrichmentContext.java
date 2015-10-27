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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.Execution;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class EnrichmentContext {

	private final URI target;
	private final URI repositoryLocation;
	private final String branchName;
	private final String commitId;

	private URI repositoryResource;
	private URI branchResource;
	private Long pendingEnrichment;
	private URI commitResource;

	EnrichmentContext(final Execution execution) {
		this.target=execution.executionId();
		this.repositoryLocation=execution.codebase().location();
		this.branchName=execution.codebase().branchName();
		this.commitId=execution.commitId();
	}

	URI target() {
		return this.target;
	}

	URI repositoryLocation() {
		return this.repositoryLocation;
	}

	String branchName() {
		return this.branchName;
	}

	String commitId() {
		return this.commitId;
	}

	boolean requiresRepository() {
		return this.repositoryLocation!=null;
	}

	boolean requiresBranch() {
		return requiresRepository() && this.branchName!=null;
	}

	boolean requiresCommit() {
		return requiresBranch() && this.commitId!=null;
	}

	Optional<URI> repositoryResource() {
		return Optional.fromNullable(this.repositoryResource);
	}

	Optional<URI> branchResource() {
		return Optional.fromNullable(this.branchResource);
	}

	Optional<URI> commitResource() {
		return Optional.fromNullable(this.commitResource);
	}

	void setRepositoryResource(final URI resource) {
		this.repositoryResource=resource;
	}

	void setBranchResource(final URI resource) {
		this.branchResource=resource;
	}

	void setCommitResource(final URI resource) {
		this.commitResource = resource;

	}

	void setPendingEnrichment(final long pendingEnrichment) {
		this.pendingEnrichment=pendingEnrichment;
	}

	boolean requiresEnrichment() {
		return
			!isResolved(requiresCommit(), this.commitResource) ||
			!isResolved(requiresBranch(), this.branchResource) ||
			!isResolved(requiresRepository(), this.repositoryResource);
	}

	private boolean isResolved(final boolean condition, final URI resource) {
		return !condition || resource!=null;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("target",this.target).
					add("repositoryLocation",this.repositoryLocation).
					add("branchName",this.branchName).
					add("commitId",this.commitId).
					add("repositoryResource",this.repositoryResource).
					add("branchResource",this.branchResource).
					add("commitResource",this.commitResource).
					add("pendingEnrichment",this.pendingEnrichment).
					toString();
	}



}