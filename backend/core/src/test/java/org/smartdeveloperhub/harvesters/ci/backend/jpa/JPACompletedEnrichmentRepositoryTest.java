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
package org.smartdeveloperhub.harvesters.ci.backend.jpa;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;

import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.CompletedEnrichment;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionException;

public class JPACompletedEnrichmentRepositoryTest extends JPATestCase {

	@Rule
	public TestName testName=new TestName();

	protected URI resource(final String name) {
		return execution(name).resolve("repository.git");
	}

	protected URI execution(final String name) {
		return URI.create(this.testName.getMethodName()+"/"+name+"/");
	}

	protected URI repository(final String name) {
		return execution("repositories").resolve(name);
	}

	protected URI commit(final String name) {
		return execution("commits").resolve(name);
	}

	protected URI branch(final String name) {
		return execution("branches").resolve(name);
	}

	private static int ADDED_COMPLETED_ENRICHMENTS=0;

	private void addCompletedEnrichment(final CompletedEnrichment completedEnrichment) throws Exception {
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				completedEnrichmentRepository().add(completedEnrichment);
				ADDED_COMPLETED_ENRICHMENTS++;
			}
		});
	}

	@Test
	public void testAdd() throws Exception {
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("master"),commit("commitId"));
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final CompletedEnrichment found = completedEnrichmentRepository().completedEnrichmentOfId(completedEnrichment.id());
				assertThat(found.repositoryResource(),equalTo(completedEnrichment.repositoryResource()));
				assertThat(found.branchResource(),equalTo(completedEnrichment.branchResource()));
				assertThat(found.commitResource(),equalTo(completedEnrichment.commitResource()));
				assertThat(found.executions(),hasItems(completedEnrichment.executions().toArray(new URI[0])));
			}
		});
	}

	@Test
	public void testAdd$noExecutions() throws Exception {
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("master"),commit("commitId"));
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final CompletedEnrichment found = completedEnrichmentRepository().completedEnrichmentOfId(completedEnrichment.id());
				assertThat(found.repositoryResource(),equalTo(completedEnrichment.repositoryResource()));
				assertThat(found.branchResource(),equalTo(completedEnrichment.branchResource()));
				assertThat(found.commitResource(),equalTo(completedEnrichment.commitResource()));
				assertThat(found.executions(),hasSize(0));
			}
		});
	}

	@Ignore("HSQLDB does not support the specified constraint for the time being")
	@Test
	public void testAdd$doesNotRepeatEnrichments() throws Exception {
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("master"),commit("commitId"));
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		final CompletedEnrichment otherCompletedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("master"),commit("commitId"));
		try {
			addCompletedEnrichment(otherCompletedEnrichment);
			fail("Should not allow adding repeated completed enrichments");
		} catch (final Exception e) {
			e.printStackTrace();
			assertThat(e,instanceOf(PersistenceException.class));
			final ConstraintViolationException cve = Throwables.findCause(e,ConstraintViolationException.class);
			assertThat(cve,notNullValue());
		} finally {
			ADDED_COMPLETED_ENRICHMENTS--;
		}
	}

	@Test
	public void testAdd$doesNotRepeatExecutions() throws Exception {
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("master"),commit("commitId"));
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		final CompletedEnrichment otherCompletedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("develop"),commit("commitId"));
		otherCompletedEnrichment.executions().add(execution("1"));
		try {
			addCompletedEnrichment(otherCompletedEnrichment);
			fail("Should not allow adding an execution to multiple completed enrichments");
		} catch (final Exception e) {
			assertThat(e,instanceOf(TransactionException.class));
			final ConstraintViolationException cve = Throwables.findCause(e,ConstraintViolationException.class);
			assertThat(cve,notNullValue());
			assertThat(cve.getConstraintName(),equalTo("EXECUTIONS_ARE_ATTACHED_TO_A_SINGLE_COMPLETED_ENRICHMENT"));
		} finally {
			ADDED_COMPLETED_ENRICHMENTS--;
		}
	}

	@Test
	public void testCompletedEnrichmentOfExecution$found() throws Exception {
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository("example"),branch("master"),commit("commitId"));
		final URI execution = execution("1");
		completedEnrichment.executions().add(execution);
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final CompletedEnrichment found = completedEnrichmentRepository().completedEnrichmentOfExecution(execution);
				assertThat(found.repositoryResource(),equalTo(completedEnrichment.repositoryResource()));
				assertThat(found.branchResource(),equalTo(completedEnrichment.branchResource()));
				assertThat(found.commitResource(),equalTo(completedEnrichment.commitResource()));
				assertThat(found.executions(),hasItems(completedEnrichment.executions().toArray(new URI[0])));
			}
		});
	}

	@Test
	public void testCompletedEnrichmentOfExecution$notFound() throws Exception {
		final URI execution = execution("1");
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final CompletedEnrichment found = completedEnrichmentRepository().completedEnrichmentOfExecution(execution);
				assertThat(found,nullValue());
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$all$found() throws Exception {
		final URI repository = repository("example");
		final URI branch = branch("master");
		final URI commit = commit("commitId");
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository,branch,commit);
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> ces = completedEnrichmentRepository().findCompletedEnrichments(null,null,null);
				assertThat(ces,hasSize(ADDED_COMPLETED_ENRICHMENTS));
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$withRepo$found() throws Exception {
		final URI repository = repository("example");
		final URI branch = branch("master");
		final URI commit = commit("commitId");
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository,branch,commit);
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> ces = completedEnrichmentRepository().findCompletedEnrichments(repository,null,null);
				assertThat(ces,hasSize(1));
				final CompletedEnrichment found=ces.get(0);
				assertThat(found.repositoryResource(),equalTo(completedEnrichment.repositoryResource()));
				assertThat(found.branchResource(),equalTo(completedEnrichment.branchResource()));
				assertThat(found.commitResource(),equalTo(completedEnrichment.commitResource()));
				assertThat(found.executions(),hasItems(completedEnrichment.executions().toArray(new URI[0])));
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$withRepo$notFound() throws Exception {
		final URI repository = repository("example");
		final URI branch = null;
		final URI commit = null;
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> found = completedEnrichmentRepository().findCompletedEnrichments(repository,branch, commit);
				assertThat(found,hasSize(0));
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$withRepoAndBranch$found() throws Exception {
		final URI repository = repository("example");
		final URI branch = branch("master");
		final URI commit = commit("commitId");
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository,branch,commit);
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> ces = completedEnrichmentRepository().findCompletedEnrichments(repository,branch,null);
				assertThat(ces,hasSize(1));
				final CompletedEnrichment found=ces.get(0);
				assertThat(found.repositoryResource(),equalTo(completedEnrichment.repositoryResource()));
				assertThat(found.branchResource(),equalTo(completedEnrichment.branchResource()));
				assertThat(found.commitResource(),equalTo(completedEnrichment.commitResource()));
				assertThat(found.executions(),hasItems(completedEnrichment.executions().toArray(new URI[0])));
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$withRepoAndBranch$notFound() throws Exception {
		final URI repository = repository("example");
		final URI branch = branch("master");
		final URI commit = null;
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> found = completedEnrichmentRepository().findCompletedEnrichments(repository,branch, commit);
				assertThat(found,hasSize(0));
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$withRepoBranchAndCommit$found() throws Exception {
		final URI repository = repository("example");
		final URI branch = branch("master");
		final URI commit = commit("commitId");
		final CompletedEnrichment completedEnrichment=Accessor.getDefault().createCompletedEnrichment(repository,branch,commit);
		completedEnrichment.executions().add(execution("1"));
		addCompletedEnrichment(completedEnrichment);
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> ces = completedEnrichmentRepository().findCompletedEnrichments(repository,branch, commit);
				assertThat(ces,hasSize(1));
				final CompletedEnrichment found=ces.get(0);
				assertThat(found.repositoryResource(),equalTo(completedEnrichment.repositoryResource()));
				assertThat(found.branchResource(),equalTo(completedEnrichment.branchResource()));
				assertThat(found.commitResource(),equalTo(completedEnrichment.commitResource()));
				assertThat(found.executions(),hasItems(completedEnrichment.executions().toArray(new URI[0])));
			}
		});
	}

	@Test
	public void testFindCompletedEnrichments$withRepoBranchAndCommit$notFound() throws Exception {
		final URI repository = repository("example");
		final URI branch = branch("master");
		final URI commit = commit("commitId");
		transactional(new Operation() {
			@Override
			protected void execute() throws Exception {
				final List<CompletedEnrichment> found = completedEnrichmentRepository().findCompletedEnrichments(repository,branch, commit);
				assertThat(found,hasSize(0));
			}
		});
	}

}
