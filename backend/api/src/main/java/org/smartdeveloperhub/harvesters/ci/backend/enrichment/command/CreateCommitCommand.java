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
package org.smartdeveloperhub.harvesters.ci.backend.enrichment.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class CreateCommitCommand extends ExternalCommand {

	public static class Builder extends ExternalCommandBuilder<CreateCommitCommand, Builder> {

		private URI repositoryLocation;
		private String branchName;
		private String commitId;

		private Builder() {
			super(Builder.class);
		}

		public Builder withRepositoryLocation(final URI repositoryLocation) {
			this.repositoryLocation = repositoryLocation;
			return this;
		}

		public Builder withBranchName(final String branchName) {
			this.branchName = branchName;
			return this;
		}

		public Builder withCommitId(final String commitId) {
			this.commitId = commitId;
			return this;
		}

		@Override
		public CreateCommitCommand build() {
			return
				new CreateCommitCommand(
					super.resource(),
					checkNotNull(this.repositoryLocation,"Repository location cannot be null"),
					checkNotNull(this.branchName,"Branch name cannot be null"),
					checkNotNull(this.commitId,"Commit identifier cannot be null"));
		}

	}

	private final URI repositoryLocation;
	private final String branchName;
	private final String commitId;

	private CreateCommitCommand(final URI resource, final URI repositoryLocation, final String branchName, final String commitId) {
		super(resource);
		this.repositoryLocation = repositoryLocation;
		this.branchName = branchName;
		this.commitId = commitId;
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

	public URI commitResource() {
		return super.resource();
	}

	@Override
	protected void toString(final ToStringHelper helper) {
		helper.
			add("repositoryLocation",this.repositoryLocation).
			add("branchName",this.branchName).
			add("commitId",this.commitId);
	}

	public static Builder builder() {
		return new Builder();
	}

}
