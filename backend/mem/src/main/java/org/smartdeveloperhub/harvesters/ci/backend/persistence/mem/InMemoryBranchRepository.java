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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-mem:0.3.0
 *   Bundle      : ci-backend-mem-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.mem;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ConcurrentMap;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Branch;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.BranchId;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.BranchRepository;

import com.google.common.collect.Maps;

public class InMemoryBranchRepository implements BranchRepository {

	private final ConcurrentMap<BranchId,Branch> branches;

	public InMemoryBranchRepository() {
		this.branches=Maps.newConcurrentMap();
	}

	@Override
	public void add(final Branch branch) {
		checkNotNull(branch,"Branch cannot be null");
		final Branch previous = this.branches.putIfAbsent(branch.id(),branch);
		checkArgument(previous==null,"A branch identified by '%s' already exists",branch.id());
	}

	@Override
	public void remove(final Branch branch) {
		checkNotNull(branch,"Branch cannot be null");
		this.branches.remove(branch.id(),branch);
	}

	@Override
	public Branch branchOfId(final BranchId branchId) {
		checkNotNull(branchId,"Branch identifier cannot be null");
		return this.branches.get(branchId);
	}

}
