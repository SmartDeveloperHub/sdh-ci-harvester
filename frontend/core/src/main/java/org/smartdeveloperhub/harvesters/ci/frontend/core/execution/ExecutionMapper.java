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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.execution;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.IndividualPropertyHelper;
import org.ldp4j.application.data.Name;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.Mapper;

final class ExecutionMapper implements ExecutionVocabulary {

	private static final URI RESULT_PATH = URI.create("#result");

	private ExecutionMapper() {
	}

	static DataSet toDataSet(Execution execution) {
		Name<URI> executionName=IdentityUtil.name(execution);

		ResultMapping resultMapping=
			ResultMapping.newInstance(execution.result());

		DataSet dataSet=DataSetFactory.createDataSet(executionName);

		DataSetHelper helper=DataSetUtils.newHelper(dataSet);

		Name<URI> buildName = IdentityUtil.buildName(execution);
		IndividualPropertyHelper executionHelper =
			helper.
				managedIndividual(executionName,ExecutionHandler.ID).
					property(TYPE).
						withIndividual(AUTO_AUTOMATION_REQUEST).
						withIndividual(CI_EXECUTION).
					property(DC_TERMS_IDENTIFIER).
						withLiteral(execution.executionId()).
					property(DC_TERMS_CREATED).
						withLiteral(Mapper.toLiteral(execution.createdOn())).
					property(DC_TERMS_TITLE).
						withLiteral("TODO: Add execution title").
					property(DC_TERMS_DESCRIPTION).
						withLiteral("TODO: Add execution description").
					property(EXECUTES_AUTOMATION_PLAN).
						withIndividual(buildName,BuildHandler.ID).
					property(CI_LOCATION).
						withLiteral(execution.executionId()).
					property(STATE).
						withIndividual(resultMapping.state()).
					property(CI_HAS_RESULT).
						withIndividual(executionName,ExecutionHandler.ID,RESULT_PATH);

		if(execution.isFinished()) {
			executionHelper.
				property(TYPE).
					withIndividual(CI_FINISHED_EXECUTION).
				property(CI_FINISHED).
					withLiteral(Mapper.toLiteral(execution.result().finishedOn()));
		} else {
			executionHelper.
				property(TYPE).
					withIndividual(CI_RUNNING_EXECUTION);
		}

		helper.
			relativeIndividual(executionName,ExecutionHandler.ID,RESULT_PATH).
				property(TYPE).
					withIndividual(AUTOMATION_RESULT).
					withIndividual(CI_EXECUTION_RESULT).
					withIndividual(resultMapping.resultType()).
				property(CI_LOCATION).
					withLiteral(execution.buildId()).
				property(STATE).
					withIndividual(resultMapping.state()).
				property(VERDICT).
					withIndividual(resultMapping.verdict()).
				property(REPORTS_ON_AUTOMATION_PLAN).
					withIndividual(buildName,BuildHandler.ID).
				property(PRODUCED_BY_AUTOMATION_REQUEST).
					withIndividual(executionName, ExecutionHandler.ID);

		return dataSet;
	}

}