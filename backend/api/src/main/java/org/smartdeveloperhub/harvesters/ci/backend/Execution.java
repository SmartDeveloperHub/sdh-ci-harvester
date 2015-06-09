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
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;

import com.google.common.base.MoreObjects;

public final class Execution {

	private URI buildId;
	private URI executionId;
	private Date createdOn;
	private Result result;

	Execution() {

	}

	public Execution(URI buildId, URI executionId, Date createdOn) {
		setBuildId(buildId);
		setExecutionId(executionId);
		setCreatedOn(createdOn);
	}

	protected void setCreatedOn(Date createdOn) {
		checkNotNull(createdOn,"Created on date cannot be null");
		this.createdOn = createdOn;
	}

	protected void setExecutionId(URI executionId) {
		checkNotNull(executionId,"Execution identifier cannot be null");
		this.executionId = executionId;
	}

	protected void setBuildId(URI buildId) {
		checkNotNull(buildId,"Build identifier cannot be null");
		this.buildId = buildId;
	}

	public URI buildId() {
		return this.buildId;
	}

	public URI executionId() {
		return this.executionId;
	}

	public Date createdOn() {
		return this.createdOn;
	}

	public Result result() {
		return this.result;
	}

	public boolean isFinished() {
		return this.result!=null;
	}

	public void finish(Result result) {
		checkNotNull(result,"Result cannot be null");
		checkState(this.result==null,"Execution already finished");
		this.result = result;
	}

	@Override
	public int hashCode() {
		return this.executionId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Execution) {
			Execution that=(Execution)obj;
			result=this.executionId.equals(that.executionId);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("buildId",this.buildId).
					add("executionId",this.executionId).
					add("createdOn",this.createdOn).
					add("result",this.result).
					toString();
	}

}
