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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

import java.net.URI;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

public enum JenkinsArtifactType {

	RESOURCE(
		"api/xml",
		JenkinsEntityType.INSTANCE,
		JenkinsEntityType.JOB,
		JenkinsEntityType.RUN
	),
	CONFIGURATION(
		"config.xml",
		JenkinsEntityType.JOB
	) {

		@Override
		public boolean isModelObject() {
			return false;
		}

	},
	SCM(
		"scm/api/xml",
		JenkinsEntityType.JOB
	),
	ARTIFACTS(
		"mavenArtifacts/api/xml",
		JenkinsEntityType.RUN
	),
	;

	private final Set<JenkinsEntityType> resources;
	private final String artifactPath;

	private JenkinsArtifactType(String artifactPath, JenkinsEntityType... types) {
		this.artifactPath = artifactPath;
		this.resources=EnumSet.copyOf(Arrays.asList(types));
	}

	private boolean isContainedBy(JenkinsEntityType type) {
		if(type==null) {
			return false;
		}
		return this.resources.contains(type.category());
	}

	public URI locate(URI resourceBase) {
		return resourceBase.resolve(this.artifactPath);
	}

	public static Set<JenkinsArtifactType> findArtifacts(JenkinsEntityType type) {
		if(type==null) {
			return EnumSet.noneOf(JenkinsArtifactType.class);
		}
		List<JenkinsArtifactType> artifacts=Lists.newArrayList();
		for(JenkinsArtifactType artifact:values()) {
			if(artifact.isContainedBy(type)) {
				artifacts.add(artifact);
			}
		}
		return EnumSet.copyOf(artifacts);
	}

	public boolean isModelObject() {
		return true;
	}

}
