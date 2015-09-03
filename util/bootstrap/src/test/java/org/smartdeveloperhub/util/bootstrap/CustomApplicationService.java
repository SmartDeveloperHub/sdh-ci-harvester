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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:0.1.0
 *   Bundle      : ci-util-bootstrap-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import java.util.concurrent.TimeUnit;

import org.smartdeveloperhub.util.console.Consoles;

import com.google.common.util.concurrent.AbstractIdleService;

class CustomApplicationService extends AbstractIdleService {

	private CustomConfig config;

	void setConfiguration(CustomConfig config) {
		this.config = config;
	}

	CustomConfig config() {
		return this.config;
	}

	@Override
	protected void startUp() {
		if(this.config.isDelayInitialization()) {
			try {
				TimeUnit.SECONDS.sleep(25);
			} catch (InterruptedException e) {
				// Nothing to do
			}
		}
		if(this.config.isFailServiceStartUp()) {
			throw new IllegalStateException(CustomConfig.FAILED_SERVICE_START_UP_MESSAGE);
		}
		Consoles.defaultConsole().printf("{%s} Hello!%n",this.config.getSetting());
	}

	@Override
	protected void shutDown() {
		if(this.config.isFailServiceShutdown()) {
			throw new IllegalStateException(CustomConfig.FAILED_SERVICE_SHUTDOWN_MESSAGE);
		}
		Consoles.defaultConsole().printf("{%s} Goodbye!%n",this.config.getSetting());
	}

}