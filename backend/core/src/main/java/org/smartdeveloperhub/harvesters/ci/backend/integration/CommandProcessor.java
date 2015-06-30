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

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.command.Command;
import org.smartdeveloperhub.harvesters.ci.backend.command.CommandVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.command.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.command.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.command.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.command.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.command.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.command.UpdateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.Transaction;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;

final class CommandProcessor {

	private final class Dispatcher implements CommandVisitor {

		private boolean retry;

		private Dispatcher() {
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

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProcessor.class);

	private final TransactionManager manager;
	private final ContinuousIntegrationService service;

	private CommandProcessor(TransactionManager manager, ContinuousIntegrationService service) {
		this.manager = manager;
		this.service = service;
	}

	boolean processCommand(Command command) throws CommandProcessingException {
		Transaction tx = this.manager.currentTransaction();
		try {
			tx.begin();
			boolean result = processTransactionally(command);
			tx.commit();
			return result;
		} catch(Exception e) {
			LOGGER.error("Could not process command {} ({})",command,e.getMessage());
			throw new CommandProcessingException("Could not process command", e);
		} finally {
			rollbackQuietly(tx);
		}
	}

	private void rollbackQuietly(Transaction tx) {
		if(tx!=null) {
			if(tx.isActive()) {
				try {
					tx.rollback();
				} catch (Exception e) {
					LOGGER.error("Could not rollback transaction for command",e);
				}
			}
		}
	}

	private boolean processTransactionally(Command command) throws TransactionException {
		LOGGER.trace("Processing command {}",command);
		Dispatcher dispatcher = new Dispatcher();
		command.accept(dispatcher);
		if(LOGGER.isTraceEnabled()) {
			LOGGER.trace(
				"{} processing of command {}",
				dispatcher.mustRetry()?
					"Aborted":
					"Completed",
				command);
		}
		return !dispatcher.mustRetry();
	}

	static CommandProcessor newInstance(TransactionManager manager, ContinuousIntegrationService service) {
		return new CommandProcessor(manager, service);
	}

}