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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0
 *   Bundle      : ci-jenkins-crawler-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.util.TaskUtils;
import org.smartdeveloperhub.jenkins.crawler.util.TaskUtils.ReferenceDifference;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeJob;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunStatus;

import com.google.common.base.Optional;

final class RefreshJobTask extends AbstractEntityCrawlingTask<Job> {

	private static final Logger LOGGER=LoggerFactory.getLogger(RefreshJobTask.class);

	private final Job job;

	RefreshJobTask(final Job job) {
		super(job.getUrl(),Job.class,JenkinsEntityType.JOB,JenkinsArtifactType.RESOURCE);
		this.job = job;
	}
	@Override
	protected String taskPrefix() {
		return "rjt";
	}

	@Override
	protected Optional<String> getTree() {
		return Optional.of("name,url,description,displayName,buildable,builds[url,number],scm[*[*]]");
	}

	@Override
	protected void processEntity(final Job currentJob, final JenkinsResource resource) throws IOException {
		final ReferenceDifference difference=
			TaskUtils.
				calculate(
					this.job.getRuns().getRuns(),
					currentJob.getRuns().getRuns());

		if(hasChanged(currentJob)) {
			super.fireEvent(
				JenkinsEventFactory.
					newJobUpdatedEvent(
						super.jenkinsInstance(),
						currentJob));
		}

		if(currentJob instanceof CompositeJob) {
			refreshSubJobs((CompositeJob)currentJob);
		}

		for(final URI createdRuns:difference.created()) {
			scheduleTask(new LoadRunTask(createdRuns));
		}

		for(final URI maintainedRun:difference.maintained()) {
			try {
				final Run run = super.entityOfId(maintainedRun,JenkinsEntityType.RUN,Run.class);
				if(run==null) {
					scheduleTask(new LoadRunTask(maintainedRun));
				} else if(RunStatus.RUNNING.equals(run.getStatus())) {
					scheduleTask(new RefreshRunTask(run));
				} // Otherwise the run is finished and we have nothing to do
			} catch (final IOException e) {
				LOGGER.warn("Could not load persisted run '"+maintainedRun+"'",e);
			}
		}

		for(final URI deletedRuns:difference.deleted()) {
			super.fireEvent(
				JenkinsEventFactory.
					newRunDeletedEvent(
						super.jenkinsInstance(),
						deletedRuns));
		}

	}

	private boolean hasChanged(final Job currentJob) {
		return
			!Objects.equals(currentJob.getTitle(),this.job.getTitle()) ||
			!Objects.equals(currentJob.getDescription(),this.job.getDescription()) ||
			!Objects.equals(currentJob.getCodebase(),this.job.getCodebase());
	}

	private void refreshSubJobs(final CompositeJob currentCompositeJob) {
		final ReferenceDifference difference=
			TaskUtils.
				calculate(
					((CompositeJob)this.job).getSubJobs().getJobs(),
					currentCompositeJob.getSubJobs().getJobs());

		for(final URI createdJob:difference.created()) {
			scheduleTask(new LoadJobTask(createdJob));
		}

		for(final URI maintainedJob:difference.maintained()) {
			try {
				final Job persistedJob = super.entityOfId(maintainedJob,JenkinsEntityType.JOB,Job.class);
				if(persistedJob==null) {
					scheduleTask(new LoadJobTask(maintainedJob));
				} else {
					scheduleTask(new RefreshJobTask(persistedJob));
				}
			} catch (final IOException e) {
				LOGGER.warn("Could not recover persisted job '"+maintainedJob+"'",e);
			}
		}

		for(final URI deletedBuild:difference.deleted()) {
			super.fireEvent(
				JenkinsEventFactory.
					newJobDeletedEvent(
						super.jenkinsInstance(),
						deletedBuild));
		}
	}

}
