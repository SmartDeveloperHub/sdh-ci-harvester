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
import java.util.Random;

import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.mem.InMemoryBuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.mem.InMemoryExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.mem.InMemoryServiceRepository;

final class BackendController {

	private BackendController() {
	}

	private static Date after(Date date) {
		Random random=new Random(System.nanoTime());
		return new Date(date.getTime()+(random.nextLong() % 3600000));
	}

	private static URI executionId(Build defaultBuild, int executionIdi) {
		return defaultBuild.buildId().resolve(executionIdi+"/");
	}

	private static Execution createExecution(ExecutionRepository repository, Build build, Execution execution, int executionIdi, Status status) {
		Date date=build.createdOn();
		if(execution!=null) {
			if(execution.isFinished()) {
				date=execution.result().finishedOn();
			} else {
				date=execution.createdOn();
			}
		}
		Execution newExecution=build.addExecution(executionId(build, executionIdi), after(date));
		if(status!=null) {
			newExecution.finish(new Result(status,after(newExecution.createdOn())));
		}
		repository.add(newExecution);
		return newExecution;
	}

	// TODO: Change to JPA persistency layer when ready
	static ContinuousIntegrationService inititializeBackend(URI jenkinsInstance) {
		ServiceRepository serviceRepository=new InMemoryServiceRepository();
		BuildRepository buildRepository=new InMemoryBuildRepository();
		ExecutionRepository executionRepository=new InMemoryExecutionRepository();

		Date initTime = new Date();

		Service defaultService = Service.newInstance(jenkinsInstance);
		serviceRepository.add(defaultService);

		Build simpleBuild=defaultService.addSimpleBuild(jenkinsInstance.resolve("jobs/example-job/"),"Example build");
		simpleBuild.setCreatedOn(after(initTime));
		simpleBuild.setDescription("An example build for testing");
		simpleBuild.setCodebase(simpleBuild.buildId().resolve("repository.git"));
		buildRepository.add(simpleBuild);

		Execution failedExecution  = createExecution(executionRepository,simpleBuild,null,            1,Status.FAILED);
		Execution warningExecution = createExecution(executionRepository,simpleBuild,failedExecution, 2,Status.WARNING);
		Execution errorExecution   = createExecution(executionRepository,simpleBuild,warningExecution,3,Status.NOT_BUILT);
		Execution passedExecution  = createExecution(executionRepository,simpleBuild,errorExecution,  4,Status.PASSED);
		Execution abortedExecution = createExecution(executionRepository,simpleBuild,passedExecution, 5,Status.ABORTED);
		                             createExecution(executionRepository,simpleBuild,abortedExecution,6,null);

		return
			new ContinuousIntegrationService(
				serviceRepository,
				buildRepository,
				executionRepository);
	}

}
