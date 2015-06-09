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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.net.URI;
import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.Service;

import com.google.common.collect.ImmutableMap;

public class PersistencyFacadeTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(PersistencyFacadeTest.class);

	private static final String JDBC_DRIVER   = "javax.persistence.jdbc.driver";
	private static final String JDBC_URL      = "javax.persistence.jdbc.url";
	private static final String JDBC_USER     = "javax.persistence.jdbc.user";
	private static final String JDBC_PASSWORD = "javax.persistence.jdbc.password";

	private static final String SCHEMA_GENERATION_DROP_TARGET    = "javax.persistence.schema-generation.scripts.drop-target";
	private static final String SCHEMA_GENERATION_CREATE_TARGET  = "javax.persistence.schema-generation.scripts.create-target";
	private static final String SCHEMA_GENERATION_SCRIPTS_ACTION = "javax.persistence.schema-generation.scripts.action";

	public static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
	public static final String HSQLDB_URL = "jdbc:hsqldb:mem:dbunit";
	public static final String HSQLDB_USER = "sa";
	public static final String HSQLDB_PASSWORD = "";

	private static File create;
	private static File drop;

	private static EntityManagerFactory factory;
	private static PersistencyFacade persistencyFacade;

	@BeforeClass
	public static void startUp() throws Exception {
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
		persistencyFacade = new PersistencyFacade(factory);
	}

	@AfterClass
	public static void shutDown() throws Exception {
		if(factory!=null) {
			factory.close();
		}
		create.delete();
		drop.delete();
	}

	@Test
	public void testConcept() throws Exception {
		LOGGER.debug("Creating example schema...");
		URI serviceId = URI.create("http://localhost/jenkins/");
		URI buildId = serviceId.resolve("build1/");
		URI executionId =buildId.resolve("exec1/");
		Date createdOn = new Date();
		Date finishedOn = new Date();
		Result inResult = new Result(Status.PASSED,finishedOn);
		Execution inExecution=null;
		Execution outExecution=null;
		Build inBuild=null;
		Build outBuild=null;

		ExecutionRepository executionRepository=persistencyFacade.getExecutionRepository();
		BuildRepository buildRepository=persistencyFacade.getBuildRepository();
		persistencyFacade.beginTransaction();
		try {
			Service service=new Service(serviceId);
			inBuild = service.addSimpleBuild(buildId);
			inExecution = inBuild.addExecution(executionId, createdOn);
			buildRepository.add(inBuild);
			executionRepository.add(inExecution);
			persistencyFacade.commitTransaction();
		} catch(Exception e) {
			persistencyFacade.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		persistencyFacade.beginTransaction();
		try {
			Execution execution = executionRepository.executionOfId(executionId);
			execution.finish(inResult);
			persistencyFacade.commitTransaction();
		} catch(Exception e) {
			persistencyFacade.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		persistencyFacade.beginTransaction();
		try {
			outBuild = buildRepository.buildOfId(buildId);
			outExecution = executionRepository.executionOfId(executionId);
			persistencyFacade.commitTransaction();
		} catch(Exception e) {
			persistencyFacade.rollbackTransaction();
			e.printStackTrace();
			throw e;
		}

		assertThat(outBuild,notNullValue());
		assertThat(outBuild,not(sameInstance(inBuild)));
		assertThat(outBuild,equalTo(inBuild));
		assertThat(outBuild.buildId(),equalTo(buildId));
		assertThat(outBuild.serviceId(),equalTo(inBuild.serviceId()));
		assertThat(outBuild.codebase(),equalTo(inBuild.codebase()));
		assertThat(outBuild.location(),equalTo(inBuild.location()));
		assertThat(outBuild.executions(),hasItems(inBuild.executions().toArray(new URI[0])));

		assertThat(outExecution,notNullValue());
		assertThat(outExecution,not(sameInstance(inExecution)));
		assertThat(outExecution,equalTo(inExecution));
		assertThat(outExecution.executionId(),equalTo(executionId));
		assertThat(outExecution.buildId(),equalTo(buildId));
		assertThat(outExecution.createdOn(),equalTo(createdOn));
		assertThat(outExecution.result(),not(sameInstance(inResult)));
		assertThat(outExecution.result(),equalTo(inResult));

	}

}
