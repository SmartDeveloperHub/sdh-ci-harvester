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

import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.SubBuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;

public final class HarvesterApplication extends Application<HarvesterConfiguration> {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterApplication.class);

	private static final URI SERVICE_ID= URI.create("http://ci.travis-ci.org/");

	private static final String SERVICE_PATH= "service/";

	private final Name<URI> serviceName;

	private ContinuousIntegrationService backendService;

	public HarvesterApplication() {
		this.serviceName=
			NamingScheme.
				getDefault().
					name(HarvesterApplication.SERVICE_ID);
	}

	@Override
	public void setup(Environment environment, Bootstrap<HarvesterConfiguration> bootstrap) {
		LOGGER.info("Starting CI Harvester Application configuration...");

		this.backendService=BackendController.inititializeBackend(HarvesterApplication.SERVICE_ID);

		bootstrap.addHandler(new ServiceHandler(this.backendService));
		bootstrap.addHandler(new BuildContainerHandler(this.backendService));
		bootstrap.addHandler(new SubBuildContainerHandler(this.backendService));
		bootstrap.addHandler(new BuildHandler(this.backendService));
		bootstrap.addHandler(new ExecutionContainerHandler(this.backendService));
		bootstrap.addHandler(new ExecutionHandler(this.backendService));

		environment.
			publishResource(
				this.serviceName,
				ServiceHandler.class,
				SERVICE_PATH);

		LOGGER.info("Contacts CI Harvester Application configuration completed.");
	}

	@Override
	public void initialize(WriteSession session) {
		LOGGER.info("Initializing CI Harvester Application...");
		try {
			BackendModelPublisher publisher=BackendModelPublisher.newInstance(session);
			Service service=this.backendService.getService(SERVICE_ID);
			publisher.publish(service);
			for(URI buildId:service.builds()) {
				Build build=publishBuild(publisher, buildId);
				if(build instanceof CompositeBuild) {
					publishSubBuilds(publisher, (CompositeBuild)build);
				}
			}
			session.saveChanges();
			LOGGER.info("CI Harvester Application initialization completed.");
		} catch (Exception e) {
			LOGGER.warn("CI Harvester Application initialization failed.",e);
			throw new IllegalStateException(e);
		}
	}

	private void publishSubBuilds(BackendModelPublisher publisher, CompositeBuild compositeBuild) {
		for(URI subBuildId:compositeBuild.subBuilds()) {
			publishBuild(publisher,subBuildId);
		}
	}

	private Build publishBuild(BackendModelPublisher publisher, URI buildId) {
		Build build = this.backendService.getBuild(buildId);
		publisher.publish(build);
		for(URI executionId:build.executions()) {
			Execution execution = this.backendService.getExecution(executionId);
			publisher.publish(execution);
		}
		return build;
	}

	@Override
	public void shutdown() {
		LOGGER.info("Starting CI Harvester Application shutdown...");
		LOGGER.info("CI Harvester Application shutdown completed.");
	}

}