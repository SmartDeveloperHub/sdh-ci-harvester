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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.io.IOException;
import java.util.Objects;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;

final class RefreshRunTask extends AbstractEntityCrawlingTask<Run> {

	private final Run run;

	RefreshRunTask(final Run run) {
		super(run.getUrl(),Run.class,JenkinsEntityType.RUN,JenkinsArtifactType.RESOURCE);
		this.run = run;
	}

	@Override
	protected String taskPrefix() {
		return "rrt";
	}

	@Override
	protected void processEntity(final Run currentRun,final JenkinsResource resource) throws IOException {
		if(hasFinished(currentRun)) {
			super.fireEvent(
				JenkinsEventFactory.
					newRunUpdatedEvent(
						super.jenkinsInstance(),
						currentRun));
		}
		if(hasFinished(currentRun)) {
			if(JenkinsEntityType.MAVEN_RUN.isCompatible(resource.entity()) && currentRun.getResult().equals(RunResult.SUCCESS)) {
				scheduleTask(new LoadRunArtifactsTask(super.location(),currentRun));
			}
		}
	}

	@SuppressWarnings("unused")
	private boolean hasChanged(final Run currentRun) { // NOSONAR
		return
			!Objects.equals(currentRun.getTitle(),this.run.getTitle()) ||
			!Objects.equals(currentRun.getDescription(),this.run.getDescription()) ||
			hasFinished(currentRun);
	}

	private boolean hasFinished(final Run currentRun) {
		return !Objects.equals(currentRun.getStatus(),this.run.getStatus());
	}

}