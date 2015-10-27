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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-test:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-test-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.test;

import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.Codebase;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryBuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryServiceRepository;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class TestingBackendController implements BackendController {

	private static final Logger LOGGER=LoggerFactory.getLogger(TestingBackendController.class);

	private URI jenkinsInstance;

	private final ServiceRepository serviceRepository;

	private final BuildRepository buildRepository;

	private final ExecutionRepository executionRepository;

	private final ContinuousIntegrationService service;

	private final TestingEntityIndex index;

	TestingBackendController() {
		this.serviceRepository = new InMemoryServiceRepository();
		this.buildRepository = new InMemoryBuildRepository();
		this.executionRepository = new InMemoryExecutionRepository();
		this.service=
			new ContinuousIntegrationService(
				this.serviceRepository,
				this.buildRepository,
				this.executionRepository);
		this.index = new TestingEntityIndex(this.service);
		populateBackend(URI.create("http://ci.jenkins-ci.org/"));
	}

	void setInstance(final URI jenkinsInstance) {
		checkState(this.jenkinsInstance==null,"Already connected (%s)",this.jenkinsInstance);
		this.jenkinsInstance = jenkinsInstance;
	}

	private static Date after(final Date date) {
		final Random random=new Random(System.nanoTime());
		return new Date(date.getTime()+(random.nextLong() % 3600000));
	}

	private static URI buildId(final Service service, final String id) {
		return service.serviceId().resolve("jobs/"+id+"/");
	}

	private static URI buildId(final CompositeBuild build, final String id) {
		return build.buildId().resolve(id+"/");
	}

	private static URI executionId(final Build build, final int executionIdi) {
		return build.buildId().resolve(executionIdi+"/");
	}

	private static Execution createExecution(final ExecutionRepository repository, final Build build, final Execution execution, final int executionIdi, final Status status) {
		Date date=build.createdOn();
		if(execution!=null) {
			if(execution.isFinished()) {
				date=execution.result().finishedOn();
			} else {
				date=execution.createdOn();
			}
		}
		final Execution newExecution=build.addExecution(executionId(build, executionIdi), after(date), build.codebase(), "12344as343asq343");
		if(status!=null) {
			newExecution.finish(new Result(status,after(newExecution.createdOn())));
		}
		repository.add(newExecution);
		return newExecution;
	}

	private static void createExecutions(final ExecutionRepository repository, final Build build) {
		final Execution failedExecution  = createExecution(repository,build,null,            1,Status.FAILED);
		final Execution warningExecution = createExecution(repository,build,failedExecution, 2,Status.WARNING);
		final Execution errorExecution   = createExecution(repository,build,warningExecution,3,Status.NOT_BUILT);
		final Execution passedExecution  = createExecution(repository,build,errorExecution,  4,Status.PASSED);
		final Execution abortedExecution = createExecution(repository,build,passedExecution, 5,Status.ABORTED);
									 createExecution(repository,build,abortedExecution,6,null);
	}

	private static void createBuild(final BuildRepository repository, final Build build, final Date createdOn, final String description) {
		build.setCreatedOn(after(createdOn));
		build.setDescription(description);
		build.setCodebase(new Codebase(build.buildId().resolve("repository.git"),"master"));
		repository.add(build);
	}

	private void populateBackend(final URI jenkinsInstance) {
		final Date initTime = new Date();

		final Service defaultService = Service.newInstance(jenkinsInstance);
		this.serviceRepository.add(defaultService);

		final SimpleBuild simpleBuild=defaultService.addSimpleBuild(buildId(defaultService, "simple-job"),"Example simple build");
		createBuild(this.buildRepository, simpleBuild, initTime, "An example simple build for testing");
		createExecutions(this.executionRepository,simpleBuild);

		final CompositeBuild compositeBuild=defaultService.addCompositeBuild(buildId(defaultService, "composite-job"),"Example composite build");
		createBuild(this.buildRepository, compositeBuild, initTime, "An example composite build for testing");
		createExecutions(this.executionRepository, compositeBuild);

		final SubBuild subBuild=compositeBuild.addSubBuild(buildId(compositeBuild, "sub-job"),"Example sub build");
		createBuild(this.buildRepository, subBuild, initTime, "An example sub build for testing");
		createExecutions(this.executionRepository, subBuild);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityIndex entityIndex() {
		return this.index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setTargetService(final URI instance) {
		setInstance(instance);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(final EntityLifecycleEventListener listener) {
		LOGGER.info("Connecting to {}...",this.jenkinsInstance);
		LOGGER.info("Connected to {}.",this.jenkinsInstance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		LOGGER.info("Disconnecting from {}...",this.jenkinsInstance);
		LOGGER.info("Disconnected from {}.",this.jenkinsInstance);
	}

}
