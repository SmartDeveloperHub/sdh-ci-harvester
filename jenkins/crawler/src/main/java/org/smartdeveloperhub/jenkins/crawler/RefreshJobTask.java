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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.util.TaskUtils;
import org.smartdeveloperhub.jenkins.crawler.util.TaskUtils.ReferenceDifference;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeBuild;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunStatus;

final class RefreshJobTask extends AbstractCrawlingTask {

	private static final Logger LOGGER=LoggerFactory.getLogger(RefreshJobTask.class);

	private final Build build;

	RefreshJobTask(Build build) {
		super(build.getUrl(), JenkinsEntityType.JOB, JenkinsArtifactType.RESOURCE);
		this.build = build;
	}
	@Override
	protected String taskPrefix() {
		return "rjt";
	}

	@Override
	protected void processResource(JenkinsResource resource) throws IOException {
		Build currentBuild = super.loadBuild(resource);

		ReferenceDifference difference=
			TaskUtils.
				calculate(
					this.build.getRuns().getRuns(),
					currentBuild.getRuns().getRuns());

		persistEntity(currentBuild,resource.entity());

		scheduleTask(new LoadJobConfigurationTask(super.location(),currentBuild,resource.entity()));
		scheduleTask(new LoadJobSCMTask(super.location(),currentBuild));

		if(currentBuild instanceof CompositeBuild) {
			refreshSubBuilds((CompositeBuild)currentBuild);
		}

		for(URI createdRuns:difference.created()) {
			scheduleTask(new LoadRunTask(createdRuns));
		}

		for(URI maintainedRun:difference.maintained()) {
			try {
				Run run = super.entityOfId(maintainedRun,JenkinsEntityType.RUN,Run.class);
				if(run==null) {
					scheduleTask(new LoadRunTask(maintainedRun));
				} else if(RunStatus.RUNNING.equals(run.getStatus())) {
					scheduleTask(new RefreshRunTask(run));
				} // Otherwise the run is finished and we have nothing to do
			} catch (IOException e) {
				LOGGER.warn("Could not load persisted run '"+maintainedRun+"'",e);
			}
		}

		for(URI deletedRuns:difference.deleted()) {
			super.fireEvent(
				JenkinsEventFactory.
					newExecutionDeletedEvent(
						super.jenkinsInstance(),
						deletedRuns));
		}

	}
	private void refreshSubBuilds(CompositeBuild currentCompositeBuild) {
		ReferenceDifference difference=
			TaskUtils.
				calculate(
					((CompositeBuild)this.build).getSubBuilds().getBuilds(),
					currentCompositeBuild.getSubBuilds().getBuilds());

		for(URI createdBuild:difference.created()) {
			scheduleTask(new LoadJobTask(createdBuild));
		}

		for(URI maintainedBuild:difference.maintained()) {
			try {
				Build persistedBuild = super.entityOfId(maintainedBuild,JenkinsEntityType.JOB,Build.class);
				if(persistedBuild==null) {
					scheduleTask(new LoadJobTask(maintainedBuild));
				} else {
					scheduleTask(new RefreshJobTask(persistedBuild));
				}
			} catch (IOException e) {
				LOGGER.warn("Could not recover build '"+maintainedBuild+"'",e);
			}
		}

		for(URI deletedBuild:difference.deleted()) {
			super.fireEvent(
				JenkinsEventFactory.
					newBuildDeletedEvent(
						super.jenkinsInstance(),
						deletedBuild));
		}
	}

}
