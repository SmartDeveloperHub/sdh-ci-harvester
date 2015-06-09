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
package org.smartdeveloperhub.harvesters.ci.backend.core;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.BuildVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.Result;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.ServiceRepository;
import org.smartdeveloperhub.harvesters.ci.backend.SimpleBuild;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.RegisterServiceCommand;

public class ContinuousIntegrationService {

	private ServiceRepository serviceRepository;
	private BuildRepository buildRepository;
	private ExecutionRepository executionRepository;

	public ContinuousIntegrationService(BuildRepository buildRepository, ServiceRepository serviceRepository, ExecutionRepository executionRepository) {
		setServiceRepository(serviceRepository);
		setBuildRepository(buildRepository);
		setExecutionRepository(executionRepository);
	}

	protected void setServiceRepository(ServiceRepository serviceRepository) {
		checkNotNull(serviceRepository,"Service repository cannot be null");
		this.serviceRepository = serviceRepository;
	}

	protected void setBuildRepository(BuildRepository buildRepository) {
		checkNotNull(buildRepository,"Build repository cannot be null");
		this.buildRepository = buildRepository;
	}

	protected void setExecutionRepository(ExecutionRepository executionRepository) {
		checkNotNull(executionRepository,"Execution repository cannot be null");
		this.executionRepository = executionRepository;
	}

	protected ServiceRepository serviceRepository() {
		return this.serviceRepository;
	}

	protected BuildRepository buildRepository() {
		return this.buildRepository;
	}

	protected ExecutionRepository executionRepository() {
		return this.executionRepository;
	}

	public List<URI> getRegisteredServices() {
		return serviceRepository().serviceIds();
	}

	public Service getService(URI serviceId) {
		checkNotNull(serviceId,"Service identifier be null");
		return serviceRepository().serviceOfId(serviceId);
	}

	public Build getBuild(URI buildId) {
		checkNotNull(buildId,"Build identifier cannot be null");
		return buildRepository().buildOfId(buildId);
	}

	public Execution getExecution(URI executionId) {
		checkNotNull(executionId,"Execution identifier cannot be null");
		return executionRepository().executionOfId(executionId);
	}

	public void registerService(RegisterServiceCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		URI serviceId=aCommand.serviceId();

		Service service = serviceRepository().serviceOfId(serviceId);
		checkArgument(service==null,"Service '%s' is already registered",serviceId);

		service=new Service(serviceId);
		serviceRepository().add(service);
	}

	public void createBuild(CreateBuildCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		URI serviceId=aCommand.serviceId();
		Service service = serviceRepository().serviceOfId(serviceId);
		checkArgument(service!=null,"Service '%s' is not registered",serviceId);

		URI buildId=aCommand.buildId();
		Build build=null;
		if(aCommand.simple()) {
			build=service.addSimpleBuild(buildId);
		} else {
			build=service.addCompositeBuild(buildId);
		}
		buildRepository().add(build);
	}

	public void deleteBuild(DeleteBuildCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		URI buildId=aCommand.buildId();
		Build build = buildRepository().buildOfId(buildId);
		checkArgument(build!=null,"Build '%s' is not registered",buildId);
		build.accept(
			new BuildVisitor() {
				private void deleteBuild(Build aBuild) {
					buildRepository().remove(aBuild);
					for(URI executionId:aBuild.executions()) {
						Execution execution = executionRepository().executionOfId(executionId);
						if(execution!=null) {
							executionRepository().remove(execution);
						}
					}
					aBuild.executions().clear();
				}

				@Override
				public void visitSimpleBuild(SimpleBuild aBuild) {
					deleteBuild(aBuild);
				}
				@Override
				public void visitCompositeBuild(CompositeBuild aBuild) {
					deleteBuild(aBuild);
					for(URI childId:aBuild.subBuilds()) {
						Build subBuild = buildRepository().buildOfId(childId);
						if(subBuild!=null) {
							deleteBuild(subBuild);
						}
					}
					aBuild.subBuilds().clear();
				}
				@Override
				public void visitSubBuild(SubBuild aBuild) {
					deleteBuild(aBuild);
					CompositeBuild parent = buildRepository().buildOfId(aBuild.parentId(),CompositeBuild.class);
					if(parent!=null) {
						parent.removeSubBuild(aBuild);
					}
				}
			}
		);
	}

	public void createExecution(CreateExecutionCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		URI buildId=aCommand.buildId();
		URI executionId=aCommand.executionId();
		Date createdOn=aCommand.createdOn();
		Build build = buildRepository().buildOfId(buildId);
		checkArgument(build!=null,"Build '%s' is not registered",buildId);
		checkState(!build.executions().contains(executionId),"An execution '%s' is already registered in build '%s'",buildId,executionId);
		Execution execution = build.addExecution(executionId, createdOn);
		executionRepository().add(execution);
	}

	public void finishExecution(FinishExecutionCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		URI executionId=aCommand.executionId();
		Execution execution = executionRepository().executionOfId(executionId);
		checkArgument(execution!=null,"Execution '%s' is not registered",executionId);
		checkState(!execution.isFinished(),"Execution '%s' is already finished",executionId);
		Result result=new Result(aCommand.status(),aCommand.finishedOn());
		execution.finish(result);
	}

	public void deleteExecution(DeleteExecutionCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		URI executionId=aCommand.executionId();
		Execution execution = executionRepository().executionOfId(executionId);
		checkArgument(execution!=null,"Execution '%s' is not registered",executionId);
		Build build = buildRepository().buildOfId(execution.buildId());
		checkArgument(build!=null,"Build '%s' is not registered",build.buildId());
		build.removeExecution(execution);
		executionRepository().remove(execution);
	}

}
