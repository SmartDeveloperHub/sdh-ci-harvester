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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateExecutionCommand.Builder;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.UpdateBuildCommand;
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

	private final boolean acceptDeletions;

	CommandProducerListener(final CommandProcessingMonitor monitor, final boolean acceptDeletions) {
		this.monitor = monitor;
		this.acceptDeletions = acceptDeletions;
	}

	private Status toStatus(final RunResult result) {
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

	private void logEvent(final JenkinsEvent event) {
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace("Received event {}",event);
		}
	}

	@Override
	public void onInstanceFound(final InstanceFoundEvent event) {
		logEvent(event);
		this.monitor.offer(
			RegisterServiceCommand.create(event.instanceId())
		);
	}

	@Override
	public void onJobCreation(final JobCreatedEvent event) {
		logEvent(event);
		final CreateBuildCommand.Builder builder=
			CreateBuildCommand.
				builder().
				withServiceId(event.instanceId()).
				withBuildId(event.jobId()).
				withTitle(event.title()).
				withDescription(event.description()).
				withCodebase(event.codebase()).
				withBranchName(event.branchName());
		switch(event.type()) {
			case SIMPLE:
				builder.simple();
				break;
			case COMPOSITE:
				builder.composite();
				break;
			case SUB_JOB:
				builder.subBuildOf(event.parent());
				break;
			default:
				break;
		}
		this.monitor.offer(builder.build());
	}

	@Override
	public void onJobUpdate(final JobUpdatedEvent event) {
		logEvent(event);
		this.monitor.offer(
			UpdateBuildCommand.
				builder().
					withBuildId(event.jobId()).
					withTitle(event.title()).
					withDescription(event.description()).
					withCodebase(event.codebase()).
					withBranchName(event.branchName()).
					build()
		);
	}

	@Override
	public void onJobDeletion(final JobDeletedEvent event) {
		logEvent(event);
		if(this.acceptDeletions) {
			this.monitor.offer(
				DeleteBuildCommand.create(event.jobId())
			);
		}
	}

	@Override
	public void onRunCreation(final RunCreatedEvent event) {
		logEvent(event);
		final Builder builder =
			CreateExecutionCommand.
				builder().
					withBuildId(event.jobId()).
					withExecutionId(event.runId()).
					withCreatedOn(event.createdOn()).
					withCodebase(event.codebase()).
					withBranchName(event.branchName()).
					withCommitId(event.commitId());
		if(event.isFinished()) {
			builder.
				withStatus(toStatus(event.result())).
				withFinishedOn(event.finishedOn());
		}
		this.monitor.offer(builder.build());
	}

	@Override
	public void onRunUpdate(final RunUpdatedEvent event) {
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
	public void onRunDeletion(final RunDeletedEvent event) {
		logEvent(event);
		if(this.acceptDeletions) {
			this.monitor.offer(
				DeleteExecutionCommand.create(event.runId())
			);
		}
	}

}