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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0
 *   Bundle      : ci-jenkins-crawler-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;

import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerStartedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerStoppedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingAbortedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingCompletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingStartedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.InstanceFoundEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.JobCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JobUpdatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunCreatedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunDeletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.RunUpdatedEvent;

import com.google.common.collect.Lists;

final class CrawlerInformationPoint implements CrawlerEventListener, JenkinsEventListener {

	private final Deque<CrawlingSession> crawlingSessions;
	private final AtomicLong sessionCounter;
	private final JenkinsInformationPoint jip;

	private CrawlerStartedEvent crawlerStartedEvent;
	private CrawlerStoppedEvent crawlerStoppedEvent;

	CrawlerInformationPoint() {
		this.sessionCounter=new AtomicLong();
		this.crawlingSessions=Lists.newLinkedList();
		this.jip=new JenkinsInformationPoint();
		newSession();
	}

	private void newSession() {
		this.crawlingSessions.add(new CrawlingSession(this.sessionCounter.getAndIncrement()));
	}

	boolean isCrawlerStopped() {
		return crawlerStartedEvent==null && crawlerStoppedEvent==null;
	}

	boolean isCrawlerStarted() {
		return crawlerStartedEvent!=null && crawlerStoppedEvent==null;
	}

	boolean isCrawlerShutdown() {
		return crawlerStartedEvent!=null && crawlerStoppedEvent!=null;
	}

	int totalCrawlingSessions() {
		return this.crawlingSessions.size()-1;
	}

	CrawlingSession currentCrawlingSession() {
		return this.crawlingSessions.peekLast();
	}

	JenkinsInformationPoint jenkinsInformationPoint() {
		return this.jip;
	}

	@Override
	public void onCrawlerStartUp(CrawlerStartedEvent event) {
		this.crawlerStartedEvent=event;
	}

	@Override
	public void onCrawlerShutdown(CrawlerStoppedEvent event) {
		this.crawlerStoppedEvent=event;
	}

	@Override
	public void onCrawlingStartUp(CrawlingStartedEvent event) {
		currentCrawlingSession().start(event.completedOn());
	}

	@Override
	public void onCrawlingAbortion(CrawlingAbortedEvent event) {
		currentCrawlingSession().abort(event.completedOn());
		newSession();
	}

	@Override
	public void onCrawlingCompletion(CrawlingCompletedEvent event) {
		currentCrawlingSession().complete(event.completedOn());
		newSession();
	}

	@Override
	public void onInstanceFound(InstanceFoundEvent event) {
		this.jip.onInstanceFound(event);
		currentCrawlingSession().jenkinsInformationPoint().onInstanceFound(event);
	}

	@Override
	public void onJobCreation(JobCreatedEvent event) {
		this.jip.onJobCreation(event);
		currentCrawlingSession().jenkinsInformationPoint().onJobCreation(event);
	}

	@Override
	public void onJobUpdate(JobUpdatedEvent event) {
		this.jip.onJobUpdate(event);
		currentCrawlingSession().jenkinsInformationPoint().onJobUpdate(event);
	}

	@Override
	public void onJobDeletion(JobDeletedEvent event) {
		this.jip.onJobDeletion(event);
		currentCrawlingSession().jenkinsInformationPoint().onJobDeletion(event);
	}

	@Override
	public void onRunCreation(RunCreatedEvent event) {
		this.jip.onRunCreation(event);
		currentCrawlingSession().jenkinsInformationPoint().onRunCreation(event);
	}

	@Override
	public void onRunUpdate(RunUpdatedEvent event) {
		this.jip.onRunUpdate(event);
		currentCrawlingSession().jenkinsInformationPoint().onRunUpdate(event);
	}

	@Override
	public void onRunDeletion(RunDeletedEvent event) {
		this.jip.onRunDeletion(event);
		currentCrawlingSession().jenkinsInformationPoint().onRunDeletion(event);
	}

}
