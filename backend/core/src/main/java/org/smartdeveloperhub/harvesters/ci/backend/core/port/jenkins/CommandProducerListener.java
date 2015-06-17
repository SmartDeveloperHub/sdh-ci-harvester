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

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Result.Status;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.Command;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.UpdateBuildCommand;
import org.smartdeveloperhub.jenkins.crawler.event.BuildCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.BuildDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.BuildUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;

final class CommandProducerListener implements JenkinsEventListener {

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProducerListener.class);

	private final LinkedBlockingQueue<Command> commandQueue;

	CommandProducerListener(LinkedBlockingQueue<Command> commandQueue) {
		this.commandQueue = commandQueue;
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
	public void onBuildCreation(BuildCreatedEvent event) {
		logEvent(event);
		commandQueue.offer(
			CreateBuildCommand.
				builder().
					withServiceId(event.service()).
					withBuildId(event.buildId()).
					withTitle(event.title()).
					withDescription(event.description()).
					withCodebase(event.codebase()).
					build()
		);
	}

	@Override
	public void onBuildUpdate(BuildUpdatedEvent event) {
		logEvent(event);
		commandQueue.offer(
			UpdateBuildCommand.
				builder().
					withBuildId(event.buildId()).
					withTitle(event.title()).
					withDescription(event.description()).
					withCodebase(event.codebase()).
					build()
		);
	}

	@Override
	public void onBuildDeletion(BuildDeletedEvent event) {
		logEvent(event);
		commandQueue.offer(
			DeleteBuildCommand.create(event.buildId())
		);
	}

	@Override
	public void onExecutionCreation(ExecutionCreatedEvent event) {
		logEvent(event);
		commandQueue.offer(
			CreateExecutionCommand.
				builder().
					withBuildId(event.buildId()).
					withExecutionId(event.executionId()).
					withCreatedOn(event.createdOn()).
					build()
		);
		if(event.isFinished()) {
			commandQueue.offer(
				FinishExecutionCommand.
					builder().
						withExecutionId(event.executionId()).
						withStatus(toStatus(event.result())).
						withFinishedOn(event.finishedOn()).
						build()
			);
		}
	}

	@Override
	public void onExecutionUpdate(ExecutionUpdatedEvent event) {
		logEvent(event);
		commandQueue.offer(
			FinishExecutionCommand.
				builder().
					withExecutionId(event.executionId()).
					withStatus(toStatus(event.result())).
					withFinishedOn(event.finishedOn()).
					build()
		);
	}

	@Override
	public void onExecutionDeletion(ExecutionDeletedEvent event) {
		logEvent(event);
		commandQueue.offer(
			DeleteExecutionCommand.create(event.executionId())
		);
	}

}