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
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import java.net.URI;

import javax.persistence.EntityManager;

import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.persistence.BuildRepository;

public class JPABuildRepository implements BuildRepository {

	private EntityManagerProvider provider;

	public JPABuildRepository(EntityManagerProvider provider) {
		this.provider = provider;
	}

	private EntityManager entityManager() {
		return this.provider.entityManager();
	}

	@Override
	public void add(Build build) {
		entityManager().persist(build);
	}

	@Override
	public void remove(Build build) {
		entityManager().remove(build);
	}

	@Override
	public Build buildOfId(URI id) {
		return buildOfId(id,Build.class);
	}

	@Override
	public <T extends Build> T buildOfId(URI id, Class<? extends T> clazz) {
		return entityManager().find(clazz,id);
	}

}