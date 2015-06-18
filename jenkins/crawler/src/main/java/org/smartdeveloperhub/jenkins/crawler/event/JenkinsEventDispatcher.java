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

import static com.google.common.base.Preconditions.*;

public final class JenkinsEventDispatcher {

	private static final class DispatchingVisitor extends JenkinsEventVisitor {

		private JenkinsEventListener listener;

		private DispatchingVisitor(JenkinsEventListener listener) {
			this.listener = listener;
		}

		@Override
		void visitServiceFoundEvent(ServiceFoundEvent event) {
			this.listener.onServiceFound(event);
		}

		@Override
		void visitBuildCreationEvent(BuildCreatedEvent event) {
			this.listener.onBuildCreation(event);
		}

		@Override
		void visitBuildUpdatedEvent(BuildUpdatedEvent event) {
			this.listener.onBuildUpdate(event);
		}

		@Override
		void visitBuildDeletionEvent(BuildDeletedEvent event) {
			this.listener.onBuildDeletion(event);
		}

		@Override
		void visitExecutionCreationEvent(ExecutionCreatedEvent event) {
			this.listener.onExecutionCreation(event);
		}

		@Override
		void visitExecutionUpdateEvent(ExecutionUpdatedEvent event) {
			this.listener.onExecutionUpdate(event);
		}

		@Override
		void visitExecutionDeletionEvent(ExecutionDeletedEvent event) {
			this.listener.onExecutionDeletion(event);
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