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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.1.0
 *   Bundle      : ci-backend-api-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class Result {

	public enum Status {
		PASSED,
		FAILED,
		WARNING,
		ABORTED,
		UNAVAILABLE,
		NOT_BUILT
	}

	private Result.Status status;
	private Date finishedOn;

	Result() {
		this.status=Status.UNAVAILABLE;
		this.finishedOn=null;
	}

	public Result(Status status, Date finishedOn) {
		setStatus(status);
		setFinishedOn(finishedOn);
	}

	protected void setFinishedOn(Date finishedOn) {
		checkNotNull(finishedOn,"Finished date cannot be null");
		this.finishedOn = finishedOn;
	}

	protected void setStatus(Status status) {
		checkNotNull(status,"Status cannot be null");
		this.status = status;
	}

	public Status status() {
		return this.status;
	}

	public Date finishedOn() {
		return this.finishedOn;
	}

	@Override
	public int hashCode() {
		return
			Objects.
				hashCode(this.status,this.finishedOn);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof Result) {
			Result that=(Result)obj;
			result=
				Objects.equal(this.status,that.status) &&
				Objects.equal(this.finishedOn,that.finishedOn);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("status",this.status).
					add("finishedOn",this.finishedOn).
					toString();
	}

}