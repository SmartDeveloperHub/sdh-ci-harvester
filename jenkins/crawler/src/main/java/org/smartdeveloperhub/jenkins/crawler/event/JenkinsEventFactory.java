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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

public final class JenkinsEventFactory {

	private JenkinsEventFactory() {
	}

	public static InstanceFoundEvent newInstanceFoundEvent(URI instanceId) {
		return InstanceFoundEvent.create(instanceId);
	}

	public static JobCreatedEvent newJobCreatedEvent(URI instanceId, Job job) {
		return JobCreatedEvent.create(instanceId).withJob(job);
	}

	public static JobUpdatedEvent newJobUpdatedEvent(URI instanceId, Job job) {
		return JobUpdatedEvent.create(instanceId).withJob(job);
	}

	public static JobDeletedEvent newJobDeletedEvent(URI instanceId, URI jobId) {
		return JobDeletedEvent.create(instanceId,jobId);
	}

	public static RunCreatedEvent newRunCreatedEvent(URI instanceId, Run run) {
		return RunCreatedEvent.create(instanceId).withRun(run);
	}

	public static RunUpdatedEvent newRunUpdatedEvent(URI instanceId, Run run) {
		return RunUpdatedEvent.create(instanceId).withRun(run);
	}

	public static RunDeletedEvent newRunDeletedEvent(URI instanceId, URI runId) {
		return RunDeletedEvent.create(instanceId,runId);
	}

}
