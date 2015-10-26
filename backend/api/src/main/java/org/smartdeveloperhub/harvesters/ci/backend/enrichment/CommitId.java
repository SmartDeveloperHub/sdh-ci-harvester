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

import java.io.Serializable;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class CommitId implements Serializable {

	private static final long serialVersionUID = 552945621672015380L;

	private BranchId branchId;
	private String commitId;

	CommitId() {
	}

	private CommitId(final BranchId branchId, final String commitId) {
		this.branchId = branchId;
		this.commitId = commitId;
	}

	public BranchId branchId() {
		return this.branchId;
	}

	public String commitId() {
		return this.commitId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.branchId,this.commitId);
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof CommitId) {
			final CommitId that=(CommitId)obj;
			result=
				Objects.equals(this.branchId,that.branchId) &&
				Objects.equals(this.commitId,that.commitId);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("branchId",this.branchId).
					add("commitId",this.commitId).
					toString();
	}

	public static CommitId create(final BranchId branchId, final String commitId) {
		return new CommitId(branchId,commitId);
	}

}