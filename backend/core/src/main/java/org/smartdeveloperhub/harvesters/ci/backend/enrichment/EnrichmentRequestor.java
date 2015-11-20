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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.Connector;
import org.smartdeveloperhub.curator.connector.ConnectorException;
import org.smartdeveloperhub.curator.connector.EnrichmentRequest;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.EnrichmentResultHandler;
import org.smartdeveloperhub.harvesters.ci.backend.util.MemoizingScheduledExecutorService;
import org.smartdeveloperhub.harvesters.ci.backend.util.MoreExecutors;

import com.google.common.base.MoreObjects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

final class EnrichmentRequestor {

	static final class JobMetrics {

		private static final class Counter {

			private final String name;
			private volatile long value;

			private Counter(final String name) {
				this.name = name;
				this.value=0;
			}

			private long get() {
				return this.value;
			}

			private long incrementAndGet() {
				return ++this.value;
			}

			private long decrementAndGet() {
				return --this.value;
			}

			@Override
			public String toString() {
				return this.name+": "+this.value;
			}

		}

		private static final int CREATED_JOBS  =0;
		private static final int PENDING_JOBS  =1;
		private static final int PROCESSED_JOBS=2;
		private static final int FAILED_JOBS   =3;
		private static final int COMPLETED_JOBS=4;

		private final Counter[] metrics=new Counter[5];

		private JobMetrics() {
			this.metrics[CREATED_JOBS]  =new Counter("createdJobs");
			this.metrics[PENDING_JOBS]  =new Counter("pendingJobs");
			this.metrics[PROCESSED_JOBS]=new Counter("processedJobs");
			this.metrics[FAILED_JOBS]   =new Counter("failedJobs");
			this.metrics[COMPLETED_JOBS]=new Counter("completedJobs");
		}

		private long createJob(final EnrichmentContext context) {
			long id=0;
			synchronized(this) {
				this.metrics[PENDING_JOBS].incrementAndGet();
				id=this.metrics[CREATED_JOBS].incrementAndGet();
			}
			LOGGER.trace("Created job #{} for {}",id,context==null?"<termination>":context.pendingEnrichment());
			return id;
		}

		private void jobProcessed(final RequestJob job, final boolean completed) {
			synchronized(this) {
				this.metrics[PENDING_JOBS].decrementAndGet();
				this.metrics[PROCESSED_JOBS].incrementAndGet();
				if(completed) {
					this.metrics[COMPLETED_JOBS].incrementAndGet();
				} else {
					this.metrics[FAILED_JOBS].incrementAndGet();
				}
			}
			LOGGER.trace("{} processing job #{} ({})",completed?"Completed ":"Failed ",job.id(),job.context().pendingEnrichment());
		}

		synchronized long pendingJobs() {
			return this.metrics[PENDING_JOBS].get();
		}

		synchronized long processedJobs() {
			return this.metrics[PROCESSED_JOBS].get();
		}

		synchronized long completedJobs() {
			return this.metrics[COMPLETED_JOBS].get();
		}

		synchronized long failedJobs() {
			return this.metrics[FAILED_JOBS].get();
		}

		@Override
		public synchronized String toString() {
			return Arrays.toString(this.metrics);
		}

	}

	private final class RequestJob implements Runnable {

		private static final int DEFAULT_RETRY_DELAY = 5000;

		private final long id;
		private final EnrichmentContext context;
		private long retries;

		private volatile boolean cancelled;

		private RequestJob(final EnrichmentContext context) {
			this.context=context;
			this.retries=0;
			this.id=EnrichmentRequestor.this.metrics.createJob(context);
		}

		long id() {
			return this.id;
		}

		boolean requiresTermination() {
			return this.context==null;
		}

		EnrichmentContext context() {
			return this.context;
		}

		void cancel() {
			this.cancelled=true;
		}

		long retry() {
			if(!this.cancelled) {
				this.retries++;
				EnrichmentRequestor.this.executor.schedule(
					this,
					DEFAULT_RETRY_DELAY,
					TimeUnit.MILLISECONDS
				);
			}
			return this.retries;
		}

		@Override
		public void run() {
			if(!RequestJob.this.cancelled) {
				EnrichmentRequestor.this.worker.queueJob(RequestJob.this);
			}
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("id",this.id).
						add("retries",this.retries).
						add("context",this.context).
						toString();
		}

	}

