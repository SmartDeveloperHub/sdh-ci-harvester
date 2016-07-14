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
package org.smartdeveloperhub.jenkins.crawler;

import static com.google.common.base.Preconditions.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

final class MultiThreadedTaskScheduler implements TaskScheduler {

	static final class Builder {

		private static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

		private Context context;
		private int threads=MAX_THREADS;

		private Builder() {
		}

		Builder withNumberOfThreads(int threads) {
			checkArgument(threads>0,"Thread number must be greater than 0 (%s)",threads);
			checkArgument(threads<=MAX_THREADS,"Thread number cannot be greater than %s (%s)",MAX_THREADS,threads);
			this.threads = threads;
			return this;
		}

		Builder withContext(Context context) {
			checkNotNull(context,"Context cannot be null");
			this.context = context;
			return this;
		}

		MultiThreadedTaskScheduler build() {
			return new MultiThreadedTaskScheduler(this.context, this.threads);
		}

	}

	private final class RunnableTask implements Runnable {

		private final Task task;

		private RunnableTask(Task task) {
			this.task = task;
		}

		private Task task() {
			return task;
		}

		@Override
		public void run() {
			LOGGER.debug("Starting task [{}]...",task().id());
			try {
				task().execute(context);
				executedTasks.incrementAndGet();
				LOGGER.debug("Task [{}] completed succesfully.",task().id());
			} catch(Exception e) {
				failedTasks.incrementAndGet();
				LOGGER.debug("Task ["+task().id()+"] failed. Full stacktrace follows: ",e);
			} finally {
				pendingTasks.decrementAndGet();
			}
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(MultiThreadedTaskScheduler.class);

	private final Context    context;
	private final int        threads;
	private final AtomicLong pendingTasks;
	private final AtomicLong executedTasks;
	private final AtomicLong failedTasks;

	private ExecutorService pool;

	private MultiThreadedTaskScheduler(Context context, int threads) {
		this.threads = threads;
		this.context = context;
		this.pendingTasks=new AtomicLong();
		this.executedTasks=new AtomicLong();
		this.failedTasks=new AtomicLong();
	}

	@Override
	public void start() {
		checkState(this.pool==null,"Scheduler already started");
		LOGGER.info("Starting task scheduler...");
		this.pool =
			Executors.
				newFixedThreadPool(
					this.threads,
					new ThreadFactoryBuilder().
						setNameFormat("JenkinsCrawler-worker-%d").
						setUncaughtExceptionHandler(
							new UncaughtExceptionHandler() {
								@Override
								public void uncaughtException(Thread t, Throwable e) {
									LOGGER.error(String.format("Thread %s died",t.getName()),e);
									failedTasks.incrementAndGet();
								}
							}).
						build());
		LOGGER.info("Task scheduler started.");
	}

	@Override
	public void stop() {
		checkState(this.pool!=null,"Scheduler not started");
		LOGGER.info("Stopping task scheduler...");
		List<Runnable> pendingRunnables=this.pool.shutdownNow();
		this.pendingTasks.set(0);
		while(!this.pool.isTerminated()) {
			try {
				this.pool.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("Task scheduler stopped.");
			if(pendingRunnables.isEmpty()) {
				LOGGER.debug("- No pending tasks aborted.");
			} else {
				LOGGER.debug("- Aborted {} pending tasks.",pendingRunnables.size());
			}
			LOGGER.debug(
				"- Processed {} tasks. {} completed succesfully, {} failed.",
				this.executedTasks.get()+this.failedTasks.get(),
				this.executedTasks,
				this.failedTasks);
		}
	}

	@Override
	public void schedule(final Task task) {
		checkState(this.pool!=null,"Scheduler not started");
		if(this.pool.isShutdown()) {
			LOGGER.debug("Task [{}] rejected. Scheduler is shutting down",task.id());
			return;
		}
		this.pendingTasks.incrementAndGet();
		LOGGER.debug("Scheduled task [{}]",task.id());
		this.pool.execute(new RunnableTask(task));
	}

	@Override
	public boolean hasPendingTasks() {
		return this.pendingTasks.get()!=0;
	}

	static Builder builder() {
		return new Builder();
	}

}