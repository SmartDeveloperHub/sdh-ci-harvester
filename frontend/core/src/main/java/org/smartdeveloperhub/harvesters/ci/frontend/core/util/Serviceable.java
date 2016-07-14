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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.3.0
 *   Bundle      : ci-frontend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.util;

import org.ldp4j.application.ext.ApplicationRuntimeException;
import org.ldp4j.application.ext.UnknownResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.BackendController;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;


public abstract class Serviceable {

	private final Logger logger; // NOSONAR

	private final BackendController controller;

	public Serviceable(BackendController controller) {
		this.controller = controller;
		this.logger=LoggerFactory.getLogger(getClass());
	}

	protected final EntityIndex entityIndex() {
		return this.controller.entityIndex();
	}

	protected final String trace(String message, Object... arguments) {
		String result = String.format(message,arguments);
		this.logger.trace(result);
		return result;
	}

	protected final String debug(String message, Object... arguments) {
		String result = String.format(message,arguments);
		this.logger.debug(result);
		return result;
	}

	protected final String info(String message, Object... arguments) {
		String result = String.format(message,arguments);
		this.logger.info(result);
		return result;
	}

	protected final ApplicationRuntimeException unexpectedFailure(Throwable failure, String message, Object... args) {
		String result = String.format(message,args);
		this.logger.error(result.concat(". Full stacktrace follows"),failure);
		String errorMessage=result;
		return new ApplicationRuntimeException(errorMessage,failure);
	}

	protected final ApplicationRuntimeException unexpectedFailure(String message, Object... args) {
		String result = String.format(message,args);
		this.logger.error(result);
		return new ApplicationRuntimeException(result);
	}

	protected final UnknownResourceException unknownResource(Object resourceId, String resourceType) {
		String errorMessage = String.format("Could not find %s resource for %s",resourceType,resourceId);
		this.logger.error(errorMessage);
		return new UnknownResourceException(errorMessage);
	}

}
