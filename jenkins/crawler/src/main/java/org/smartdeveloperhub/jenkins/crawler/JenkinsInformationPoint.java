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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.4.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.net.URI;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.smartdeveloperhub.jenkins.crawler.event.InstanceFoundEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.JobCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunUpdatedEvent;

import com.google.common.collect.Lists;

final class JenkinsInformationPoint implements JenkinsEventListener {

	private final List<URI> instancesFound;

	private final List<URI> jobsCreated;
	private final List<URI> jobsModified;
	private final List<URI> jobsDeleted;
	private final AtomicLong jobEvents;

	private final List<URI> runsCreated;
	private final List<URI> runsModified;
	private final List<URI> runsDeleted;
	private final AtomicLong runEvents;

	JenkinsInformationPoint() {
		this.instancesFound=Lists.newArrayList();
		this.jobsCreated=Lists.newArrayList();
		this.jobsModified=Lists.newArrayList();
		this.jobsDeleted=Lists.newArrayList();
		this.jobEvents=new AtomicLong();
		this.runsCreated=Lists.newArrayList();
		this.runsModified=Lists.newArrayList();
		this.runsDeleted=Lists.newArrayList();
		this.runEvents=new AtomicLong();
	}

	@Override
	public void onInstanceFound(InstanceFoundEvent event) {
		synchronized(this.instancesFound) {
			this.instancesFound.add(event.instanceId());
		}
	}

	@Override
	public void onJobCreation(JobCreatedEvent event) {
		this.jobEvents.incrementAndGet();
		synchronized(this.jobsCreated) {
			this.jobsCreated.add(event.jobId());
		}
	}

	@Override
	public void onJobUpdate(JobUpdatedEvent event) {
		this.jobEvents.incrementAndGet();
		synchronized(this.jobsModified) {
			this.jobsModified.add(event.jobId());
		}
	}

	@Override
	public void onJobDeletion(JobDeletedEvent event) {
		this.jobEvents.incrementAndGet();
		synchronized(this.jobsDeleted) {
			this.jobsDeleted.add(event.jobId());
		}
	}

	@Override
	public void onRunCreation(RunCreatedEvent event) {
		this.runEvents.incrementAndGet();
		synchronized(this.runsCreated) {
			this.runsCreated.add(event.runId());
		}
	}

	@Override
	public void onRunUpdate(RunUpdatedEvent event) {
		this.runEvents.incrementAndGet();
		synchronized(this.runsModified) {
			this.runsModified.add(event.runId());
		}
	}

	@Override
	public void onRunDeletion(RunDeletedEvent event) {
		this.runEvents.incrementAndGet();
		synchronized(this.runsDeleted) {
			this.runsDeleted.add(event.runId());
		}
	}

}