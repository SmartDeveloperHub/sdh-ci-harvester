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

import com.google.common.base.MoreObjects.ToStringHelper;

public final class UpdateBuildCommand extends BuildCommand {

	public static final class Builder extends BuildCommandBuilder<UpdateBuildCommand,Builder> {

		private Builder() {
			super(Builder.class);
		}

		@Override
		public UpdateBuildCommand build() {
			return
				new UpdateBuildCommand(
					checkNotNull(super.buildId(),"Build identifier cannot be null"),
					checkNotNull(super.title(),"Title cannot be null"),
					super.description(),
					super.createdOn(),
					super.codebase()
				);
		}

	}

	private UpdateBuildCommand(URI buildId, String title, String description, Date creationDate, URI codebase) {
		super(buildId,title,description,creationDate,codebase);
	}

	@Override
	public void accept(CommandVisitor visitor) {
		if(visitor!=null) {
			visitor.visitUpdateBuildCommand(this);
		}
	}

	@Override
	protected void toString(ToStringHelper helper) {
		// Nothing to add
	}

	public static Builder builder() {
		return new Builder();
	}

}
