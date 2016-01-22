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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.domain;

import java.net.URI;

public final class SimpleBuild extends Build {

	private static final int PRIME = 19;

	SimpleBuild() {
	}

	private SimpleBuild(SimpleBuild build) {
		super(build.serviceId(),build.buildId(),build.title());
	}

	SimpleBuild(URI serviceId, URI buildId, String title) {
		super(serviceId,buildId,title);
	}

	@Override
	Build makeClone() {
		return new SimpleBuild(this);
	}

	@Override
	public void accept(BuildVisitor visitor) {
		visitor.visitSimpleBuild(this);
	}

	@Override
	public int hashCode() {
		return super.hashCode()+PRIME*SimpleBuild.class.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && SimpleBuild.class.isInstance(obj);
	}

}
