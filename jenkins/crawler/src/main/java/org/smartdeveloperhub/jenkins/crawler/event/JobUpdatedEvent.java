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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0
 *   Bundle      : ci-jenkins-crawler-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;

import com.google.common.base.Optional;
import com.google.common.base.MoreObjects.ToStringHelper;

public final class JobUpdatedEvent extends JenkinsEvent {

	private Job job;
	private Codebase codebase;

	private JobUpdatedEvent(URI instanceId, Date date) {
		super(instanceId,date);
	}

	JobUpdatedEvent withJob(Job job) {
		this.job=job;
		this.codebase=Optional.fromNullable(this.job.getCodebase()).or(new Codebase());
		return this;
	}

	Job job() {
		return this.job;
	}

	@Override
	void accept(JenkinsEventVisitor visitor) {
		if(visitor!=null) {
			visitor.visitJobUpdatedEvent(this);
		}
	}

	public String title() {
		return this.job.getTitle();
	}

	public String description() {
		return this.job.getDescription();
	}

	public URI codebase() {
		return this.codebase.getLocation();
	}

	public String branchName() {
		return this.codebase.getBranch();
	}

	public URI jobId() {
		return this.job.getUrl();
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.
			add("jobId", jobId()).
			add("title", title()).
			add("description", description()).
			add("codebase", codebase()).
			add("branchName", branchName());
	}

	static JobUpdatedEvent create(URI instanceId) {
		return new JobUpdatedEvent(instanceId, new Date());
	}

}
