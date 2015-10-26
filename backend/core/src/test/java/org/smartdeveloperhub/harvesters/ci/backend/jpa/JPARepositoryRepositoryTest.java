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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.net.URI;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Repository;

import com.google.common.collect.Lists;

public class JPARepositoryRepositoryTest extends JPATestCase {

	@Rule
	public TestName testName=new TestName();

	protected URI resource(final String name) {
		return location(name).resolve("repository.git");
	}

	protected URI location(final String name) {
		return URI.create(this.testName.getMethodName()+"/"+name+"/");
	}

	private static final List<URI> ADDED_REPOSITORIES=Lists.newArrayList();

	@Test
	public void testRepositoryLocations() throws Exception {
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<URI> repositoryLocations = repositoryRepository().repositoryLocations();
				assertThat(repositoryLocations,hasSize(ADDED_REPOSITORIES.size()));
				for(final URI location:ADDED_REPOSITORIES) {
					assertThat(repositoryLocations,contains(location));
				}
			}
		});
	}

	@Test
	public void testAdd() throws Exception {
		final Repository repository = Accessor.getDefault().createRepository(location("1"),resource("1"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				repositoryRepository().add(repository);
			}
		});
		ADDED_REPOSITORIES.add(repository.location());
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final Repository found = repositoryRepository().repositoryOfLocation(repository.location());
				assertThat(found.resource(),equalTo(repository.resource()));
				assertThat(found.branches(),equalTo(repository.branches()));
			}
		});
	}

	@Test
	public void testRemove() throws Exception {
		final Repository repository = Accessor.getDefault().createRepository(location("1"),resource("1"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				repositoryRepository().add(repository);
			}
		});
		ADDED_REPOSITORIES.add(repository.location());
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final Repository found = repositoryRepository().repositoryOfLocation(repository.location());
				assertThat(found,notNullValue());
				repositoryRepository().remove(found);
			}
		});
		ADDED_REPOSITORIES.remove(repository.location());
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final Repository found = repositoryRepository().repositoryOfLocation(repository.location());
				assertThat(found,nullValue());
			}
		});
	}

}
