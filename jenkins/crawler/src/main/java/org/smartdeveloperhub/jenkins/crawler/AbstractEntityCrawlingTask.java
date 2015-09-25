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
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Instance;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

abstract class AbstractEntityCrawlingTask<T extends Entity> extends AbstractCrawlingTask {

	private final Class<? extends T> clazz;

	AbstractEntityCrawlingTask(URI location, Class<? extends T> clazz, JenkinsEntityType entity, JenkinsArtifactType artifact) {
		super(location,entity,artifact);
		this.clazz=clazz;
	}

	private T loadEntity(JenkinsResource resource) throws IOException {
		Object object=null;
		if(resource.entity().isInstance()) {
			object=modelMapper().loadInstance(resource);
		} else if(resource.entity().isJob()) {
			object=modelMapper().loadJob(resource);
		} else if(resource.entity().isRun()) {
			object=modelMapper().loadRun(resource);
		} else {
			logger().error("Could not load entity for task {}: unexpected entity type {}",id(),resource.entity());
			throw new AssertionError("Unexpected entity type "+resource.entity());
		}
		return this.clazz.cast(object);
	}

	private boolean isProcessable(T entity) {
		boolean processEntity=false;
		if(entity instanceof Instance) {
			processEntity=true;
		} else if(entity instanceof Job) {
			processEntity=
				super.
					crawlingDecissionPoint().
						canProcessJob(
							(Job)entity,
							super.jenkinsInformationPoint(),
							super.currentCrawlingSession()
						);
		} else if(entity instanceof Run) {
			processEntity=
				super.
					crawlingDecissionPoint().
						canProcessRun(
							(Run)entity,
							super.jenkinsInformationPoint(),
							super.currentCrawlingSession()
						);
		}
		return processEntity;
	}

	@Override
	protected final void processResource(JenkinsResource resource) throws IOException {
		T entity=loadEntity(resource);
		persistEntity(entity,resource.entity());
		if(!isProcessable(entity)) {
			logger().info("Aborted processing of entity {}: not allowed",id());
			return;
		}
		processEntity(entity,resource);
	}

	protected abstract void processEntity(T entity, JenkinsResource resource) throws IOException;

}