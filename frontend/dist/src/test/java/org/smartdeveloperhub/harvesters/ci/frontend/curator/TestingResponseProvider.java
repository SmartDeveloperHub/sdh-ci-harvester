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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.3.0
 *   Bundle      : ci-frontend-dist-0.3.0.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.curator;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.smartdeveloperhub.curator.connector.Bindings;
import org.smartdeveloperhub.curator.connector.EnrichmentRequest;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.Failure;
import org.smartdeveloperhub.curator.connector.ResponseProvider;
import org.smartdeveloperhub.curator.connector.UseCase;
import org.smartdeveloperhub.curator.protocol.Binding;
import org.smartdeveloperhub.curator.protocol.Constraint;
import org.smartdeveloperhub.curator.protocol.Filter;
import org.smartdeveloperhub.curator.protocol.Literal;
import org.smartdeveloperhub.curator.protocol.Resource;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.Variable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

final class TestingResponseProvider implements ResponseProvider {

	private static final URI FOR_COMMIT     = URI.create(UseCase.ci("forCommit"));
	private static final URI FOR_BRANCH     = URI.create(UseCase.ci("forBranch"));
	private static final URI FOR_REPOSITORY = URI.create(UseCase.ci("forRepository"));

	private static final Map<URI,URI> REQUIRED_PROPERTY=
			ImmutableMap.
				<URI,URI>builder().
					put(FOR_REPOSITORY,URI.create("http://www.smartdeveloperhub.org/vocabulary/scm#location")).
					put(FOR_BRANCH,URI.create("http://usefulinc.com/ns/doap#name")).
					put(FOR_COMMIT,URI.create("http://www.smartdeveloperhub.org/vocabulary/scm#commitId")).
					build();

	private final Logger logger;
	private final ConcurrentMap<String,URI> repositories;
	private final List<Action> actions;

	TestingResponseProvider(final Logger logger) {
		this.logger=logger;
		this.repositories=Maps.newConcurrentMap();
		this.actions=Lists.newArrayList();
	}

	private EnrichmentResult createResult(final URI targetResource, final URI repository, final URI branch, final URI commit) {
		return
			EnrichmentResult.
				newInstance().
					withTargetResource(targetResource).
					withAdditions(Bindings.
						newInstance().
							withProperty(FOR_REPOSITORY).andResource(repository).
							withProperty(FOR_BRANCH).andResource(branch).
							withProperty(FOR_COMMIT).andResource(commit));
	}

	private URI createCommit(final URI branch, final Value value) {
		return branch.resolve(((Literal)value).lexicalForm()+"/");
	}

	private URI createBranch(final URI repository, final Value value) {
		return repository.resolve(((Literal)value).lexicalForm()+"/");
	}

	private URI createRepository(final Value value) {
		final String gitRepo = ((Literal)value).lexicalForm();
		final URI uri=URI.create("http://www.example.org/harvester/"+Integer.toHexString(gitRepo.hashCode())+"/");
		URI current = this.repositories.putIfAbsent(gitRepo, uri);
		if(current==null) {
			current=uri;
		}
		return current;
	}

	private Map<URI, Value> getFilters(final EnrichmentRequest request) {
		final Map<URI,Value> values=Maps.newLinkedHashMap();
		for(final Filter filter:request.filters()) {
			final URI constrainedProperty = REQUIRED_PROPERTY.get(filter.property());
			final Variable target=filter.variable();
			for(final Constraint constraint:request.constraints()) {
				if(target.equals(constraint.target())) {
					for(final Binding binding:constraint.bindings()) {
						if(constrainedProperty.equals(binding.property())) {
							values.put(filter.property(),binding.value());
						}
					}
				}
			}
		}
		return values;
	}

	private String toString(final Value value) {
		if(value instanceof Variable) {
			return ((Variable)value).name();
		} else if(value instanceof Resource) {
			return ((Resource)value).name().toString();
		}
		return ((Literal)value).lexicalForm();
	}

	@Override
	public boolean isExpected(final UUID messageId) {
		return true;
	}

	@Override
	public boolean isAccepted(final UUID messageId) {
		return true;
	}

	@Override
	public Failure getFailure(final UUID messageId) {
		return Failure.newInstance().withCode(1).withReason("A failure");
	}

	@Override
	public EnrichmentResult getResult(final UUID messageId, final EnrichmentRequest request) {
		final URI targetResource = request.targetResource();
		final Map<URI, Value> values = getFilters(request);

		if(values.size()!=3) {
			this.logger.warn("Rejected partial enrichment request:");
			this.logger.info("  - Message Id: {} ",messageId);
			this.logger.info("  - Target resource: {}",targetResource);
			this.logger.info("  - Filters: ",request.filters());
			return null;
		}

		final URI repository=createRepository(values.get(FOR_REPOSITORY));
		final URI branch=createBranch(repository,values.get(FOR_BRANCH));
		final URI commit=createCommit(branch,values.get(FOR_COMMIT));
		this.actions.add(Action.newInstance(messageId,targetResource,repository,branch,commit));

		this.logger.info("Processed complete enrichment request:");
		this.logger.info("  - Message Id: {} ",messageId);
		this.logger.info("  - Target resource: {}",targetResource);
		this.logger.info("  - Filters: ");
		for(final Entry<URI,Value> entry:values.entrySet()) {
			this.logger.info("    + {} ({})",entry.getKey(),toString(entry.getValue()));
		}
		this.logger.info("  - Generated result:");
		this.logger.info("    + Additions: ");
		this.logger.info("      * {}: {}",FOR_REPOSITORY,repository);
		this.logger.info("      * {}: {}",FOR_BRANCH,branch);
		this.logger.info("      * {}: {}",FOR_COMMIT,commit);

		return createResult(targetResource, repository, branch, commit);
	}

	@Override
	public long acknowledgeDelay(final UUID messageId,final TimeUnit unit) {
		return 0;
	}

	@Override
	public long resultDelay(final UUID messageId, final TimeUnit unit) {
		return 0;
	}

	List<Action> actions() {
		return this.actions;
	}
}