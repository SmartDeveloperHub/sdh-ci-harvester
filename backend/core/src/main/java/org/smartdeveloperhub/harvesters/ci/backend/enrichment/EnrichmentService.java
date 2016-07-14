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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.4.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.4.0-SNAPSHOT.jar
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
import org.smartdeveloperhub.curator.connector.CuratorConfiguration;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Codebase;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateBranchCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateCommitCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateRepositoryCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CompletedEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent.EntityType;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent.State;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventNotification;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;
import org.smartdeveloperhub.jenkins.crawler.util.ListenerManager;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class EnrichmentService {

	private interface ServiceState {

		void connect() throws IOException;
		void disconnect() throws IOException;

		boolean isConnected();

		void enrich(Execution execution) throws IOException;
		void addEnrichment(EnrichmentContext context, ExecutionEnrichment enrichment) throws IOException;

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
			doRequestEnrichment(execution);
		}

		@Override
		public void addEnrichment(final EnrichmentContext context, final ExecutionEnrichment enrichment) throws IOException {
			doProcessEnrichment(context,enrichment);
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

		@Override
		public void addEnrichment(final EnrichmentContext context, final ExecutionEnrichment enrichment) {
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

	private final TransactionManager transactionManager;

	private final ListenerManager<EntityLifecycleEventListener> listeners;

	public EnrichmentService(final SourceCodeManagementService scmService, final ExecutionRepository executionRepository, final PendingEnrichmentRepository repository, final CompletedEnrichmentRepository completedRepository, final TransactionManager transactionManager, final Deployment deployment) {
		this.scmService = scmService;
		this.executionRepository = executionRepository;
		this.pendingRepository = repository;
		this.completedRepository = completedRepository;
		this.transactionManager = transactionManager;
		this.state=new ServiceDisconnected();
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.listeners=ListenerManager.newInstance();
		this.resolver=new NullResolverService();
		this.connector =
			Connector.
				builder().
					withConnectorChannel(
						ProtocolFactory.
							newDeliveryChannel().
								withBroker(deployment.broker()).
								withRoutingKey("ci.connector.enrichments").
								build()).
					withCuratorConfiguration(
						CuratorConfiguration.
							newInstance().
								withBroker(deployment.broker())).
					withQueueName("ci.connector.queue").
					withBase(deployment.base()).
					withNamespacePrefix(UseCase.CI_NAMESPACE,"ci").
					withNamespacePrefix(UseCase.SCM_NAMESPACE,"scm").
					withNamespacePrefix(UseCase.DOAP_NAMESPACE,"doap").
					build();
	}

	private void doConnect() throws IOException {
		LOGGER.info("Initializing Enrichment Service...");
		this.requestor=new EnrichmentRequestor(this,this.connector,this.resolver);
		this.requestor.start();
		initializePendingEnrichmentsTransactionally();
		LOGGER.info("Enrichment Service initialized.");
		this.state=new ServiceConnected();
	}

	private void initializePendingEnrichmentsTransactionally() {
		final Transaction tx = this.transactionManager.currentTransaction();
		try {
			tx.begin();
			initializePendingEnrichments();
			tx.commit();
		} catch (final Exception e) {
			LOGGER.error("Could not initialize pending enrichments. Full stacktrace follows",e);
		} finally {
			if(tx.isActive()) {
				try {
					tx.rollback();
				} catch (final TransactionException e) {
					LOGGER.warn("Transaction rollback failure while initializing pending enrichments. Full stacktrace follows",e);
				}
			}
		}
	}

	private void initializePendingEnrichments() {
		this.pendingRepository.removeAll();
		final List<URI> executions=this.executionRepository.executionIds();
		final Set<URI> enrichedExecutions=enrichedExecutions();
		final Set<URI> toBeEnriched=Sets.newLinkedHashSet(executions);
		toBeEnriched.removeAll(enrichedExecutions);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Executions           ({}): {}",executions.size(),executions);
			LOGGER.trace("Enriched executions  ({}): {}",enrichedExecutions.size(),enrichedExecutions);
			LOGGER.trace("Executions to enrich ({}): {}",toBeEnriched.size(),toBeEnriched);
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
			doRequestEnrichment(execution);
		}
	}

	private void doRequestEnrichment(final Execution execution) {
		LOGGER.debug("Requested enrichment for {}",execution);
		final EnrichmentContext context = createContext(execution);
		if(context.requiresEnrichment()) {
			processPendingEnrichment(context);
		} else {
			processCompletedEnrichment(context,ImmutableSet.of(execution.executionId()));
		}
	}

	private void doProcessEnrichment(final EnrichmentContext context, final ExecutionEnrichment enrichment) throws IOException {
		LOGGER.debug("Requested adding execution enrichment {} for {}",enrichment,context);
		final Transaction tx = this.transactionManager.currentTransaction();
		try {
			tx.begin();
			processExecutionEnrichment(context, enrichment);
			tx.commit();
		} catch (final Exception e) {
			throw new IOException("Could not process "+enrichment+" for "+context,e);
		} finally {
			if(tx.isActive()) {
				try {
					tx.rollback();
				} catch (final TransactionException e) {
					LOGGER.warn("Transaction rollback failure while processing enrichment result {} about {}. Full stacktrace follows",enrichment,context,e);
				}
			}
		}
	}

	private void processExecutionEnrichment(final EnrichmentContext context, final ExecutionEnrichment enrichment) {
		final PendingEnrichment pendingEnrichment=this.pendingRepository.pendingEnrichmentOfId(context.pendingEnrichment().id());
		if(pendingEnrichment==null) {
			LOGGER.info("Discarding enrichment {}: pending enrichment does not exist",enrichment);
			return;
		}

		final Execution execution = this.executionRepository.executionOfId(context.targetExecution().executionId());

		final EnrichmentContext freshContext=createContext(execution);
		if(!freshContext.requiresEnrichment()) {
			LOGGER.info("Discarding enrichment {}: execution {} is already enriched",enrichment,execution);
			return;
		}

		final URI repositoryLocation = context.repositoryLocation();
		if(freshContext.requiresRepository() && !freshContext.enrichment().repositoryResource().isPresent()) {
			if(enrichment.repositoryResource().isPresent()) {
				createRepository(freshContext, enrichment, repositoryLocation);
			} else {
				LOGGER.error("Enrichment does not have the expected resource for repository {}",repositoryLocation);
				return;
			}
		}

		final String branchName = context.branchName();
		if(freshContext.requiresBranch() && !freshContext.enrichment().branchResource().isPresent()) {
			if(enrichment.branchResource().isPresent()) {
				createBranch(freshContext, enrichment, repositoryLocation,branchName);
			} else {
				LOGGER.error("Enrichment does not have the expected resource for branch {{}}{}",repositoryLocation,branchName);
				return;
			}
		}

		final String commitId = context.commitId();
		if(freshContext.requiresCommit() && !freshContext.enrichment().commitResource().isPresent()) {
			if(enrichment.commitResource().isPresent()) {
				createCommit(freshContext, enrichment, repositoryLocation, branchName, commitId);
			} else {
				LOGGER.error("Enrichment does not have the expected resource for commit {{{}}{}}{}",repositoryLocation,branchName,commitId);
				return;
			}
		}

		finalizePendingEnrichment(pendingEnrichment);
		processCompletedEnrichment(freshContext,pendingEnrichment.executions());
	}

	private void createCommit(
			final EnrichmentContext context,
			final ExecutionEnrichment enrichment,
			final URI repositoryLocation,
			final String branchName,
			final String commitId) {
		final URI commitResource = enrichment.commitResource().get();
		this.scmService.createCommit(
			CreateCommitCommand.
				builder().
					withRepositoryLocation(repositoryLocation).
					withBranchName(branchName).
					withCommitId(commitId).
					withResource(commitResource).
					build());
		context.setCommitResource(commitResource);
		LOGGER.debug("Created commit {} ({}) in branch {} of repository {}",commitId,commitResource,branchName,repositoryLocation);
	}

	private void createBranch(
			final EnrichmentContext context,
			final ExecutionEnrichment enrichment,
			final URI repositoryLocation,
			final String branchName) {
		final URI branchResource = enrichment.branchResource().get();
		this.scmService.createBranch(
			CreateBranchCommand.
				builder().
					withRepositoryLocation(repositoryLocation).
					withBranchName(branchName).
					withResource(branchResource).
					build());
		context.setBranchResource(branchResource);
		LOGGER.debug("Created branch {} ({}) in repository {}",branchName,branchResource,repositoryLocation);
	}

	private void createRepository(
			final EnrichmentContext context,
			final ExecutionEnrichment enrichment,
			final URI repositoryLocation) {
		final URI repositoryResource = enrichment.repositoryResource().get();
		this.scmService.createRepository(
			CreateRepositoryCommand.
				builder().
					withRepositoryLocation(repositoryLocation).
					withResource(repositoryResource).
					build());
		context.setRepositoryResource(repositoryResource);
		LOGGER.debug("Created repository {} ({})",repositoryLocation,repositoryResource);
	}

	private void finalizePendingEnrichment(final PendingEnrichment pendingEnrichment) {
		this.pendingRepository.remove(pendingEnrichment);
		for(final URI executionId:pendingEnrichment.executions()) {
			final EntityLifecycleEvent event =
				EntityLifecycleEvent.
					newInstance(EntityType.EXECUTION,State.ENRICHED,executionId);
			final EntityLifecycleEventNotification notification=new EntityLifecycleEventNotification(event);
			this.listeners.notify(notification);
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

	private void processCompletedEnrichment(final EnrichmentContext context, final Set<URI> executions) {
		final CompletedEnrichment completed=this.completedRepository.completedEnrichmentOfExecution(context.targetExecution().executionId());
		if(completed!=null) {
			LOGGER.trace("{} enrichment is already completed (#{})",context,completed.id());
			return;
		}

		final ExecutionEnrichment enrichment=context.enrichment();
		final List<CompletedEnrichment> potentialEnrichments=this.completedRepository.findCompletedEnrichments(enrichment.repositoryResource().orNull(),enrichment.branchResource().orNull(),enrichment.commitResource().orNull());
		if(!potentialEnrichments.isEmpty()) {
			final CompletedEnrichment delegate = potentialEnrichments.get(0);
			delegate.executions().addAll(executions);
			LOGGER.trace("{} enrichment is now completed by enrichment #{}",context,delegate.id());
			return;
		}

		final CompletedEnrichment newCompleted=CompletedEnrichment.newInstance(enrichment.repositoryResource().orNull(),enrichment.branchResource().orNull(),enrichment.commitResource().orNull());
		newCompleted.executions().addAll(executions);
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
			delegate.executions().add(context.targetExecution().executionId());
			LOGGER.trace("{} enrichment request joins to pending enrichment #{}",context,delegate.id());
			return;
		}

		final PendingEnrichment newPending=PendingEnrichment.newInstance(context.repositoryLocation(),context.branchName(),context.commitId());
		newPending.executions().add(context.targetExecution().executionId());
		this.pendingRepository.add(newPending);
		LOGGER.trace("{} creates {}",context,newPending);

		context.setPendingEnrichment(newPending);
		this.requestor.enqueueRequest(context);
	}

	public EnrichmentService withResolverService(final ResolverService resolver) {
		this.write.lock();
		try {
			checkState(!this.state.isConnected(),"Cannot change resolver service while connected");
			if(resolver!=null) {
				this.resolver=resolver;
			} else {
				this.resolver=new NullResolverService();
			}
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

	public void addEnrichment(final EnrichmentContext context, final ExecutionEnrichment enrichment) throws IOException {
		this.read.lock();
		try {
			this.state.addEnrichment(context,enrichment);
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

	public EnrichmentService registerListener(final EntityLifecycleEventListener listener) {
		this.listeners.registerListener(listener);
		return this;
	}

	public EnrichmentService deregisterListener(final EntityLifecycleEventListener listener) {
		this.listeners.deregisterListener(listener);
		return this;
	}

}
