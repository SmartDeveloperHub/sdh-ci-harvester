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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.1.0
 *   Bundle      : ci-frontend-core-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.service;


abstract class ServiceVocabulary {

	static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	static final String DC_TYPE_SERVICE_TYPE = "http://purl.org/dc/dcmitype/Service";
	static final String DC_TERMS_SOURCE      = "http://purl.org/dc/terms/source";

	static final String HARVESTER                      = "http://www.smartdeveloperhub.org/vocabulary/platform#Harvester";
	static final String LINKED_DATA_MICRO_SERVICE_TYPE = "http://www.smartdeveloperhub.org/vocabulary/platform#LinkedDataMicroService";
	static final String MICRO_SERVICE_TYPE             = "http://www.smartdeveloperhub.org/vocabulary/platform#MicroService";
	static final String VOCABULARY_TYPE                = "http://www.smartdeveloperhub.org/vocabulary/platform#Vocabulary";

	static final String DOMAIN            = "http://www.smartdeveloperhub.org/vocabulary/platform#domain";
	static final String PROVIDES_DOMAIN   = "http://www.smartdeveloperhub.org/vocabulary/platform#providesDomain";
	static final String VOCABULARY        = "http://www.smartdeveloperhub.org/vocabulary/platform#vocabulary";
	static final String HAS_RESOURCE_TYPE = "http://www.smartdeveloperhub.org/vocabulary/platform#hasResourceType";
	static final String HAS_RESOURCE      = "http://www.smartdeveloperhub.org/vocabulary/platform#hasResource";
	static final String SOURCE            = "http://www.smartdeveloperhub.org/vocabulary/platform#source";
	static final String IMPLEMENTS        = "http://www.smartdeveloperhub.org/vocabulary/platform#implements";

	static final String CI_BUILD_TYPE      = "http://www.smartdeveloperhub.org/vocabulary/ci#Build";
	static final String CI_HARVESTER       = "http://www.smartdeveloperhub.org/vocabulary/ci#CIHarvester";
	static final String CI_DOMAIN_TYPE     = "http://www.smartdeveloperhub.org/vocabulary/ci#CIDomain";
	static final String CI_VOCABULARY_TYPE = "http://www.smartdeveloperhub.org/vocabulary/ci#CIVocabulary";

	static final String CI_HAS_BUILD         = "http://www.smartdeveloperhub.org/vocabulary/ci#hasBuild";

	static final String CI_V1_TTL         = "http://www.smartdeveloperhub.org/vocabulary/v1/ci.ttl";

	ServiceVocabulary() {
	}

}
