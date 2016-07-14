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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.4.0-SNAPSHOT
 *   Bundle      : ci-backend-mem-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Commit;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.CommitId;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CommitRepository;

import com.google.common.collect.Maps;

public class InMemoryCommitRepository implements CommitRepository {

	private final ConcurrentMap<CommitId,Commit> commits;

	public InMemoryCommitRepository() {
		this.commits=Maps.newConcurrentMap();
	}

	@Override
	public void add(final Commit commit) {
		checkNotNull(commit,"Commit cannot be null");
		final Commit previous = this.commits.putIfAbsent(commit.id(),commit);
		checkArgument(previous==null,"A commit identified by '%s' already exists",commit.id());
	}

	@Override
	public void remove(final Commit commit) {
		checkNotNull(commit,"Commit cannot be null");
		this.commits.remove(commit.id(),commit);
	}

	@Override
	public Commit commitOfId(final CommitId commitId) {
		checkNotNull(commitId,"Commit identifier cannot be null");
		return this.commits.get(commitId);
	}

}
