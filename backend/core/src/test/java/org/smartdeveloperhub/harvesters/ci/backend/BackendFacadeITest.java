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
package org.smartdeveloperhub.harvesters.ci.backend;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.BrokerConfig;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentConfig;

public class BackendFacadeITest extends SmokeTest {

	private BackendFacade facade;

	@Before
	public void startUp() throws IOException {
		final DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setProvider(DerbyProvider.class.getCanonicalName());
		final BackendConfig config=new BackendConfig();
		config.setDatabase(databaseConfig);
		final EnrichmentConfig enrichment = new EnrichmentConfig();
		config.setEnrichment(enrichment);
		enrichment.setBroker(new BrokerConfig());
		this.facade = BackendFacade.create(config);
	}

	@After
	public void shutDown() throws Exception {
		this.facade.close();
	}

	@Test
	public void smokeTest() throws Exception {
		smokeTest(
			this.facade.applicationService(),
			this.facade.integrationService(),
			this.facade.enrichmentService());
	}

}
