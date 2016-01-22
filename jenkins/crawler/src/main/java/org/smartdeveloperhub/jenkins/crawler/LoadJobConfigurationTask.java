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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;

final class LoadJobConfigurationTask extends AbstractJobConfigurationTask {

	private static final Logger LOGGER=LoggerFactory.getLogger(LoadJobConfigurationTask.class);

	LoadJobConfigurationTask(final URI location, final Job job, final JenkinsEntityType type) {
		super(location,job,type);
	}

	@Override
	protected String taskPrefix() {
		return "ljct";
	}

	@Override
	protected void processSubresource(final Job parent, final JenkinsResource resource) {
		final Codebase codebase = loadCodebase(parent, resource);
		try {
			LOGGER.trace("Retrieved SCM information for {}: {}",parent.getUrl(),codebase);
			if(SCMUtil.isDefined(codebase)) {
				LOGGER.debug("Setting SCM information for {} to {}",parent.getUrl(),codebase);
				parent.withCodebase(codebase);
				super.persistEntity(parent, entityType());
				super.fireEvent(JenkinsEventFactory.newJobUpdatedEvent(super.jenkinsInstance(),parent));
			}
		} catch (final IOException e) {
			LOGGER.error("Could not update SCM information {} for {}",codebase,parent,e);
		}
	}

}