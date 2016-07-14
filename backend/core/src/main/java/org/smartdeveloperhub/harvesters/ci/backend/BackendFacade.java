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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0
 *   Bundle      : ci-backend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import java.io.Closeable;
import java.io.IOException;

import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Deployment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.SourceCodeManagementService;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.spi.ComponentRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.spi.RuntimeDelegate;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;

public final class BackendFacade implements Closeable {

	private final ContinuousIntegrationService applicationService;
	private final JenkinsIntegrationService integrationService;

	private final ComponentRegistry componentRegistry;
	private final EnrichmentService enrichmentService;
	private final SourceCodeManagementService scmService;

	private BackendFacade(final ComponentRegistry componentRegistry, final Deployment configuration) {
		this.componentRegistry=componentRegistry;
		this.scmService =
			new SourceCodeManagementService(
				this.componentRegistry.getRepositoryRepository(),
				this.componentRegistry.getBranchRepository(),
				this.componentRegistry.getCommitRepository());
		this.enrichmentService=
			new EnrichmentService(
				this.scmService,
				this.componentRegistry.getExecutionRepository(),
				this.componentRegistry.getPendingEnrichmentRepository(),
				this.componentRegistry.getCompletedEnrichmentRepository(),
				this.componentRegistry.getTransactionManager(),
				configuration);
		this.applicationService=
			new ContinuousIntegrationService(
				this.componentRegistry.getServiceRepository(),
				this.componentRegistry.getBuildRepository(),
				this.componentRegistry.getExecutionRepository());
		this.integrationService=
			new JenkinsIntegrationService(
				this.applicationService,
				this.enrichmentService,
				this.componentRegistry.getTransactionManager());
	}

	public static BackendFacade create(final BackendConfig config) {
		return
			new BackendFacade(
				RuntimeDelegate.getInstance().getComponentRegistry(config.getDatabase()),
				Deployment.fromConfiguration(config.getEnrichment()));
	}

	public TransactionManager transactionManager() {
		return this.componentRegistry.getTransactionManager();
	}

	public ContinuousIntegrationService applicationService() {
		return this.applicationService;
	}

	public JenkinsIntegrationService integrationService() {
		return this.integrationService;
	}

	public EnrichmentService enrichmentService() {
		return this.enrichmentService;
	}

	public SourceCodeManagementService sourceCodeManagementService() {
		return this.scmService;
	}

	@Override
	public void close() throws IOException {
		this.componentRegistry.close();
	}

}
