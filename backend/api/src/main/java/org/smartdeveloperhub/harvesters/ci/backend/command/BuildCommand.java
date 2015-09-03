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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.1.0
 *   Bundle      : ci-backend-api-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.command;

import java.net.URI;
import java.util.Date;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

abstract class BuildCommand implements Command {

	abstract static class BuildCommandBuilder<T extends BuildCommand, B extends BuildCommandBuilder<T,B>> {

		private final Class<? extends B> clazz;

		private URI buildId;
		private String title;
		private String description;
		private Date creationDate;
		private URI codebase;

		BuildCommandBuilder(Class<? extends B> clazz) {
			this.clazz = clazz;
		}


		public final B withBuildId(URI buildId) {
			this.buildId = buildId;
			return this.clazz.cast(this);
		}

		public final B withTitle(String title) {
			this.title=title;
			return this.clazz.cast(this);
		}

		public final B withDescription(String description) {
			this.description=description;
			return this.clazz.cast(this);
		}

		public final B withCreationDate(Date creationDate) {
			this.creationDate=creationDate;
			return this.clazz.cast(this);
		}

		public final B withCodebase(URI codebase) {
			this.codebase = codebase;
			return this.clazz.cast(this);
		}

		final URI buildId() {
			return this.buildId;
		}

		final String title() {
			return this.title;
		}

		final String description() {
			return this.description;
		}

		final Date createdOn() {
			return this.creationDate;
		}

		final URI codebase() {
			return this.codebase;
		}

		public abstract T build();

	}

	private final URI buildId;
	private final String title;
	private final String description;
	private final Date createdOn;
	private final URI codebase;

	BuildCommand(URI buildId, String title, String description, Date createdOn, URI codebase) {
		this.buildId = buildId;
		this.title = title;
		this.description = description;
		this.createdOn = createdOn;
		this.codebase = codebase;
	}

	public final URI buildId() {
		return this.buildId;
	}

	public final String title() {
		return this.title;
	}

	public final String description() {
		return this.description;
	}

	public final Date createdOn() {
		return this.createdOn;
	}

	public final URI codebase() {
		return this.codebase;
	}

	@Override
	public final String toString() {
		ToStringHelper helper = MoreObjects.
			toStringHelper(getClass()).
				omitNullValues().
				add("buildId", this.buildId).
				add("title", this.title).
				add("description", this.description).
				add("createdOn", this.createdOn).
				add("codebase", this.codebase);
		toString(helper);
		return helper.toString();
	}

	protected abstract void toString(ToStringHelper helper);

}
