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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0
 *   Bundle      : ci-backend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CommandVisitor;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.CreateExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.DeleteExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.FinishExecutionCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.RegisterServiceCommand;
import org.smartdeveloperhub.harvesters.ci.backend.domain.command.UpdateBuildCommand;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent.EntityType;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent.State;

final class EntityLifecycleEventCreator implements CommandVisitor {

	private EntityLifecycleEvent event;

	private void initEvent(EntityType type, State state, URI id) {
		this.event=EntityLifecycleEvent.newInstance(type, state,id);
	}

	EntityLifecycleEvent getEvent() {
		return this.event;
	}

	@Override
	public void visitRegisterServiceCommand(RegisterServiceCommand command) {
		// NOTHING TO DO
	}

	@Override
	public void visitCreateBuildCommand(CreateBuildCommand command) {
		initEvent(EntityType.BUILD,State.CREATED,command.buildId());
	}

	@Override
	public void visitUpdateBuildCommand(UpdateBuildCommand command) {
		initEvent(EntityType.BUILD,State.MODIFIED,command.buildId());
	}

	@Override
	public void visitDeleteBuildCommand(DeleteBuildCommand command) {
		initEvent(EntityType.BUILD,State.DELETED,command.buildId());
	}

	@Override
	public void visitCreateExecutionCommand(CreateExecutionCommand command) {
		initEvent(EntityType.EXECUTION,State.CREATED,command.executionId());
	}

	@Override
	public void visitFinishExecutionCommand(FinishExecutionCommand command) {
		initEvent(EntityType.EXECUTION,State.MODIFIED,command.executionId());
	}

	@Override
	public void visitDeleteExecutionCommand(DeleteExecutionCommand command) {
		initEvent(EntityType.EXECUTION,State.DELETED,command.executionId());
	}

}