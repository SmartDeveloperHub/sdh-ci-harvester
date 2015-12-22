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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.service;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.ext.ResourceHandler;
import org.ldp4j.application.ext.UnknownResourceException;
import org.ldp4j.application.ext.annotations.Attachment;
import org.ldp4j.application.ext.annotations.Resource;
import org.ldp4j.application.session.ResourceSnapshot;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildContainerHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.Serviceable;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;

@Resource(
	id=ServiceHandler.ID,
	attachments={
		@Attachment(
			id=ServiceHandler.SERVICE_BUILDS,
			path="builds/",
			handler=BuildContainerHandler.class
		)
	}
)
public class ServiceHandler extends Serviceable implements ResourceHandler {

	public static final String ID="ServiceHandler";

	public static final String SERVICE_BUILDS="ServiceBuilds";

	public ServiceHandler(BackendController controller) {
		super(controller);
	}

	private Service findService(URI id) throws UnknownResourceException {
		Service service=entityIndex().findService(id);
		if(service==null) {
			super.unknownResource(id,"Service");
		}
		return service;
	}

	@Override
	public DataSet get(ResourceSnapshot resource) throws UnknownResourceException {
		URI serviceId = IdentityUtil.serviceId(resource);
		trace("Requested service %s retrieval",serviceId);
		Service service = findService(serviceId);
		info("Retrieved service %s: %s",serviceId,service);
		return ServiceMapper.toDataSet(service);
	}

}
