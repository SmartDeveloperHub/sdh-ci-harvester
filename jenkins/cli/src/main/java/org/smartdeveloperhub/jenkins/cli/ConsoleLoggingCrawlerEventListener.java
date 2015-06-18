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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-cli:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-cli-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.cli;

import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerStartedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerStoppedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingAbortedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingCompletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingStartedEvent;
import org.smartdeveloperhub.util.console.Consoles;

final class ConsoleLoggingCrawlerEventListener implements CrawlerEventListener {

	@Override
	public void onCrawlingStartUp(CrawlingStartedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Crawling session %d started on %s%n",event.date(),event.sessionId(),event.completedOn());
	}

	@Override
	public void onCrawlingCompletion(CrawlingCompletedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Crawling session %d completed on %s%n",event.date(),event.sessionId(),event.completedOn());
	}

	@Override
	public void onCrawlingAbortion(CrawlingAbortedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Crawling session %d was aborted on %s%n",event.date(),event.sessionId(),event.completedOn());
	}

	@Override
	public void onCrawlerStartUp(CrawlerStartedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Crawler started on %s%n",event.date(),event.startedOn());
	}

	@Override
	public void onCrawlerShutdown(CrawlerStoppedEvent event) {
		Consoles.
			defaultConsole().
				printf("[%s] Crawler stopped on %s%n",event.date(),event.stoppedOn());
	}

}