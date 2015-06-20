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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:1.0.0-SNAPSHOT
 *   Bundle      : ci-util-bootstrap-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import java.util.Collections;

import com.google.common.util.concurrent.Service;

final class CustomBootstrap extends AbstractBootstrap<CustomConfig> {

	static final String NAME = "CustomApplication";;

	private CustomApplicationService service;

	public CustomBootstrap() {
		super(NAME,CustomConfig.class);
	}

	public static void main(String[] args) throws Exception {
		CustomBootstrap bs = new CustomBootstrap();
		bs.run(args);
	}

	@Override
	protected void shutdown() {
		if(this.service.config().isFailBootstrapShutdown()) {
			throw new IllegalStateException(CustomConfig.FAILED_BOOTSTRAP_SHUTDOWN_MESSAGE);
		}
	}

	@Override
	protected Iterable<Service> getServices(CustomConfig config) {
		if(config.isFailBootstrapStartUp()) {
			throw new IllegalStateException(CustomConfig.FAILED_BOOTSTRAP_START_UP_MESSAGE);
		}
		this.service=new CustomApplicationService();
		this.service.setConfiguration(config);
		return Collections.<Service>singleton(this.service);
	}

	CustomApplicationService service() {
		return this.service;
	}

}