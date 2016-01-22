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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-dist-0.3.0-SNAPSHOT.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.curator;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.Notifier;
import org.smartdeveloperhub.curator.connector.UseCase;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.EnrichmentResponseMessage;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class HarvesterCurator {

	private static final Logger LOGGER = LoggerFactory.getLogger(HarvesterCurator.class);

	private static final class Worker implements Runnable {

		private static final class ResponseMemoizer extends Notifier {

			private final List<UUID> answeredRequests;
			private final TestingResponseProvider provider;

			private ResponseMemoizer(final TestingResponseProvider provider) {
				this.provider=provider;
				this.answeredRequests=Lists.newArrayList();
			}

			@Override
			public synchronized void onEnrichmentResponse(final EnrichmentResponseMessage response) {
				this.answeredRequests.add(response.responseTo());
			}

			private synchronized List<Action> submittedActions() {
				final List<Action> acknowledged=Lists.newArrayList();
				for(final Action action:this.provider.actions()) {
					if(this.answeredRequests.contains(action.requestId())) {
						acknowledged.add(action);
					}
				}
				return acknowledged;
			}

		}

		private final TestingCurator curator;
		private final Agent agent;
		private final TestingResponseProvider provider;
		private final Object lock;
		private boolean terminate;
		private final ResponseMemoizer memoizer;

		private Worker() {
			this.provider = new TestingResponseProvider(LOGGER);
			this.memoizer = new ResponseMemoizer(this.provider);
			this.curator =
				TestingCurator.
					builder().
						withResponseProvider(this.provider).
						withNotifier(this.memoizer).
						withConnectorConfiguration(
							ProtocolFactory.
								newDeliveryChannel().
									withRoutingKey("curator.testing").
									build()).
						withConversionContext(
							ConversionContext.
								newInstance().
									withNamespacePrefix(UseCase.CI_NAMESPACE,"ci").
									withNamespacePrefix(UseCase.SCM_NAMESPACE,"scm").
										withNamespacePrefix(UseCase.DOAP_NAMESPACE,"doap")).
						build();
			this.agent=ProtocolFactory.newAgent().withAgentId(UUID.randomUUID()).build();
			this.terminate=false;
			this.lock=new Object();
		}

		@Override
		public void run() {
			LOGGER.info("Starting testing curator [{}]...",this.agent.agentId());
			this.curator.connect(this.agent);
			LOGGER.info("Awaiting for enrichment requests...");
			awaitTermination();
			LOGGER.info("Terminating testing curator...");
			this.curator.disconnect();
		}

		private void awaitTermination() {
			synchronized(this.lock) {
				while(!this.terminate) {
					try {
						this.lock.wait();
					} catch (final InterruptedException e) {
						// IGNORE
					}
				}
			}
		}

		private void terminate() {
			synchronized(this.lock) {
				this.terminate=true;
				this.lock.notify();
			}
		}
	}

	private final ExecutorService executor;
	private final Worker worker;

	public HarvesterCurator() {
		this.executor =
			Executors.
				newSingleThreadExecutor(
					new ThreadFactoryBuilder().
						setNameFormat("testing-curator").
						setPriority(Thread.MAX_PRIORITY).
						setUncaughtExceptionHandler(
							new UncaughtExceptionHandler() {
								@Override
								public void uncaughtException(final Thread t, final Throwable e) {
									LOGGER.error("Testing harvester unexpected termination",e);
								}
							}).
						build());
		this.worker=new Worker();
	}

	public void start() {
		this.executor.execute(this.worker);
	}

	public List<Action> actionsUndertaken() {
		return this.worker.memoizer.submittedActions();
	}

	public void stop() {
		this.worker.terminate();
		this.executor.shutdown();
		int tries=0;
		while(!this.executor.isTerminated() && tries<5) {
			try {
				this.executor.awaitTermination(1, TimeUnit.SECONDS);
				tries++;
			} catch (final InterruptedException e) {
				// IGNORE
			}
		}
		if(!this.executor.isTerminated()) {
			this.executor.shutdownNow();
		}
	}

}