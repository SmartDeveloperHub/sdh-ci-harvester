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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:0.2.0
 *   Bundle      : ci-backend-cli-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.spi.ComponentRegistry;
import org.smartdeveloperhub.harvesters.ci.backend.spi.RuntimeDelegate;
import org.smartdeveloperhub.util.bootstrap.AbstractBootstrap;
import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.Service;

public class BackendDump extends AbstractBootstrap<BackendConfig> {

	private static final Logger LOGGER=LoggerFactory.getLogger(BackendDump.class);

	private static final String NAME = "BackendDump";

	private ComponentRegistry registry;

	public BackendDump() {
		super(NAME,BackendConfig.class);
	}

	public static void main(String[] args) throws Exception {
		BackendDump bs = new BackendDump();
		bs.run(args);
		bs.terminate();
	}

	@Override
	protected void shutdown() {
		LOGGER.info("Shutting down application...");
		try {
			this.registry.close();
		} catch (Exception e) {
			LOGGER.warn("Failed to shutdown the application. Full stacktrace follows",e);
		}
	}

	@Override
	protected Iterable<Service> getServices(BackendConfig config) {
		Consoles.
			defaultConsole().
				printf("Dumping data stored in '%s'%n",config.getDatabase().getLocation());
		this.registry = RuntimeDelegate.getInstance().getComponentRegistry(config.getDatabase());
		return Collections.<Service>singleton(new BackendDumpService(registry));
	}

}