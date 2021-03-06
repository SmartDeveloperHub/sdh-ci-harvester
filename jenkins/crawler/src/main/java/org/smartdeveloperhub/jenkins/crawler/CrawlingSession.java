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
package org.smartdeveloperhub.jenkins.crawler;

import java.util.Date;

final class CrawlingSession {

	private final long number;
	private final JenkinsInformationPoint jip;

	private Date startedOn;
	private Date finishedOn;
	private boolean aborted;

	CrawlingSession(long number) {
		this.number=number;
		this.jip=new JenkinsInformationPoint();
	}

	void start(Date date) {
		this.startedOn=date;
	}

	void complete(Date date) {
		this.finishedOn=date;
		this.aborted=false;
	}

	void abort(Date date) {
		this.finishedOn=date;
		this.aborted=true;
	}

	long sessionNumber() {
		return this.number;
	}

	boolean isIdle() {
		return this.startedOn==null;
	}

	boolean isActive() {
		return this.startedOn!=null && this.finishedOn==null;
	}

	boolean isFinished() {
		return this.startedOn!=null && this.finishedOn!=null;
	}

	boolean finishedSuccesfully() {
		return !this.aborted;
	}

	JenkinsInformationPoint jenkinsInformationPoint() {
		return this.jip;
	}

}