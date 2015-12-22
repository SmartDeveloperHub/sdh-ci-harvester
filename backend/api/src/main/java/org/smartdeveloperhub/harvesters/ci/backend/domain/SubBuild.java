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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class SubBuild extends Build {

	private static final int PRIME = 13;

	private URI parentId;

	SubBuild() {
	}

	private SubBuild(URI serviceId, URI parentId, URI buildId, String title) {
		super(serviceId,buildId,title);
		setParentId(parentId);
	}

	private SubBuild(SubBuild build) {
		this(build.serviceId(),build.parentId(),build.buildId(),build.title());
	}

	SubBuild(CompositeBuild parent, URI buildId,String title) {
		this(parent.serviceId(),parent.buildId(),buildId,title);
	}

	protected void setParentId(URI parentId) {
		checkNotNull(parentId,"Parent identifier cannot be null");
		this.parentId = parentId;
	}

	@Override
	Build makeClone() {
		return new SubBuild(this);
	}

	public URI parentId() {
		return this.parentId;
	}

	@Override
	public void accept(BuildVisitor visitor) {
		visitor.visitSubBuild(this);
	}


	@Override
	public int hashCode() {
		return super.hashCode()+PRIME*SubBuild.class.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && SubBuild.class.isInstance(obj);
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.add("parentId",this.parentId);
	}

}