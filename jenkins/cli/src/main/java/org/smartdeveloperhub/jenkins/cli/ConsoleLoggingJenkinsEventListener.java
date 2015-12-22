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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-cli:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-cli-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.cli;

import org.smartdeveloperhub.jenkins.crawler.event.InstanceFoundEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.JobCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunUpdatedEvent;
import org.smartdeveloperhub.util.console.Consoles;

final class ConsoleLoggingJenkinsEventListener implements JenkinsEventListener {

	@Override
	public void onInstanceFound(final InstanceFoundEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Found instance %s%n",event.date(),event.instanceId());
	}

	@Override
	public void onRunUpdate(final RunUpdatedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Updated run %s%n",event.date(),event.runId());
	}

	@Override
	public void onRunDeletion(final RunDeletedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Deleted run %s%n",event.date(),event.runId());
	}

	@Override
	public void onRunCreation(final RunCreatedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Created run %s {%s -> %s :: %s }%n",event.date(),event.runId(),event.codebase(),event.branchName(),event.commitId());
	}

	@Override
	public void onJobDeletion(final JobDeletedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Deleted job %s%n",event.date(),event.jobId());
	}

	@Override
	public void onJobCreation(final JobCreatedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Created job %s {%s -> %s}%n",event.date(),event.jobId(),event.codebase(),event.branchName());
	}

	@Override
	public void onJobUpdate(final JobUpdatedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Updated job %s {%s -> %s}%n",event.date(),event.jobId(),event.codebase(),event.branchName());
	}

}