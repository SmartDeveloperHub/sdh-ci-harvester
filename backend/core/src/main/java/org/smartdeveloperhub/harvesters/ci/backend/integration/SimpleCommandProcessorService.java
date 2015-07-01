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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.command.Command;
import org.smartdeveloperhub.harvesters.ci.backend.command.CommandVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;
import org.smartdeveloperhub.jenkins.crawler.util.ListenerManager;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class SimpleCommandProcessorService extends AbstractExecutionThreadService {

	private static final class Poison implements Command {

		private static final SimpleCommandProcessorService.Poison SINGLETON=new Poison();

		private Poison() {
		}

		@Override
		public void accept(CommandVisitor visitor) {
			throw new UnsupportedOperationException("Poison command is not visitable");
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(SimpleCommandProcessorService.class);

	private final CommandProcessingMonitor monitor;
	private final CommandProcessor processor;
	private final ListenerManager<EntityLifecycleEventListener> listeners;

	private volatile boolean shuttingDown;

	SimpleCommandProcessorService(CommandProcessingMonitor monitor, TransactionManager manager, ContinuousIntegrationService service, ListenerManager<EntityLifecycleEventListener> listeners) {
		this.monitor = monitor;
		this.listeners = listeners;
		this.processor=CommandProcessor.newInstance(manager,service);
		this.shuttingDown=false;
	}

	@Override
	protected void run() {
		do {
			processCommands();
		} while(!this.shuttingDown);
		LOGGER.info("Command processing terminated.");
	}

	private void processCommands() {
		Command command=null;
		try {
			while((command=this.monitor.take())!=Poison.SINGLETON) {
				processCommand(command);
			}
		} catch (InterruptedException e) {
			LOGGER.info("Interrupted while waiting for command",e);
		}
	}

	private void processCommand(Command command) {
		try {
			if(!this.processor.processCommand(command)) {
				this.monitor.retryLater(command);
			} else {
				EntityLifecycleEventCreator creator = new EntityLifecycleEventCreator();
				command.accept(creator);
				this.listeners.notify(new EntityLifecycleEventNotification(creator.getEvent()));
			}
		} catch (CommandProcessingException e) {
			LOGGER.debug("Failed to consume command",e);
		}
	}

	@Override
	protected void triggerShutdown() {
		this.shuttingDown=true;
		this.monitor.offer(Poison.SINGLETON);
		LOGGER.info("Requested command processing termination...");
	}

}