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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class CreateBranchCommand extends ExternalCommand {

	public static class Builder extends ExternalCommandBuilder<CreateBranchCommand, Builder> {

		private URI repositoryLocation;
		private String branchName;

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

		@Override
		public CreateBranchCommand build() {
			return
				new CreateBranchCommand(
					super.resource(),
					checkNotNull(this.repositoryLocation,"Repository location cannot be null"),
					checkNotNull(this.branchName,"Branch name cannot be null"));
		}

	}

	private final URI repositoryLocation;
	private final String branchName;

	private CreateBranchCommand(final URI resource, final URI repositoryLocation, final String branchName) {
		super(resource);
		this.repositoryLocation = repositoryLocation;
		this.branchName = branchName;
	}

	public URI repositoryLocation() {
		return this.repositoryLocation;
	}

	public String branchName() {
		return this.branchName;
	}

	public URI branchResource() {
		return super.resource();
	}

	@Override
	protected void toString(final ToStringHelper helper) {
		helper.
			add("repositoryLocation",this.repositoryLocation).
			add("branchName",this.branchName);
	}

	public static Builder builder() {
		return new Builder();
	}

}
