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
package org.smartdeveloperhub.harvesters.ci.backend.core.port.jenkins;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.Command;
import org.smartdeveloperhub.harvesters.ci.backend.core.infrastructure.persistence.jpa.PersistencyFacade;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class CommandProcessorService extends AbstractExecutionThreadService {

	private static final class Poison implements Command {

		private final static CommandProcessorService.Poison SINGLETON=new Poison();

		private Poison() {
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProcessorService.class);

	private final LinkedBlockingQueue<Command> commandQueue;
	private final PersistencyFacade facade;
	@SuppressWarnings("unused")
	private final ContinuousIntegrationService service;

	CommandProcessorService(LinkedBlockingQueue<Command> commandQueue, PersistencyFacade facade, ContinuousIntegrationService service) {
		this.commandQueue = commandQueue;
		this.facade = facade;
		this.service = service;
	}

	@Override
	protected void run() throws Exception {
		Command command=null;
		while((command=commandQueue.take())!=Poison.SINGLETON) {
			facade.beginTransaction();
			try {
				LOGGER.trace("TODO: process command {}",command);
				facade.commitTransaction();
				// TODO: from here we should propagate the creation to the frontend
			} catch(Exception exception) {
				facade.rollbackTransaction();
			}
		}
	}

	@Override
	protected void triggerShutdown() {
		commandQueue.offer(Poison.SINGLETON);
	}

}