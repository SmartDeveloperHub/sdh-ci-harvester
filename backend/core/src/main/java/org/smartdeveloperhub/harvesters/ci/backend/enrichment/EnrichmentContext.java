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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class EnrichmentContext {

	private final Execution targetExecution;
	private final URI repositoryLocation;
	private final String branchName;
	private final String commitId;

	private ImmutableExecutionEnrichment enrichment;
	private PendingEnrichment pendingEnrichment;

	EnrichmentContext(final Execution execution) {
		this.targetExecution=execution;
		this.repositoryLocation=execution.codebase().location();
		this.branchName=execution.codebase().branchName();
		this.commitId=execution.commitId();
		this.enrichment=new ImmutableExecutionEnrichment();
	}

	Execution targetExecution() {
		return this.targetExecution;
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

	ExecutionEnrichment enrichment() {
		return this.enrichment;
	}

	PendingEnrichment pendingEnrichment() {
		return this.pendingEnrichment;
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

	void setRepositoryResource(final URI resource) {
		this.enrichment=this.enrichment.withRepositoryResource(resource);
	}

	void setBranchResource(final URI resource) {
		this.enrichment=this.enrichment.withBranchResource(resource);
	}

	void setCommitResource(final URI resource) {
		this.enrichment=this.enrichment.withCommitResource(resource);
	}

	void setPendingEnrichment(final PendingEnrichment pendingEnrichment) {
		this.pendingEnrichment=pendingEnrichment;
	}

	boolean requiresEnrichment() {
		return
			!isResolved(requiresCommit(), this.enrichment.commitResource()) ||
			!isResolved(requiresBranch(), this.enrichment.branchResource()) ||
			!isResolved(requiresRepository(), this.enrichment.repositoryResource());
	}

	private boolean isResolved(final boolean condition, final Optional<URI> resource) {
		return !condition || resource.isPresent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("targetExecution",this.targetExecution.executionId()).
					add("repositoryLocation",this.repositoryLocation).
					add("branchName",this.branchName).
					add("commitId",this.commitId).
					add("enrichment",this.enrichment).
					add("pendingEnrichment",this.pendingEnrichment).
					toString();
	}

}