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
package org.smartdeveloperhub.harvesters.ci.backend.persistence.hsqldb;

import java.io.File;
import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.Database;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.DatabaseLifecycleException;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.DatabaseConfig.Deployment;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.DatabaseConfig.Mode;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.spi.DatabaseProvider;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

public class HSQLDBDatabaseProvider implements DatabaseProvider {

	private static final Logger LOGGER=LoggerFactory.getLogger(HSQLDBDatabaseProvider.class);

	@Override
	public Database create(final DatabaseConfig config) {
		return new Database() {

			private EntityManagerFactory emf;
			private String unpackedLocation;

			@Override
			public EntityManagerFactory getEntityManagerFactory() {
				String persistenceUnit="dropCreate";
				if(Mode.MUST_EXIST.equals(config.getMode())) {
					persistenceUnit="mustExist";
				}
				this.emf =
					Persistence.
						createEntityManagerFactory(
							persistenceUnit,
							configure(config));
				return this.emf;
			}

			@Override
			public void close() {
				this.emf.close();
				if(Deployment.PACKED.equals(config.getDeployment())) {
					try {
						Packer.pack(config.getLocation(),Utils.dbResources(this.unpackedLocation));
					} catch (IOException e) {
						throw new DatabaseLifecycleException("Could not pack database",e);
					}
				}
			}

			private ImmutableMap<String, String> configure(DatabaseConfig config) {
				String connectionURL=null;
				String location = config.getLocation();
				switch(config.getDeployment()) {
					case LOCAL:
						connectionURL=
							Utils.
								urlBuilder().
									persistent(location).
									build();
						break;
					case PACKED:
						this.unpackedLocation = unpack(location);
						connectionURL=
							Utils.
								urlBuilder().
									persistent(this.unpackedLocation).
									build();
						break;
					case REMOTE:
						connectionURL=
							Utils.
								urlBuilder().
									remote(location).
									build();
						break;
					default:
						break;
				}
				LOGGER.debug("Connecting to DB: {}%n",connectionURL);
				return
					ImmutableMap.
						<String,String>builder().
							put(JPAProperties.JDBC_URL, connectionURL).
							build();
			}

			private String unpack(String sourceFile) {
				try {
					File source=new File(sourceFile);
					if(!source.exists()) {
						source.getParentFile().mkdirs();
					}
					File targetDirectory=Files.createTempDir();
					File dbFile=Packer.unpack(sourceFile, targetDirectory.getAbsolutePath());
					LOGGER.debug("Unpacked data to {}...",dbFile);
					return dbFile.toURI().getSchemeSpecificPart().substring(1);
				} catch (IOException e) {
					LOGGER.error("Could not unpack database. Full stacktrace follows",e);
					throw new DatabaseLifecycleException("Could not unpack database",e);
				}
			}

		};
	}

}
