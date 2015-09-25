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
package org.smartdeveloperhub.harvesters.ci.backend.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;

import com.google.common.base.MoreObjects;

public final class FinishExecutionCommand implements Command {

	public static final class Builder {

		private URI executionId;
		private Status status;
		private Date finishedOn;

		private Builder() {
			this.status=Status.PASSED;
		}

		public Builder withExecutionId(URI executionId) {
			this.executionId = executionId;
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

		public FinishExecutionCommand build() {
			return
				new FinishExecutionCommand(
					checkNotNull(this.executionId,"Execution identifier cannot be null"),
					checkNotNull(this.status,"Status cannot be null"),
					checkNotNull(this.finishedOn,"Finalization date cannot be null")
				);
		}

	}

	private final URI executionId;
	private final Status status;
	private final Date finishedOn;

	private FinishExecutionCommand(URI executionId, Status status, Date finishedOn) {
		this.executionId = executionId;
		this.status = status;
		this.finishedOn = finishedOn;
	}

	@Override
	public void accept(CommandVisitor visitor) {
		if(visitor!=null) {
			visitor.visitFinishExecutionCommand(this);
		}
	}

	public URI executionId() {
		return this.executionId;
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
					add("executionId",this.executionId).
					add("status",this.status).
					add("finishedOn",this.finishedOn).
					toString();
	}

	public static Builder builder() {
		return new Builder();
	}

}