	private final class Worker implements Runnable {

		private final Connector connector;
		private final ResolverService resolver;
		private final BlockingQueue<RequestJob> queue;

		private volatile boolean terminated;

		private Worker(final Connector connector, final ResolverService resolver) {
			this.connector=connector;
			this.resolver=resolver;
			this.queue=new LinkedBlockingDeque<RequestJob>();
			this.terminated=false;
		}

		@Override
		public void run() {
			try {
				LOGGER.debug("Starting Enrichment Requestor worker. Awaiting resolver availability...");
				awaitAvailability();
				if(!this.terminated) {
					LOGGER.debug("Resolver is available. Started processing queued request...");
					processJobs();
					LOGGER.debug("Processing of queued requests completed.");
				}
				LOGGER.debug("Enrichment Requestor worker terminated.");
			} catch (final InterruptedException e) {
				LOGGER.trace("Enrichment Requestor worker interrupted",e);
			}
		}

		void queueJob(final RequestJob job) {
			if(!job.requiresTermination() && this.terminated) {
				LOGGER.info("Rejected request job for execution {}",job.context().targetExecution().executionId());
				return;
			}
			while(true) {
				try {
					this.queue.put(job);
					LOGGER.trace("Queued {}",job);
					break;
				} catch (final InterruptedException e) {
					LOGGER.info("Enrichment Requestor interrupted while awaiting for enqueueing {}",job,e);
				}
			}
		}

		void triggerTermination() {
			this.terminated=true;
			queueJob(new RequestJob(null));
		}

		private void processJobs() throws InterruptedException {
			while(true) {
				try {
					final RequestJob job=this.queue.take();
					if(job.requiresTermination()) {
						cancelPendingJobs();
					} else {
						processJob(job);
					}
				} catch (final InterruptedException e) {
					// Ignore interruption. Worker can only be terminated via
					// the triggerTermination method
					LOGGER.trace("Interrupted while waiting for RequestJob",e);
				}
			}
		}

		private void processJob(final RequestJob job) {
			final URI executionResource =
				this.resolver.
					resolveExecution(
						job.context().targetExecution());
			if(executionResource==null) {
				retryJob(job);
			} else {
				completeJob(job, executionResource);
			}
		}

		private void retryJob(final RequestJob job) {
			final long retries = job.retry();
			LOGGER.
				info(
					"Retrying job #{} ({}): could not resolve resource for execution {} ",
					job.id(),
					retries,
					job.context().targetExecution().executionId(),
					retries);
		}

		private void completeJob(final RequestJob job, final URI executionResource) {
			final EnrichmentContext ctx = job.context();
			boolean completed=false;
			try {
				submitEnrichmentRequest(ctx,UseCase.createRequest(executionResource,ctx));
				completed=true;
			} catch (final Exception e) {
				LOGGER.error("Could not process {} ({}). Full stacktrace follows",ctx.pendingEnrichment(),executionResource,e);
			} finally {
				EnrichmentRequestor.this.metrics.jobProcessed(job, completed);
			}
		}

		private void submitEnrichmentRequest(final EnrichmentContext context, final EnrichmentRequest request) {
			try {
				LOGGER.trace("{} submitting {}",context,request);
				this.connector.requestEnrichment(
					request,
					new EnrichmentResultHandler() {
						@Override
						public void onResult(final EnrichmentResult result) {
							processEnrichmentResult(context,request,result);
						}
					}
				);
			} catch (final Exception e) {
				LOGGER.error("Could not submit {} related to {}. Full stacktrace follows",request,context,e);
			}
		}

		private void cancelPendingJobs() {
			final List<RequestJob> pendingJobs=Lists.newArrayList();
			this.queue.drainTo(pendingJobs);
			for(final RequestJob job:pendingJobs) {
				job.cancel();
			}
			if(LOGGER.isTraceEnabled()) {
				final List<URI> executionIds=Lists.newArrayList();
				for(final RequestJob job:pendingJobs) {
					executionIds.add(job.context().targetExecution().executionId());
				}
				LOGGER.debug("Cancelled {} pending execution enrichment request jobs ({})",pendingJobs.size(),executionIds);
			}
		}

