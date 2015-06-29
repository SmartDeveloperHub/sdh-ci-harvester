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
package org.smartdeveloperhub.harvesters.ci.frontend.core.execution;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ResourceSnapshot;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.Serviceable;

@Resource(
	id=ExecutionHandler.ID
)
public class ExecutionHandler extends Serviceable implements ResourceHandler {

	public static final String ID="ExecutionHandler";

	public ExecutionHandler(ContinuousIntegrationService service) {
		super(service);
	}

	private Execution findExecution(URI id) throws UnknownResourceException {
		Execution execution=
			continuousIntegrationService().getExecution(id);
		if(execution==null) {
			super.unknownResource(id,"Execution");
		}
		return execution;
	}

	@Override
	public DataSet get(ResourceSnapshot resource) throws UnknownResourceException {
		URI executionId = IdentityUtil.executionId(resource);
		trace("Requested execution %s retrieval",executionId);
		Execution execution = findExecution(executionId);
		info("Retrieved execution %s: %s",executionId,execution);
		return ExecutionMapper.toDataSet(execution);
	}

}
