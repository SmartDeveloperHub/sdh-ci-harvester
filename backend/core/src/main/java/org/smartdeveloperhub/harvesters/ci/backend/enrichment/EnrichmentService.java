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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Codebase;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;

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
			EnrichmentService.this.state=new ServiceDisconnected();
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
			EnrichmentService.this.state=new ServiceConnected();
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
	private final PendingEnrichmentRepository repository;

	private final Lock read;
	private final Lock write;

	private ServiceState state;

	public EnrichmentService(final SourceCodeManagementService scmService, final PendingEnrichmentRepository repository) {
		this.scmService = scmService;
		this.repository = repository;
		this.state=new ServiceDisconnected();
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
	}

	private void doEnrich(final Execution execution) {
		final Codebase codebase=checkNotNull(execution.codebase(),"Codebase cannot be null");
		final EnrichmentContext context=new EnrichmentContext(execution);
		LOGGER.debug("Requested enrichment for {}",context);
		if(context.requiresCommit()) {
			final Commit commit = this.scmService.findCommit(codebase.location(),codebase.branchName(), execution.commitId());
			if(commit!=null) {
				LOGGER.trace("{} does not require enrichment: {} is available",context,commit);
				return;
			}
		}
		if(context.requiresBranch()) {
			final Branch branch = this.scmService.findBranch(codebase.location(),codebase.branchName());
			if(branch!=null) {
				if(!context.requiresCommit()) {
					LOGGER.trace("{} does not require enrichment: {} is available",context,branch);
					return;
				}
				context.setBranchResource(branch.resource());
			}
		}
		if(context.requiresRepository()) {
			final Repository repository = this.scmService.findRepository(codebase.location());
			if(repository!=null) {
				if(!context.requiresBranch()) {
					LOGGER.trace("{} does not require enrichment: {} is available",context,repository);
					return;
				}
				context.setRepositoryResource(repository.resource());
			}
		} else {
			LOGGER.trace("{} does not require enrichment: no SCM information is available",context);
			return;
		}
		processEnrichmentContext(context);
	}

	private void processEnrichmentContext(final EnrichmentContext context) {
		final PendingEnrichment pending=this.repository.pendingEnrichmentOfExecution(context.target());
		if(pending!=null) {
			LOGGER.trace("{} request is already being undertaken by {}",context,pending);
			return;
		}

		final List<PendingEnrichment> potentialEnrichments=this.repository.findPendingEnrichments(context.repositoryLocation(),context.branchName(),context.commitId());
		if(!potentialEnrichments.isEmpty()) {
			final PendingEnrichment delegate = potentialEnrichments.get(0);
			LOGGER.trace("{} request joins to {}",context,delegate);
			delegate.executions().add(context.target());
			return;
		}

		final PendingEnrichment newPending=PendingEnrichment.newInstance(context.repositoryLocation(),context.branchName(),context.commitId());
		newPending.executions().add(context.target());
		this.repository.add(newPending);
		context.setPendingEnrichment(newPending.id());
		fireEnrichmentRequest(context);
	}

	private void fireEnrichmentRequest(final EnrichmentContext context) {
		LOGGER.warn("{} requires firing an enrichment request",context);
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

	public List<PendingEnrichment> pendingEnrichments() {
		this.read.lock();
		try {
			return this.repository.findPendingEnrichments(null,null,null);
		} finally {
			this.read.unlock();
		}
	}

}
