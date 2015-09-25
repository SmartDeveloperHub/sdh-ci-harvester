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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;

import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.SubBuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class BackendModelPublisher {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendModelPublisher.class);

	static final class Builder {

		private EntityIndex index;
		private URI serviceId;

		Builder withBackendService(EntityIndex index) {
			this.index = index;
			return this;
		}

		Builder withMainService(URI serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		BackendModelPublisher build() {
			return new BackendModelPublisher(this.index,this.serviceId);
		}
	}

	private final EntityIndex index;

	private final URI serviceId;

	private BackendModelPublisher(EntityIndex index, URI serviceId) {
		this.index = index;
		this.serviceId = serviceId;
	}

	private ContainerSnapshot findBuildContainer(WriteSession session, Build build) {
		ContainerSnapshot buildContainerSnapshot=null;
		if(build instanceof SubBuild) {
			buildContainerSnapshot=
				session.
					find(
						ContainerSnapshot.class,
						IdentityUtil.parentBuildContainerName((SubBuild)build),
						SubBuildContainerHandler.class);
		} else {
			buildContainerSnapshot=
				session.
					find(
						ContainerSnapshot.class,
						IdentityUtil.buildContainerName(build),
						BuildContainerHandler.class);
		}
		return buildContainerSnapshot;
	}

	private void publishSubBuilds(WriteSession session, CompositeBuild compositeBuild) {
		for(URI subBuildId:compositeBuild.subBuilds()) {
			publishBuild(session,subBuildId);
		}
	}

	private Build publishBuild(WriteSession session, URI buildId) {
		Build build = this.index.findBuild(buildId);
		if(build!=null) {
			publish(session,build);
			for(URI executionId:build.executions()) {
				publishExecution(session, executionId);
			}
		}
		return build;
	}

	private void publishExecution(WriteSession session, URI executionId) {
		Execution execution = this.index.findExecution(executionId);
		if(execution!=null) {
			publish(session,execution);
		}
	}

	void publish(WriteSession session, Service service) {
		ResourceSnapshot serviceSnapshot=
			session.
				find(
					ResourceSnapshot.class,
					IdentityUtil.serviceName(service),
					ServiceHandler.class);
		serviceSnapshot.
			createAttachedResource(
				ContainerSnapshot.class,
				ServiceHandler.SERVICE_BUILDS,
				IdentityUtil.serviceName(service),
				BuildContainerHandler.class);
		LOGGER.debug("Published build container for service {}",service.serviceId());
	}

	void publish(WriteSession session, Build build) {
		ContainerSnapshot buildContainerSnapshot=findBuildContainer(session,build);
		if(buildContainerSnapshot==null) {
			LOGGER.warn("Cannot publish orphan execution build {} ({})",build.buildId(),build instanceof SubBuild?((SubBuild)build).parentId():build.serviceId());
			return;
		}
		ResourceSnapshot buildSnapshot=
			buildContainerSnapshot.
				addMember(IdentityUtil.buildName(build));
		LOGGER.debug("Published resource for build {} @ {} ({})",build.buildId(),buildContainerSnapshot.name(),buildContainerSnapshot.templateId());
		buildSnapshot.
			createAttachedResource(
				ContainerSnapshot.class,
				BuildHandler.BUILD_EXECUTIONS,
				IdentityUtil.buildName(build),
				ExecutionContainerHandler.class);
		LOGGER.debug("Published execution container for build {}",build.buildId());
		if(build instanceof CompositeBuild) {
			buildSnapshot.
				createAttachedResource(
					ContainerSnapshot.class,
					BuildHandler.BUILD_SUB_BUILDS,
					IdentityUtil.buildName(build),
					SubBuildContainerHandler.class);
			LOGGER.debug("Published sub-build container for composite build {}",build.buildId());
		}
	}

	void publish(WriteSession session, Execution execution) {
		ContainerSnapshot executionContainerSnapshot=
			session.
				find(
					ContainerSnapshot.class,
					IdentityUtil.executionContainerName(execution),
					ExecutionContainerHandler.class);
		if(executionContainerSnapshot==null) {
			LOGGER.warn("Cannot publish orphan execution execution {} ({})",execution.executionId(),execution.buildId());
			return;
		}
		executionContainerSnapshot.addMember(IdentityUtil.executionName(execution));
		LOGGER.debug("Published resource for execution {} ({})",execution.executionId(),execution);
	}

	void publish(WriteSession session) {
		Service targetService=this.index.findService(this.serviceId);
		if(targetService==null) {
			LOGGER.warn("Nothing to publish. Service {} not found.",this.serviceId);
			return;
		}
		publish(session,targetService);
		for(URI buildId:targetService.builds()) {
			Build build=publishBuild(session, buildId);
			if(build instanceof CompositeBuild) {
				publishSubBuilds(session, (CompositeBuild)build);
			}
		}

	}

	static Builder builder() {
		return new Builder();
	}

}
