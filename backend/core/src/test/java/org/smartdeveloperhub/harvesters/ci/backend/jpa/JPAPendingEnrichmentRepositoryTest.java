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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.net.URI;

import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.PendingEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;

public class JPAPendingEnrichmentRepositoryTest extends JPATestCase {

	@Rule
	public TestName testName=new TestName();

	protected URI resource(final String name) {
		return execution(name).resolve("repository.git");
	}

	protected URI execution(final String name) {
		return URI.create(this.testName.getMethodName()+"/"+name+"/");
	}

	@Test
	public void testAdd() throws Exception {
		final PendingEnrichment pendingEnrichment=Accessor.getDefault().createPendingEnrichment(resource("1"),"master","commitId");
		pendingEnrichment.executions().add(execution("1"));
		pendingEnrichment.executions().add(execution("2"));
		pendingEnrichment.executions().add(execution("3"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				pendingEnrichmentRepository().add(pendingEnrichment);
			}
		});
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final PendingEnrichment found = pendingEnrichmentRepository().pendingEnrichmentOfId(pendingEnrichment.id());
				assertThat(found.repositoryLocation(),equalTo(pendingEnrichment.repositoryLocation()));
				assertThat(found.branchName(),equalTo(pendingEnrichment.branchName()));
				assertThat(found.commitId(),equalTo(pendingEnrichment.commitId()));
				assertThat(found.executions(),hasItems(pendingEnrichment.executions().toArray(new URI[0])));
			}
		});
	}

	@Ignore("HSQLDB does not support the specified constraint for the time being")
	@Test
	public void testAdd$doesNotRepeatEnrichments() throws Exception {
		final PendingEnrichment pendingEnrichment=Accessor.getDefault().createPendingEnrichment(resource("1"),"master","commitId");
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				pendingEnrichmentRepository().add(pendingEnrichment);
			}
		});
		final PendingEnrichment otherEnrichment=Accessor.getDefault().createPendingEnrichment(resource("1"),"master","commitId");
		try {
			transactional(new Operation() {
				@Override
				protected void execute() throws Exception {
					pendingEnrichmentRepository().add(otherEnrichment);
				}
			});
			fail("Should not allow adding repeated enrichments");
		} catch (final Exception e) {
			e.printStackTrace();
			assertThat(e,instanceOf(PersistenceException.class));
			final ConstraintViolationException cve = Throwables.findCause(e,ConstraintViolationException.class);
			assertThat(cve,notNullValue());
		}
	}

	@Test
	public void testAdd$doesNotRepeatExecutions() throws Exception {
		final PendingEnrichment pendingEnrichment=Accessor.getDefault().createPendingEnrichment(resource("1"),"master","commitId");
		pendingEnrichment.executions().add(execution("1"));
		pendingEnrichment.executions().add(execution("2"));
		pendingEnrichment.executions().add(execution("3"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				pendingEnrichmentRepository().add(pendingEnrichment);
			}
		});
		final PendingEnrichment otherEnrichment=Accessor.getDefault().createPendingEnrichment(resource("1"),"master","anotherCommit");
		otherEnrichment.executions().add(execution("1"));
		try {
			transactional(new Operation() {
				@Override
				protected void execute() throws Exception {
					pendingEnrichmentRepository().add(otherEnrichment);
				}
			});
			fail("Should not allow adding an execution to multiple enrichments");
		} catch (final Exception e) {
			assertThat(e,instanceOf(TransactionException.class));
			final ConstraintViolationException cve = Throwables.findCause(e,ConstraintViolationException.class);
			assertThat(cve,notNullValue());
			assertThat(cve.getConstraintName(),equalTo("EXECUTIONS_ARE_ATTACHED_TO_A_SINGLE_ENRICHMENT_REQUEST"));
		}
	}

	@Test
	public void testRemove() throws Exception {
		final PendingEnrichment pendingEnrichment=Accessor.getDefault().createPendingEnrichment(resource("1"),"master","commitId");
		pendingEnrichment.executions().add(execution("1"));
		pendingEnrichment.executions().add(execution("2"));
		pendingEnrichment.executions().add(execution("3"));
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				pendingEnrichmentRepository().add(pendingEnrichment);
			}
		});
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final PendingEnrichment found = pendingEnrichmentRepository().pendingEnrichmentOfId(pendingEnrichment.id());
				assertThat(found,notNullValue());
				pendingEnrichmentRepository().remove(found);
			}
		});
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final PendingEnrichment found = pendingEnrichmentRepository().pendingEnrichmentOfId(pendingEnrichment.id());
				assertThat(found,nullValue());
			}
		});
	}

}
