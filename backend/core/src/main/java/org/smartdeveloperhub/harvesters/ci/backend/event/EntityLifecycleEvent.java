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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0
 *   Bundle      : ci-backend-core-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.event;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Date;

import com.google.common.base.MoreObjects;

public final class EntityLifecycleEvent {

	public enum EntityType {
		BUILD,
		EXECUTION,
	}

	public enum State {
		CREATED,
		MODIFIED,
		DELETED,
		ENRICHED,
	}

	private final Date ocurredOn;
	private final State state;
	private final EntityType entityType;
	private final URI entityId;

	public EntityLifecycleEvent(final EntityType entityType, final State state, final URI entityId) {
		this.entityType = entityType;
		this.state = state;
		this.entityId = entityId;
		this.ocurredOn=new Date();
	}

	public Date ocurredOn() {
		return this.ocurredOn;
	}

	public EntityType entityType() {
		return this.entityType;
	}

	public State state() {
		return this.state;
	}

	public URI entityId() {
		return this.entityId;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("ocurredOn",this.ocurredOn).
					add("state",this.state).
					add("entityType",this.entityType).
					add("entityId",this.entityId).toString();
	}

	public static EntityLifecycleEvent newInstance(final EntityType entityType, final State state, final URI entityId) {
		return
			new EntityLifecycleEvent(
				checkNotNull(entityType,"Entity type cannot be null"),
				checkNotNull(state,"State cannot be null"),
				checkNotNull(entityId,"Entity identifier cannot be null"));
	}

}
