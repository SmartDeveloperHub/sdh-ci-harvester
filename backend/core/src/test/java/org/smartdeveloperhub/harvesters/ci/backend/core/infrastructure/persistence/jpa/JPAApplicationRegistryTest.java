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
package org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa;

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
import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa.JPAApplicationRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionManager;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;

import com.google.common.collect.ImmutableMap;

public class JPAApplicationRegistryTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(JPAApplicationRegistryTest.class);

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

	private static final String SUB_BUILD_TITLE = "SUB-BUILD TITLE";
	private static final String BUILD_TITLE     = "BUILD TITLE";


	private static File create;
	private static File drop;

	private static EntityManagerFactory factory;
	private static JPAApplicationRegistry persistencyFacade;

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
		persistencyFacade = new JPAApplicationRegistry(factory);
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
	public void testSimpleBuildManagement() throws Exception {
		LOGGER.debug("Started Simple Build management");

		URI serviceId = URI.create("http://localhost/jenkins/");
		URI buildId = serviceId.resolve("simpleBuild/");
		URI executionId =buildId.resolve("exec1/");
		Date createdOn = new Date();
		Date finishedOn = new Date();
		Result inResult = new Result(Status.PASSED,finishedOn);
		Execution inExecution=null;
		Execution outExecution=null;
		SimpleBuild inBuild=null;
		SimpleBuild outBuild=null;

		ExecutionRepository executionRepository=persistencyFacade.getExecutionRepository();
		BuildRepository buildRepository=persistencyFacade.getBuildRepository();
		TransactionManager transactionManager = persistencyFacade.getTransactionManager();

		Transaction tx1 = transactionManager.currentTransaction();
		tx1.begin();
		try {
			Service service=new Service(serviceId);
			inBuild = service.addSimpleBuild(buildId,BUILD_TITLE);
			inExecution = inBuild.addExecution(executionId, createdOn);
			buildRepository.add(inBuild);
			executionRepository.add(inExecution);
			tx1.commit();
		} catch(Exception e) {
			tx1.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		Transaction tx2 = transactionManager.currentTransaction();
		tx2.begin();
		try {
			Execution execution = executionRepository.executionOfId(executionId);
			execution.finish(inResult);
			tx2.commit();
		} catch(Exception e) {
			tx2.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			outBuild = buildRepository.buildOfId(buildId,SimpleBuild.class);
			outExecution = executionRepository.executionOfId(executionId);
			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

		assertThat(outBuild,notNullValue());
		assertThat(outBuild,not(sameInstance(inBuild)));
		assertThat(outBuild.buildId(),equalTo(inBuild.buildId()));
		assertThat(outBuild.serviceId(),equalTo(inBuild.serviceId()));
		assertThat(outBuild.codebase(),equalTo(inBuild.codebase()));
		assertThat(outBuild.location(),equalTo(inBuild.location()));
		assertThat(outBuild.executions(),hasItems(inBuild.executions().toArray(new URI[0])));

		assertThat(outExecution,notNullValue());
		assertThat(outExecution,not(sameInstance(inExecution)));
		assertThat(outExecution,equalTo(inExecution));
		assertThat(outExecution.executionId(),equalTo(inExecution.executionId()));
		assertThat(outExecution.buildId(),equalTo(inExecution.buildId()));
		assertThat(outExecution.createdOn(),equalTo(inExecution.createdOn()));
		assertThat(outExecution.result(),not(sameInstance(inResult)));
		assertThat(outExecution.result(),equalTo(inResult));

		LOGGER.debug("Completed Simple Build management");
	}

	@Test
	public void testCompositeBuildManagement() throws Exception {
		LOGGER.debug("Started Composite Build management");

		URI serviceId = URI.create("http://localhost/jenkins/");
		URI buildId = serviceId.resolve("compositeBuild/");
		URI executionId =buildId.resolve("exec1/");
		Date createdOn = new Date();
		Date finishedOn = new Date();
		Result inResult = new Result(Status.PASSED,finishedOn);
		Execution inExecution=null;
		Execution outExecution=null;
		CompositeBuild inBuild=null;
		CompositeBuild outBuild=null;

		ExecutionRepository executionRepository=persistencyFacade.getExecutionRepository();
		BuildRepository buildRepository=persistencyFacade.getBuildRepository();
		TransactionManager transactionManager = persistencyFacade.getTransactionManager();

		Transaction tx1 = transactionManager.currentTransaction();
		tx1.begin();
		try {
			Service service=new Service(serviceId);
			inBuild = service.addCompositeBuild(buildId,BUILD_TITLE);
			inExecution = inBuild.addExecution(executionId, createdOn);
			buildRepository.add(inBuild);
			executionRepository.add(inExecution);
			tx1.commit();
		} catch(Exception e) {
			tx1.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		Transaction tx2 = transactionManager.currentTransaction();
		tx2.begin();
		try {
			Execution execution = executionRepository.executionOfId(executionId);
			execution.finish(inResult);
			tx2.commit();
		} catch(Exception e) {
			tx2.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			outBuild = buildRepository.buildOfId(buildId,CompositeBuild.class);
			outExecution = executionRepository.executionOfId(executionId);
			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

		assertThat(outBuild,notNullValue());
		assertThat(outBuild,not(sameInstance(inBuild)));
		assertThat(outBuild,equalTo(inBuild));
		assertThat(outBuild.buildId(),equalTo(inBuild.buildId()));
		assertThat(outBuild.serviceId(),equalTo(inBuild.serviceId()));
		assertThat(outBuild.codebase(),equalTo(inBuild.codebase()));
		assertThat(outBuild.location(),equalTo(inBuild.location()));
		assertThat(outBuild.executions(),hasItems(inBuild.executions().toArray(new URI[0])));
		assertThat(outBuild.subBuilds(),hasItems(inBuild.subBuilds().toArray(new URI[0])));

		assertThat(outExecution,notNullValue());
		assertThat(outExecution,not(sameInstance(inExecution)));
		assertThat(outExecution,equalTo(inExecution));
		assertThat(outExecution.executionId(),equalTo(inExecution.executionId()));
		assertThat(outExecution.buildId(),equalTo(inExecution.buildId()));
		assertThat(outExecution.createdOn(),equalTo(inExecution.createdOn()));
		assertThat(outExecution.result(),not(sameInstance(inResult)));
		assertThat(outExecution.result(),equalTo(inResult));

		LOGGER.debug("Completed Composite Build management");
	}

	@Test
	public void testSubBuildManagement() throws Exception {
		LOGGER.debug("Started Sub Build management");

		URI serviceId = URI.create("http://localhost/jenkins/");
		URI buildId = serviceId.resolve("parentBuild/");
		URI subBuildId = buildId.resolve("subBuild/");
		URI executionId =subBuildId.resolve("exec1/");
		Date createdOn = new Date();
		Date finishedOn = new Date();
		Result inResult = new Result(Status.PASSED,finishedOn);
		Execution inExecution=null;
		Execution outExecution=null;
		SubBuild inBuild=null;
		SubBuild outBuild=null;

		ExecutionRepository executionRepository=persistencyFacade.getExecutionRepository();
		BuildRepository buildRepository=persistencyFacade.getBuildRepository();
		TransactionManager transactionManager = persistencyFacade.getTransactionManager();

		Transaction tx1 = transactionManager.currentTransaction();
		tx1.begin();
		try {
			Service service=new Service(serviceId);
			CompositeBuild compositeBuild = service.addCompositeBuild(buildId,BUILD_TITLE);
			inBuild=compositeBuild.addSubBuild(subBuildId,SUB_BUILD_TITLE);
			inExecution = inBuild.addExecution(executionId, createdOn);
			buildRepository.add(compositeBuild);
			buildRepository.add(inBuild);
			executionRepository.add(inExecution);
			tx1.commit();
		} catch(Exception e) {
			tx1.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		Transaction tx2 = transactionManager.currentTransaction();
		tx2.begin();
		try {
			Execution execution = executionRepository.executionOfId(executionId);
			execution.finish(inResult);
			tx2.commit();
		} catch(Exception e) {
			tx2.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.disposeManagers();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			outBuild = buildRepository.buildOfId(subBuildId,SubBuild.class);
			outExecution = executionRepository.executionOfId(executionId);
			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

		assertThat(outBuild,notNullValue());
		assertThat(outBuild,not(sameInstance(inBuild)));
		assertThat(outBuild,equalTo(inBuild));
		assertThat(outBuild.buildId(),equalTo(inBuild.buildId()));
		assertThat(outBuild.serviceId(),equalTo(inBuild.serviceId()));
		assertThat(outBuild.codebase(),equalTo(inBuild.codebase()));
		assertThat(outBuild.location(),equalTo(inBuild.location()));
		assertThat(outBuild.executions(),hasItems(inBuild.executions().toArray(new URI[0])));
		assertThat(outBuild.parentId(),equalTo(inBuild.parentId()));

		assertThat(outExecution,notNullValue());
		assertThat(outExecution,not(sameInstance(inExecution)));
		assertThat(outExecution,equalTo(inExecution));
		assertThat(outExecution.executionId(),equalTo(inExecution.executionId()));
		assertThat(outExecution.buildId(),equalTo(inExecution.buildId()));
		assertThat(outExecution.createdOn(),equalTo(inExecution.createdOn()));
		assertThat(outExecution.result(),not(sameInstance(inResult)));
		assertThat(outExecution.result(),equalTo(inResult));

		LOGGER.debug("Completed Sub Build management");
	}

}
