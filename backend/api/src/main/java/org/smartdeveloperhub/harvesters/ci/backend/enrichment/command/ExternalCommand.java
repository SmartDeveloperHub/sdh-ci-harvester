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

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public abstract class ExternalCommand {

	public abstract static class ExternalCommandBuilder<T extends ExternalCommand, B extends ExternalCommandBuilder<T,B>> {

		private final Class<? extends B> clazz;
		private URI resource;

		ExternalCommandBuilder(final Class<? extends B> clazz) {
			this.clazz = clazz;
		}

		public B withResource(final URI resource) {
			this.resource=resource;
			return this.clazz.cast(this);
		}

		URI resource() {
			return checkNotNull(this.resource,"Resource URI cannot be null");
		}

		public abstract T build();

	}

	private final URI resource;

	ExternalCommand(final URI resource) {
		this.resource = resource;
	}

	public final URI resource() {
		return this.resource;
	}

	@Override
	public final String toString() {
		final ToStringHelper helper =
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("resource", this.resource);
		toString(helper);
		return helper.toString();
	}

	protected abstract void toString(ToStringHelper helper);

}
