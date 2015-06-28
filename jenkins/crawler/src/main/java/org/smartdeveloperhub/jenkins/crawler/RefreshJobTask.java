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
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeJob;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunStatus;

final class RefreshJobTask extends AbstractEntityCrawlingTask<Job> {

	private static final Logger LOGGER=LoggerFactory.getLogger(RefreshJobTask.class);

	private final Job job;

	RefreshJobTask(Job job) {
		super(job.getUrl(),Job.class,JenkinsEntityType.JOB,JenkinsArtifactType.RESOURCE);
		this.job = job;
	}
	@Override
	protected String taskPrefix() {
		return "rjt";
	}

	@Override
	protected void processEntity(Job currentJob, JenkinsResource resource) throws IOException {
		ReferenceDifference difference=
			TaskUtils.
				calculate(
					this.job.getRuns().getRuns(),
					currentJob.getRuns().getRuns());

		scheduleTask(new LoadJobConfigurationTask(super.location(),currentJob,resource.entity()));
		scheduleTask(new LoadJobSCMTask(super.location(),currentJob));

		if(currentJob instanceof CompositeJob) {
			refreshSubJobs((CompositeJob)currentJob);
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
					newRunDeletedEvent(
						super.jenkinsInstance(),
						deletedRuns));
		}

	}
	private void refreshSubJobs(CompositeJob currentCompositeJob) {
		ReferenceDifference difference=
			TaskUtils.
				calculate(
					((CompositeJob)this.job).getSubJobs().getJobs(),
					currentCompositeJob.getSubJobs().getJobs());

		for(URI createdJob:difference.created()) {
			scheduleTask(new LoadJobTask(createdJob));
		}

		for(URI maintainedJob:difference.maintained()) {
			try {
				Job persistedJob = super.entityOfId(maintainedJob,JenkinsEntityType.JOB,Job.class);
				if(persistedJob==null) {
					scheduleTask(new LoadJobTask(maintainedJob));
				} else {
					scheduleTask(new RefreshJobTask(persistedJob));
				}
			} catch (IOException e) {
				LOGGER.warn("Could not recover persisted job '"+maintainedJob+"'",e);
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
