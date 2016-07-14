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

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class BranchId implements Serializable {

	private static final long serialVersionUID = 3148456671327778847L;

	private URI repository;
	private String name;

	private BranchId() {
	}

	private BranchId(final URI repository, final String branchName) {
		this.repository = repository;
		this.name = branchName;
	}

	public URI repository() {
		return this.repository;
	}

	public String name() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.repository,this.name);
	}

	@Override
	public boolean equals(final Object obj) {
		boolean result=false;
		if(obj instanceof BranchId) {
			final BranchId that=(BranchId)obj;
			result=
				Objects.equals(this.repository,that.repository) &&
				Objects.equals(this.name,that.name);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("repository",this.repository).
					add("name",this.name).
					toString();
	}

	public static BranchId create(final URI location, final String branchName) {
		return new BranchId(location,branchName);
	}

}