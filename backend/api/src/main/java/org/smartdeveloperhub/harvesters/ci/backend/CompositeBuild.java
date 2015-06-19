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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;

public final class CompositeBuild extends Build {

	private static final int PRIME = 17;

	private List<URI> subBuilds;

	CompositeBuild() {
	}

	private CompositeBuild(URI serviceId,URI buildId,String title, List<URI> subBuilds) {
		super(serviceId,buildId,title);
		setSubBuilds(Lists.newArrayList(subBuilds));
	}

	private CompositeBuild(CompositeBuild build) {
		this(build.serviceId(),build.buildId(),build.title(),build.subBuilds());
	}

	CompositeBuild(URI serviceId,URI buildId,String title) {
		this(serviceId,buildId,title,Collections.<URI>emptyList());
	}

	@Override
	Build makeClone() {
		return new CompositeBuild(this);
	}

	protected void setSubBuilds(List<URI> subBuilds) {
		checkNotNull(subBuilds,"Sub builds cannot be null");
		this.subBuilds=subBuilds;
	}

	public SubBuild addSubBuild(URI buildId, String title) {
		SubBuild childBuild=new SubBuild(this,buildId,title);
		this.subBuilds().add(childBuild.buildId());
		return childBuild;
	}

	public void removeSubBuild(SubBuild subBuild) {
		checkNotNull(subBuild,"Sub build cannot be null");
		this.subBuilds().remove(subBuild.buildId());
	}

	public List<URI> subBuilds() {
		return this.subBuilds;
	}

	@Override
	public void accept(BuildVisitor visitor) {
		visitor.visitCompositeBuild(this);
	}

	@Override
	public int hashCode() {
		return super.hashCode()+PRIME*CompositeBuild.class.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) && CompositeBuild.class.isInstance(obj);
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.add("subBuilds",this.subBuilds);
	}

}