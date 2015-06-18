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

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

public final class JenkinsEventFactory {

	private JenkinsEventFactory() {
	}

	public static ServiceFoundEvent newServiceFoundEvent(URI service) {
		return ServiceFoundEvent.create(service);
	}

	public static BuildCreatedEvent newBuildCreatedEvent(URI service, Build build) {
		return BuildCreatedEvent.create(service).withBuild(build);
	}

	public static BuildUpdatedEvent newBuildUpdatedEvent(URI service, Build build) {
		return BuildUpdatedEvent.create(service).withBuild(build);
	}

	public static BuildDeletedEvent newBuildDeletedEvent(URI service, URI build) {
		return BuildDeletedEvent.create(service,build);
	}

	public static ExecutionCreatedEvent newExecutionCreatedEvent(URI service, Run run) {
		return ExecutionCreatedEvent.create(service).withRun(run);
	}

	public static ExecutionUpdatedEvent newExecutionUpdatedEvent(URI service, Run run) {
		return ExecutionUpdatedEvent.create(service).withRun(run);
	}

	public static ExecutionDeletedEvent newExecutionDeletedEvent(URI service, URI execution) {
		return ExecutionDeletedEvent.create(service,execution);
	}

}
