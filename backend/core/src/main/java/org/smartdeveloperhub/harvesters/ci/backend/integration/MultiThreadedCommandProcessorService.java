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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.Command;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CommandVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

final class MultiThreadedCommandProcessorService extends AbstractExecutionThreadService {

	private static final class Poison implements Command {

		private static final MultiThreadedCommandProcessorService.Poison SINGLETON=new Poison();

		private Poison() {
		}

		@Override
		public void accept(final CommandVisitor visitor) {
			throw new UnsupportedOperationException("Poison command is not visitable");
		}

	}

	private final class CommandProcessingTask implements Runnable {

		private final Command command;

		public CommandProcessingTask(final Command command) {
			this.command = command;
		}

		@Override
		public void run() {
			try {
				if(!MultiThreadedCommandProcessorService.this.processor.processCommand(this.command)) {
					MultiThreadedCommandProcessorService.this.monitor.retryLater(this.command);
				}
			} catch (final CommandProcessingException e) {
				LOGGER.debug("Failed to consume command",e);
			}
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(MultiThreadedCommandProcessorService.class);

	private final CommandProcessingMonitor monitor;

	private final CommandProcessor processor;

	private volatile boolean shuttingDown;

	private ExecutorService executor;

	MultiThreadedCommandProcessorService(final CommandProcessingMonitor monitor, final TransactionManager manager, final ContinuousIntegrationService ciService, final EnrichmentService erService) {
		this.monitor = monitor;
		this.processor=CommandProcessor.newInstance(manager,ciService,erService);
		this.shuttingDown=false;
	}

	@Override
	protected void run() {
		this.executor=createExecutor();
		do {
			processCommands();
		} while(!this.shuttingDown);
		this.executor.shutdownNow();
		LOGGER.info("Command processing terminated.");
	}

	private ExecutorService createExecutor() {
		return
			Executors.
				newCachedThreadPool(
					new ThreadFactoryBuilder().
						setNameFormat("CommandProcessor-worker-%d").
						setUncaughtExceptionHandler(
							new UncaughtExceptionHandler() {
								@Override
								public void uncaughtException(final Thread t, final Throwable e) {
									LOGGER.error(String.format("Thread %s died",t.getName()),e);
								}
							}
						).
						build()
				);
	}

	private void processCommands() {
		Command command=null;
		try {
			while((command=this.monitor.take())!=Poison.SINGLETON) {
				this.executor.execute(new CommandProcessingTask(command));
			}
		} catch (final InterruptedException e) {
			LOGGER.info("Interrupted while waiting for command",e);
		}
	}


	@Override
	protected void triggerShutdown() {
		this.shuttingDown=true;
		this.monitor.offer(Poison.SINGLETON);
		LOGGER.info("Requested command processing termination...");
	}

}