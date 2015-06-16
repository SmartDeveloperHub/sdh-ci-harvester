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
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.util.Timer;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Service;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

final class CrawlingController {

	static class Builder {

		private TaskScheduler scheduler;
		private URI instance;
		private FileBasedStorage storage;

		private Builder() {
		}

		Builder withTaskScheduler(TaskScheduler scheduler) {
			this.scheduler = scheduler;
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
			return new CrawlingController(this.instance,this.storage,this.scheduler);

		}

	}

	private static final class ControlledScheduledExecutorService extends ScheduledThreadPoolExecutor {

		private ControlledScheduledExecutorService(int corePoolSize, ThreadFactory threadFactory) {
			super(corePoolSize, threadFactory);
		}

		@Override
		protected void afterExecute(Runnable r, Throwable t) {
			super.afterExecute(r,t);
			LOGGER.debug("Runnable {} completed",r.getClass().getName());
			if(t == null && r instanceof Future<?>) {
				try {
					Future<?> future = (Future<?>) r;
					if(future.isDone()) {
						future.get();
					}
				} catch (CancellationException ce) {
					t = ce;
				} catch (ExecutionException ee) {
					t = ee.getCause();
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt(); // ignore/reset
				}
			}
			if(t != null) {
				LOGGER.error(String.format("Runnable %s died",r.getClass().getName()),t);
			}
		}
	}

	final class CrawlingBootstrap implements Runnable {

		private final long timeOut=1;
		private final TimeUnit unit=TimeUnit.SECONDS;
		private final AtomicBoolean terminate=new AtomicBoolean(false);

		@Override
		public void run() {
			if(this.terminate.get()) {
				LOGGER.info(
					"Aborted crawling {}: termination requested",
					jenkinsInstance());
				return;
			}
			Timer timer=logStart();
			boolean started=bootstrapCrawling();
			if(started) {
				awaitCompletion();
			}
			logTermination(started,timer);
			persistState();
		}

		private boolean bootstrapCrawling() {
			boolean result=false;
			try {
				Service existingService = storage.entityOfId(jenkinsInstance(),JenkinsEntityType.SERVICE,Service.class);
				Task task=null;
				if(existingService==null) {
					task=new LoadServiceTask(jenkinsInstance());
				} else {
					task=new RefreshServiceTask(existingService);
				}
				taskScheduler().schedule(task);
				result=true;
			} catch (IOException e) {
				LOGGER.error("Could not bootstrap crawling process",e);
			}
			return result;
		}

		private URI jenkinsInstance() {
			return CrawlingController.this.instance;
		}

		private Timer logStart() {
			LOGGER.info("Started crawling {}...",jenkinsInstance());
			Timer timer = new Timer();
			timer.start();
			return timer;
		}

		private void logTermination(boolean started, Timer timer) {
			timer.stop();
			if(started) {
				LOGGER.info(
						"Crawling of {} {}. Execution took {}",
						jenkinsInstance(),
						terminate.get()?
							"aborted":
							"completed",
						timer);
			} else {
				LOGGER.error("Could not start crawling {}",jenkinsInstance());
			}
		}

		private void awaitCompletion() {
			while(taskScheduler().hasPendingTasks() && !terminate.get()) {
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

	private static final Logger LOGGER=LoggerFactory.getLogger(CrawlingController.class);

	private final ScheduledExecutorService pool;
	private final CrawlingBootstrap bootstrap;

	private final URI instance;
	private final TaskScheduler scheduler;
	private final FileBasedStorage storage;

	private CrawlingController(URI instance, FileBasedStorage storage, TaskScheduler scheduler) {
		this.instance = instance;
		this.storage = storage;
		this.scheduler = scheduler;
		this.pool=createPool();
		this.bootstrap=new CrawlingBootstrap();
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

	private void persistState() {
		try {
			CrawlingController.this.storage.save();
		} catch (IOException e) {
			LOGGER.warn("Could not persist crawler state",e);
		}
	}

	static Builder builder() {
		return new Builder();
	}

	private static ScheduledExecutorService createPool() {
		ThreadFactory threadFactory =
			new ThreadFactoryBuilder().
				setNameFormat("JenkinsCrawler-admin-%d").
				build();
		return
			new ControlledScheduledExecutorService(1, threadFactory);
	}

}