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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;

import com.google.common.collect.Lists;

final class RefreshJobConfigurationTask extends AbstractJobConfigurationTask {

	private static final Logger LOGGER=LoggerFactory.getLogger(RefreshJobConfigurationTask.class);

	private final Job previousJob;

	RefreshJobConfigurationTask(final URI location, final Job previousJob, final Job job, final JenkinsEntityType type) {
		super(location,job,type);
		this.previousJob= previousJob;
	}

	@Override
	protected String taskPrefix() {
		return "rjct";
	}

	@Override
	protected void processSubresource(final Job parent, final JenkinsResource resource) {
		final Codebase currentCodebase = loadCodebase(parent, resource);
		final Codebase oldCodebase = this.previousJob.getCodebase();
		try {
			LOGGER.trace("Retrieved SCM information for {}: {}",parent.getUrl(),currentCodebase);
			final List<Codebase> codebases=Lists.newArrayList();
			codebases.add(currentCodebase);
			codebases.add(oldCodebase);
			final Codebase newCodebase=SCMUtil.mergeCodebases(codebases);
			if(SCMUtil.isDefined(newCodebase) && !newCodebase.equals(oldCodebase)) {
				LOGGER.debug("Updating SCM information for {} from {} to {}",parent.getUrl(),oldCodebase,newCodebase);
				parent.setCodebase(newCodebase);
				super.persistEntity(parent, entityType());
				super.fireEvent(JenkinsEventFactory.newJobUpdatedEvent(super.jenkinsInstance(),parent));
			}
		} catch (final Exception e) {
			LOGGER.error("Could not refresh SCM information {} for {}",currentCodebase,parent,e);
		}
	}

}