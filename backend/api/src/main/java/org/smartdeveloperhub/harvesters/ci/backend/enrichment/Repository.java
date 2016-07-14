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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.3.0
 *   Bundle      : ci-backend-api-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

public final class Repository extends External {

	private URI location;
	private Set<String> branches;

	Repository() {
		super();
	}

	private Repository(final URI location, final URI resource) {
		super(resource);
		this.location=location;
		this.branches=Sets.newLinkedHashSet();
	}

	public URI location() {
		return this.location;
	}

	public Set<String> branches() {
		return this.branches;
	}

	public Branch createBranch(final String branchName, final URI resource) {
		checkNotNull(branchName,"Branch name cannot be null");
		checkNotNull(resource,"Branch resource cannot be null");
		checkState(branches().add(branchName),"A branch named '%s' does already exist",branchName);
		return Branch.newInstance(this,branchName,resource);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.location,resource());
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof Repository) {
			final Repository that=(Repository)obj;
			result=
				Objects.equals(this.location,that.location) &&
				Objects.equals(this.resource(),that.resource());
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("location",this.location).
					add("resource",resource()).
					add("branches",this.branches).
					toString();
	}

	static Repository newInstance(final URI location, final URI resource) {
		return new Repository(location,resource);
	}

}
