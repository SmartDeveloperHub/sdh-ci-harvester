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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:0.3.0
 *   Bundle      : ci-util-bootstrap-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import java.lang.ref.WeakReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class BootstrapCleaner extends Thread {

	private static final Logger LOGGER=LoggerFactory.getLogger(BootstrapCleaner.class);

	private final String bootstrapId;

	private final WeakReference<AbstractBootstrap<?>> reference;

	BootstrapCleaner(String bootstrapId, AbstractBootstrap<?> bootstrap) {
		super("BootstrapCleaner-"+bootstrapId);
		this.bootstrapId = bootstrapId;
		this.reference=new WeakReference<AbstractBootstrap<?>>(bootstrap);
	}

	@Override
	public void run() {
		AbstractBootstrap<?> bootstrap = this.reference.get();
		if(bootstrap!=null) {
			tryTerminate(bootstrap);
		}
	}

	private void tryTerminate(AbstractBootstrap<?> bootstrap) {
		LOGGER.info("{} has not been disposed yet...",this.bootstrapId);
		try {
			bootstrap.terminate();
			LOGGER.info("{} disposed.",this.bootstrapId);
		} catch (Exception e) {
			LOGGER.error(this.bootstrapId+" termination while disposal failed. Full stacktrace follows:",e);
		}
	}

	static BootstrapCleaner newInstance(AbstractBootstrap<?> bootstrap) {
		return new BootstrapCleaner(bootstrap.id(),bootstrap);
	}
}