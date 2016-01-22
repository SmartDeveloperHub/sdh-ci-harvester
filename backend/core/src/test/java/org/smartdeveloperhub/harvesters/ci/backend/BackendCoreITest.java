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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import java.io.IOException;
import java.net.URI;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartdeveloperhub.harvesters.ci.backend.database.Database;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Deployment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ResolverService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.SourceCodeManagementService;
import org.smartdeveloperhub.harvesters.ci.backend.integration.JenkinsIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.jpa.JPAComponentRegistry;

public class BackendCoreITest extends SmokeTest {

	private EntityManagerFactory factory;
	private JPAComponentRegistry persistencyFacade;

	@Before
	public void startUp() throws IOException {
		this.factory = Persistence.createEntityManagerFactory("itTestsDerby");
		this.persistencyFacade =
			new JPAComponentRegistry(
				new Database(){
					@Override
					public void close() throws IOException {
						if(BackendCoreITest.this.factory!=null) {
							BackendCoreITest.this.factory.close();
						}
					}
					@Override
					public EntityManagerFactory getEntityManagerFactory() {
						return BackendCoreITest.this.factory;
					}
				}
			);
	}

	@After
	public void shutDown() throws Exception {
		if(this.persistencyFacade!=null) {
			this.persistencyFacade.close();
		}
	}

	@Test
	public void smokeTest() throws Exception {
		final EnrichmentService ers =
			new EnrichmentService(
				new SourceCodeManagementService(
					this.persistencyFacade.getRepositoryRepository(),
					this.persistencyFacade.getBranchRepository(),
					this.persistencyFacade.getCommitRepository()),
				this.persistencyFacade.getExecutionRepository(),
				this.persistencyFacade.getPendingEnrichmentRepository(),
				this.persistencyFacade.getCompletedEnrichmentRepository(),
				this.persistencyFacade.getTransactionManager(),
				Deployment.builder().build());
		final ContinuousIntegrationService cis =
			new ContinuousIntegrationService(
				this.persistencyFacade.getServiceRepository(),
				this.persistencyFacade.getBuildRepository(),
				this.persistencyFacade.getExecutionRepository());
		final JenkinsIntegrationService jis=
			new JenkinsIntegrationService(
				cis,
				ers,
				this.persistencyFacade.getTransactionManager());
		jis.setResolverService(
			new ResolverService() {
				@Override
				public URI resolveExecution(final Execution execution) {
					return null;
				}
				@Override
				public boolean isReady() {
					return true;
				}
			}
		);
		smokeTest(cis,jis,ers);
	}


}
