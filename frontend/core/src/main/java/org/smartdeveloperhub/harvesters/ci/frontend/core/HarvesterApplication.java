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

import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.SubBuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;

public final class HarvesterApplication extends Application<HarvesterConfiguration> {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterApplication.class);

	private static final URI SERVICE_ID=URI.create("http://ci.travis-ci.org/");

	private static final String SERVICE_PATH="service/";

	private ContinuousIntegrationService backendService;

	private BackendController controller;

	@Override
	public void setup(Environment environment, Bootstrap<HarvesterConfiguration> bootstrap) {
		LOGGER.info("Starting CI Harvester Application configuration...");

		this.controller=BackendControllerManager.connect(SERVICE_ID);

		this.backendService=this.controller.continuousIntegrationService();

		bootstrap.addHandler(new ServiceHandler(this.backendService));
		bootstrap.addHandler(new BuildContainerHandler(this.backendService));
		bootstrap.addHandler(new SubBuildContainerHandler(this.backendService));
		bootstrap.addHandler(new BuildHandler(this.backendService));
		bootstrap.addHandler(new ExecutionContainerHandler(this.backendService));
		bootstrap.addHandler(new ExecutionHandler(this.backendService));

		environment.
			publishResource(
				NamingScheme.
					getDefault().
						name(HarvesterApplication.SERVICE_ID),
				ServiceHandler.class,
				SERVICE_PATH);

		LOGGER.info("Contacts CI Harvester Application configuration completed.");
	}

	@Override
	public void initialize(WriteSession session) throws ApplicationInitializationException {
		LOGGER.info("Initializing CI Harvester Application...");
		try {
			BackendModelPublisher publisher=
				BackendModelPublisher.
					builder().
						withBackendService(this.backendService).
						withMainService(SERVICE_ID).
						build();
			publisher.publish(session);
			session.saveChanges();
			LOGGER.info("CI Harvester Application initialization completed.");
		} catch (Exception e) {
			String errorMessage = "CI Harvester Application initialization failed";
			LOGGER.warn(errorMessage+". Full stacktrace follows: ",e);
			throw new ApplicationInitializationException(e);
		}
	}

	@Override
	public void shutdown() {
		LOGGER.info("Starting CI Harvester Application shutdown...");
		this.controller.disconnect();
		LOGGER.info("CI Harvester Application shutdown completed.");
	}

}