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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0
 *   Bundle      : ci-backend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class ImmutableExecutionEnrichment implements ExecutionEnrichment {

	private final URI repositoryResource;
	private final URI branchResource;
	private final URI commitResource;

	private ImmutableExecutionEnrichment(final URI repositoryResource, final URI branchResource, final URI commitResource) {
		this.repositoryResource = repositoryResource;
		this.branchResource = branchResource;
		this.commitResource = commitResource;
	}

	ImmutableExecutionEnrichment() {
		this(null,null,null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<URI> repositoryResource() {
		return Optional.fromNullable(this.repositoryResource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<URI> branchResource() {
		return Optional.fromNullable(this.branchResource);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<URI> commitResource() {
		return Optional.fromNullable(this.commitResource);
	}

	ImmutableExecutionEnrichment withRepositoryResource(final URI resource) {
		return new ImmutableExecutionEnrichment(resource,this.branchResource,this.commitResource);
	}

	ImmutableExecutionEnrichment withBranchResource(final URI resource) {
		return new ImmutableExecutionEnrichment(this.repositoryResource,resource,this.commitResource);
	}

	ImmutableExecutionEnrichment withCommitResource(final URI resource) {
		return new ImmutableExecutionEnrichment(this.repositoryResource,this.branchResource,resource);
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
					add("repositoryResource",this.repositoryResource).
					add("branchResource",this.branchResource).
					add("commitResource",this.commitResource).
					toString();
	}

}