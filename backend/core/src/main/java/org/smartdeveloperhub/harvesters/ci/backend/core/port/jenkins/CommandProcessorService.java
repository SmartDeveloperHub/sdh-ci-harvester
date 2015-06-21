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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.Command;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CommandVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.UpdateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionManager;

import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class CommandProcessorService extends AbstractExecutionThreadService {

	private static final class Poison implements Command {

		private static final CommandProcessorService.Poison SINGLETON=new Poison();

		private Poison() {
		}

		@Override
		public void accept(CommandVisitor visitor) {
			throw new UnsupportedOperationException("Poison command is not visitable");
		}

	}

	private final class CommandDispatchingVisitor implements CommandVisitor {

		@Override
		public void visitRegisterServiceCommand(RegisterServiceCommand command) {
			service.registerService(command);
		}

		@Override
		public void visitCreateBuildCommand(CreateBuildCommand command) {
			service.createBuild(command);
		}

		@Override
		public void visitUpdateBuildCommand(UpdateBuildCommand command) {
			service.updateBuild(command);
		}

		@Override
		public void visitDeleteBuildCommand(DeleteBuildCommand command) {
			service.deleteBuild(command);
		}

		@Override
		public void visitCreateExecutionCommand(CreateExecutionCommand command) {
			service.createExecution(command);
		}

		@Override
		public void visitFinishExecutionCommand(FinishExecutionCommand command) {
			service.finishExecution(command);
		}

		@Override
		public void visitDeleteExecutionCommand(DeleteExecutionCommand command) {
			service.deleteExecution(command);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProcessorService.class);

	private final CommandProcessingMonitor monitor;
	private final TransactionManager manager;
	private final ContinuousIntegrationService service;
	private final CommandDispatchingVisitor dispatcher;

	private volatile boolean shuttingDown;

	CommandProcessorService(CommandProcessingMonitor monitor, TransactionManager manager, ContinuousIntegrationService service) {
		this.monitor = monitor;
		this.manager = manager;
		this.service = service;
		this.shuttingDown=false;
		this.dispatcher=new CommandDispatchingVisitor();
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
		Transaction tx = this.manager.currentTransaction();
		try {
			processTransactionally(tx, command);
		} catch (TransactionException e) {
			LOGGER.error("Transactional failure when processing command "+command,e);
		}
	}

	private void processTransactionally(Transaction tx, Command command) throws TransactionException {
		tx.begin();
		try {
			command.accept(this.dispatcher);
			tx.commit();
			LOGGER.trace("Processed command {}",command);
		} catch(TransactionException e) {
			throw e;
		} catch(Exception e) {
			LOGGER.error("Could not process command "+command,e);
			tx.rollback();
		}
	}

	@Override
	protected void triggerShutdown() {
		this.shuttingDown=true;
		this.monitor.offer(Poison.SINGLETON);
		LOGGER.info("Requested command processing termination...");
	}

}