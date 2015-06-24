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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;
import java.util.Date;

import org.junit.Test;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.mem.InMemoryBuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.mem.InMemoryExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.mem.InMemoryServiceRepository;

public class HarvesterApplicationTest {

	private static final URI SERVICE_ID= URI.create("http://ci.travis-ci.org/");
	private static final URI BUILD_ID= SERVICE_ID.resolve("jobs/example-job/");
	private static final URI EXECUTION_ID= BUILD_ID.resolve("1/");

	@Test
	public void doTest() {
		ServiceRepository serviceRepository=new InMemoryServiceRepository();
		BuildRepository buildRepository=new InMemoryBuildRepository();
		ExecutionRepository executionRepository=new InMemoryExecutionRepository();


		Date executionCreationDate = new Date();
		Date buildCreationDate = new Date(executionCreationDate.getTime()-3600000);

		Service defaultService = Service.newInstance(SERVICE_ID);
		Build defaultBuild=defaultService.addSimpleBuild(BUILD_ID,"Example build");
		Execution defaultExecution=defaultBuild.addExecution(EXECUTION_ID, executionCreationDate);

		defaultBuild.setCreatedOn(buildCreationDate);
		defaultBuild.setDescription("An example build for testing");
		defaultBuild.setCodebase(BUILD_ID);

		serviceRepository.add(defaultService);
		buildRepository.add(defaultBuild);
		executionRepository.add(defaultExecution);

		ContinuousIntegrationService service =
				new ContinuousIntegrationService(
					serviceRepository,
					buildRepository,
					executionRepository);

		System.out.println(service.getService(SERVICE_ID));
		System.out.println(service.getBuild(BUILD_ID));
		System.out.println(service.getExecution(EXECUTION_ID));
	}

}
