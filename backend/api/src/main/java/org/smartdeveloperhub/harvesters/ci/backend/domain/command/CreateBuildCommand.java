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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.domain.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class CreateBuildCommand extends BuildCommand {

	public static final class Builder extends BuildCommandBuilder<CreateBuildCommand,Builder> {

		private URI serviceId;
		private boolean simple;
		private URI parent;

		private Builder() {
			super(Builder.class);
			this.simple=true;
		}

		public Builder withServiceId(URI serviceId) {
			this.serviceId=serviceId;
			return this;
		}

		public Builder simple() {
			this.simple=true;
			this.parent=null;
			return this;
		}

		public Builder composite() {
			this.simple=false;
			this.parent=null;
			return this;
		}

		public Builder subBuildOf(URI parent) {
			this.parent=parent;
			this.simple=false;
			return this;
		}

		@Override
		public CreateBuildCommand build() {
			return
				new CreateBuildCommand(
					checkNotNull(this.serviceId,"Service identifier cannot be null"),
					checkNotNull(super.buildId(),"Build identifier cannot be null"),
					this.simple,
					this.parent,
					checkNotNull(super.title(),"Title cannot be null"),
					super.description(),
					super.createdOn(),
					super.codebase(),
					super.branchName()
				);
		}

	}

	private final URI serviceId;
	private final boolean simple;
	private final URI parent;

	private CreateBuildCommand(URI serviceId, URI buildId, boolean simple, URI parent, String title, String description, Date creationDate, URI codebase, String branchName) { // NOSONAR
		super(buildId,title,description,creationDate,codebase,branchName);
		this.serviceId = serviceId;
		this.simple = simple;
		this.parent = parent;
	}

	@Override
	public void accept(CommandVisitor visitor) {
		if(visitor!=null) {
			visitor.visitCreateBuildCommand(this);
		}
	}

	public URI serviceId() {
		return this.serviceId;
	}

	public boolean simple() {
		return this.parent==null && this.simple;
	}

	public boolean composite() {
		return this.parent==null && !this.simple;
	}

	public URI subBuildOf() {
		return this.parent;
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.
			add("serviceId", this.serviceId).
			add("simple", simple()).
			add("parent", this.parent);
	}

	public static Builder builder() {
		return new Builder();
	}

}
