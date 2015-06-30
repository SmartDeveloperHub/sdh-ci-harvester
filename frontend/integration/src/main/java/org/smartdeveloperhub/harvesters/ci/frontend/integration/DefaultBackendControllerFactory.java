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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-integration:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-integration-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.integration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.BackendFacade;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendControllerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class DefaultBackendControllerFactory implements BackendControllerFactory {

	private static final String CI_HARVESTER_DATABASE_CONFIG_PATH = "ci.harvester.config";

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultBackendControllerFactory.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BackendController create(URI jenkinsInstance) {
		String property = System.getProperty(CI_HARVESTER_DATABASE_CONFIG_PATH);
		try {
			DatabaseConfig config=loadConfiguration(property);
			BackendFacade backend = BackendFacade.create(config);
			return
				hasInstance(jenkinsInstance, backend)?
					new DefaultBackendController(jenkinsInstance, backend):
					null;
		} catch (Exception e) {
			LOGGER.error("Could not initialize backend",e);
			return null;
		}
	}

	private boolean hasInstance(URI jenkinsInstance, BackendFacade backend) {
		List<URI> availableServices=
			backend.
				applicationService().
					getRegisteredServices();
		boolean contains = availableServices.contains(jenkinsInstance);
		if(!contains) {
			LOGGER.warn("Could not find instance "+jenkinsInstance+" ("+availableServices+")");
		}
		return contains;
	}

	private static DatabaseConfig loadConfiguration(String pathname) throws IOException {
		try {
			LOGGER.info("Loading configuration from {}...",pathname);
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			DatabaseConfig configuration = mapper.readValue(new File(pathname), DatabaseConfig.class);
			LOGGER.info("Configuration loaded: {}",configuration);
			return configuration;
		} catch (Exception e) {
			String errorMessage = String.format("Could not load configuration from %s",pathname);
			LOGGER.warn(errorMessage+". Full stacktrace follows: ",e);
			throw new IOException(errorMessage,e);
		}
	}

}
