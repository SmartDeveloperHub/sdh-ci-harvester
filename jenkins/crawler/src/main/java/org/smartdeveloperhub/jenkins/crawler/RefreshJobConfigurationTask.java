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

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.util.GitUtil;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.util.xml.XmlUtils;

final class RefreshJobConfigurationTask extends AbstractArtifactCrawlingTask<Job> {

	private static final Logger LOGGER=LoggerFactory.getLogger(RefreshJobConfigurationTask.class);

	private final Job previousJob;

	RefreshJobConfigurationTask(URI location, Job previousJob, Job job, JenkinsEntityType type) {
		super(location,type,JenkinsArtifactType.CONFIGURATION,job);
		this.previousJob= previousJob;
	}

	@Override
	protected String taskPrefix() {
		return "rjct";
	}

	@Override
	protected void processSubresource(Job parent, JenkinsResource resource) {
		try {
			String rawURI=
				XmlUtils.
					evaluateXPath(
						"//scm[@class='hudson.plugins.git.GitSCM']/userRemoteConfigs//url",
						resource.content().get());
			boolean updatedCodebase=false;
			if(rawURI!=null && !rawURI.isEmpty()) {
				updatedCodebase=true;
				parent.withCodebase(URI.create(rawURI));
			}
			String branchName=
				XmlUtils.
					evaluateXPath(
						"//scm[@class='hudson.plugins.git.GitSCM']/branches//name",
						resource.content().get());
			boolean updatedBranchName=false;
			if(branchName!=null && !branchName.isEmpty()) {
				updatedBranchName=true;
				parent.setBranch(GitUtil.normalizeBranchName(branchName));
			}
			if(updatedCodebase && !parent.getCodebase().equals(this.previousJob.getCodebase())) {
				persistEntity(parent, entityType());
				super.fireEvent(JenkinsEventFactory.newJobUpdatedEvent(super.jenkinsInstance(),parent));
			} else if(updatedBranchName && !parent.getBranch().equals(this.previousJob.getBranch())) {
				persistEntity(parent, entityType());
				super.fireEvent(JenkinsEventFactory.newJobUpdatedEvent(super.jenkinsInstance(),parent));
			}
		} catch (Exception e) {
			LOGGER.error("Could not recover SCM information",e);
		}
	}

}