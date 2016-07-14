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
package org.smartdeveloperhub.jenkins.crawler.application;

import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationException;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationService;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Instance;
import org.w3c.dom.Document;

import com.google.common.collect.ImmutableMap;

public final class ModelMappingService {

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

	public Instance loadInstance(JenkinsResource resource) throws TransformationException {
		Document content = resource.content().get();
		String localName =
			content.
				getFirstChild().
					getNodeName();
		Source data = new DOMSource(content);
		return
			this.transformationService.transform(
				localName,
				data,
				createParameters(resource),
				Instance.class
			);
	}

	public Job loadJob(JenkinsResource resource) throws TransformationException {
		Document content = resource.content().get();
		String localName =
			content.
				getFirstChild().
					getNodeName();
		Source data = new DOMSource(content);
		return
			this.transformationService.transform(
				localName,
				data,
				createParameters(resource),
				Job.class
			);
	}

	public Run loadRun(JenkinsResource resource) throws TransformationException {
		Document content = resource.content().get();
		String localName =
			content.
				getFirstChild().
					getNodeName();
		Source data = new DOMSource(content);
		return
			this.transformationService.transform(
				localName,
				data,
				createParameters(resource),
				Run.class
			);
	}

	public static ModelMappingService newInstance(TransformationService transformationManager) {
		return new ModelMappingService(transformationManager);
	}

}
