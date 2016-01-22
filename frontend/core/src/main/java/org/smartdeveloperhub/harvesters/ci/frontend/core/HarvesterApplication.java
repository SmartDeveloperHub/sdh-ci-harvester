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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.io.File;
import java.net.URI;

import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.ext.Application;
import org.ldp4j.application.ext.ApplicationInitializationException;
import org.ldp4j.application.ext.ApplicationSetupException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.setup.Bootstrap;
import org.ldp4j.application.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendConfig;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.BrokerConfig;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentConfig;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.SubBuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class HarvesterApplication extends Application<HarvesterConfiguration> {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterApplication.class);

	private static final String SERVICE_PATH="service/";

	private HarvesterConfiguration configuration;

	private BackendController controller;

	@Override
	public void setup(final Environment environment, final Bootstrap<HarvesterConfiguration> bootstrap) throws ApplicationSetupException {
		LOGGER.info("Starting CI Harvester Application configuration...");

		this.configuration = bootstrap.configuration();
		final BackendConfig backendConfig=toBackendConfig(this.configuration);

		logConfiguration(backendConfig);

		final DefaultResolverService resolver=
			new DefaultResolverService(
				URI.create(this.configuration.canonicalBase()));

		this.controller=BackendControllerManager.create(this.configuration.provider(),backendConfig);
		this.controller.setExecutionResolver(resolver);

		environment.lifecycle().addApplicationLifecycleListener(resolver);

		bootstrap.addHandler(new ServiceHandler(this.controller));
		bootstrap.addHandler(new BuildContainerHandler(this.controller));
		bootstrap.addHandler(new SubBuildContainerHandler(this.controller));
		bootstrap.addHandler(new BuildHandler(this.controller));
		bootstrap.addHandler(new ExecutionContainerHandler(this.controller));
		bootstrap.addHandler(new ExecutionHandler(this.controller));

		environment.
			publishResource(
				NamingScheme.
					getDefault().
						name(this.configuration.target()),
				ServiceHandler.class,
				SERVICE_PATH);

		LOGGER.info("Contacts CI Harvester Application configuration completed.");
	}

	@Override
	public void initialize(final WriteSession session) throws ApplicationInitializationException {
		LOGGER.info("Initializing CI Harvester Application...");
		if(!this.controller.setTargetService(this.configuration.target())) {
			final String errorMessage = "CI Harvester Application initialization failed: cannot create target service "+this.configuration.target();
			LOGGER.error(errorMessage);
			throw new ApplicationInitializationException(errorMessage);
		}
		try {
			final EntityIndex index=this.controller.entityIndex();
			final BackendModelPublisher publisher=
				BackendModelPublisher.
					builder().
						withBackendService(index).
						withMainService(this.configuration.target()).
						build();
			publisher.publish(session);
			session.saveChanges();
			this.controller.connect(new FrontendSynchronizer(index,publisher));
			LOGGER.info("CI Harvester Application initialization completed.");
		} catch (final Exception e) {
			final String errorMessage = "CI Harvester Application initialization failed";
			LOGGER.error(errorMessage+". Full stacktrace follows: ",e);
			throw new ApplicationInitializationException(e);
		}
	}

	@Override
	public void shutdown() {
		LOGGER.info("Starting CI Harvester Application shutdown...");
		this.controller.disconnect();
		LOGGER.info("CI Harvester Application shutdown completed.");
	}

	private void logConfiguration(final BackendConfig backendConfig) {
		LOGGER.info("- Target..................: {}",this.configuration.target());
		LOGGER.info("- Provider................: {}",this.configuration.provider());
		LOGGER.info("- Database configuration..:");
		LOGGER.info("  + Deployment............: {}",backendConfig.getDatabase().getDeployment());
		LOGGER.info("  + Location..............: {}",backendConfig.getDatabase().getLocation());
		LOGGER.info("  + Mode..................: {}",backendConfig.getDatabase().getMode());
		LOGGER.info("- Enrichment configuration:");
		LOGGER.info("  + Canonical base........: {}",this.configuration.canonicalBase());
		LOGGER.info("  + Messaging broker......:");
		LOGGER.info("    + Host................: {}",this.configuration.brokerHost());
		LOGGER.info("    + Port................: {}",this.configuration.brokerPort());
	}

	static BackendConfig toBackendConfig(final HarvesterConfiguration config) throws ApplicationSetupException {
		final BackendConfig cfg=new BackendConfig();
		cfg.setDatabase(loadDatabaseConfig(config));
		cfg.setEnrichment(createEnrichmentConfig(config));
		return cfg;
	}

	private static EnrichmentConfig createEnrichmentConfig(final HarvesterConfiguration config) {
		final BrokerConfig brokerConfig = new BrokerConfig();
		brokerConfig.setHost(config.brokerHost());
		brokerConfig.setPort(config.brokerPort());
		final EnrichmentConfig enrichmentConfig = new EnrichmentConfig();
		enrichmentConfig.setBroker(brokerConfig);
		enrichmentConfig.setBase(config.canonicalBase());
		return enrichmentConfig;
	}

	private static DatabaseConfig loadDatabaseConfig(final HarvesterConfiguration config) throws ApplicationSetupException {
		final String pathname=config.databaseConfigPath();
		try {
			LOGGER.info("Loading database configuration from {}...",pathname);
			final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			final DatabaseConfig configuration = mapper.readValue(new File(pathname), DatabaseConfig.class);
			LOGGER.info("Database configuration loaded: {}",configuration);
			return configuration;
		} catch (final Exception e) {
			final String errorMessage = String.format("Could not load database configuration from %s",pathname);
			LOGGER.warn(errorMessage+". Full stacktrace follows: ",e);
			throw new ApplicationSetupException(errorMessage,e);
		}
	}

}