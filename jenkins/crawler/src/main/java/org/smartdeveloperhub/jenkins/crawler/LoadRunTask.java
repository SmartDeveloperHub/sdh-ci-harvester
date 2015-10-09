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
import org.smartdeveloperhub.jenkins.JenkinsURI;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.util.GitUtil;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;

import com.google.common.base.Optional;

final class LoadRunTask extends AbstractEntityCrawlingTask<Run> {

	private static final String SEPARATOR = "/";

	LoadRunTask(URI location) {
		super(location,Run.class,JenkinsEntityType.RUN,JenkinsArtifactType.RESOURCE);
	}

	@Override
	protected String taskPrefix() {
		return "lrt";
	}

	@Override
	protected void processEntity(Run run, JenkinsResource resource) throws IOException {
		if(run.getCodebase()!=null && run.getCodebase().toString().isEmpty()) {
			run.setCodebase(null);
		}
		run.setBranch(GitUtil.normalizeBranchName(run.getBranch()));

		if(JenkinsEntityType.MAVEN_MODULE_RUN.isCompatible(resource.entity())) {
			Run parent = super.entityOfId(parentURI(resource), JenkinsEntityType.MAVEN_MULTIMODULE_RUN,Run.class);
			if(parent!=null) {
				run.setCodebase(parent.getCodebase());
				run.setBranch(parent.getBranch());
				run.setCommit(parent.getCommit());
				super.persistEntity(run, JenkinsEntityType.MAVEN_MODULE_RUN);
			}
		}
		if(run.getCodebase()==null || run.getBranch()==null) {
			Job job = super.entityOfId(jobURI(resource), JenkinsEntityType.MAVEN_MULTIMODULE_BUILD,Job.class);
			if(job!=null) {
				run.setCodebase(Optional.fromNullable(run.getCodebase()).or(job.getCodebase()));
				run.setBranch(Optional.fromNullable(run.getBranch()).or(job.getBranch()));
				super.persistEntity(run, JenkinsEntityType.MAVEN_MODULE_RUN);
			}
		}

		super.fireEvent(
				JenkinsEventFactory.
					newRunCreatedEvent(
						super.jenkinsInstance(),
						run));

		if(JenkinsEntityType.MAVEN_RUN.isCompatible(resource.entity()) && run.getResult().equals(RunResult.SUCCESS)) {
			scheduleTask(new LoadRunArtifactsTask(super.location(),run));
		}

	}

	private URI parentURI(JenkinsResource resource) {
		JenkinsURI breakdown = JenkinsURI.create(resource.location());
		return URI.create(breakdown.instance()+"job/"+breakdown.job()+SEPARATOR+breakdown.run()+SEPARATOR);
	}

	private URI jobURI(JenkinsResource resource) {
		JenkinsURI breakdown = JenkinsURI.create(resource.location());
		return URI.create(breakdown.instance()+"job/"+breakdown.job()+SEPARATOR);
	}

}