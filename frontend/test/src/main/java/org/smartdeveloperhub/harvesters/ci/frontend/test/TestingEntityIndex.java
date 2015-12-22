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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-test:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-test-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.test;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ExecutionEnrichment;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EnrichedExecution;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class TestingEntityIndex implements EntityIndex {

	private final ContinuousIntegrationService cis;
	private final EnrichmentService es;

	TestingEntityIndex(final ContinuousIntegrationService cis, final EnrichmentService es) {
		this.cis = cis;
		this.es = es;
	}

	@Override
	public Service findService(final URI serviceId) {
		return this.cis.getService(serviceId);
	}

	@Override
	public Build findBuild(final URI buildId) {
		return this.cis.getBuild(buildId);
	}

	@Override
	public Execution findExecution(final URI executionId) {
		return this.cis.getExecution(executionId);
	}

	@Override
	public EnrichedExecution findEnrichedExecution(final URI executionId) {
		final Execution execution = findExecution(executionId);
		final ExecutionEnrichment enrichment=this.es.getEnrichment(execution);
		return new SimpleEnrichedExecution(execution,enrichment);
	}

}