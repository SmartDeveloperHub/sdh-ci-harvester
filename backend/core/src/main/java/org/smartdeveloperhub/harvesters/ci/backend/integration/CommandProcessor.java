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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.Command;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CommandVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.UpdateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
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
		public void visitRegisterServiceCommand(final RegisterServiceCommand command) {
			CommandProcessor.this.ciService.registerService(command);
			this.retry=false;
		}

		@Override
		public void visitCreateBuildCommand(final CreateBuildCommand command) {
			final URI parentId = command.subBuildOf();
			if(parentId!=null && CommandProcessor.this.ciService.getBuild(parentId)!=null) {
				CommandProcessor.this.ciService.createBuild(command);
				this.retry=false;
			} else if(CommandProcessor.this.ciService.getService(command.serviceId())!=null) {
				CommandProcessor.this.ciService.createBuild(command);
				this.retry=false;
			}
		}

		@Override
		public void visitUpdateBuildCommand(final UpdateBuildCommand command) {
			if(CommandProcessor.this.ciService.getBuild(command.buildId())!=null) {
				CommandProcessor.this.ciService.updateBuild(command);
				this.retry=false;
			}
		}

		@Override
		public void visitDeleteBuildCommand(final DeleteBuildCommand command) {
			if(CommandProcessor.this.ciService.getBuild(command.buildId())!=null) {
				CommandProcessor.this.ciService.deleteBuild(command);
				this.retry=false;
			}
		}

		@Override
		public void visitCreateExecutionCommand(final CreateExecutionCommand command) {
			if(CommandProcessor.this.ciService.getBuild(command.buildId())!=null) {
				CommandProcessor.this.ciService.createExecution(command);
				final URI executionId = command.executionId();
				enrich(executionId);
				this.retry=false;
			}
		}

		@Override
		public void visitFinishExecutionCommand(final FinishExecutionCommand command) {
			final URI executionId = command.executionId();
			if(CommandProcessor.this.ciService.getExecution(executionId)!=null) {
				CommandProcessor.this.ciService.finishExecution(command);
				enrich(executionId);
				this.retry=false;
			}
		}

		@Override
		public void visitDeleteExecutionCommand(final DeleteExecutionCommand command) {
			if(CommandProcessor.this.ciService.getExecution(command.executionId())!=null) {
				CommandProcessor.this.ciService.deleteExecution(command);
				this.retry=false;
			}
		}

		private void enrich(final URI executionId) {
			final Execution execution = CommandProcessor.this.ciService.getExecution(executionId);
			try {
				CommandProcessor.this.erService.enrich(execution);
			} catch (final IOException e) {
				LOGGER.error("Could not enrich execution {}",execution,e);
			}
		}

		private boolean mustRetry() {
			return this.retry;
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProcessor.class);

	private final TransactionManager manager;
	private final ContinuousIntegrationService ciService;
	private final EnrichmentService erService;

	private CommandProcessor(final TransactionManager manager, final ContinuousIntegrationService ciService, final EnrichmentService erService) {
		this.manager = manager;
		this.ciService = ciService;
		this.erService = erService;
	}

	boolean processCommand(final Command command) throws CommandProcessingException {
		final Transaction tx = this.manager.currentTransaction();
		try {
			tx.begin();
			final boolean result = processTransactionally(command);
			tx.commit();
			return result;
		} catch(final Exception e) {
			LOGGER.error("Could not process command {} ({})",command,e.getMessage());
			throw new CommandProcessingException("Could not process command", e);
		} finally {
			rollbackQuietly(tx);
		}
	}

	private void rollbackQuietly(final Transaction tx) {
		if(tx!=null && tx.isActive()) {
			try {
				tx.rollback();
			} catch (final Exception e) {
				LOGGER.error("Could not rollback transaction for command",e);
			}
		}
	}

	private boolean processTransactionally(final Command command) throws TransactionException {
		LOGGER.trace("Processing command {}",command);
		final Dispatcher dispatcher = new Dispatcher();
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

	static CommandProcessor newInstance(final TransactionManager manager, final ContinuousIntegrationService service, final EnrichmentService erService) {
		return new CommandProcessor(manager, service, erService);
	}

}