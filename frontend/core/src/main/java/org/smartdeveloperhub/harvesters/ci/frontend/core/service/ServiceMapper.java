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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.3.0
 *   Bundle      : ci-frontend-core-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.service;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSets;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.IndividualPropertyHelper;
import org.ldp4j.application.data.Name;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Service;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;

final class ServiceMapper extends ServiceVocabulary {

	private static final URI VOCABULARY_PATH = URI.create("#vocabulary");

	private ServiceMapper() {
	}

	static DataSet toDataSet(Service service) {
		Name<URI> serviceName=IdentityUtil.serviceName(service);

		DataSet dataSet=DataSets.createDataSet(serviceName);

		DataSetHelper helper=DataSetUtils.newHelper(dataSet);

		IndividualPropertyHelper serviceHelper =
			helper.
				managedIndividual(serviceName, ServiceHandler.ID).
					property(TYPE).
						withIndividual(DC_TYPE_SERVICE_TYPE).
						withIndividual(MICRO_SERVICE_TYPE).
						withIndividual(LINKED_DATA_MICRO_SERVICE_TYPE).
						withIndividual(HARVESTER).
						withIndividual(CI_HARVESTER).
					property(HAS_RESOURCE_TYPE).
						withIndividual(CI_BUILD_TYPE).
					property(DOMAIN).
						withIndividual(CI_DOMAIN_TYPE).
					property(PROVIDES_DOMAIN).
						withIndividual(CI_DOMAIN_TYPE).
					property(VOCABULARY).
						withIndividual(serviceName,ServiceHandler.ID,VOCABULARY_PATH);

		for(URI buildId:service.builds()) {
			Name<URI> buildName=IdentityUtil.buildName(buildId);
			serviceHelper.
				property(HAS_RESOURCE).
					withIndividual(buildName,BuildHandler.ID);
		}

		helper.
			relativeIndividual(serviceName,ServiceHandler.ID,VOCABULARY_PATH).
				property(TYPE).
					withIndividual(CI_VOCABULARY_TYPE).
					withIndividual(VOCABULARY_TYPE).
				property(SOURCE).
					withLiteral(URI.create(CI_V1_TTL)).
				property(DC_TERMS_SOURCE).
					withLiteral(URI.create(CI_V1_TTL)).
				property(IMPLEMENTS).
					withIndividual(CI_DOMAIN_TYPE);

		return dataSet;
	}

}