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
package org.smartdeveloperhub.jenkins.crawler;

import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventDispatcher;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventListener;
import org.smartdeveloperhub.jenkins.crawler.util.ListenerManager;
import org.smartdeveloperhub.jenkins.crawler.util.Notification;

final class DefaultCrawlerEventManager implements CrawlerEventManager {

	private static final class CrawlerEventNotification implements Notification<CrawlerEventListener> {

		private final CrawlerEvent event;

		private CrawlerEventNotification(CrawlerEvent event) {
			this.event = event;
		}

		@Override
		public void propagate(CrawlerEventListener listener) {
			CrawlerEventDispatcher.
				create(listener).
					fireEvent(this.event);
		}
	}

	private final ListenerManager<CrawlerEventListener> listeners;

	DefaultCrawlerEventManager(ListenerManager<CrawlerEventListener> listeners) {
		this.listeners = listeners;
	}

	@Override
	public void fireEvent(CrawlerEvent event) {
		this.listeners.notify(new CrawlerEventNotification(event));
	}

}
