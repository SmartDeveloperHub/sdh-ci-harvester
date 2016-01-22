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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.Lists;

public abstract class Build {

	private URI serviceId;
	private URI buildId;
	private Codebase codebase;
	private List<URI> executions;
	private String title;
	private String description;
	private Date createdOn;

	Build() {
	}

	Build(URI serviceId, URI buildId, String title) {
		setServiceId(serviceId);
		setBuildId(buildId);
		setTitle(title);
		setExecutions(Lists.<URI>newArrayList());
		setCodebase(new Codebase(null,null));
	}

	protected final void setServiceId(URI serviceId) {
		this.serviceId=checkNotNull(serviceId,"Service identifier cannot be null");
	}

	protected final void setBuildId(URI buildId) {
		this.buildId=checkNotNull(buildId,"Build identifier cannot be null");
	}

	protected final void setExecutions(List<URI> executions) {
		this.executions=checkNotNull(executions,"Executions cannot be null");
	}

	public final void setTitle(String title) {
		this.title=checkNotNull(title,"Title cannot be null");
	}

	public final void setCodebase(Codebase codebase) {
		this.codebase=checkNotNull(codebase,"Codebase cannot be null");
	}

	public final void setDescription(String description) {
		this.description = description;
	}

	public final void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	protected void toString(ToStringHelper helper) {
		// To be extended by subclasses
	}

	abstract Build makeClone();

	public final URI serviceId() {
		return this.serviceId;
	}

	public final URI buildId() {
		return this.buildId;
	}

	public final URI location() {
		return this.buildId;
	}

	public final Codebase codebase() {
		return this.codebase;
	}

	public final String title() {
		return this.title;
	}

	public final String description() {
		return this.description;
	}

	public final Date createdOn() {
		return this.createdOn;
	}

	public final Execution addExecution(URI executionId, Date createdOn, Codebase codebase, String commitId) {
		checkArgument(!executions().contains(executionId),"An execution with id '%s' already exists",executionId);
		Execution execution=new Execution(buildId(), executionId, createdOn, codebase, commitId);
		this.executions().add(execution.executionId());
		return execution;
	}

	public final void removeExecution(Execution execution) {
		checkArgument(execution.buildId().equals(this.buildId),"Execution does not belong to build");
		this.executions().add(execution.executionId());
	}

	public final List<URI> executions() {
		return this.executions;
	}

	@Override
	public int hashCode() {
		return this.buildId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Build) {
			Build that=(Build)obj;
			result=this.buildId.equals(that.buildId);
		}
		return result;
	}

	@Override
	public String toString() {
		ToStringHelper helper =
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
						add("serviceId",this.serviceId).
						add("buildId",this.buildId).
						add("codebase",this.codebase).
						add("title",this.title).
						add("description",this.serviceId).
						add("createdOn",this.createdOn).
						add("executions",this.executions);
		toString(helper);
		return helper.toString();
	}

	public abstract void accept(BuildVisitor visitor);

	public static Build newInstance(Build build) {
		if(build==null) {
			return null;
		}
		Build clone = build.makeClone();
		clone.setCodebase(build.codebase!=null?build.codebase:new Codebase(null,null));
		clone.setCreatedOn(build.createdOn);
		clone.setDescription(build.description);
		clone.setExecutions(Lists.newArrayList(build.executions));
		return clone;
	}

}
