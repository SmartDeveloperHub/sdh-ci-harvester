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

import org.smartdeveloperhub.harvesters.ci.backend.cli.hsqldb.Utils;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa.JPAApplicationRegistry;
import org.smartdeveloperhub.util.bootstrap.AbstractBootstrap;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Service;

public class BackendPopulator extends AbstractBootstrap<BackendConfig> {

	static final String NAME = "BackendPopulator";;

	private BackendPopulatorService service;

	private EntityManagerFactory factory;

	public BackendPopulator() {
		super(NAME,BackendConfig.class);
	}

	public static void main(String[] args) throws Exception {
		BackendPopulator bs = new BackendPopulator();
		bs.run(args);
		Consoles.defaultConsole().printf("<<HIT ENTER TO STOP THE INTEGRATION SERVICE>>%n");
		Consoles.defaultConsole().readLine();
		bs.terminate();
		Consoles.defaultConsole().printf("<<POPULATOR ENDED>>%n");
	}

	@Override
	protected void shutdown() {
		Consoles.defaultConsole().printf("Shutting down populator...%n");
		try {
			this.factory.close();
		} catch (Exception e) {
			Consoles.defaultConsole().printf("Failed to shutdown the populator. Full stacktrace follows");
			e.printStackTrace(Consoles.defaultConsole().writer());
		}
	}

	@Override
	protected Iterable<Service> getServices(BackendConfig config) {
		String fqDatabaseFile = config.getWorkingDirectory()+config.getDatabase();
		String connectionURL=
			Utils.
				urlBuilder().
					persistent(fqDatabaseFile).
					build();
		Consoles.
		defaultConsole().
			printf("Using '%s' as working directory%n",config.getWorkingDirectory()).
			printf("Persisting retrieved data in '%s'%n",fqDatabaseFile).
			printf("Connecting to DB: %s%n",connectionURL);
		ImmutableMap<String, String> properties =
			ImmutableMap.
				<String,String>builder().
					put(JPAProperties.JDBC_URL, connectionURL).
					build();
		this.factory = Persistence.createEntityManagerFactory("populator",properties);
		JPAApplicationRegistry applicationRegistry = new JPAApplicationRegistry(factory);
		this.service=new BackendPopulatorService(config,applicationRegistry);
		return Collections.<Service>singleton(this.service);
	}

}