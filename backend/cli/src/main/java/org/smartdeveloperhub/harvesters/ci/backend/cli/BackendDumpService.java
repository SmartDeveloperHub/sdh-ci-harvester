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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.net.URI;
import java.util.List;

import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.backend.domain.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.CompletedEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CompletedEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.PendingEnrichmentRepository;
import org.smartdeveloperhub.harvesters.ci.backend.spi.ComponentRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.AbstractIdleService;

final class BackendDumpService extends AbstractIdleService {

	private final ComponentRegistry registry;
	private ServiceRepository serviceRepository;
	private BuildRepository buildRepository;
	private ExecutionRepository executionRepository;

	BackendDumpService(final ComponentRegistry registry) {
		this.registry = registry;
	}

	private ExecutionRepository executionRepository() {
		if(this.executionRepository==null) {
			this.executionRepository=this.registry.getExecutionRepository();
		}
		return this.executionRepository;
	}

	private BuildRepository buildRepository() {
		if(this.buildRepository==null) {
			this.buildRepository=this.registry.getBuildRepository();
		}
		return this.buildRepository;
	}

	private ServiceRepository serviceRepository() {
		if(this.serviceRepository==null) {
			this.serviceRepository=this.registry.getServiceRepository();
		}
		return this.serviceRepository;
	}

	private PendingEnrichmentRepository pendingEnrichmentRepository() {
		return this.registry.getPendingEnrichmentRepository();
	}

	private CompletedEnrichmentRepository completedEnrichmentRepository() {
		return this.registry.getCompletedEnrichmentRepository();
	}

	@Override
	protected void startUp() throws Exception {
		final Transaction tx = this.registry.getTransactionManager().currentTransaction();
		tx.begin();
		try {
			Consoles.defaultConsole().printf("Starting dump...%n");
			for(final URI serviceId:serviceRepository().serviceIds()) {
				dumpService(serviceRepository().serviceOfId(serviceId));
			}
			dumpCompletedEnrichments(completedEnrichmentRepository().findCompletedEnrichments(null,null,null));
			dumpPendingEnrichments(pendingEnrichmentRepository().findPendingEnrichments(null,null,null));
			Consoles.defaultConsole().printf("Dump completed.%n");
		} finally {
			tx.rollback();
		}
	}

	private void dumpPendingEnrichments(final List<PendingEnrichment> pendingEnrichments) {
		Consoles.defaultConsole().printf("- Pending enrichments (%d):%n",pendingEnrichments.size());
		for(final PendingEnrichment pendingEnrichment:pendingEnrichments) {
			Consoles.defaultConsole().printf("  + Pending enrichment [%04d]:%n",pendingEnrichment.id());
			Consoles.defaultConsole().printf("    * Repository location: %s%n",pendingEnrichment.repositoryLocation());
			Consoles.defaultConsole().printf("    * Branch name........: %s%n",pendingEnrichment.branchName());
			Consoles.defaultConsole().printf("    * Commit identifier..: %s%n",pendingEnrichment.commitId());
			Consoles.defaultConsole().printf("    * Executions (%d): %n",pendingEnrichment.executions().size());
			for(final URI execution:pendingEnrichment.executions()) {
				Consoles.defaultConsole().printf("      - %s%n",execution);
			}
		}
	}

	private void dumpCompletedEnrichments(final List<CompletedEnrichment> completedEnrichments) {
		Consoles.defaultConsole().printf("- Completed enrichments (%d):%n",completedEnrichments.size());
		for(final CompletedEnrichment pendingEnrichment:completedEnrichments) {
			Consoles.defaultConsole().printf("  + Completed enrichment [%04d]:%n",pendingEnrichment.id());
			Consoles.defaultConsole().printf("    * Repository resource: %s%n",pendingEnrichment.repositoryResource());
			Consoles.defaultConsole().printf("    * Branch resource....: %s%n",pendingEnrichment.branchResource());
			Consoles.defaultConsole().printf("    * Commit resource....: %s%n",pendingEnrichment.commitResource());
			Consoles.defaultConsole().printf("    * Executions (%d): %n",pendingEnrichment.executions().size());
			for(final URI execution:pendingEnrichment.executions()) {
				Consoles.defaultConsole().printf("      - %s%n",execution);
			}
		}
	}

	private void dumpService(final Service service) {
		Consoles.defaultConsole().printf("- Service %s : %s%n",service.serviceId(),service);
		for(final URI buildId:service.builds()) {
			final Build build=buildRepository().buildOfId(buildId);
			if(build instanceof SimpleBuild) {
				dumpSimpleBuild(build);
			} else {
				dumpCompositeBuild(build);
			}
		}
	}

	private void dumpCompositeBuild(final Build build) {
		Consoles.defaultConsole().printf("  + Composite build %s : %s%n",build.buildId(),build);
		for(final URI executionId:build.executions()) {
			final Execution execution=executionRepository().executionOfId(executionId);
			Consoles.defaultConsole().printf("    * Execution %s : %s%n",executionId,execution);
		}
		for(final URI subBuildId:((CompositeBuild)build).subBuilds()) {
			dumpSubBuild(buildRepository().buildOfId(subBuildId));
		}
	}

	private void dumpSubBuild(final Build subBuild) {
		Consoles.defaultConsole().printf("    * Sub-build %s : %s%n",subBuild.buildId(),subBuild);
		for(final URI executionId:subBuild.executions()) {
			final Execution execution=executionRepository().executionOfId(executionId);
			Consoles.defaultConsole().printf("      + Execution %s : %s%n",executionId,execution);
		}
	}

	private void dumpSimpleBuild(final Build build) {
		Consoles.defaultConsole().printf("  + Simple build %s : %s%n",build.buildId(),build);
		for(final URI executionId:build.executions()) {
			final Execution execution=executionRepository().executionOfId(executionId);
			Consoles.defaultConsole().printf("    * Execution %s : %s%n",executionId,execution);
		}
	}

	@Override
	protected void shutDown() throws Exception {
		// NOTHING TO DO:
	}

}
