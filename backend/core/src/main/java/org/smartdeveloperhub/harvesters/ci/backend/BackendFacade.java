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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import java.io.Closeable;
import java.io.IOException;

import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.spi.ComponentRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.spi.RuntimeDelegate;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;

public final class BackendFacade implements Closeable {

	private ContinuousIntegrationService applicationService;
	private JenkinsIntegrationService integrationService;

	private final ComponentRegistry componentRegistry;

	private BackendFacade(ComponentRegistry componentRegistry) {
		this.componentRegistry=componentRegistry;
		this.applicationService=
			new ContinuousIntegrationService(
				this.componentRegistry.getServiceRepository(),
				this.componentRegistry.getBuildRepository(),
				this.componentRegistry.getExecutionRepository());
		this.integrationService=
			new JenkinsIntegrationService(
				this.applicationService,
				this.componentRegistry.getTransactionManager());
	}

	public static BackendFacade create(DatabaseConfig config) {
		return new BackendFacade(RuntimeDelegate.getInstance().getComponentRegistry(config));
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

	@Override
	public void close() throws IOException {
		this.componentRegistry.close();
	}

}
