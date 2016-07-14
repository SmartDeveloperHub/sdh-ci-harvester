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
package org.smartdeveloperhub.jenkins.crawler.event;

import static com.google.common.base.Preconditions.*;

public final class JenkinsEventDispatcher {

	private static final class DispatchingVisitor extends JenkinsEventVisitor {

		private JenkinsEventListener listener;

		private DispatchingVisitor(JenkinsEventListener listener) {
			this.listener = listener;
		}

		@Override
		void visitInstanceFoundEvent(InstanceFoundEvent event) {
			this.listener.onInstanceFound(event);
		}

		@Override
		void visitJobCreatedEvent(JobCreatedEvent event) {
			this.listener.onJobCreation(event);
		}

		@Override
		void visitJobUpdatedEvent(JobUpdatedEvent event) {
			this.listener.onJobUpdate(event);
		}

		@Override
		void visitJobDeletedEvent(JobDeletedEvent event) {
			this.listener.onJobDeletion(event);
		}

		@Override
		void visitRunCreatedEvent(RunCreatedEvent event) {
			this.listener.onRunCreation(event);
		}

		@Override
		void visitRunUpdatedEvent(RunUpdatedEvent event) {
			this.listener.onRunUpdate(event);
		}

		@Override
		void visitRunDeletedEvent(RunDeletedEvent event) {
			this.listener.onRunDeletion(event);
		}

	}

	private final DispatchingVisitor visitor;

	private JenkinsEventDispatcher(JenkinsEventListener listener) {
		this.visitor=new DispatchingVisitor(listener);
	}

	public void fireEvent(JenkinsEvent event) {
		checkNotNull(event,"Event cannot be null");
		event.accept(this.visitor);
	}

	public static JenkinsEventDispatcher create(JenkinsEventListener listener) {
		checkNotNull(listener,"Listener cannot be null");
		return new JenkinsEventDispatcher(listener);
	}

}