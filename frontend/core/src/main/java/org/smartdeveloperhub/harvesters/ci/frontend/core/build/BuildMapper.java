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
package org.smartdeveloperhub.harvesters.ci.frontend.core.build;

import java.net.URI;

import org.ldp4j.application.data.DataSet;
import org.ldp4j.application.data.DataSetFactory;
import org.ldp4j.application.data.DataSetHelper;
import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.IndividualPropertyHelper;
import org.ldp4j.application.data.Name;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.Mapper;

final class BuildMapper implements BuildVocabulary {

	private BuildMapper() {
	}

	static DataSet toDataSet(Build build) {
		Name<URI> buildName=IdentityUtil.name(build);

		DataSet dataSet=DataSetFactory.createDataSet(buildName);

		DataSetHelper helper=DataSetUtils.newHelper(dataSet);

		IndividualPropertyHelper buildHeper =
			helper.
				managedIndividual(buildName, BuildHandler.ID).
					property(TYPE).
						withIndividual(AUTOMATION_PLAN).
						withIndividual(CI_BUILD_TYPE).
					property(DC_TERMS_IDENTIFIER).
						withLiteral(build.buildId()).
					property(DC_TERMS_CREATED).
						withLiteral(Mapper.toLiteral(build.createdOn())).
					property(DC_TERMS_TITLE).
						withLiteral(build.title()).
					property(DC_TERMS_DESCRIPTION).
						withLiteral(build.description()).
					property(CI_CODEBASE).
						withLiteral(build.codebase()).
					property(CI_LOCATION).
						withLiteral(build.location());

		if(build instanceof SubBuild) {
			addSubBuildDescription(buildHeper, (SubBuild)build);
		} else if(build instanceof CompositeBuild) {
			addCompositeBuildDescription(buildHeper, (CompositeBuild)build);
		}
		return dataSet;
	}

	private static void addCompositeBuildDescription(IndividualPropertyHelper buildHeper, CompositeBuild compositeBuild) {
		buildHeper.
			property(TYPE).
				withIndividual(CI_COMPOSITE_BUILD_TYPE);
		for(URI subBuild:compositeBuild.subBuilds()) {
			buildHeper.
				property(CI_INCLUDES_BUILD).
					withIndividual(IdentityUtil.subBuild(compositeBuild,subBuild),BuildHandler.ID);
		}
	}

	private static void addSubBuildDescription(IndividualPropertyHelper buildHeper, SubBuild subBuild) {
		buildHeper.
			property(TYPE).
				withIndividual(CI_SUB_BUILD_TYPE).
			property(CI_IS_PART_OF).
				withIndividual(IdentityUtil.parentBuild(subBuild),BuildHandler.ID);
	}

}