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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventFactory;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingEvent;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.util.Timer;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Instance;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class CrawlingController extends AbstractExecutionThreadService {

	static class Builder {

		private TaskScheduler scheduler;
		private URI instance;
		private FileBasedStorage storage;
		private CrawlingEventDispatcher dispatcher;
		private OperationDecissionPoint odp;
		private CrawlerInformationPoint cip;
		private CrawlingDecissionPoint cdp;

		private Builder() {
		}

		Builder withJenkinsInstance(URI instance) {
			this.instance = instance;
			return this;
		}

		Builder withTaskScheduler(TaskScheduler scheduler) {
			this.scheduler = scheduler;
			return this;
		}

		Builder withCrawlingEventDispatcher(CrawlingEventDispatcher dispatcher) {
			this.dispatcher = dispatcher;
			return this;
		}

		Builder withStorage(FileBasedStorage manager) {
			this.storage=manager;
			return this;
		}

		Builder withCrawlerInformationPoint(CrawlerInformationPoint cip) {
			this.cip=cip;
			return this;
		}

		Builder withOperationDecissionPoint(OperationDecissionPoint odp) {
			this.odp=odp;
			return this;
		}

		Builder withCrawlingDecissionPoint(CrawlingDecissionPoint cdp) {
			this.cdp=cdp;
			return this;
		}

		CrawlingController build() {
			checkNotNull(this.instance);
			checkNotNull(this.storage);
			checkNotNull(this.scheduler);
			checkNotNull(this.dispatcher);
			checkNotNull(this.cip);
			checkNotNull(this.odp);
			checkNotNull(this.cdp);
			return
				new CrawlingController(
					this.cip,
					this.odp,
					this.cdp,
					this.instance,
					this.storage,
					this.scheduler,
					this.dispatcher);

		}

	}

	private final class CrawlingSession {

		private final Timer timer;
		private final long sessionId;

		private CrawlingSession(long sessionId) {
			this.sessionId=sessionId;
			this.timer=new Timer();
		}

		public void start() {
			this.timer.start();
			LOGGER.info("Started crawling {}...",CrawlingController.this.instance);
			fireEvent(CrawlerEventFactory.newCrawlingStartedEvent(this.sessionId,timer.startedOn()));
		}

		public void terminate() {
			this.timer.stop();
			LOGGER.info(
				"Crawling of {} {}. Execution took {}",
				CrawlingController.this.instance,
				isAborted()?"aborted":"completed",
				this.timer);
			fireEvent(createTerminationEvent());
		}

		private CrawlingEvent createTerminationEvent() {
			CrawlingEvent event=null;
			if(!isAborted()) {
				event=CrawlerEventFactory.newCrawlingCompletedEvent(this.sessionId,this.timer.stoppedOn());
			} else {
				event=CrawlerEventFactory.newCrawlingAbortedEvent(this.sessionId,this.timer.stoppedOn());
			}
			return event;
		}

		private void fireEvent(CrawlingEvent event) {
			CrawlingController.this.dispatcher.fireEvent(event);
		}

		private boolean isAborted() {
			return CrawlingController.this.terminate.get();
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CrawlingController.class);

	private final URI instance;
	private final TaskScheduler scheduler;
	private final FileBasedStorage storage;

	private final CrawlingEventDispatcher dispatcher;

	private final CrawlerInformationPoint cip;
	private final OperationDecissionPoint odp;

	@SuppressWarnings("unused")
	private final CrawlingDecissionPoint cdp;

	private final AtomicBoolean terminate;
	private final AtomicLong sessionCounter;
	private final long timeOut;
	private final TimeUnit unit;

	private CrawlingController(
			CrawlerInformationPoint cip,
			OperationDecissionPoint odp,
			CrawlingDecissionPoint cdp,
			URI instance,
			FileBasedStorage storage,
			TaskScheduler scheduler,
			CrawlingEventDispatcher dispatcher) {
		this.instance = instance;
		this.storage = storage;
		this.scheduler = scheduler;
		this.dispatcher = dispatcher;
		this.timeOut = 1;
		this.unit = TimeUnit.SECONDS;
		this.cip = cip;
		this.odp = odp;
		this.cdp = cdp;
		this.sessionCounter=new AtomicLong(0);
		this.terminate=new AtomicBoolean(false);
	}

	@Override
	protected void run() throws Exception {
		boolean completed=false;
		while(!this.terminate.get() && !completed) {
			CrawlingSession session=
				new CrawlingSession(this.sessionCounter.incrementAndGet());
			session.start();
			if(bootstrapCrawling()) {
				awaitCrawlingCompletion();
				persistCrawlerState();
				session.terminate();
			}
			completed = this.odp.canContinueCrawling(this.cip);
			if(!completed) {
				suspendCrawling();
			}
		}
		if(LOGGER.isInfoEnabled()) {
			String message =
				completed?
					"Completed crawling of {}":
					"Aborted crawling {}: termination requested";
			LOGGER.info(message,this.instance);
		}
	}

	@Override
	protected void triggerShutdown() {
		LOGGER.info("Requested crawler termination {}",this.instance);
		this.terminate.set(true);
	}

	private boolean bootstrapCrawling() {
		boolean result=false;
		try {
			Instance existingService=
				this.storage.
					entityOfId(
						this.instance,
						JenkinsEntityType.INSTANCE,
						Instance.class);
			Task task=null;
			if(existingService==null) {
				task=new LoadInstanceTask(this.instance);
			} else {
				task=new RefreshInstanceTask(existingService);
			}
			this.scheduler.schedule(task);
			result=true;
		} catch (IOException e) {
			LOGGER.error("Could not start crawling "+this.instance,e);
		}
		return result;
	}

	private void suspendCrawling() {
		try {
			Delayed crawlingDelay = this.odp.getCrawlingDelay(this.cip);
			LOGGER.info("Suspend crawling of {} for {}",this.instance,crawlingDelay);
			TimeUnit.MILLISECONDS.sleep(
				crawlingDelay.
					getDelay(TimeUnit.MILLISECONDS));
			LOGGER.info("Resuming crawling of {}",this.instance);
		} catch (InterruptedException e) {
			LOGGER.info("Interrupted while suspended crawling of {}",this.instance);
			Thread.currentThread().interrupt();
		}
	}

	private void awaitCrawlingCompletion() {
		while(this.scheduler.hasPendingTasks() && !this.terminate.get()) {
			try {
				this.unit.sleep(this.timeOut);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void persistCrawlerState() {
		try {
			this.storage.save();
		} catch (IOException e) {
			LOGGER.warn("Could not persist crawler state",e);
		}
	}

	static Builder builder() {
		return new Builder();
	}

}