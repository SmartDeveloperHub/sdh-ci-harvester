/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-test:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-test-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.test;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Codebase;
import org.smartdeveloperhub.harvesters.ci.backend.domain.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Result;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.backend.domain.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Deployment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ResolverService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.SourceCodeManagementService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateBranchCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateCommitCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateRepositoryCommand;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryBranchRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryBuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryCommitRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryCompletedEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryPendingEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryRepositoryRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.mem.InMemoryTransactionManager;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class TestingBackendController implements BackendController {

	private static final Logger LOGGER=LoggerFactory.getLogger(TestingBackendController.class);

	private static int count;

	private URI jenkinsInstance;

	private final ServiceRepository serviceRepository;

	private final BuildRepository buildRepository;

	private final ExecutionRepository executionRepository;

	private final ContinuousIntegrationService service;

	private final TestingEntityIndex index;

	private final SourceCodeManagementService scmService;

	private final EnrichmentService es;

	TestingBackendController(final Deployment deployment) {
		this.serviceRepository = new InMemoryServiceRepository();
		this.buildRepository = new InMemoryBuildRepository();
		this.executionRepository = new InMemoryExecutionRepository();
		this.service=
			new ContinuousIntegrationService(
				this.serviceRepository,
				this.buildRepository,
				this.executionRepository);
		this.scmService =
			new SourceCodeManagementService(
				new InMemoryRepositoryRepository(),
				new InMemoryBranchRepository(),
				new InMemoryCommitRepository());
		this.es =
			new EnrichmentService(
				this.scmService,
				this.executionRepository,
				new InMemoryPendingEnrichmentRepository(),
				new InMemoryCompletedEnrichmentRepository(),
				new InMemoryTransactionManager(),
				deployment);
		this.index=new TestingEntityIndex(this.service,this.es);
	}

	void setInstance(final URI jenkinsInstance) {
		checkState(this.jenkinsInstance==null,"Already connected (%s)",this.jenkinsInstance);
		this.jenkinsInstance = jenkinsInstance;
	}

	private static Date after(final Date date) {
		final Random random=new Random(System.nanoTime());
		return new Date(date.getTime()+(random.nextLong() % 3600000));
	}

	private static URI buildId(final Service service, final String id) {
		return service.serviceId().resolve("jobs/"+id+"/");
	}

	private static URI buildId(final CompositeBuild build, final String id) {
		return build.buildId().resolve(id+"/");
	}

	private static URI executionId(final Build build, final int executionIdi) {
		return build.buildId().resolve(executionIdi+"/");
	}

	private static Execution createExecution(final ExecutionRepository repository, final SourceCodeManagementService scmService, final Build build, final Execution execution, final int executionIdi, final Status status) {
		Date date=build.createdOn();
		if(execution!=null) {
			if(execution.isFinished()) {
				date=execution.result().finishedOn();
			} else {
				date=execution.createdOn();
			}
		}
		final Execution newExecution=build.addExecution(executionId(build, executionIdi), after(date), build.codebase(), randomSHA1(build,executionIdi));
		if(status!=null) {
			newExecution.finish(new Result(status,after(newExecution.createdOn())));
		}
		repository.add(newExecution);
		final CreateCommitCommand command =
			CreateCommitCommand.
				builder().
					withRepositoryLocation(newExecution.codebase().location()).
					withBranchName(newExecution.codebase().branchName()).
					withCommitId(newExecution.commitId()).
					withResource(build.buildId().resolve("scm/repo/master/"+newExecution.commitId()+"/")).
					build();
		scmService.createCommit(command);
		return newExecution;
	}

	private static String randomSHA1(final Build build, final int executionId) {
		final Random r=new Random(System.currentTimeMillis());
		count++;
		final int[] n= {
			build.buildId().hashCode(),
			executionId,
			r.nextInt(),
			r.nextInt(),
			count,
		};
		final StringBuilder builder=new StringBuilder();
		for(int i=0;i<n.length;i++) {
			final String hexString = Integer.toHexString(n[i]);
			for(int j=0;j<4-hexString.length();j++) {
				builder.append("0");
			}
			builder.append(hexString);
		}
		return builder.toString();
	}

	private static void createExecutions(final ExecutionRepository repository, final Build build, final SourceCodeManagementService scmService) {
		final Execution failedExecution  = createExecution(repository,scmService,build,null,            1,Status.FAILED);
		final Execution warningExecution = createExecution(repository,scmService,build,failedExecution, 2,Status.WARNING);
		final Execution errorExecution   = createExecution(repository,scmService,build,warningExecution,3,Status.NOT_BUILT);
		final Execution passedExecution  = createExecution(repository,scmService,build,errorExecution,  4,Status.PASSED);
		final Execution abortedExecution = createExecution(repository,scmService,build,passedExecution, 5,Status.ABORTED);
		createExecution(repository,scmService,build,abortedExecution,6,null);
	}

	private static void createBuild(final BuildRepository repository, final Build build, final Date createdOn, final String description, final SourceCodeManagementService scmService) {
		build.setCreatedOn(after(createdOn));
		build.setDescription(description);
		build.setCodebase(new Codebase(build.buildId().resolve("repository.git"),"master"));
		scmService.createRepository(
			CreateRepositoryCommand.
				builder().
					withRepositoryLocation(build.codebase().location()).
					withResource(build.buildId().resolve("scm/repo/")).
					build());
		scmService.createBranch(
			CreateBranchCommand.
				builder().
					withRepositoryLocation(build.codebase().location()).
					withBranchName(build.codebase().branchName()).
					withResource(build.buildId().resolve("scm/repo/master/")).
					build());
		repository.add(build);
	}

	private void populateBackend(final URI jenkinsInstance) {
		final Date initTime = new Date();

		final Service defaultService = Service.newInstance(jenkinsInstance);
		this.serviceRepository.add(defaultService);

		final SimpleBuild simpleBuild=defaultService.addSimpleBuild(buildId(defaultService, "simple-job"),"Example simple build");
		createBuild(this.buildRepository, simpleBuild, initTime, "An example simple build for testing",this.scmService);
		createExecutions(this.executionRepository,simpleBuild,this.scmService);

		final CompositeBuild compositeBuild=defaultService.addCompositeBuild(buildId(defaultService, "composite-job"),"Example composite build");
		createBuild(this.buildRepository, compositeBuild, initTime, "An example composite build for testing",this.scmService);
		createExecutions(this.executionRepository, compositeBuild,this.scmService);

		final SubBuild subBuild=compositeBuild.addSubBuild(buildId(compositeBuild, "sub-job"),"Example sub build");
		createBuild(this.buildRepository, subBuild, initTime, "An example sub build for testing",this.scmService);
		createExecutions(this.executionRepository, subBuild,this.scmService);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityIndex entityIndex() {
		return this.index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setTargetService(final URI instance) {
		try {
			setInstance(instance);
			populateBackend(this.jenkinsInstance);
			return true;
		} catch (final Exception e) {
			LOGGER.error("Could not populate testing backend controller",e);
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setExecutionResolver(final ResolverService resolver) {
		checkNotNull(resolver,"Execution resolver cannot be null");
		this.es.withResolverService(resolver);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void connect(final EntityLifecycleEventListener listener) {
		LOGGER.info("Connecting to {}...",this.jenkinsInstance);
		LOGGER.info("Connected to {}.",this.jenkinsInstance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnect() {
		LOGGER.info("Disconnecting from {}...",this.jenkinsInstance);
		LOGGER.info("Disconnected from {}.",this.jenkinsInstance);
	}

}
