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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:0.4.0-SNAPSHOT
 *   Bundle      : ci-util-bootstrap-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import java.util.IdentityHashMap;
import java.util.Map;

final class BootstrapTracker {

	private static final BootstrapTracker SINGLETON=new BootstrapTracker();

	private final Map<Class<?>,ApplicationInstanceTracker> references;

	private BootstrapTracker() {
		this.references=new IdentityHashMap<Class<?>,ApplicationInstanceTracker>();
	}

	static String track(Class<?> bootstrapClass, String applicationName) {
		return
			BootstrapTracker.
				SINGLETON.
					getTracker(bootstrapClass).
						track(applicationName);
	}

	private synchronized ApplicationInstanceTracker getTracker(Class<?> bootstrapClass) {
		ApplicationInstanceTracker tracker=this.references.get(bootstrapClass);
		if(tracker==null) {
			tracker=ApplicationInstanceTracker.newInstance(bootstrapClass);
			this.references.put(bootstrapClass,tracker);
		}
		return tracker;
	}

}
