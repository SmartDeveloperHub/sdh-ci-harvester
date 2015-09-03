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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.1.0
 *   Bundle      : ci-frontend-core-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.build;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.annotations.Attachment;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ResourceSnapshot;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.Serviceable;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;

@Resource(
	id=BuildHandler.ID,
	attachments={
		@Attachment(
			id=BuildHandler.BUILD_EXECUTIONS,
			path="executions/",
			handler=ExecutionContainerHandler.class
		),
		@Attachment(
			id=BuildHandler.BUILD_SUB_BUILDS,
			path="child/",
			handler=SubBuildContainerHandler.class
		)
	}
)
public class BuildHandler extends Serviceable implements ResourceHandler {

	public static final String ID="BuildHandler";

	public static final String BUILD_EXECUTIONS="BuildExecutions";
	public static final String BUILD_SUB_BUILDS="BuildSubBuilds";

	public BuildHandler(BackendController controller) {
		super(controller);
	}

	private Build findBuild(URI id) throws UnknownResourceException {
		Build build=entityIndex().findBuild(id);
		if(build==null) {
			super.unknownResource(id,"Build");
		}
		return build;
	}

	@Override
	public DataSet get(ResourceSnapshot resource) throws UnknownResourceException {
		URI buildId = IdentityUtil.buildId(resource);
		trace("Requested build %s retrieval",buildId);
		Build build = findBuild(buildId);
		info("Retrieved build %s: %s",buildId,build);
		return BuildMapper.toDataSet(build);
	}

}
