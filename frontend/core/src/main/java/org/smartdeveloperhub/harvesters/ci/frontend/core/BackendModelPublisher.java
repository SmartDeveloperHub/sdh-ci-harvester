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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-1.0.0-SNAPSHOT.jar
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
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.SubBuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;

final class BackendModelPublisher {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendModelPublisher.class);

	static final class Builder {

		private ContinuousIntegrationService service;
		private URI serviceId;

		Builder withBackendService(ContinuousIntegrationService service) {
			this.service = service;
			return this;
		}

		Builder withMainService(URI serviceId) {
			this.serviceId = serviceId;
			return this;
		}

		BackendModelPublisher build() {
			return new BackendModelPublisher(this.service,this.serviceId);
		}
	}

	private final ContinuousIntegrationService service;

	private final URI serviceId;

	private BackendModelPublisher(ContinuousIntegrationService service, URI serviceId) {
		this.service = service;
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
		executionContainerSnapshot.addMember(IdentityUtil.executionName(execution));
		LOGGER.debug("Published resource for execution {} ({})",execution.executionId(),execution);
	}

	private void publishSubBuilds(WriteSession session, CompositeBuild compositeBuild) {
		for(URI subBuildId:compositeBuild.subBuilds()) {
			publishBuild(session,subBuildId);
		}
	}

	private Build publishBuild(WriteSession session, URI buildId) {
		Build build = this.service.getBuild(buildId);
		publish(session,build);
		for(URI executionId:build.executions()) {
			Execution execution = this.service.getExecution(executionId);
			publish(session,execution);
		}
		return build;
	}

	void publish(WriteSession session) {
		Service targetService=this.service.getService(this.serviceId);
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
