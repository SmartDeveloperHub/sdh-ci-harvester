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

import java.io.File;
import java.io.IOException;
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
import com.google.common.io.Files;
import com.google.common.util.concurrent.Service;

public class BackendDump extends AbstractBootstrap<BackendConfig> {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendDump.class);

	static final String NAME = "BackendDump";;

	private EntityManagerFactory factory;

	public BackendDump() {
		super(NAME,BackendConfig.class);
	}

	public static void main(String[] args) throws Exception {
		BackendDump bs = new BackendDump();
		bs.run(args);
		bs.terminate();
	}

	@Override
	protected void shutdown() {
		LOGGER.info("Shutting down application...");
		try {
			this.factory.close();
		} catch (Exception e) {
			LOGGER.warn("Failed to shutdown the application. Full stacktrace follows",e);
		}
	}

	@Override
	protected Iterable<Service> getServices(BackendConfig config) {
		this.factory = Persistence.createEntityManagerFactory("dumper",configure(config));
		JPAApplicationRegistry applicationRegistry = new JPAApplicationRegistry(factory);
		return Collections.<Service>singleton(new BackendDumpService(applicationRegistry));
	}

	private ImmutableMap<String, String> configure(BackendConfig config) {
		String specifiedDatabaseFile = config.targetDatabase();
		Consoles.
			defaultConsole().
				printf("Dumping data stored in '%s'%n",specifiedDatabaseFile);
		String usedDatabaseFile=specifiedDatabaseFile;
		if(config.pack()) {
			usedDatabaseFile=unpack(specifiedDatabaseFile);
		}
		String connectionURL=
			Utils.
				urlBuilder().
					mustExist().
					persistent(usedDatabaseFile).
					build();
		LOGGER.debug("Connecting to DB: {}%n",connectionURL);
		return
			ImmutableMap.
				<String,String>builder().
					put(JPAProperties.JDBC_URL, connectionURL).
					build();
	}

	private String unpack(String sourceFile) {
		try {
			File targetDirectory=Files.createTempDir();
			File dbFile=Packer.unpack(sourceFile, targetDirectory.getAbsolutePath());
			LOGGER.debug("Unpacked data to {}...",dbFile);
			return dbFile.toURI().getSchemeSpecificPart().substring(1);
		} catch (IOException e) {
			LOGGER.error("Could not unpack database. Full stacktrace follows",e);
			throw new RuntimeException(e);
		}
	}

}