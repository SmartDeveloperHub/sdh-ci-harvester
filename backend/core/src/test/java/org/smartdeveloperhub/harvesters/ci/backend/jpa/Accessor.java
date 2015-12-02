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
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Branch;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Commit;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.CompletedEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Factory;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Repository;

public abstract class Accessor {

	private static volatile Accessor DEFAULT;

	public static void setDefault(final Accessor accessor) {
		if (DEFAULT != null) {
			throw new IllegalStateException();
		}
		DEFAULT = accessor;
	}

	public static Accessor getDefault() {
		final Accessor a = DEFAULT;
		if (a != null) {
			return a;
		}
		try {
			Class.forName(Factory.class.getName(),true, Factory.class.getClassLoader());
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
		return DEFAULT;
	}

	public abstract Repository createRepository(URI location, URI resource);
	public abstract Branch createBranch(URI location, String branchName, URI resource);
	public abstract Commit createCommit(URI location, String branchName, String commitId, URI resource);
	public abstract PendingEnrichment createPendingEnrichment(URI repositoryLocation, String branchName, String commitId);
	public abstract CompletedEnrichment createCompletedEnrichment(URI repositoryResource, URI branchResource, URI commitResource);

}
