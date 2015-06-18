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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.io.IOException;
import java.net.URI;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeBuild;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;

final class LoadJobTask extends AbstractCrawlingTask {

	LoadJobTask(URI location) {
		super(location,JenkinsEntityType.JOB,JenkinsArtifactType.RESOURCE);
	}

	@Override
	protected String taskPrefix() {
		return "ljt";
	}

	@Override
	protected void processResource(JenkinsResource resource) throws IOException {
		Build build = super.loadBuild(resource);
		persistEntity(build,resource.entity());

		super.fireEvent(
			JenkinsEventFactory.
				newBuildCreatedEvent(super.jenkinsInstance(),build));

		scheduleTask(new LoadJobConfigurationTask(super.location(),build,resource.entity()));
		scheduleTask(new LoadJobSCMTask(super.location(),build));

		if(build instanceof CompositeBuild) {
			CompositeBuild cBuild=(CompositeBuild)build;
			for(Reference ref:cBuild.getSubBuilds().getBuilds()) {
				scheduleTask(new LoadJobTask(ref.getValue()));
			}
		}

		for(Reference ref:build.getRuns().getRuns()) {
			scheduleTask(new LoadRunTask(ref.getValue()));
		}

	}

}