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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.util.Collections;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.cli.hsqldb.Utils;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa.JPAApplicationRegistry;
import org.smartdeveloperhub.util.bootstrap.AbstractBootstrap;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;

public class BackendPopulator extends AbstractBootstrap<BackendConfig> {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendPopulator.class);

 	static final String NAME = "BackendPopulator";;

	private BackendPopulatorService service;

	private EntityManagerFactory factory;

	private BackendConfig config;

	public BackendPopulator() {
		super(NAME,BackendConfig.class);
	}

	public static void main(String[] args) throws Exception {
		BackendPopulator bs = new BackendPopulator();
		bs.run(args);
		bs.terminate();
	}

	@Override
	protected void shutdown() {
		LOGGER.info("Shutting down application...");
		try {
			this.factory.close();
			String fqDatabaseFile=this.config.targetDatabase();
			if(this.config.pack()) {
				fqDatabaseFile=Packer.pack(fqDatabaseFile,Utils.dbResources(fqDatabaseFile)).toString();
			}
			Consoles.defaultConsole().printf("Retrieved data available at %s.",fqDatabaseFile);
		} catch (Exception e) {
			Consoles.defaultConsole().printf("Failed to shutdown the populator");
			LOGGER.error("Failed to shutdown the application. Full stacktrace follows",e);
		}
	}

	@Override
	protected Iterable<Service> getServices(BackendConfig config) {
		this.factory = Persistence.createEntityManagerFactory("populator",configure(config));
		JPAApplicationRegistry applicationRegistry = new JPAApplicationRegistry(factory);
		this.service=new BackendPopulatorService(config,applicationRegistry);
		return Collections.<Service>singleton(this.service);
	}

	private ImmutableMap<String, String> configure(BackendConfig config) {
		this.config = config;
		String fqDatabaseFile = this.config.targetDatabase();
		String connectionURL=
			Utils.
				urlBuilder().
					persistent(fqDatabaseFile).
					build();
		Consoles.
			defaultConsole().
				printf("Using '%s' as working directory%n",config.getWorkingDirectory()).
				printf("Persisting retrieved data in '%s'%n",fqDatabaseFile);
		LOGGER.debug("Connecting to DB: {}%n",connectionURL);
		return
			ImmutableMap.
				<String,String>builder().
					put(JPAProperties.JDBC_URL, connectionURL).
					build();
	}

}