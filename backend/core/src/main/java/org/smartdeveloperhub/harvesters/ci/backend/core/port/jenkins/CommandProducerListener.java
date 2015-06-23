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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.core.port.jenkins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.UpdateBuildCommand;
import org.smartdeveloperhub.jenkins.crawler.event.InstanceFoundEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.JobCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;

final class CommandProducerListener implements JenkinsEventListener {

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProducerListener.class);

	private final CommandProcessingMonitor monitor;

	CommandProducerListener(CommandProcessingMonitor monitor) {
		this.monitor = monitor;
	}

	private Status toStatus(RunResult result) {
		Status status=null;
		switch(result) {
			case ABORTED:
				status=Status.ABORTED;
				break;
			case FAILURE:
				status=Status.FAILED;
				break;
			case SUCCESS:
				status=Status.PASSED;
				break;
			case UNSTABLE:
				status=Status.WARNING;
				break;
			case NOT_BUILT:
				status=Status.NOT_BUILT;
				break;
			default:
				status=Status.UNAVAILABLE;
				break;
		}
		return status;
	}

	private void logEvent(JenkinsEvent event) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received event {}",event);
		}
	}

	@Override
	public void onInstanceFound(InstanceFoundEvent event) {
		logEvent(event);
		this.monitor.offer(
			RegisterServiceCommand.create(event.instanceId())
		);
	}

	@Override
	public void onJobCreation(JobCreatedEvent event) {
		logEvent(event);
		this.monitor.offer(
			CreateBuildCommand.
				builder().
					withServiceId(event.instanceId()).
					withBuildId(event.jobId()).
					withTitle(event.title()).
					withDescription(event.description()).
					withCodebase(event.codebase()).
					build()
		);
	}

	@Override
	public void onJobUpdate(JobUpdatedEvent event) {
		logEvent(event);
		this.monitor.offer(
			UpdateBuildCommand.
				builder().
					withBuildId(event.jobId()).
					withTitle(event.title()).
					withDescription(event.description()).
					withCodebase(event.codebase()).
					build()
		);
	}

	@Override
	public void onJobDeletion(JobDeletedEvent event) {
		logEvent(event);
		this.monitor.offer(
			DeleteBuildCommand.create(event.jobId())
		);
	}

	@Override
	public void onRunCreation(RunCreatedEvent event) {
		logEvent(event);
		this.monitor.offer(
			CreateExecutionCommand.
				builder().
					withBuildId(event.jobId()).
					withExecutionId(event.runId()).
					withCreatedOn(event.createdOn()).
					build()
		);
		if(event.isFinished()) {
			this.monitor.offer(
				FinishExecutionCommand.
					builder().
						withExecutionId(event.runId()).
						withStatus(toStatus(event.result())).
						withFinishedOn(event.finishedOn()).
						build()
			);
		}
	}

	@Override
	public void onRunUpdate(RunUpdatedEvent event) {
		logEvent(event);
		this.monitor.offer(
			FinishExecutionCommand.
				builder().
					withExecutionId(event.runId()).
					withStatus(toStatus(event.result())).
					withFinishedOn(event.finishedOn()).
					build()
		);
	}

	@Override
	public void onRunDeletion(RunDeletedEvent event) {
		logEvent(event);
		this.monitor.offer(
			DeleteExecutionCommand.create(event.runId())
		);
	}

}