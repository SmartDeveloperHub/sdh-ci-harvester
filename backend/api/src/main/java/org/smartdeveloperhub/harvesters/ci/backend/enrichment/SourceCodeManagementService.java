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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-api:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateBranchCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateCommitCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.command.CreateRepositoryCommand;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.BranchRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.CommitRepository;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.persistence.RepositoryRepository;

public class SourceCodeManagementService {

	private RepositoryRepository repositoryRepository;
	private BranchRepository branchRepository;
	private CommitRepository commitRepository;

	public SourceCodeManagementService(final RepositoryRepository repositoryRepository, final BranchRepository branchRepository, final CommitRepository commitRepository) {
		setRepositoryRepository(repositoryRepository);
		setBranchRepository(branchRepository);
		setCommitRepository(commitRepository);
	}

	protected void setCommitRepository(final CommitRepository commitRepository) {
		checkNotNull(commitRepository,"Commit repository cannot be null");
		this.commitRepository = commitRepository;
	}

	protected void setBranchRepository(final BranchRepository branchRepository) {
		checkNotNull(branchRepository,"Branch repository cannot be null");
		this.branchRepository = branchRepository;
	}

	protected void setRepositoryRepository(final RepositoryRepository repositoryRepository) {
		checkNotNull(repositoryRepository,"Repository repository cannot be null");
		this.repositoryRepository = repositoryRepository;
	}

	protected RepositoryRepository repositoryRepository() {
		return this.repositoryRepository;
	}

	protected BranchRepository branchRepository() {
		return this.branchRepository;
	}

	protected CommitRepository commitRepository() {
		return this.commitRepository;
	}

	private Repository findRepository(final CreateBranchCommand aCommand) {
		final URI location=aCommand.repositoryLocation();
		final Repository repository = repositoryRepository().repositoryOfLocation(location);
		checkArgument(repository!=null,"No repository located at '%s' is registered",location);
		return repository;
	}

	private Branch findBranch(final CreateCommitCommand aCommand) {
		final BranchId branchId=BranchId.create(aCommand.repositoryLocation(), aCommand.branchName());
		final Branch branch = branchRepository().branchOfId(branchId);
		checkArgument(branch!=null,"No branch for repository located at '%s' with name '%s' is registered",branchId.repository(),branchId.name());
		return branch;
	}

	public Repository findRepository(final URI location) {
		return repositoryRepository().repositoryOfLocation(location);
	}

	public Branch findBranch(final URI location, final String name) {
		return branchRepository().branchOfId(BranchId.create(location,name));
	}

	public Commit findCommit(final URI location, final String name, final String commitId) {
		return commitRepository().commitOfId(CommitId.create(BranchId.create(location,name),commitId));
	}

	public void createRepository(final CreateRepositoryCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");

		final URI location=aCommand.repositoryLocation();
		Repository repository = repositoryRepository().repositoryOfLocation(location);
		checkState(repository!=null,"A repository locate at '%s' does already exist",location);

		final URI resource=aCommand.repositoryResource();
		repository = Repository.newInstance(location, resource);
		repositoryRepository().add(repository);
	}

	public void createBranch(final CreateBranchCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		final Repository repository=findRepository(aCommand);
		final Branch branch = repository.createBranch(aCommand.branchName(), aCommand.branchResource());
		branchRepository().add(branch);
	}

	public void createCommit(final CreateCommitCommand aCommand) {
		checkNotNull(aCommand,"Command cannot be null");
		final Branch branch=findBranch(aCommand);
		final Commit commit = branch.createCommit(aCommand.commitId(), aCommand.commitResource());
		commitRepository().add(commit);
	}

}
