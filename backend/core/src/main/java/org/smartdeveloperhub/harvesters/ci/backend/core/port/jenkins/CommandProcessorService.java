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

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.google.common.util.concurrent.ThreadFactoryBuilder;

final class CommandProcessorService extends AbstractExecutionThreadService {

	private final class CommandProcessor implements Runnable {

		private final Command command;

		private CommandProcessor(Command command) {
			this.command=command;
		}

		@Override
		public void run() {
			processCommand(command);
		}
	}

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

		private boolean retry;

		private CommandDispatchingVisitor() {
			this.retry=true;
		}

		@Override
		public void visitRegisterServiceCommand(RegisterServiceCommand command) {
			service.registerService(command);
			this.retry=false;
		}

		@Override
		public void visitCreateBuildCommand(CreateBuildCommand command) {
			URI parentId = command.subBuildOf();
			if(parentId!=null && service.getBuild(parentId)!=null) {
				service.createBuild(command);
				this.retry=false;
			} else if(service.getService(command.serviceId())!=null) {
				service.createBuild(command);
				this.retry=false;
			}
		}

		@Override
		public void visitUpdateBuildCommand(UpdateBuildCommand command) {
			if(service.getBuild(command.buildId())!=null) {
				service.updateBuild(command);
				this.retry=false;
			}
		}

		@Override
		public void visitDeleteBuildCommand(DeleteBuildCommand command) {
			if(service.getBuild(command.buildId())!=null) {
				service.deleteBuild(command);
				this.retry=false;
			}
		}

		@Override
		public void visitCreateExecutionCommand(CreateExecutionCommand command) {
			if(service.getBuild(command.buildId())!=null) {
				service.createExecution(command);
				this.retry=false;
			}
		}

		@Override
		public void visitFinishExecutionCommand(FinishExecutionCommand command) {
			if(service.getExecution(command.executionId())!=null) {
				service.finishExecution(command);
				this.retry=false;
			}
		}

		@Override
		public void visitDeleteExecutionCommand(DeleteExecutionCommand command) {
			if(service.getExecution(command.executionId())!=null) {
				service.deleteExecution(command);
				this.retry=false;
			}
		}

		private boolean mustRetry() {
			return this.retry;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProcessorService.class);

	private final CommandProcessingMonitor monitor;
	private final TransactionManager manager;
	private final ContinuousIntegrationService service;

	private volatile boolean shuttingDown;

	private ExecutorService executor;

	CommandProcessorService(CommandProcessingMonitor monitor, TransactionManager manager, ContinuousIntegrationService service) {
		this.monitor = monitor;
		this.manager = manager;
		this.service = service;
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
								public void uncaughtException(Thread t, Throwable e) {
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
				this.executor.execute(new CommandProcessor(command));
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
			CommandDispatchingVisitor dispatcher = new CommandDispatchingVisitor();
			command.accept(dispatcher);
			tx.commit();
			if(dispatcher.mustRetry()) {
				this.monitor.retryLater(command);
			} else {
				LOGGER.trace("Processed command {}",command);
			}
		} catch(Exception e) {
			LOGGER.error("Could not process command {}",command,e);
		} finally {
			if(tx.isActive()) {
				try {
					tx.rollback();
				} catch (Exception e) {
					LOGGER.error("Could not rollback transaction",e);
				}
			}
		}
	}

	@Override
	protected void triggerShutdown() {
		this.shuttingDown=true;
		this.monitor.offer(Poison.SINGLETON);
		LOGGER.info("Requested command processing termination...");
	}

}