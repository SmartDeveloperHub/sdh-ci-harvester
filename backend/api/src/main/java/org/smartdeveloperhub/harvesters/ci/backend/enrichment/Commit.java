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
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class Commit extends External {

	private CommitId id;

	Commit() {
		super();
	}

	private Commit(final CommitId id, final URI resource) {
		super(resource);
		this.id=id;
	}

	public CommitId id() {
		return this.id;
	}

	public URI repository() {
		return this.id.branchId().repository();
	}

	public String name() {
		return this.id.branchId().name();
	}

	public String commitId() {
		return this.id.commitId();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id,resource());
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof Commit) {
			final Commit that=(Commit)obj;
			result=
				Objects.equals(this.id,that.id) &&
				Objects.equals(this.resource(),that.resource());
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("id",this.id).
					add("resource",resource()).
					toString();
	}

	static Commit newInstance(final Branch branch, final String commitId, final URI resource) {
		return new Commit(CommitId.create(branch.id(), commitId),resource);
	}

}
