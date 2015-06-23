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
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
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

	private static URI buildId(Service service, String id) {
		return service.serviceId().resolve("jobs/"+id+"/");
	}

	private static URI buildId(CompositeBuild build, String id) {
		return build.buildId().resolve(id+"/");
	}

	private static URI executionId(Build build, int executionIdi) {
		return build.buildId().resolve(executionIdi+"/");
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

	private static void createExecutions(ExecutionRepository repository, Build build) {
		Execution failedExecution  = createExecution(repository,build,null,            1,Status.FAILED);
		Execution warningExecution = createExecution(repository,build,failedExecution, 2,Status.WARNING);
		Execution errorExecution   = createExecution(repository,build,warningExecution,3,Status.NOT_BUILT);
		Execution passedExecution  = createExecution(repository,build,errorExecution,  4,Status.PASSED);
		Execution abortedExecution = createExecution(repository,build,passedExecution, 5,Status.ABORTED);
									 createExecution(repository,build,abortedExecution,6,null);
	}

	private static void createBuild(BuildRepository repository, Build build, Date createdOn, String description) {
		build.setCreatedOn(after(createdOn));
		build.setDescription(description);
		build.setCodebase(build.buildId().resolve("repository.git"));
		repository.add(build);
	}

	// TODO: Change to JPA persistency layer when ready
	static ContinuousIntegrationService inititializeBackend(URI jenkinsInstance) {
		ServiceRepository serviceRepository=new InMemoryServiceRepository();
		BuildRepository buildRepository=new InMemoryBuildRepository();
		ExecutionRepository executionRepository=new InMemoryExecutionRepository();

		Date initTime = new Date();

		Service defaultService = Service.newInstance(jenkinsInstance);
		serviceRepository.add(defaultService);

		SimpleBuild simpleBuild=defaultService.addSimpleBuild(buildId(defaultService, "simple-job"),"Example simple build");
		createBuild(buildRepository, simpleBuild, initTime, "An example simple build for testing");
		createExecutions(executionRepository,simpleBuild);

		CompositeBuild compositeBuild=defaultService.addCompositeBuild(buildId(defaultService, "composite-job"),"Example composite build");
		createBuild(buildRepository, compositeBuild, initTime, "An example composite build for testing");
		createExecutions(executionRepository, compositeBuild);

		SubBuild subBuild=compositeBuild.addSubBuild(buildId(compositeBuild, "sub-job"),"Example sub build");
		createBuild(buildRepository, subBuild, initTime, "An example sub build for testing");
		createExecutions(executionRepository, subBuild);

		return
			new ContinuousIntegrationService(
				serviceRepository,
				buildRepository,
				executionRepository);
	}

}
