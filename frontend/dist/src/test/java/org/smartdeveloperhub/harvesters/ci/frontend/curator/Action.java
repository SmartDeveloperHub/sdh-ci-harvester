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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.4.0-SNAPSHOT
 *   Bundle      : ci-frontend-dist-0.4.0-SNAPSHOT.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.curator;

import java.net.URI;
import java.util.UUID;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ExecutionEnrichment;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public final class Action {

	private static final class ImmutableExecutionEnrichment implements ExecutionEnrichment {

		private final URI branch;
		private final URI commit;
		private final URI repository;

		private ImmutableExecutionEnrichment(final URI repository, final URI branch, final URI commit) {
			this.branch = branch;
			this.commit = commit;
			this.repository = repository;
		}

		@Override
		public Optional<URI> repositoryResource() {
			return Optional.fromNullable(this.repository);
		}

		@Override
		public Optional<URI> commitResource() {
			return Optional.fromNullable(this.commit);
		}

		@Override
		public Optional<URI> branchResource() {
			return Optional.fromNullable(this.branch);
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						omitNullValues().
						add("repository",this.repository).
						add("branch",this.branch).
						add("commit",this.commit).
						toString();
		}
	}

	private final URI targetResource;
	private final ExecutionEnrichment enrichment;
	private final UUID requestId;

	private Action(final UUID requestId, final URI targetResource, final ExecutionEnrichment enrichment) {
		this.requestId = requestId;
		this.targetResource = targetResource;
		this.enrichment = enrichment;
	}

	UUID requestId() {
		return this.requestId;
	}

	public URI targetResource() {
		return this.targetResource;
	}

	public ExecutionEnrichment enrichment() {
		return this.enrichment;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("targetResource",this.targetResource).
					add("enrichment",this.enrichment).
					toString();
	}

	static Action newInstance(final UUID requestId, final URI targetResource, final URI repository, final URI branch, final URI commit) {
		return new Action(requestId,targetResource,new ImmutableExecutionEnrichment(repository, branch, commit));
	}

}