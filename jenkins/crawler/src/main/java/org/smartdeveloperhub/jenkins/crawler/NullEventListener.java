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

import org.smartdeveloperhub.jenkins.crawler.event.BuildCreationEvent;
import org.smartdeveloperhub.jenkins.crawler.event.BuildDeletionEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionCreationEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionDeletionEvent;
import org.smartdeveloperhub.jenkins.crawler.event.ExecutionUpdateEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;

final class NullEventListener implements JenkinsEventListener {

	private static final NullEventListener DEFAULT=new NullEventListener();

	private NullEventListener() {
	}

	@Override
	public void onExecutionUpdate(ExecutionUpdateEvent event) {
		// Nothing to do
	}
	@Override
	public void onExecutionDeletion(ExecutionDeletionEvent event) {
		// Nothing to do
	}
	@Override
	public void onExecutionCreation(ExecutionCreationEvent event) {
		// Nothing to do
	}
	@Override
	public void onBuildDeletion(BuildDeletionEvent event) {
		// Nothing to do
	}
	@Override
	public void onBuildCreation(BuildCreationEvent event) {
		// Nothing to do
	}

	static JenkinsEventListener create() {
		return DEFAULT;
	}
}