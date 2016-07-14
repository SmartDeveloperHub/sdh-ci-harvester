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
package org.smartdeveloperhub.harvesters.ci.backend.domain.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.harvesters.ci.backend.domain.Result.Status;

import com.google.common.base.MoreObjects;

public final class CreateExecutionCommand implements Command {

	public static final class Builder {

		private URI buildId;
		private Date createdOn;
		private URI executionId;
		private String branchName;
		private URI codebase;
		private String commitId;
		private Status status;
		private Date finishedOn;

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

		public Builder withCodebase(URI codebase) {
			this.codebase = codebase;
			return this;
		}

		public Builder withBranchName(String branchName) {
			this.branchName = branchName;
			return this;
		}

		public Builder withCommitId(String commitId) {
			this.commitId = commitId;
			return this;
		}

		public Builder withStatus(Status status) {
			this.status = status;
			return this;
		}

		public Builder withFinishedOn(Date finishedOn) {
			this.finishedOn = finishedOn;
			return this;
		}

		public CreateExecutionCommand build() {
			return
				new CreateExecutionCommand(
					checkNotNull(this.buildId,"Build identifier cannot be null"),
					checkNotNull(this.executionId,"Execution identifier cannot be null"),
					checkNotNull(this.createdOn,"Creation date cannot be null"),
					this.codebase,
					this.branchName,
					this.commitId,
					this.status,
					this.finishedOn
				);
		}

	}

	private final URI executionId;
	private final URI buildId;
	private final Date createdOn;
	private final URI codebase;
	private final String branchName;
	private final String commitId;
	private final Status status;
	private final Date finishedOn;

	private CreateExecutionCommand(URI buildId, URI executionId, Date createdOn, URI codebase, String branchName, String commitId, Status status, Date finishedOn) {  // NOSONAR
		this.executionId = executionId;
		this.buildId = buildId;
		this.createdOn = createdOn;
		this.codebase = codebase;
		this.branchName = branchName;
		this.commitId = commitId;
		this.status=status;
		this.finishedOn=finishedOn;
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

	public URI codebase() {
		return this.codebase;
	}

	public String branchName() {
		return this.branchName;
	}

	public String commitId() {
		return this.commitId;
	}

	public Status status() {
		return this.status;
	}

	public Date finishedOn() {
		return this.finishedOn;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("buildId",this.buildId).
					add("executionId",this.executionId).
					add("createdOn",this.createdOn).
					add("codebase",this.codebase).
					add("branchName",this.branchName).
					add("commitId",this.commitId).
					add("status",this.status).
					add("finishedOn",this.finishedOn).
					toString();
	}

	public static Builder builder() {
		return new Builder();
	}

}
