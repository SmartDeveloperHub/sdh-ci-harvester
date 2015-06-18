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

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.util.xml.XmlUtils;

final class LoadJobConfigurationTask extends AbstractCrawlingSubTask<Build> {

	private static final Logger LOGGER=LoggerFactory.getLogger(LoadJobConfigurationTask.class);

	LoadJobConfigurationTask(URI location, Build build, JenkinsEntityType type) {
		super(location,type,JenkinsArtifactType.CONFIGURATION,build);
	}

	@Override
	protected String taskPrefix() {
		return "ljct";
	}

	@Override
	protected void processSubresource(Build parent, JenkinsResource resource) {
		try {
			String rawURI=
				XmlUtils.
					evaluateXPath(
						"//scm[@class='hudson.plugins.git.GitSCM']/userRemoteConfigs//url",
						resource.content().get());
			parent.withCodebase(URI.create(rawURI));
			persistEntity(parent, entityType());
			super.fireEvent(JenkinsEventFactory.newBuildUpdatedEvent(super.jenkinsInstance(),parent));
		} catch (Exception e) {
			LOGGER.error("Could not recover SCM information",e);
		}
	}

}