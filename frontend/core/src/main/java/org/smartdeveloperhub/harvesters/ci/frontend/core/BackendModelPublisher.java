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

import org.ldp4j.application.session.ContainerSnapshot;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.service.ServiceHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;

final class BackendModelPublisher {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendModelPublisher.class);

	private final WriteSession session;

	private BackendModelPublisher(WriteSession session) {
		this.session = session;
	}

	void publish(Service service) {
		ResourceSnapshot serviceSnapshot=
			this.session.
				find(
					ResourceSnapshot.class,
					IdentityUtil.name(service),
					ServiceHandler.class);
		serviceSnapshot.
			createAttachedResource(
				ContainerSnapshot.class,
				ServiceHandler.SERVICE_BUILDS,
				IdentityUtil.name(service),
				BuildContainerHandler.class);
		LOGGER.debug("Published build container for service {}",service.serviceId());
	}

	void publish(Build build) {
		ContainerSnapshot buildContainerSnapshot=
			this.session.
				find(
					ContainerSnapshot.class,
					IdentityUtil.buildContainer(build),
					BuildContainerHandler.class);
		ResourceSnapshot buildSnapshot=
			buildContainerSnapshot.
				addMember(IdentityUtil.name(build));
		LOGGER.debug("Published resource for build {} ({})",build.buildId(),build);
		buildSnapshot.
			createAttachedResource(
				ContainerSnapshot.class,
				BuildHandler.BUILD_EXECUTIONS,
				IdentityUtil.name(build),
				ExecutionContainerHandler.class);
		LOGGER.debug("Published execution container for build {}",build.buildId());
		if(build instanceof CompositeBuild) {
			buildSnapshot.
				createAttachedResource(
					ContainerSnapshot.class,
					BuildHandler.BUILD_SUB_BUILDS,
					IdentityUtil.name(build),
					BuildContainerHandler.class);
			LOGGER.debug("Published sub-build container for composite build {}",build.buildId());
		}
	}

	void publish(Execution execution) {
		ContainerSnapshot executionContainerSnapshot=
			this.session.
				find(
					ContainerSnapshot.class,
					IdentityUtil.executionContainer(execution),
					ExecutionContainerHandler.class);
		executionContainerSnapshot.
			addMember(IdentityUtil.name(execution));
		LOGGER.debug("Published resource for execution {} ({})",execution.executionId(),execution);
	}

	static BackendModelPublisher newInstance(WriteSession session) {
		return new BackendModelPublisher(session);
	}

}
