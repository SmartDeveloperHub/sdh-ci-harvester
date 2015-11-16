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
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.connector.Constraints;
import org.smartdeveloperhub.curator.connector.EnrichmentRequest;
import org.smartdeveloperhub.curator.connector.Filters;
import org.smartdeveloperhub.curator.protocol.vocabulary.RDF;
import org.smartdeveloperhub.curator.protocol.vocabulary.XSD;

final class UseCase {

	private static final String BRANCH = "branch";
	private static final String COMMIT = "commit";

	private static final Logger LOGGER=LoggerFactory.getLogger(UseCase.class);

	static final String DOAP_NAMESPACE = "http://usefulinc.com/ns/doap#";
	static final String SCM_NAMESPACE  = "http://www.smartdeveloperhub.org/vocabulary/scm#";
	static final String CI_NAMESPACE   = "http://www.smartdeveloperhub.org/vocabulary/ci#";

	private UseCase() {
	}

	static EnrichmentRequest createRequest(final URI targetResource, final EnrichmentContext context) {
		LOGGER.warn("{} enrichment request creation is still missing",context);
		return
			EnrichmentRequest.
				newInstance().
					withTargetResource(targetResource).
					withFilters(
						Filters.
							newInstance().
								withFilter(ci("forBranch"), BRANCH).
								withFilter(ci("forCommit"), COMMIT)).
					withConstraints(
						Constraints.
							newInstance().
								forVariable("repository").
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
