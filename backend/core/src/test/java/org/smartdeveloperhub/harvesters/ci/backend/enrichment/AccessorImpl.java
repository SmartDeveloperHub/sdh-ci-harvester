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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Branch;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Commit;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Repository;
import org.smartdeveloperhub.harvesters.ci.backend.jpa.Accessor;

final class AccessorImpl extends Accessor {

	@Override
	public Repository createRepository(final URI location, final URI resource) {
		return Repository.newInstance(location, resource);
	}

	@Override
	public Branch createBranch(final URI location, final String branchName, final URI resource) {
		return Branch.newInstance(createRepository(location,resource), branchName, resource);
	}

	@Override
	public Commit createCommit(final URI location, final String branchName, final String commitId, final URI resource) {
		return Commit.newInstance(createBranch(location,branchName,resource),commitId,resource);
	}

	@Override
	public PendingEnrichment createPendingEnrichment(final URI repositoryLocation, final String branchName, final String commitId) {
		return PendingEnrichment.newInstance(repositoryLocation,branchName,commitId);
	}

}
