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
import java.net.URI;
import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa.JPAApplicationRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.core.port.jenkins.JenkinsIntegrationService;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.collect.ImmutableMap;

public final class ContinuousIntegrationExec {

	private static final String JDBC_DRIVER   = "javax.persistence.jdbc.driver";
	private static final String JDBC_URL      = "javax.persistence.jdbc.url";
	private static final String JDBC_USER     = "javax.persistence.jdbc.user";
	private static final String JDBC_PASSWORD = "javax.persistence.jdbc.password"; // NOSONAR

	private static final String SCHEMA_GENERATION_DROP_TARGET    = "javax.persistence.schema-generation.scripts.drop-target";
	private static final String SCHEMA_GENERATION_CREATE_TARGET  = "javax.persistence.schema-generation.scripts.create-target";
	private static final String SCHEMA_GENERATION_SCRIPTS_ACTION = "javax.persistence.schema-generation.scripts.action";

	public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String HSQLDB_URL = "jdbc:hsqldb:mem:dbunit";
	public static final String HSQLDB_USER = "sa";
	public static final String HSQLDB_PASSWORD = ""; // NOSONAR


	private static File create;
	private static File drop;

	private static EntityManagerFactory factory;
	private static JPAApplicationRegistry persistencyFacade;

	private ContinuousIntegrationExec() {
	}

	private static void startUp() throws IOException {
		create = File.createTempFile("create",".ddl");
		drop = File.createTempFile("drop",".ddl");
		ImmutableMap<String, String> properties =
			ImmutableMap.
				<String,String>builder().
					put(JDBC_DRIVER, HSQLDB_DRIVER).
					put(JDBC_URL, HSQLDB_URL).
					put(JDBC_USER, HSQLDB_USER).
					put(JDBC_PASSWORD, HSQLDB_PASSWORD).
					put(SCHEMA_GENERATION_SCRIPTS_ACTION, "drop-and-create").
					put(SCHEMA_GENERATION_CREATE_TARGET, create.getAbsolutePath()).
					put(SCHEMA_GENERATION_DROP_TARGET, drop.getAbsolutePath()).
					build();
		factory = Persistence.createEntityManagerFactory("jpaPersistency",properties);
		persistencyFacade = new JPAApplicationRegistry(factory);
	}

	private static void shutDown() {
		if(factory!=null) {
			factory.close();
		}
		create.delete();
		drop.delete();
	}

	public static void main(String... args) throws IOException {
		startUp();
		File tmpDirectory = new File("target","jenkins"+new Date().getTime());
		tmpDirectory.deleteOnExit();
		try {
			ContinuousIntegrationService cis =
				new ContinuousIntegrationService(
					persistencyFacade.getServiceRepository(),
					persistencyFacade.getBuildRepository(),
					persistencyFacade.getExecutionRepository());
			JenkinsIntegrationService jis=
				new JenkinsIntegrationService(
					cis,
					persistencyFacade.getTransactionManager()).
					setWorkingDirectory(tmpDirectory);
			jis.connect(URI.create("http://ci.jenkins-ci.org/"));
			Consoles.defaultConsole().printf("<<HIT ENTER TO STOP THE INTEGRATION SERVICE>>%n");
			Consoles.defaultConsole().readLine();
			jis.disconnect();
		} finally {
			shutDown();
		}
	}

}
