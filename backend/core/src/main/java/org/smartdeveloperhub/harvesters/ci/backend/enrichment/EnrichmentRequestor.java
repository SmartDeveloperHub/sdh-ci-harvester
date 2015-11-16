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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.Connector;
import org.smartdeveloperhub.curator.connector.ConnectorException;
import org.smartdeveloperhub.curator.connector.EnrichmentRequest;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.EnrichmentResultHandler;

final class EnrichmentRequestor {

	private final class Worker implements Runnable {

		private final Connector connector;
		private final ResolverService resolver;
		private final BlockingQueue<EnrichmentContext> queue;
		private volatile boolean terminate;

		private Worker(final Connector connector, final ResolverService resolver) {
			this.connector=connector;
			this.resolver=resolver;
			this.queue=new LinkedBlockingDeque<EnrichmentContext>();
			this.terminate=false;
		}

		private void queueJob(final EnrichmentContext context) {
			while(true) {
				try {
					this.queue.put(context);
					break;
				} catch (final InterruptedException e) {
					LOGGER.info("Enrichment Requestor interrupted while awaiting for enqueueing {}",context,e);
				}
			}
		}

		private void processJobs() throws InterruptedException {
			while(true) {
				final EnrichmentContext context=this.queue.take();
				if(context==EnrichmentContext.NULL) {
					break;
				}
				final EnrichmentRequest request=UseCase.createRequest(this.resolver.resolveExecution(context.targetExecution()),context);
				try {
					LOGGER.trace("{} submitting {}",context,request);
					this.connector.requestEnrichment(
						request,
						new EnrichmentResultHandler() {
							@Override
							public void onResult(final EnrichmentResult result) {
								LOGGER.warn("Handling of result {} for context {} is still to be implemented",result,context);
							}
						}
					);
				} catch (final IOException e) {
					LOGGER.warn("Could not request enrichment {} ({}). Full stacktrace follows",context,request,e);
					// TODO: Think about how to handle the failure. Propagate?
				}
			}
		}

		// TODO: Use an exponential back-off delay
		private void awaitAvailability() throws InterruptedException {
			while(!this.resolver.isReady() && !this.terminate) {
				TimeUnit.MILLISECONDS.sleep(3000);
			}
		}

		private void triggerTermination() {
			this.terminate=true;
			queueJob(EnrichmentContext.NULL);
		}

		@Override
		public void run() {
			try {
				LOGGER.debug("Starting Enrichment Requestor worker. Awaiting resolver availability...");
				awaitAvailability();
				if(!this.terminate) {
					LOGGER.debug("Resolver is availble. Started processing queued request...");
					processJobs();
					LOGGER.debug("Processing of queued requests completed.");
				}
				LOGGER.debug("Enrichment Requestor worker terminated.");
			} catch (final InterruptedException e) {
				LOGGER.trace("Enrichment Requestor worker interrupted",e);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(EnrichmentService.class);


	private final Thread thread;
	private final Worker worker;
	private boolean started;

	EnrichmentRequestor(final Connector connector, final ResolverService resolver) {
		this.worker = new Worker(connector,resolver);
		this.thread = new Thread(this.worker,"EnrichmentRequestor");
		this.thread.setUncaughtExceptionHandler(
			new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(final Thread t, final Throwable e) {
					LOGGER.error("Requestor thread died. Full stacktrace follows",e);
				}
			}
		);
		this.started=false;
	}

	void enqueueRequest(final EnrichmentContext context) {
		if(context.requiresCommit()) {
			this.worker.queueJob(context);
		}
	}

	void stop() throws IOException {
		LOGGER.info("Stopping Enrichment Requestor...");
		if(this.started) {
			this.worker.triggerTermination();
			try {
				this.thread.join();
			} catch (final InterruptedException e) {
				LOGGER.trace("Enrichment Requestor interrupted while awaiting for daemon thread termination",e);
			}
			try {
				this.worker.connector.disconnect();
			} catch (final ConnectorException e) {
				LOGGER.error("Could not disconnect the curator connector. Full stacktrace follows",e);
				throw new IOException("Could not disconnect the curator connector",e);
			}
		}
		LOGGER.info("Enrichment Requestor stopped.");
	}

	void start() throws IOException {
		LOGGER.info("Starting Enrichment Requestor...");
		try {
			this.worker.connector.connect();
			this.thread.start();
			this.started=true;
			LOGGER.info("Enrichment Requestor started.");
		} catch (final ConnectorException e) {
			LOGGER.error("Could not initialize the curator connector. Full stacktrace follows",e);
			throw new IOException("Could not initialize the curator connector",e);
		}
	}

}