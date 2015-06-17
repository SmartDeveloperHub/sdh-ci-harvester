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

public final class UpdateBuildCommand {

	public static final class Builder {

		private URI buildId;
		private String title;
		private String description;
		private Date creationDate;
		private URI codebase;

		public Builder withBuildId(URI buildId) {
			this.buildId = buildId;
			return this;
		}

		public Builder withTitle(String title) {
			this.title=title;
			return this;
		}

		public Builder withDescription(String description) {
			this.description=description;
			return this;
		}

		public Builder withCreationDate(Date creationDate) {
			this.creationDate=creationDate;
			return this;
		}

		public Builder withCodebase(URI codebase) {
			this.codebase = codebase;
			return this;
		}

		public UpdateBuildCommand build() {
			return
				new UpdateBuildCommand(
					checkNotNull(this.buildId,"Build identifier cannot be null"),
					checkNotNull(this.title,"Title cannot be null"),
					this.description,
					checkNotNull(this.creationDate,"Creation date cannot be null"),
					this.codebase
				);
		}

	}

	private final URI buildId;
	private final String title;
	private final String description;
	private final Date createdOn;
	private final URI codebase;

	private UpdateBuildCommand(URI buildId, String title, String description, Date creationDate, URI codebase) {
		this.buildId = buildId;
		this.title = title;
		this.description = description;
		this.createdOn = creationDate;
		this.codebase = codebase;
	}

	public URI buildId() {
		return this.buildId;
	}

	public String title() {
		return this.title;
	}

	public String description() {
		return this.description;
	}

	public Date createdOn() {
		return this.createdOn;
	}

	public URI codebase() {
		return this.codebase;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("buildId", this.buildId).
					add("title", this.title).
					add("description", this.description).
					add("createdOn", this.createdOn).
					add("codebase", this.codebase).
					toString();
	}

	public static Builder builder() {
		return new Builder();
	}

}
