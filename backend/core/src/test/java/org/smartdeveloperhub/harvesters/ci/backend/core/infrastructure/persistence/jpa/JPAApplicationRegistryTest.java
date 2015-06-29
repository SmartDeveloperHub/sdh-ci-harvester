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
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Date;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.core.lifecycle.EntityId;
import org.smartdeveloperhub.harvesters.ci.backend.core.lifecycle.EntityId.Type;
import org.smartdeveloperhub.harvesters.ci.backend.core.lifecycle.LifecycleDescriptor;
import org.smartdeveloperhub.harvesters.ci.backend.core.lifecycle.LifecycleDescriptorRepository;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionManager;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ExecutionRepository;

public class JPAApplicationRegistryTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(JPAApplicationRegistryTest.class);

	private static final String SUB_BUILD_TITLE = "SUB-BUILD TITLE";
	private static final String BUILD_TITLE     = "BUILD TITLE";

	private static EntityManagerFactory factory;
	private static JPAApplicationRegistry persistencyFacade;

	@BeforeClass
	public static void startUp() throws Exception {
		factory = Persistence.createEntityManagerFactory("unitTests");
		persistencyFacade = new JPAApplicationRegistry(factory);
	}

	@AfterClass
	public static void shutDown() throws Exception {
		if(factory!=null) {
			factory.close();
		}
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
			Service service=Service.newInstance(serviceId);
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

		persistencyFacade.clear();

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

		persistencyFacade.clear();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			outBuild = buildRepository.buildOfId(buildId,SimpleBuild.class);
			outExecution = executionRepository.executionOfId(executionId);
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
			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

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
			Service service=Service.newInstance(serviceId);
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

		persistencyFacade.clear();

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

		persistencyFacade.clear();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			outBuild = buildRepository.buildOfId(buildId,CompositeBuild.class);
			outExecution = executionRepository.executionOfId(executionId);
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
			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

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
			Service service=Service.newInstance(serviceId);
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

		persistencyFacade.clear();

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

		persistencyFacade.clear();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			outBuild = buildRepository.buildOfId(subBuildId,SubBuild.class);
			outExecution = executionRepository.executionOfId(executionId);

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

			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

		LOGGER.debug("Completed Sub Build management");
	}

	@Test
	public void testLifecycleManagement() throws Exception {
		LOGGER.debug("Started Lifecycle management");

		URI nativeId = URI.create("http://localhost/jenkins/");
		EntityId serviceId=EntityId.newInstance(nativeId, Type.SERVICE);
		EntityId buildId=EntityId.newInstance(serviceId.nativeId().resolve("job/my-build/"), Type.BUILD);
		EntityId executionId=EntityId.newInstance(buildId.nativeId().resolve("3/"), Type.EXECUTION);

		Date registeredOn = new Date();
		Date deletedOn = new Date();

		LifecycleDescriptorRepository repository=persistencyFacade.getLifecycleDescriptorRepository();
		TransactionManager transactionManager = persistencyFacade.getTransactionManager();

		Transaction tx1 = transactionManager.currentTransaction();
		tx1.begin();
		try {
			LifecycleDescriptor descriptor=LifecycleDescriptor.newInstance(serviceId);
			descriptor.register(registeredOn);
			LifecycleDescriptor b=LifecycleDescriptor.newInstance(buildId);
			LifecycleDescriptor e=LifecycleDescriptor.newInstance(executionId);
			repository.add(descriptor);
			repository.add(b);
			repository.add(e);
			LOGGER.debug("TX1: {}",descriptor);
			LOGGER.debug("TX1: {}",b);
			LOGGER.debug("TX1: {}",e);
			tx1.commit();
		} catch(Exception e) {
			tx1.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.clear();

		Transaction tx2 = transactionManager.currentTransaction();
		tx2.begin();
		try {
			LifecycleDescriptor descriptor = repository.descriptorOfId(serviceId);
			LOGGER.debug("TX2-in: {}",descriptor);
			assertThat(descriptor.index(),notNullValue());
			assertThat(descriptor.registeredOn(),equalTo(registeredOn));
			assertThat(descriptor.deletedOn(),nullValue());
			assertThat(descriptor.isTransient(),equalTo(false));
			assertThat(descriptor.isActive(),equalTo(true));
			assertThat(descriptor.isDeleted(),equalTo(false));
			descriptor.delete(deletedOn);
			LOGGER.debug("TX2-out: {}",descriptor);
			tx2.commit();
		} catch(Exception e) {
			tx2.rollback();
			e.printStackTrace();
			throw e;
		}

		persistencyFacade.clear();

		Transaction tx3 = transactionManager.currentTransaction();
		tx3.begin();
		try {
			LifecycleDescriptor descriptor = repository.descriptorOfId(serviceId);
			LOGGER.debug("TX3-in: {}",descriptor);
			assertThat(descriptor.index(),notNullValue());
			assertThat(descriptor.registeredOn(),equalTo(registeredOn));
			assertThat(descriptor.deletedOn(),equalTo(deletedOn));
			assertThat(descriptor.isTransient(),equalTo(false));
			assertThat(descriptor.isActive(),equalTo(false));
			assertThat(descriptor.isDeleted(),equalTo(true));
			repository.remove(descriptor);
			LOGGER.debug("TX3-out: {}",(String)null);
			tx3.commit();
		} catch(Exception e) {
			tx3.rollback();
			e.printStackTrace();
			throw e;
		}

		Transaction tx4 = transactionManager.currentTransaction();
		tx4.begin();
		try {
			LifecycleDescriptor descriptor = repository.descriptorOfId(serviceId);
			LOGGER.debug("TX4-in: {}",descriptor);
			assertThat(descriptor,nullValue());
			tx4.commit();
		} catch(Exception e) {
			tx4.rollback();
			e.printStackTrace();
			throw e;
		}

		Transaction tx5 = transactionManager.currentTransaction();
		tx5.begin();
		try {
			LifecycleDescriptor b=LifecycleDescriptor.newInstance(buildId);
			repository.add(b);
			tx5.commit();
			fail("Should not add descriptors with the non-unique entity identifiers");
		} catch(Exception e) {
			tx5.rollback();
		}

		LOGGER.debug("Completed Lifecycle management");
	}

}
