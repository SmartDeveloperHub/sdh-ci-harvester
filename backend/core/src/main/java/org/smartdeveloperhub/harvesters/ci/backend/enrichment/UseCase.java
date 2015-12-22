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
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.Bindings;
import org.smartdeveloperhub.curator.connector.Constraints;
import org.smartdeveloperhub.curator.connector.EnrichmentRequest;
import org.smartdeveloperhub.curator.connector.EnrichmentResult;
import org.smartdeveloperhub.curator.connector.Filters;
import org.smartdeveloperhub.curator.protocol.Resource;
import org.smartdeveloperhub.curator.protocol.Value;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

final class UseCase {

	private static final String REPOSITORY = "repository";
	private static final String BRANCH     = "branch";
	private static final String COMMIT     = "commit";

	private static final Logger LOGGER=LoggerFactory.getLogger(UseCase.class);

	static final String DOAP_NAMESPACE = "http://usefulinc.com/ns/doap#";
	static final String SCM_NAMESPACE  = "http://www.smartdeveloperhub.org/vocabulary/scm#";
	static final String CI_NAMESPACE   = "http://www.smartdeveloperhub.org/vocabulary/ci#";

	static {
		LOGGER.warn("Execution enrichment request customization is still missing");
		LOGGER.warn("Execution enrichment result customized processing is still missing");
	}

	private UseCase() {
	}

	static EnrichmentRequest createRequest(final URI targetResource, final EnrichmentContext context) {
		return
			EnrichmentRequest.
				newInstance().
					withTargetResource(targetResource).
					withFilters(
						Filters.
							newInstance().
								withFilter(ci("forRepository"), REPOSITORY).
								withFilter(ci("forBranch"), BRANCH).
								withFilter(ci("forCommit"), COMMIT)).
					withConstraints(
						Constraints.
							newInstance().
								forVariable(REPOSITORY).
									withProperty(RDF.TYPE).
										andResource(scm("Repository")).
									withProperty(scm("location")).
										andTypedLiteral(context.repositoryLocation(),XSD.ANY_URI_TYPE).
									withProperty(scm("hasBranch")).
										andVariable(BRANCH).
								forVariable(BRANCH).
									withProperty(RDF.TYPE).
										andResource(scm("Branch")).
									withProperty(doap("name")).
										andTypedLiteral(context.branchName(),XSD.STRING_TYPE).
									withProperty(scm("hasCommit")).
										andVariable(COMMIT).
								forVariable(COMMIT).
									withProperty(RDF.TYPE).
										andResource(scm("Commit")).
									withProperty(scm("commitId")).
										andTypedLiteral(context.commitId(),XSD.STRING_TYPE));
	}

	static ExecutionEnrichment processResult(final EnrichmentContext context, final EnrichmentResult result) {
		final Bindings additions = result.additions();
		return
			new ImmutableExecutionEnrichment().
				withRepositoryResource(resolveResource(additions, ci("forRepository"))).
				withBranchResource(resolveResource(additions, ci("forBranch"))).
				withCommitResource(resolveResource(additions, ci("forCommit")));
	}

	private static URI resolveResource(final Bindings additions, final String property) {
		URI name=null;
		final Value value = additions.value(URI.create(property));
		if(value instanceof Resource) {
			name=((Resource)value).name();
		}
		return name;
	}

	static String ci(final String term) {
		return CI_NAMESPACE+term;
	}

	static String scm(final String term) {
		return SCM_NAMESPACE+term;
	}

	static String doap(final String term) {
		return DOAP_NAMESPACE+term;
	}

}