		private void processEnrichmentResult(final EnrichmentContext context, final EnrichmentRequest request, final EnrichmentResult result) {
			LOGGER.debug("Processing enrichment result {} about {} ({})",result,request,context);
			final ExecutionEnrichment enrichment=UseCase.processResult(context,result);
			try {
				EnrichmentRequestor.this.service.addEnrichment(context,enrichment);
			} catch (final IOException e) {
				LOGGER.warn("Processing of enrichment result {} about {} ({}) failed. Full stacktrace follows",result,request,context,e);
			}
		}

		// TODO: Use an exponential back-off delay
		private void awaitAvailability() throws InterruptedException {
			while(!this.resolver.isReady() && !this.terminated) {
				TimeUnit.MILLISECONDS.sleep(3000);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(EnrichmentRequestor.class);

	private final Worker worker;
	private final MemoizingScheduledExecutorService executor;
	private final EnrichmentService service;
	private final JobMetrics metrics;

	private boolean started;

	EnrichmentRequestor(final EnrichmentService service, final Connector connector, final ResolverService resolver) {
		this.service = service;
		this.metrics = new JobMetrics();
		this.worker = new Worker(connector,resolver);
		final ThreadFactory threadFactory =
			new ThreadFactoryBuilder().
				setNameFormat("EnrichmentRequestor-worker-%d").
				setPriority(Thread.MAX_PRIORITY).
				setUncaughtExceptionHandler(
					new UncaughtExceptionHandler() {
						@Override
						public void uncaughtException(final Thread t, final Throwable e) {
							LOGGER.error("Requestor thread {} died unexpectedly",t,e);
						}
					}).
				build();
		this.executor=MoreExecutors.newMemoizingScheduledExecutorService(2, threadFactory);
		this.started=false;
	}

	void enqueueRequest(final EnrichmentContext context) {
		if(context.requiresCommit()) {
			this.worker.queueJob(new RequestJob(context));
		}
	}

	JobMetrics metrics() {
		return this.metrics;
	}

	void start() throws IOException {
		LOGGER.info("Starting Enrichment Requestor...");
		try {
			this.worker.connector.connect();
			this.executor.submit(this.worker);
			this.started=true;
			LOGGER.info("Enrichment Requestor started.");
		} catch (final ConnectorException e) {
			LOGGER.error("Could not initialize the curator connector. Full stacktrace follows",e);
			throw new IOException("Could not initialize the curator connector",e);
		}
	}

	void stop() throws IOException {
		LOGGER.info("Stopping Enrichment Requestor...");
		if(this.started) {
			this.worker.triggerTermination();
			shutdownPoolGracefully(5,TimeUnit.SECONDS);
			try {
				this.worker.connector.disconnect();
			} catch (final ConnectorException e) {
				LOGGER.error("Could not disconnect the curator connector. Full stacktrace follows",e);
				throw new IOException("Could not disconnect the curator connector",e);
			}
		}
		LOGGER.info("Enrichment Requestor stopped.");
	}

	private void shutdownPoolGracefully(final int period, final TimeUnit unit) {
		this.executor.shutdown();
		final Stopwatch watch=Stopwatch.createStarted();
		while(!this.executor.isTerminated() && watch.elapsed(unit)<period) {
			try {
				this.executor.awaitTermination(500, TimeUnit.MILLISECONDS);
			} catch (final InterruptedException e) {
				LOGGER.trace("Enrichment Requestor interrupted while awaiting for termination",e);
				Thread.currentThread().interrupt();
			}
		}
		if(!this.executor.isTerminated()) {
			final List<Runnable> unfinished = this.executor.shutdownNow();
			logAbortedRequestJobs(unfinished);
		}
	}

	private void logAbortedRequestJobs(final List<Runnable> unfinished) {
		if(LOGGER.isTraceEnabled()) {
			final List<URI> aborted=Lists.newArrayList();
			for(final Runnable runnable:unfinished) {
				final RequestJob job=this.executor.unwrap(runnable,RequestJob.class);
				if(job!=null) {
					aborted.add(job.context().targetExecution().executionId());
				}
			}
			LOGGER.trace("Aborted {} pending execution enrichment requests ({})",aborted.size(),aborted);
		}
	}

}