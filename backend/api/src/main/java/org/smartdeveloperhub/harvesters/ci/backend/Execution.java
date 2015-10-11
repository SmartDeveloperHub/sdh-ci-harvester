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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import static com.google.common.base.Preconditions.*;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;

import com.google.common.base.MoreObjects;

public final class Execution {

	private static final Result UNAVAILABLE_RESULT = new Result();

	private URI buildId;
	private URI executionId;
	private Date createdOn;
	private Result result;

	private Codebase codebase;
	private String commitId;

	private Execution(Execution execution) {
		this(execution.buildId(),execution.executionId(),execution.createdOn(),execution.codebase(),execution.commitId());
		this.result=execution.result;
	}

	Execution() {
		this.result=UNAVAILABLE_RESULT;
	}

	Execution(URI buildId, URI executionId, Date createdOn, Codebase codebase, String commitId) {
		this();
		setBuildId(buildId);
		setExecutionId(executionId);
		setCreatedOn(createdOn);
		setCodebase(codebase);
		setCommitId(commitId);
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

	protected void setCodebase(Codebase codebase) {
		this.codebase = codebase;
	}

	protected void setCommitId(String commitId) {
		this.commitId = commitId;
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

	public Codebase codebase() {
		return this.codebase;
	}

	public String commitId() {
		return this.commitId;
	}

	public Result result() {
		return this.result;
	}

	public boolean isFinished() {
		return !Status.UNAVAILABLE.equals(this.result.status());
	}

	public void finish(Result result) {
		checkNotNull(result,"Result cannot be null");
		checkArgument(!Status.UNAVAILABLE.equals(result.status()),"Result must be available");
		checkState(Status.UNAVAILABLE.equals(this.result.status()),"Execution already finished");
		this.result = result;
	}

	@Override
	public int hashCode() {
		return this.executionId.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean equals=false;
		if(obj instanceof Execution) {
			Execution that=(Execution)obj;
			equals=this.executionId.equals(that.executionId);
		}
		return equals;
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
					add("codebase",this.codebase).
					add("commitId",this.commitId).
					add("result",this.result).
					toString();
	}

	public static Execution newInstance(Execution execution) {
		if(execution==null) {
			return null;
		}
		return new Execution(execution);
	}

}
