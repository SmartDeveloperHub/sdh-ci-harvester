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

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;

final class ApplicationInstanceTracker {

	private final String className;
	private final ConcurrentMap<String,AtomicLong> instances;

	private ApplicationInstanceTracker(Class<?> clazz) {
		this.className=clazz.getCanonicalName();
		this.instances=Maps.newConcurrentMap();
	}

	String track(String applicationName) {
		AtomicLong counter = getCounter(applicationName);
		return generateId(applicationName, counter);
	}

	private String generateId(String applicationName, AtomicLong counter) {
		return applicationName+"{"+counter.incrementAndGet()+"}";
	}

	private AtomicLong getCounter(String applicationName) {
		AtomicLong tmp=new AtomicLong();
		AtomicLong counter=this.instances.putIfAbsent(applicationName, tmp);
		if(counter==null) {
			counter=tmp;
		}
		return counter;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("className",this.className).
					add("instances",this.instances).
					toString();
	}

	static ApplicationInstanceTracker newInstance(Class<?> clazz) {
		return new ApplicationInstanceTracker(clazz);
	}

}