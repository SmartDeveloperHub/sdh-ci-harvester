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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.Connector;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Codebase;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CompletedEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ExecutionRepository;

import com.google.common.collect.Sets;

public class EnrichmentService {

	private interface ServiceState {

		void connect() throws IOException;
		void disconnect() throws IOException;

		boolean isConnected();

		void enrich(Execution execution) throws IOException;

	}

	private final class ServiceConnected implements ServiceState {

		@Override
		public void connect() throws IOException {
			throw new IllegalStateException("Already connected");
		}

		@Override
		public void disconnect() throws IOException {
			doDisconnect();
		}

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public void enrich(final Execution execution) {
			doEnrich(execution);
		}

	}

	private class ServiceDisconnected implements ServiceState {

		@Override
		public void connect() throws IOException {
			doConnect();
		}

		@Override
		public void disconnect() throws IOException {
			// Nothing to do
		}

		@Override
		public boolean isConnected() {
			return false;
		}

		@Override
		public void enrich(final Execution execution) {
			throw new IllegalStateException("Not connected");
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(EnrichmentService.class);

	private final SourceCodeManagementService scmService;
	private final PendingEnrichmentRepository pendingRepository;
	private final CompletedEnrichmentRepository completedRepository;
	private final ExecutionRepository executionRepository;
	private final Connector connector;

	private final Lock read;
	private final Lock write;

	private ServiceState state;
	private ResolverService resolver;
	private EnrichmentRequestor requestor;

	public EnrichmentService(final SourceCodeManagementService scmService, final ExecutionRepository executionRepository, final PendingEnrichmentRepository repository, final CompletedEnrichmentRepository completedRepository, final Deployment deployment) {
		this.scmService = scmService;
		this.executionRepository = executionRepository;
		this.pendingRepository = repository;
		this.completedRepository = completedRepository;
		this.state=new ServiceDisconnected();
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.connector =
			Connector.
				builder().
					withConnectorChannel(
						ProtocolFactory.
							newDeliveryChannel().
								withBroker(deployment.broker()).
								withRoutingKey("ci.connector.enrichments").
								build()).
					withQueueName("ci.connector.queue").
					withBase(deployment.base()).
					withNamespacePrefix(UseCase.CI_NAMESPACE,"ci").
					withNamespacePrefix(UseCase.SCM_NAMESPACE,"scm").
					withNamespacePrefix(UseCase.DOAP_NAMESPACE,"doap").
					build();
	}

	private void doConnect() throws IOException {
		LOGGER.info("Initializing Enrichment Service...");
		this.requestor=new EnrichmentRequestor(this.connector,this.resolver);
		this.requestor.start();
		initializeEnrichments();
		LOGGER.info("Enrichment Service initialized.");
		this.state=new ServiceConnected();
	}

	private void initializeEnrichments() {
		final List<URI> executions=this.executionRepository.executionIds();
		final Set<URI> enrichedExecutions=enrichedExecutions();
		final Set<URI> pendingExecutions=clearPendingEnrichments();
		final Set<URI> toBeEnriched=Sets.newLinkedHashSet();
		toBeEnriched.addAll(executions);
		toBeEnriched.addAll(pendingExecutions);
		toBeEnriched.removeAll(enrichedExecutions);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Executions..........: {}",executions);
			LOGGER.trace("Enriched executions.: {}",enrichedExecutions);
			LOGGER.trace("Pending executions..: {}",pendingExecutions);
			LOGGER.trace("Executions to enrich: {}",toBeEnriched);
		}
		retryEnrichments(toBeEnriched);
	}

	private void doDisconnect() throws IOException {
		this.requestor.stop();
		LOGGER.info("Enrichment Service stopped.");
		this.state=new ServiceDisconnected();
	}

	private Set<URI> enrichedExecutions() {
		final Set<URI> executions=Sets.newLinkedHashSet();
		for(final CompletedEnrichment pe:completedEnrichments()) {
			executions.addAll(pe.executions());
		}
		return executions;
	}

	private void retryEnrichments(final Set<URI> executions) {
		for(final URI executionId:executions) {
			final Execution execution = this.executionRepository.executionOfId(executionId);
			doEnrich(execution);
		}
	}

	private Set<URI> clearPendingEnrichments() {
		final Set<URI> executions=Sets.newLinkedHashSet();
		for(final PendingEnrichment pe:pendingEnrichments()) {
			executions.addAll(pe.executions());
		}
		this.pendingRepository.removeAll();
		return executions;
	}

	private void doEnrich(final Execution execution) {
		LOGGER.debug("Requested enrichment for {}",execution);
		final EnrichmentContext context = createContext(execution);
		if(context.requiresEnrichment()) {
			processPendingEnrichment(context);
		} else {
			processCompletedEnrichment(context);
		}
	}

