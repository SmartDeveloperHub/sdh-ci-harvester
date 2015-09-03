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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.1.0
 *   Bundle      : ci-backend-core-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartdeveloperhub.harvesters.ci.backend.database.Database;
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
						if(factory!=null) {
							factory.close();
						}
					}
					@Override
					public EntityManagerFactory getEntityManagerFactory() {
						return factory;
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
		ContinuousIntegrationService cis =
			new ContinuousIntegrationService(
				this.persistencyFacade.getServiceRepository(),
				this.persistencyFacade.getBuildRepository(),
				this.persistencyFacade.getExecutionRepository());
		JenkinsIntegrationService jis=
			new JenkinsIntegrationService(
				cis,
				persistencyFacade.getTransactionManager());
		smokeTest(cis,jis);
	}


}
