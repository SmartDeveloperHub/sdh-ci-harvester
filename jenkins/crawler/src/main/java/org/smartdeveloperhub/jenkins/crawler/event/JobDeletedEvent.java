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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0
 *   Bundle      : ci-jenkins-crawler-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class JobDeletedEvent extends JenkinsEvent {

	private final URI jobId;

	JobDeletedEvent(URI instanceId, Date date, URI jobId) {
		super(instanceId,date);
		this.jobId = jobId;
	}

	public URI jobId() {
		return this.jobId;
	}

	@Override
	void accept(JenkinsEventVisitor visitor) {
		if(visitor!=null) {
			visitor.visitJobDeletedEvent(this);
		}
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.add("jobId", jobId());
	}

	static JobDeletedEvent create(URI instanceId, URI jobId) {
		return new JobDeletedEvent(instanceId, new Date(),jobId);
	}

}
