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

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.spi.ComponentRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.AbstractIdleService;

final class BackendDumpService extends AbstractIdleService {

	private ComponentRegistry registry;
	private ServiceRepository serviceRepository;
	private BuildRepository buildRepository;
	private ExecutionRepository executionRepository;

	BackendDumpService(ComponentRegistry registry) {
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

	@Override
	protected void startUp() throws Exception {
		Transaction tx = registry.getTransactionManager().currentTransaction();
		tx.begin();
		try {
			Consoles.defaultConsole().printf("Starting dump...%n");
			for(URI serviceId:serviceRepository().serviceIds()) {
				Service service=serviceRepository().serviceOfId(serviceId);
				Consoles.defaultConsole().printf("- Service %s : %s%n",serviceId,service);
				for(URI buildId:service.builds()) {
					Build build=buildRepository().buildOfId(buildId);
					if(build instanceof SimpleBuild) {
						Consoles.defaultConsole().printf("  + Simple build %s : %s%n",buildId,build);
						for(URI executionId:build.executions()) {
							Execution execution=executionRepository().executionOfId(executionId);
							Consoles.defaultConsole().printf("    * Execution %s : %s%n",executionId,execution);
						}
					} else {
						Consoles.defaultConsole().printf("  + Composite build %s : %s%n",buildId,build);
						for(URI executionId:build.executions()) {
							Execution execution=executionRepository().executionOfId(executionId);
							Consoles.defaultConsole().printf("    * Execution %s : %s%n",executionId,execution);
						}
						for(URI subBuildId:((CompositeBuild)build).subBuilds()) {
							Build subBuild=buildRepository().buildOfId(subBuildId);
							Consoles.defaultConsole().printf("    * Sub-build %s : %s%n",subBuildId,subBuild);
							for(URI executionId:subBuild.executions()) {
								Execution execution=executionRepository().executionOfId(executionId);
								Consoles.defaultConsole().printf("      + Execution %s : %s%n",executionId,execution);
							}
						}
					}
				}
			}
			Consoles.defaultConsole().printf("Dump completed.%n");
		} finally {
			tx.rollback();
		}
	}

	@Override
	protected void shutDown() throws Exception {
		// NOTHING TO DO:
	}

}
