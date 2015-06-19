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

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

public class ApplicationShutdownException extends BootstrapException {

	/**
	 *
	 */
	private static final long serialVersionUID = 4908753969194340357L;

	private final Map<String, Throwable> failures;

	public ApplicationShutdownException(String message, Throwable cause, Map<String,Throwable> failures) {
		super(message,cause);
		this.failures=failures;
	}

	public ApplicationShutdownException(String message, Map<String,Throwable> failures) {
		this(message,null,failures);
	}

	public ApplicationShutdownException(String message, Throwable cause) {
		this(message,cause,Maps.<String,Throwable>newLinkedHashMap());
	}

	public ApplicationShutdownException(String message) {
		this(message,(Throwable)null);
	}

	public Set<String> failedServices() {
		return ImmutableSet.copyOf(this.failures.keySet());
	}

	public Throwable serviceFailure(String service) {
		return this.failures.get(service);
	}

}
