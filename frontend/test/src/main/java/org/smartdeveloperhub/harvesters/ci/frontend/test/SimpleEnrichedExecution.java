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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-test:0.4.0-SNAPSHOT
 *   Bundle      : ci-frontend-test-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.test;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ExecutionEnrichment;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EnrichedExecution;

import com.google.common.base.Optional;

final class SimpleEnrichedExecution implements EnrichedExecution {

	private final Execution execution;
	private final ExecutionEnrichment enrichment;

	SimpleEnrichedExecution(final Execution execution, final ExecutionEnrichment enrichment) {
		this.execution = execution;
		this.enrichment = enrichment;
	}

	@Override
	public Optional<URI> repositoryResource() {
		return this.enrichment.repositoryResource();
	}

	@Override
	public Optional<URI> branchResource() {
		return this.enrichment.branchResource();
	}

	@Override
	public Optional<URI> commitResource() {
		return this.enrichment.commitResource();
	}

	@Override
	public Execution target() {
		return this.execution;
	}
}