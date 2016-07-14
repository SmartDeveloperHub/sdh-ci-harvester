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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.3.0
 *   Bundle      : ci-jenkins-api-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

import static org.smartdeveloperhub.jenkins.RangeFactory.and;
import static org.smartdeveloperhub.jenkins.RangeFactory.empty;
import static org.smartdeveloperhub.jenkins.RangeFactory.equalTo;
import static org.smartdeveloperhub.jenkins.RangeFactory.greaterOrEqualThan;
import static org.smartdeveloperhub.jenkins.RangeFactory.lowerThan;
import static org.smartdeveloperhub.jenkins.RangeFactory.or;

public enum Status {
	AVAILABLE(equalTo(200)) {
		@Override
		public boolean isFailure() {
			return false;
		}
	},
	UNAVAILABLE(or(equalTo(404),equalTo(410))),
	UNKNOWN(or(and(greaterOrEqualThan(300),lowerThan(400)),and(greaterOrEqualThan(500),lowerThan(600)))),
	ERROR(empty()),
	UNSUPPORTED_RESOURCE(equalTo(-1)),
	INCOMPATIBLE_RESOURCE(equalTo(-2)),
	UNPROCESSABLE_RESOURCE(equalTo(-3)),
	INVALID_RESOURCE(equalTo(-4)),
	;

	private Range range;

	Status(Range range) {
		this.range = range;
	}

	public static Status fromHttpStatusCode(int statusCode) {
		for(Status status:values()) {
			if(status.range.contains(statusCode)) {
				return status;
			}
		}
		return Status.ERROR;
	}

	public boolean isFailure() {
		return true;
	}

}