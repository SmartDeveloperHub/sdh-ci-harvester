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

import static com.google.common.base.Preconditions.*;

public final class CrawlerEventDispatcher {

	private static final class DispatchingVisitor extends CrawlerEventVisitor {

		private CrawlerEventListener listener;

		private DispatchingVisitor(CrawlerEventListener listener) {
			this.listener = listener;
		}

		@Override
		public void visitCrawlerStartedEvent(CrawlerStartedEvent event) {
			this.listener.onCrawlerStartUp(event);
		}

		@Override
		public void visitCrawlerStoppedEvent(CrawlerStoppedEvent event) {
			this.listener.onCrawlerShutdown(event);
		}

		@Override
		public void visitCrawlingStartedEvent(CrawlingStartedEvent event) {
			this.listener.onCrawlingStartUp(event);
		}

		@Override
		public void visitCrawlingAbortedEvent(CrawlingAbortedEvent event) {
			this.listener.onCrawlingAbortion(event);
		}

		@Override
		public void visitCrawlingCompletedEvent(CrawlingCompletedEvent event) {
			this.listener.onCrawlingCompletion(event);
		}

	}

	private final DispatchingVisitor visitor;

	private CrawlerEventDispatcher(CrawlerEventListener listener) {
		this.visitor=new DispatchingVisitor(listener);
	}

	public void fireEvent(CrawlerEvent event) {
		checkNotNull(event,"Event cannot be null");
		event.accept(this.visitor);
	}

	public static CrawlerEventDispatcher create(CrawlerEventListener listener) {
		checkNotNull(listener,"Listener cannot be null");
		return new CrawlerEventDispatcher(listener);
	}

}