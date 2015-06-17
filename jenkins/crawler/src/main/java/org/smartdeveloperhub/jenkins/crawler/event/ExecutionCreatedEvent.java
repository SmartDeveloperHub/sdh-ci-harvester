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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunStatus;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class ExecutionCreatedEvent extends JenkinsEvent {

	private Run run;

	private ExecutionCreatedEvent(URI service, Date date) {
		super(service,date);
	}

	ExecutionCreatedEvent withRun(Run run) {
		this.run = run;
		return this;
	}

	@Override
	void accept(JenkinsEventVisitor visitor) {
		if(visitor!=null) {
			visitor.visitExecutionCreationEvent(this);
		}
	}

	Run run() {
		return this.run;
	}

	public URI buildId() {
		return run.getBuild();
	}

	public URI executionId() {
		return this.run.getUrl();
	}

	public Date createdOn() {
		return new Date(this.run.getTimestamp());
	}

	public boolean isFinished() {
		return RunStatus.FINISHED.equals(this.run.getStatus());
	}

	public Date finishedOn() {
		Date result=null;
		if(isFinished()) {
			result=new Date(this.run.getTimestamp()+this.run.getDuration());
		}
		return result;
	}

	public RunResult result() {
		return this.run.getResult();
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.
			add("buildId", buildId()).
			add("executionId", executionId()).
			add("createdOn",createdOn()).
			add("finishedOn",finishedOn()).
			add("result",result());
	}

	static ExecutionCreatedEvent create(URI location) {
		return new ExecutionCreatedEvent(location, new Date());
	}

}
