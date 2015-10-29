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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.build;


abstract class BuildVocabulary {

	static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	static final String AUTOMATION_PLAN = "http://open-services.net/ns/auto#AutomationPlan";

	static final String DC_TERMS_TITLE       = "http://purl.org/dc/terms/title";
	static final String DC_TERMS_CREATED     = "http://purl.org/dc/terms/created";
	static final String DC_TERMS_IDENTIFIER  = "http://purl.org/dc/terms/identifier";
	static final String DC_TERMS_DESCRIPTION = "http://purl.org/dc/terms/description";

	static final String CI_BUILD_TYPE           = "http://www.smartdeveloperhub.org/vocabulary/ci#Build";
	static final String CI_COMPOSITE_BUILD_TYPE = "http://www.smartdeveloperhub.org/vocabulary/ci#CompositeBuild";
	static final String CI_SUB_BUILD_TYPE       = "http://www.smartdeveloperhub.org/vocabulary/ci#SubBuild";
	static final String CI_LOCATION             = "http://www.smartdeveloperhub.org/vocabulary/ci#location";
	static final String SCM_LOCATION            = "http://www.smartdeveloperhub.org/vocabulary/scm#location";
	static final String CI_BRANCH_SPECIFIER     = "http://www.smartdeveloperhub.org/vocabulary/ci#branchSpecifier";
	static final String CI_HAS_EXECUTION        = "http://www.smartdeveloperhub.org/vocabulary/ci#hasResource";
	static final String CI_INCLUDES_BUILD       = "http://www.smartdeveloperhub.org/vocabulary/ci#includesBuild";
	static final String CI_IS_PART_OF           = "http://www.smartdeveloperhub.org/vocabulary/ci#isPartOf";

	BuildVocabulary() {
	}

}
