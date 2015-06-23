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

import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;

public final class HarvesterApplication extends Application<HarvesterConfiguration> {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterApplication.class);

	private static final URI SERVICE_ID= URI.create("http://ci.travis-ci.org/");
	private static final URI BUILD_ID= SERVICE_ID.resolve("jobs/example-job/");
	private static final URI EXECUTION_ID= BUILD_ID.resolve("1/");

	private static final String SERVICE_PATH= "service/";

	private final Name<URI> serviceName;

	private ContinuousIntegrationService backendService;

	public HarvesterApplication() {
		this.serviceName=
			NamingScheme.
				getDefault().
					name(SERVICE_ID);
	}

	@Override
	public void setup(Environment environment, Bootstrap<HarvesterConfiguration> bootstrap) {
		LOGGER.info("Starting CI Harvester Application configuration...");

		this.backendService=inititializeBackend();

		bootstrap.addHandler(new ServiceHandler(backendService));
		bootstrap.addHandler(new BuildContainerHandler(backendService));
		bootstrap.addHandler(new BuildHandler(backendService));
		bootstrap.addHandler(new ExecutionContainerHandler(backendService));
		bootstrap.addHandler(new ExecutionHandler(backendService));

		environment.
			publishResource(
				this.serviceName,
				ServiceHandler.class,
				SERVICE_PATH);

		LOGGER.info("Contacts CI Harvester Application configuration completed.");
	}

	private ContinuousIntegrationService inititializeBackend() {
		// TODO: Change to JPS persistency layer when ready
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

		return
			new ContinuousIntegrationService(
				serviceRepository,
				buildRepository,
				executionRepository);
	}

	@Override
	public void initialize(WriteSession session) {
		LOGGER.info("Initializing CI Harvester Application...");
		try {
			BackendModelPublisher publisher=BackendModelPublisher.newInstance(session);
			Service service=this.backendService.getService(SERVICE_ID);
			publisher.publish(service);
			for(URI buildId:service.builds()) {
				Build build = this.backendService.getBuild(buildId);
				publisher.publish(build);
				for(URI executionId:build.executions()) {
					Execution execution = this.backendService.getExecution(executionId);
					publisher.publish(execution);
				}
			}
			session.saveChanges();
			LOGGER.info("CI Harvester Application initialization completed.");
		} catch (WriteSessionException e) {
			LOGGER.warn("CI Harvester Application initialization failed.",e);
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void shutdown() {
		LOGGER.info("Starting CI Harvester Application shutdown...");
		LOGGER.info("CI Harvester Application shutdown completed.");
	}

}