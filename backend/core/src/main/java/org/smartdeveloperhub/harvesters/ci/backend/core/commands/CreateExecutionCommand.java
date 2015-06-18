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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.core.commands;

import java.net.URI;
import java.util.Date;

import com.google.common.base.MoreObjects;

import static com.google.common.base.Preconditions.*;

public final class CreateExecutionCommand implements Command {

	public static final class Builder {

		private URI buildId;
		private Date createdOn;
		private URI executionId;

		private Builder() {
		}

		public Builder withBuildId(URI buildId) {
			this.buildId = buildId;
			return this;
		}

		public Builder withExecutionId(URI buildId) {
			this.executionId = buildId;
			return this;
		}

		public Builder withCreatedOn(Date createdOn) {
			this.createdOn = createdOn;
			return this;
		}

		public CreateExecutionCommand build() {
			return
				new CreateExecutionCommand(
					checkNotNull(this.buildId,"Build identifier cannot be null"),
					checkNotNull(this.executionId,"Execution identifier cannot be null"),
					checkNotNull(this.createdOn,"Creation date cannot be null")
				);
		}

	}

	private final URI executionId;
	private final URI buildId;
	private final Date createdOn;

	private CreateExecutionCommand(URI buildId, URI executionId, Date createdOn) {
		this.executionId = executionId;
		this.buildId = buildId;
		this.createdOn = createdOn;
	}

	@Override
	public void accept(CommandVisitor visitor) {
		if(visitor!=null) {
			visitor.visitCreateExecutionCommand(this);
		}
	}

	public URI buildId() {
		return this.buildId;
	}

	public URI executionId() {
		return this.executionId;
	}

	public Date createdOn() {
		return this.createdOn;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("buildId",this.buildId).
					add("executionId",this.executionId).
					add("createdOn",this.createdOn).
					toString();
	}

	public static Builder builder() {
		return new Builder();
	}

}