	private EnrichmentContext createContext(final Execution execution) {
		final Codebase codebase=checkNotNull(execution.codebase(),"Codebase cannot be null");
		final EnrichmentContext context=new EnrichmentContext(execution);
		if(context.requiresCommit()) {
			final Commit commit = this.scmService.findCommit(codebase.location(),codebase.branchName(), execution.commitId());
			if(commit!=null) {
				context.setCommitResource(commit.resource());
			}
		}
		if(context.requiresBranch()) {
			final Branch branch = this.scmService.findBranch(codebase.location(),codebase.branchName());
			if(branch!=null) {
				context.setBranchResource(branch.resource());
			}
		}
		if(context.requiresRepository()) {
			final Repository repository = this.scmService.findRepository(codebase.location());
			if(repository!=null) {
				context.setRepositoryResource(repository.resource());
			}
		}
		return context;
	}

	private void processCompletedEnrichment(final EnrichmentContext context) {
		final CompletedEnrichment completed=this.completedRepository.completedEnrichmentOfExecution(context.targetExecution().executionId());
		if(completed!=null) {
			LOGGER.trace("{} enrichment is already completed (#{})",context,completed.id());
			return;
		}

		final ExecutionEnrichment enrichment=context.enrichment();
		final List<CompletedEnrichment> potentialEnrichments=this.completedRepository.findCompletedEnrichments(enrichment.repositoryResource().orNull(),enrichment.branchResource().orNull(),enrichment.commitResource().orNull());
		if(!potentialEnrichments.isEmpty()) {
			final CompletedEnrichment delegate = potentialEnrichments.get(0);
			LOGGER.trace("{} enrichment is now completed by enrichment #{}",context,delegate.id());
			delegate.executions().add(context.targetExecution().executionId());
			return;
		}

		final CompletedEnrichment newCompleted=CompletedEnrichment.newInstance(enrichment.repositoryResource().orNull(),enrichment.branchResource().orNull(),enrichment.commitResource().orNull());
		newCompleted.executions().add(context.targetExecution().executionId());
		this.completedRepository.add(newCompleted);
		LOGGER.trace("{} enrichment is completed by {}",context,newCompleted);

	}

	private void processPendingEnrichment(final EnrichmentContext context) {
		final PendingEnrichment pending=this.pendingRepository.pendingEnrichmentOfExecution(context.targetExecution().executionId());
		if(pending!=null) {
			LOGGER.trace("{} enrichment request is already being undertaken by pending enrichment #{}",context,pending.id());
			return;
		}

		final List<PendingEnrichment> potentialEnrichments=this.pendingRepository.findPendingEnrichments(context.repositoryLocation(),context.branchName(),context.commitId());
		if(!potentialEnrichments.isEmpty()) {
			final PendingEnrichment delegate = potentialEnrichments.get(0);
			LOGGER.trace("{} enrichment request joins to pending enrichment #{}",context,delegate.id());
			delegate.executions().add(context.targetExecution().executionId());
			return;
		}

		final PendingEnrichment newPending=PendingEnrichment.newInstance(context.repositoryLocation(),context.branchName(),context.commitId());
		newPending.executions().add(context.targetExecution().executionId());
		this.pendingRepository.add(newPending);
		LOGGER.trace("{} creates {}",context,pending);

		context.setPendingEnrichment(newPending.id());
		this.requestor.enqueueRequest(context);
	}

	public EnrichmentService withResolverService(final ResolverService resolver) {
		this.write.lock();
		try {
			checkState(!this.state.isConnected(),"Cannot change resolver service while connected");
			this.resolver=resolver;
			return this;
		} finally {
			this.write.unlock();
		}
	}

	public void connect() throws IOException {
		this.write.lock();
		try {
			this.state.connect();
		} finally {
			this.write.unlock();
		}
	}

	public void disconnect() throws IOException {
		this.write.lock();
		try {
			this.state.disconnect();
		} finally {
			this.write.unlock();
		}
	}

	public void enrich(final Execution execution) throws IOException {
		this.read.lock();
		try {
			this.state.enrich(execution);
		} finally {
			this.read.unlock();
		}
	}

	public ExecutionEnrichment getEnrichment(final Execution execution) {
		this.read.lock();
		try {
			return createContext(execution).enrichment();
		} finally {
			this.read.unlock();
		}
	}

	public List<PendingEnrichment> pendingEnrichments() {
		this.read.lock();
		try {
			return this.pendingRepository.findPendingEnrichments(null,null,null);
		} finally {
			this.read.unlock();
		}
	}

	public List<CompletedEnrichment> completedEnrichments() {
		this.read.lock();
		try {
			return this.completedRepository.findCompletedEnrichments(null,null,null);
		} finally {
			this.read.unlock();
		}
	}

}
