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
package org.smartdeveloperhub.jenkins.crawler;

import org.smartdeveloperhub.jenkins.crawler.event.BuildCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.BuildDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;

final class NullEventListener implements JenkinsEventListener {

	private static final NullEventListener DEFAULT=new NullEventListener();

	private NullEventListener() {
	}

	@Override
	public void onExecutionUpdate(ExecutionUpdatedEvent event) {
		// Nothing to do
	}
	@Override
	public void onExecutionDeletion(ExecutionDeletedEvent event) {
		// Nothing to do
	}
	@Override
	public void onExecutionCreation(ExecutionCreatedEvent event) {
		// Nothing to do
	}
	@Override
	public void onBuildDeletion(BuildDeletedEvent event) {
		// Nothing to do
	}
	@Override
	public void onBuildCreation(BuildCreatedEvent event) {
		// Nothing to do
	}

	static JenkinsEventListener getInstance() {
		return DEFAULT;
	}
}