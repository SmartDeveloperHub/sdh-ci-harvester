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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Result;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunStatus;

import com.google.common.base.Optional;

abstract class RunEvent<T extends RunEvent<T>> extends JenkinsEvent {

	private final Class<? extends T> clazz;

	private Run run;
	private Codebase codebase;
	private Result result;

	RunEvent(URI instanceId, Date date, Class<? extends T> clazz) {
		super(instanceId,date);
		this.clazz = clazz;
	}

	final T withRun(Run run) {
		this.run = run;
		this.codebase=Optional.fromNullable(this.run.getCodebase()).or(new Codebase());
		this.result=Optional.fromNullable(this.run.getResult()).or(new Result());
		return this.clazz.cast(this);
	}

	final Run run() {
		return this.run;
	}

	public final URI runId() {
		return this.run.getUrl();
	}

	public final boolean isFinished() {
		return RunStatus.FINISHED.equals(this.run.getStatus());
	}

	public final Date finishedOn() {
		Date result=null;
		if(isFinished()) {
			result=new Date(this.run.getTimestamp()+this.run.getResult().getDuration());
		}
		return result;
	}

	public final RunResult result() {
		return this.result.getStatus();
	}

	public final URI codebase() {
		return this.codebase.getLocation();
	}

	public final String branchName() {
		return this.codebase.getBranch();
	}

	public final String commitId() {
		return this.run.getCommit();
	}

}
