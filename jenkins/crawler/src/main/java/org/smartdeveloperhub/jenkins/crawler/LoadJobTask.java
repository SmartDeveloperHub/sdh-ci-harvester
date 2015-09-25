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
import java.net.URI;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeJob;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;

final class LoadJobTask extends AbstractEntityCrawlingTask<Job> {

	LoadJobTask(URI location) {
		super(location,Job.class,JenkinsEntityType.JOB,JenkinsArtifactType.RESOURCE);
	}

	@Override
	protected String taskPrefix() {
		return "ljt";
	}

	@Override
	protected void processEntity(Job job, JenkinsResource resource) throws IOException {
		super.fireEvent(
			JenkinsEventFactory.
				newJobCreatedEvent(super.jenkinsInstance(),job));

		scheduleTask(new LoadJobConfigurationTask(super.location(),job,resource.entity()));
		scheduleTask(new LoadJobSCMTask(super.location(),job));

		if(job instanceof CompositeJob) {
			CompositeJob compositeJob=(CompositeJob)job;
			for(Reference ref:compositeJob.getSubJobs().getJobs()) {
				scheduleTask(new LoadJobTask(ref.getValue()));
			}
		}

		for(Reference ref:job.getRuns().getRuns()) {
			scheduleTask(new LoadRunTask(ref.getValue()));
		}

	}

}