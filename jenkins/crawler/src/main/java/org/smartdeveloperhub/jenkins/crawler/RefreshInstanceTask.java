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
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Instance;

final class RefreshInstanceTask extends AbstractCrawlingTask {

	private static final Logger LOGGER=LoggerFactory.getLogger(RefreshInstanceTask.class);

	private final Instance instance;

	RefreshInstanceTask(Instance instance) {
		super(instance.getUrl(), JenkinsEntityType.INSTANCE, JenkinsArtifactType.RESOURCE);
		this.instance = instance;
	}

	@Override
	protected String taskPrefix() {
		return "rit";
	}

	@Override
	protected void processResource(JenkinsResource resource) throws IOException {
		Instance currentService = super.loadInstance(resource);

		ReferenceDifference difference=
			TaskUtils.
				calculate(
					this.instance.getJobs().getJobs(),
					currentService.getJobs().getJobs());

		persistEntity(currentService,resource.entity());

		for(URI createdBuild:difference.created()) {
			scheduleTask(new LoadJobTask(createdBuild));
		}

		for(URI maintainedBuild:difference.maintained()) {
			try {
				Job build=super.entityOfId(maintainedBuild,JenkinsEntityType.JOB,Job.class);
				if(build==null) {
					scheduleTask(new LoadJobTask(maintainedBuild));
				} else {
					scheduleTask(new RefreshJobTask(build));
				}
			} catch (Exception e) {
				LOGGER.warn("Could not load persisted build '"+maintainedBuild+"'",e);
			}
		}

		for(URI deletedBuild:difference.deleted()) {
			super.fireEvent(
				JenkinsEventFactory.
					newJobDeletedEvent(
						super.jenkinsInstance(),
						deletedBuild));
		}

	}

}