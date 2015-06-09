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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.Lists;

public abstract class Build {

	private URI serviceId;
	private URI buildId;
	private URI location;
	private URI codebase;
	private List<URI> executions;

	Build() {
	}

	Build(URI serviceId, URI buildId) {
		setServiceId(serviceId);
		setBuildId(buildId);
		setExecutions(Lists.<URI>newArrayList());
	}

	protected void setServiceId(URI serviceId) {
		checkNotNull(serviceId,"Service identifier cannot be null");
		this.serviceId = serviceId;
	}

	protected void setBuildId(URI buildId) {
		checkNotNull(buildId,"Build identifier cannot be null");
		this.buildId = buildId;
	}

	protected void setExecutions(List<URI> executions) {
		checkNotNull(executions,"Executions cannot be null");
		this.executions=executions;
	}

	protected void setLocation(URI location) {
		checkNotNull(location,"Build location cannot be null");
		this.location=location;
	}

	protected void setCodebase(URI codebase) {
		checkNotNull(location,"Build codebase cannot be null");
		this.codebase=codebase;
	}

	protected void toString(ToStringHelper helper) {

	}

	public URI serviceId() {
		return this.serviceId;
	}

	public URI buildId() {
		return this.buildId;
	}

	public URI location() {
		return this.location;
	}

	public URI codebase() {
		return this.codebase;
	}

	public Execution addExecution(URI executionId, Date createdOn) {
		checkArgument(!executions().contains(executionId),"An execution with id '%s' already exists",executionId);
		Execution execution=new Execution(buildId(), executionId, createdOn);
		this.executions().add(execution.executionId());
		return execution;
	}

	public void removeExecution(Execution execution) {
		checkArgument(execution.buildId().equals(this.buildId),"Execution does not belong to build");
		this.executions().add(execution.executionId());
	}

	public List<URI> executions() {
		return this.executions;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.buildId);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Build) {
			Build that=(Build)obj;
			result=
				this.getClass()==that.getClass() &&
				Objects.equal(this.buildId,that.buildId);
		}
		return result;
	}

	@Override
	public String toString() {
		ToStringHelper helper =
			Objects.
				toStringHelper(getClass()).
					omitNullValues().
						add("serviceId",this.serviceId).
						add("buildId",this.serviceId).
						add("location",this.serviceId).
						add("codebase",this.serviceId).
						add("executions",this.executions);
		toString(helper);
		return helper.toString();
	}

	public abstract void accept(BuildVisitor visitor);

}
