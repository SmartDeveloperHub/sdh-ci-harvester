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

import java.io.Serializable;
import java.net.URI;
import java.util.Objects;

import com.google.common.base.MoreObjects;

public final class EntityId implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 8133178571592171254L;

	public enum Type {
		SERVICE,
		BUILD,
		EXECUTION
	}

	private URI nativeId;
	private EntityId.Type type;

	EntityId() {
	}

	private void setNativeId(URI nativeId) {
		this.nativeId=nativeId;
	}

	private void setType(Type type) {
		this.type=type;
	}

	public URI nativeId() {
		return this.nativeId;
	}

	public EntityId.Type type() {
		return this.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.nativeId,this.type);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof EntityId) {
			EntityId that=(EntityId)obj;
			result=
				Objects.equals(this.nativeId, that.nativeId) &&
				Objects.equals(this.type, that.type);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("nativeId", this.nativeId).
					add("type",this.type).
					toString();
	}

	public static EntityId newInstance(URI nativeId, EntityId.Type type) {
		EntityId result=new EntityId();
		result.setNativeId(nativeId);
		result.setType(type);
		return result;
	}

}