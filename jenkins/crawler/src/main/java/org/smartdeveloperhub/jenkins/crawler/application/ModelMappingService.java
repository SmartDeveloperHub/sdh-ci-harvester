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
package org.smartdeveloperhub.jenkins.crawler.application;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationException;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationService;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Service;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;

public final class ModelMappingService {

	@SuppressWarnings("unused")
	private static final Logger LOGGER=LoggerFactory.getLogger(ModelMappingService.class);

	private final TransformationService transformationService;

	private ModelMappingService(TransformationService transformationService) {
		this.transformationService = transformationService;
	}

	private Map<String, Object> createParameters(JenkinsResource resource) {
		return
			ImmutableMap.
				<String,Object>builder().
					put("serviceUrl",resource.location()).
					build();
	}

	public Service loadService(JenkinsResource resource) throws TransformationException {
		Document content = resource.content().get();
		String localName =
			content.
				getFirstChild().
					getNodeName();
		Source data = new DOMSource(content);
		Service service=
			this.transformationService.transform(
				localName,
				data,
				createParameters(resource),
				Service.class
			);
		return service;
	}

	public Build loadBuild(JenkinsResource resource) throws TransformationException {
		Document content = resource.content().get();
		String localName =
			content.
				getFirstChild().
					getNodeName();
		Source data = new DOMSource(content);
		Build build=
			this.transformationService.transform(
				localName,
				data,
				createParameters(resource),
				Build.class
			);
		return build;
	}

	public Run loadRun(JenkinsResource resource) throws TransformationException {
		Document content = resource.content().get();
		String localName =
			content.
				getFirstChild().
					getNodeName();
		Source data = new DOMSource(content);
		Run run=
			this.transformationService.transform(
				localName,
				data,
				createParameters(resource),
				Run.class
			);
		return run;
	}

	public static ModelMappingService newInstance(TransformationService transformationManager) {
		return new ModelMappingService(transformationManager);
	}

}
