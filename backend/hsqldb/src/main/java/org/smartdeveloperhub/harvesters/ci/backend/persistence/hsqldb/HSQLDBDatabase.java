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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-hsqldb:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-hsqldb-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.hsqldb;

import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.database.Database;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseLifecycleException;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig.Deployment;
import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig.Mode;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.hsqldb.Utils.URLBuilder;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

final class HSQLDBDatabase implements Database {

	private static final Logger LOGGER=LoggerFactory.getLogger(HSQLDBDatabase.class);

	private final DatabaseConfig config;

	private EntityManagerFactory emf;
	private File unpackedLocation;

	HSQLDBDatabase(DatabaseConfig config) {
		this.config = config;
	}

	@Override
	public EntityManagerFactory getEntityManagerFactory() {
		String persistenceUnit="dropCreate";
		if(Mode.MUST_EXIST.equals(this.config.getMode())) {
			persistenceUnit="mustExist";
		}
		this.emf =
			Persistence.
				createEntityManagerFactory(
					persistenceUnit,
					configure(this.config));
		return this.emf;
	}

	@Override
	public void close() {
		this.emf.close();
		unloadDriver();
		postProcess();
	}

	private void postProcess() {
		if(Deployment.PACKED.equals(this.config.getDeployment())) {
			try {
				File packedLocation = Packer.pack(this.config.getLocation(),Utils.dbResources(fileConnectionPath(this.unpackedLocation)));
				LOGGER.debug("Packed data from {} to {}.",this.unpackedLocation.getAbsolutePath(),packedLocation.getAbsolutePath());
			} catch (IOException e) {
				LOGGER.error("Could not pack data. Full stacktrace follows",e);
				throw new DatabaseLifecycleException("Could not pack database",e);
			}
		}
	}

	private void unloadDriver() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			processDriver(cl, drivers.nextElement());
		}
	}

	private void processDriver(ClassLoader cl, Driver driver) {
		if(driver instanceof org.hsqldb.jdbc.JDBCDriver) {
			processTargetDriver(cl, driver);
		} else {
			LOGGER.trace("Not deregistering JDBC driver {} as it not the target one ({})",driver,org.hsqldb.jdbc.JDBCDriver.class.getName());
		}
	}

	private void processTargetDriver(ClassLoader cl, Driver driver) {
		if(driver.getClass().getClassLoader()==cl) {
			try {
				LOGGER.info("Deregistering JDBC driver {}", driver);
				DriverManager.deregisterDriver(driver);
			} catch (SQLException ex) {
				LOGGER.error("Error deregistering JDBC driver {}", driver,ex);
			}
		} else {
			LOGGER.trace("Not deregistering JDBC driver {} as it does not belong to this application ClassLoader",driver);
		}
	}

	private String fileConnectionPath(File file) {
		return file.toURI().getSchemeSpecificPart();
	}

	private ImmutableMap<String, String> configure(DatabaseConfig config) {
		String location=config.getLocation();
		URLBuilder builder=Utils.urlBuilder();
		switch(config.getDeployment()) {
			case LOCAL:
				builder.persistent(location);
				break;
			case PACKED:
				this.unpackedLocation = unpack(location);
				builder.persistent(fileConnectionPath(this.unpackedLocation));
				break;
			case REMOTE:
				builder.remote(location);
				break;
			default:
				break;
		}
		String connectionURL=builder.build();
		LOGGER.debug("Connecting to DB: {}",connectionURL);
		return
			ImmutableMap.
				<String,String>builder().
					put(JPAProperties.JDBC_URL, connectionURL).
					build();
	}

	private File unpack(String sourceFile) {
		try {
			File source=new File(sourceFile);
			if(!source.exists()) {
				source.getParentFile().mkdirs();
			}
			File targetDirectory=Files.createTempDir();
			File dbFile=Packer.unpack(sourceFile, targetDirectory.getAbsolutePath());
			LOGGER.debug("Unpacked data from {} to {}.",source.getAbsolutePath(),dbFile.getAbsolutePath());
			return dbFile;
		} catch (IOException e) {
			LOGGER.error("Could not unpack data. Full stacktrace follows",e);
			throw new DatabaseLifecycleException("Could not unpack database",e);
		}
	}
}