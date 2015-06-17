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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventFactory;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingEvent;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.util.ControlledScheduledExecutorService;
import org.smartdeveloperhub.jenkins.crawler.util.Timer;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

final class CrawlingController {

	private final class CrawlingBootstrap implements Runnable {

		final class CrawlingSession {

			private final Timer timer;
			private final long sessionId;

			private CrawlingSession() {
				this.sessionId=session.incrementAndGet();
				this.timer=new Timer();
			}

			public void start() {
				this.timer.start();
				LOGGER.info("Started crawling {}...",jenkinsInstance());
				fireEvent(CrawlerEventFactory.newCrawlingStartedEvent(sessionId,timer.startedOn()));
			}

			public void terminate() {
				this.timer.stop();
				fireEvent(createTerminationEvent(this.sessionId,this.timer));
				LOGGER.info(
					"Crawling of {} {}. Execution took {}",
					jenkinsInstance(),
					isAborted()?
						"aborted":
						"completed",
					timer);
			}

			private CrawlingEvent createTerminationEvent(long sessionId, Timer timer) {
				CrawlingEvent event=null;
				if(!isAborted()) {
					event=CrawlerEventFactory.newCrawlingCompletedEvent(sessionId,timer.stoppedOn());
				} else {
					event=CrawlerEventFactory.newCrawlingAbortedEvent(sessionId,timer.stoppedOn());
				}
				return event;
			}

			private boolean isAborted() {
				return CrawlingBootstrap.this.terminate.get();
			}

		}

		private final AtomicBoolean terminate;
		private final AtomicLong session;
		private final long timeOut;
		private final TimeUnit unit;

		private CrawlingBootstrap(long timeOut, TimeUnit unit) {
			this.timeOut = timeOut;
			this.unit = unit;
			this.session=new AtomicLong(0);
			this.terminate=new AtomicBoolean(false);
		}

		@Override
		public void run() {
			if(this.terminate.get()) {
				LOGGER.info(
					"Aborted crawling {}: termination requested",
					jenkinsInstance());
				return;
			}

			CrawlingSession session=new CrawlingSession();
			session.start();
			if(bootstrapCrawling()) {
				awaitCompletion();
				persistState();
				session.terminate();
			}
		}

		private void fireEvent(CrawlingEvent event) {
			CrawlingController.this.dispatcher.fireEvent(event);
		}

		private boolean bootstrapCrawling() {
			boolean result=false;
			try {
				Service existingService=
					storage.
						entityOfId(
							jenkinsInstance(),
							JenkinsEntityType.SERVICE,
							Service.class);
				Task task=null;
				if(existingService==null) {
					task=new LoadServiceTask(jenkinsInstance());
				} else {
					task=new RefreshServiceTask(existingService);
				}
				taskScheduler().schedule(task);
				result=true;
			} catch (IOException e) {
				LOGGER.error("Could not start crawling {}",jenkinsInstance());
			}
			return result;
		}

		private URI jenkinsInstance() {
			return CrawlingController.this.instance;
		}

		private void awaitCompletion() {
			while(taskScheduler().hasPendingTasks() && !this.terminate.get()) {
				try {
					this.unit.sleep(this.timeOut);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}

		private void terminate() {
			this.terminate.set(true);
		}

		private TaskScheduler taskScheduler() {
			return CrawlingController.this.scheduler;
		}

	}

	static class Builder {

		private TaskScheduler scheduler;
		private URI instance;
		private FileBasedStorage storage;
		private CrawlingEventDispatcher dispatcher;

		private Builder() {
		}

		private ScheduledExecutorService createPool() {
			return
				ControlledScheduledExecutorService.
					builder().
						withPoolSize(1).
						withThreadFactory(
							new ThreadFactoryBuilder().
								setNameFormat("JenkinsCrawler-admin-%d").
								build()
						).
						build();
		}

		Builder withTaskScheduler(TaskScheduler scheduler) {
			this.scheduler = scheduler;
			return this;
		}

		Builder withCrawlingEventDispatcher(CrawlingEventDispatcher dispatcher) {
			this.dispatcher = dispatcher;
			return this;
		}

		Builder withJenkinsInstance(URI instance) {
			this.instance = instance;
			return this;
		}

		Builder withStorage(FileBasedStorage manager) {
			this.storage=manager;
			return this;
		}

		CrawlingController build() {
			checkNotNull(this.instance);
			checkNotNull(this.storage);
			checkNotNull(this.scheduler);
			checkNotNull(this.dispatcher);
			return new CrawlingController(this.instance,this.storage,this.scheduler,createPool(),this.dispatcher);

		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CrawlingController.class);

	private final ScheduledExecutorService pool;
	private final CrawlingBootstrap bootstrap;

	private final URI instance;
	private final TaskScheduler scheduler;
	private final FileBasedStorage storage;

	private final CrawlingEventDispatcher dispatcher;

	private CrawlingController(URI instance, FileBasedStorage storage, TaskScheduler scheduler, ScheduledExecutorService pool, CrawlingEventDispatcher dispatcher) {
		this.instance = instance;
		this.storage = storage;
		this.scheduler = scheduler;
		this.pool = pool;
		this.dispatcher = dispatcher;
		this.bootstrap=new CrawlingBootstrap(1,TimeUnit.SECONDS);
	}

	private void persistState() {
		try {
			CrawlingController.this.storage.save();
		} catch (IOException e) {
			LOGGER.warn("Could not persist crawler state",e);
		}
	}

	void start() {
		this.pool.
			scheduleWithFixedDelay(
				bootstrap,
				1,
				60,
				TimeUnit.SECONDS);
	}

	void stop() {
		this.bootstrap.terminate();
		this.pool.shutdown();
		while(!this.pool.isTerminated()) {
			try {
				this.pool.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	static Builder builder() {
		return new Builder();
	}

}