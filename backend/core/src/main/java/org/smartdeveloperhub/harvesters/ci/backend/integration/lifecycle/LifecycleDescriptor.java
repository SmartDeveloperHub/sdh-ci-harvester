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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration.lifecycle;

import java.util.Date;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class LifecycleDescriptor implements Lifecycle {

	private EntityId entityId;
	private long index;
	private Date registeredOn;
	private Date deletedOn;

	LifecycleDescriptor() {
	}

	private LifecycleDescriptor(LifecycleDescriptor that) {
		setEntityId(that.entityId);
		this.index=that.index;
		this.registeredOn=that.registeredOn;
		this.deletedOn=that.deletedOn;
	}

	private void setEntityId(EntityId entityId) {
		this.entityId = entityId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EntityId entityId() {
		return this.entityId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(Date date) {
		this.registeredOn=date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(Date date) {
		this.deletedOn=date;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long index() {
		return this.index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isTransient() {
		return this.registeredOn==null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isActive() {
		return !isTransient() && !isDeleted();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDeleted() {
		return this.deletedOn!=null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date registeredOn() {
		return this.registeredOn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date deletedOn() {
		return this.deletedOn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.entityId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof LifecycleDescriptor) {
			LifecycleDescriptor that=(LifecycleDescriptor)obj;
			result=Objects.equals(this.entityId,that.entityId);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("entityId",this.entityId).
					add("index",this.index).
					add("registeredOn", this.registeredOn).
					add("deletedOn",this.deletedOn).
					toString();
	}


	public static LifecycleDescriptor newInstance(EntityId entityId) {
		LifecycleDescriptor descriptor = new LifecycleDescriptor();
		descriptor.setEntityId(entityId);
		return descriptor;

	}

	public static LifecycleDescriptor newInstance(LifecycleDescriptor descriptor) {
		if(descriptor==null) {
			return null;
		}
		return new LifecycleDescriptor(descriptor);
	}
}
