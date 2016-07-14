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
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;

public final class Branch extends External {

	private BranchId id;
	private Set<String> commits;

	Branch() {
		super();
	}

	private Branch(final BranchId id, final URI resource) {
		super(resource);
		this.id=id;
		this.commits=Sets.newLinkedHashSet();
	}

	public BranchId id() {
		return this.id;
	}

	public URI repository() {
		return this.id.repository();
	}

	public String name() {
		return this.id.name();
	}

	public Set<String> commits() {
		return this.commits;
	}

	public Commit createCommit(final String commitId, final URI resource) {
		checkNotNull(commitId,"Commit identifier cannot be null");
		checkNotNull(resource,"Commit resource cannot be null");
		checkState(commits().add(commitId),"A commit identified by '%s' does already exist in branch '%s' of repository '%s'",commitId,this.id.name(),this.id.repository());
		return Commit.newInstance(this,commitId,resource);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id,resource());
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof Branch) {
			final Branch that=(Branch)obj;
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
					add("commits",this.commits).
					toString();
	}

	static Branch newInstance(final Repository repository, final String branchName, final URI resource) {
		return new Branch(BranchId.create(repository.location(),branchName),resource);
	}

}
