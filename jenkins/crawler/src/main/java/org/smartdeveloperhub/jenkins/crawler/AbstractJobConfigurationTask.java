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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.JenkinsURI;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;

import com.google.common.collect.Lists;

abstract class AbstractJobConfigurationTask extends AbstractArtifactCrawlingTask<Job> {

	private static final Logger LOGGER=LoggerFactory.getLogger(AbstractJobConfigurationTask.class);

	private static final String SEPARATOR = "/";

	AbstractJobConfigurationTask(final URI location, final Job job, final JenkinsEntityType type) {
		super(location,type,JenkinsArtifactType.CONFIGURATION,job);
	}

	protected final Codebase loadCodebase(final Job parent, final JenkinsResource resource) {
		final List<Codebase> codebases=Lists.newArrayList();
		addSCMInformationFromConfiguration(resource, codebases);

		if(JenkinsEntityType.MAVEN_MODULE_BUILD.isCompatible(resource.entity())) {
			// We know for sure that this type of job does not bear any SCM
			// information in its configuration
			addSCMInformationFromAggregatorJob(parent, codebases);
		}

		return SCMUtil.mergeCodebases(codebases);
	}

	private void addSCMInformationFromConfiguration(final JenkinsResource resource, final List<Codebase> codebases) {
		final Codebase codebase = SCMUtil.createCodebase(resource);
		LOGGER.trace("Gathering SCM information {} from configuration {}",codebase,resource.location());
		codebases.add(codebase);
	}

	private void addSCMInformationFromAggregatorJob(final Job job, final List<Codebase> codebases) {
		final JenkinsURI breakdown = JenkinsURI.create(job.getUrl());
		final URI parentURI = URI.create(breakdown.instance()+"job/"+breakdown.job()+SEPARATOR);
		try {
			final Job aggregator = super.entityOfId(parentURI,JenkinsEntityType.MAVEN_MULTIMODULE_BUILD,Job.class);
			if(aggregator!=null) {
				final Codebase aggregatorCodebase = aggregator.getCodebase();
				LOGGER.trace("Gathering SCM information {} from aggregator job {}",aggregatorCodebase,aggregator.getUrl());
				codebases.add(aggregatorCodebase);
			} else {
				LOGGER.warn("Could not find aggregator job {} of module job {}",parentURI,job.getUrl());
			}
		} catch (final IOException e) {
			LOGGER.error("Could not retrieve aggregator job {} for module job {}",parentURI,job.getUrl(),e);
		}
	}

}