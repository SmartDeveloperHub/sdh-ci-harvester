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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.4.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.net.URI;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.Commit;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;

public class JPACommitRepositoryTest extends JPATestCase {

	@Rule
	public TestName testName=new TestName();

	protected URI resource(final String name) {
		return location(name).resolve("repository.git");
	}

	protected URI location(final String name) {
		return URI.create(this.testName.getMethodName()+"/"+name+"/");
	}

	@Test
	public void testAdd() throws Exception {
		final Commit commit = Accessor.getDefault().createCommit(location("1"),"master","1",resource("1"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				commitRepository().add(commit);
			}
		});
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final Commit found = commitRepository().commitOfId(commit.id());
				assertThat(found.resource(),equalTo(commit.resource()));
			}
		});
	}

	@Test
	public void testAdd$doesNotRepeatBranches() throws Exception {
		final Commit commit = Accessor.getDefault().createCommit(location("1"),"master","1",resource("1"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				commitRepository().add(commit);
			}
		});
		final Commit same = Accessor.getDefault().createCommit(location("1"),"master","1",resource("1"));
		try {
			transactional(new Operation() {
				@Override
				protected void execute() throws Exception {
					commitRepository().add(same);
				}
			});
		} catch (final Exception e) {
			assertThat(e,instanceOf(TransactionException.class));
			final ConstraintViolationException found = Throwables.findCause(e,ConstraintViolationException.class);
			assertThat(found,notNullValue());
		}
	}

	@Test
	public void testRemove() throws Exception {
		final Commit commit = Accessor.getDefault().createCommit(location("1"),"master","1",resource("1"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				commitRepository().add(commit);
			}
		});
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final Commit found = commitRepository().commitOfId(commit.id());
				assertThat(found,notNullValue());
				commitRepository().remove(found);
			}
		});
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final Commit found = commitRepository().commitOfId(commit.id());
				assertThat(found,nullValue());
			}
		});
	}

}
