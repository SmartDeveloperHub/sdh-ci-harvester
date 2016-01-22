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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.UpdateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.BuildRepository;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ExecutionRepository;
import org.smartdeveloperhub.harvesters.ci.backend.domain.persistence.ServiceRepository;

public final class ContinuousIntegrationService {

	private final class BuildDeleter extends BuildVisitor {

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
			CompositeBuild parent=
				buildRepository().
					buildOfId(aBuild.parentId(),CompositeBuild.class);
			if(parent!=null) {
				parent.removeSubBuild(aBuild);
			}
		}
	}

	private static final String EXECUTION_IS_NOT_REGISTERED = "Execution '%s' is not registered";
	private static final String BUILD_IS_NOT_REGISTERED     = "Build '%s' is not registered";
	private static final String COMMAND_CANNOT_BE_NULL      = "Command cannot be null";

	private ServiceRepository serviceRepository;
	private BuildRepository buildRepository;
	private ExecutionRepository executionRepository;

	public ContinuousIntegrationService(ServiceRepository serviceRepository, BuildRepository buildRepository, ExecutionRepository executionRepository) {
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

	private Service findService(CreateBuildCommand aCommand) {
		URI serviceId=aCommand.serviceId();
		Service service = serviceRepository().serviceOfId(serviceId);
		checkArgument(service!=null,"Service '%s' is not registered",serviceId);
		return service;
	}

	public List<URI> getRegisteredServices() {
		return serviceRepository().serviceIds();
	}

	public Service getService(URI serviceId) {
		checkNotNull(serviceId,"Service identifier be null");
		return Service.newInstance(serviceRepository().serviceOfId(serviceId));
	}

	public Build getBuild(URI buildId) {
		checkNotNull(buildId,"Build identifier cannot be null");
		Build build = buildRepository().buildOfId(buildId);
		return Build.newInstance(build);
	}

	public Execution getExecution(URI executionId) {
		checkNotNull(executionId,"Execution identifier cannot be null");
		Execution execution = executionRepository().executionOfId(executionId);
		return Execution.newInstance(execution);
	}

	public void registerService(RegisterServiceCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI serviceId=aCommand.serviceId();

		Service service = serviceRepository().serviceOfId(serviceId);
		checkArgument(service==null,"Service '%s' is already registered",serviceId);

		service=Service.newInstance(serviceId);
		serviceRepository().add(service);
	}

	public void createBuild(CreateBuildCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI buildId=aCommand.buildId();
		Build build=null;
		if(aCommand.simple()) {
			Service service = findService(aCommand);
			build=service.addSimpleBuild(buildId,aCommand.title());
		} else if(aCommand.composite()) {
			Service service = findService(aCommand);
			build=service.addCompositeBuild(buildId,aCommand.title());
		} else {
			URI parentId = aCommand.subBuildOf();
			Build parent = buildRepository().buildOfId(parentId);
			checkArgument(parent instanceof CompositeBuild,"Parent build '%s' is not composite",parentId);
			CompositeBuild cb=(CompositeBuild)parent;
			build=cb.addSubBuild(buildId, aCommand.title());
		}
		build.setDescription(aCommand.description());
		build.setCreatedOn(aCommand.createdOn());
		buildRepository().add(build);
	}

	public void updateBuild(UpdateBuildCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI buildId=aCommand.buildId();
		Build build = buildRepository().buildOfId(buildId);
		checkArgument(build!=null,BUILD_IS_NOT_REGISTERED,buildId);
		build.setTitle(aCommand.title());
		build.setCodebase(new Codebase(aCommand.codebase(),aCommand.branchName()));
		build.setDescription(aCommand.description());
		build.setCreatedOn(aCommand.createdOn());
	}

	public void deleteBuild(DeleteBuildCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI buildId=aCommand.buildId();
		Build build = buildRepository().buildOfId(buildId);
		checkArgument(build!=null,BUILD_IS_NOT_REGISTERED,buildId);
		build.accept(new BuildDeleter());
	}

	public void createExecution(CreateExecutionCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI buildId=aCommand.buildId();
		URI executionId=aCommand.executionId();
		Date createdOn=aCommand.createdOn();
		Build build = buildRepository().buildOfId(buildId);
		checkArgument(build!=null,BUILD_IS_NOT_REGISTERED,buildId);
		checkState(!build.executions().contains(executionId),"Execution '%s' is already registered in build '%s'",executionId,buildId);
		String commitId = aCommand.commitId();
		Codebase codebase = new Codebase(aCommand.codebase(),aCommand.branchName());
		Execution execution=build.addExecution(executionId, createdOn,codebase,commitId);
		if(aCommand.status()!=null && aCommand.finishedOn()!=null) {
			Result result=new Result(aCommand.status(),aCommand.finishedOn());
			execution.finish(result);
		}
		executionRepository().add(execution);
	}

	public void finishExecution(FinishExecutionCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI executionId=aCommand.executionId();
		Execution execution = executionRepository().executionOfId(executionId);
		checkArgument(execution!=null,EXECUTION_IS_NOT_REGISTERED,executionId);
		checkState(!execution.isFinished(),"Execution '%s' is already finished",executionId);
		Result result=new Result(aCommand.status(),aCommand.finishedOn());
		execution.finish(result);
	}

	public void deleteExecution(DeleteExecutionCommand aCommand) {
		checkNotNull(aCommand,COMMAND_CANNOT_BE_NULL);
		URI executionId=aCommand.executionId();
		Execution execution = executionRepository().executionOfId(executionId);
		checkArgument(execution!=null,EXECUTION_IS_NOT_REGISTERED,executionId);
		Build build = buildRepository().buildOfId(execution.buildId());
		checkArgument(build!=null,BUILD_IS_NOT_REGISTERED,build.buildId());
		build.removeExecution(execution);
		executionRepository().remove(execution);
	}

}