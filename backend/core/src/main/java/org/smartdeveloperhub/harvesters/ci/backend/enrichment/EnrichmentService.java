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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Codebase;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;

public class EnrichmentService {

	private static final Logger LOGGER=LoggerFactory.getLogger(EnrichmentService.class);

	private final SourceCodeManagementService scmService;
	private final PendingEnrichmentRepository repository;

	public EnrichmentService(final SourceCodeManagementService scmService, final PendingEnrichmentRepository repository) {
		this.scmService = scmService;
		this.repository = repository;
	}

	public void enrich(final Execution execution) {
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

	public List<PendingEnrichment> pendingEnrichments() {
		return this.repository.findPendingEnrichments(null,null,null);
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

}
