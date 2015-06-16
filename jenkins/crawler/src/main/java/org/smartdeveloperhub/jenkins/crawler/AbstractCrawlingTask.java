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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.Status;
import org.smartdeveloperhub.jenkins.client.JenkinsClientException;
import org.smartdeveloperhub.jenkins.client.JenkinsResourceProxy;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationException;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEvent;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Service;

abstract class AbstractCrawlingTask implements Task {

	private static final int RETRY_THRESHOLD = 5;

	private final Logger logger=LoggerFactory.getLogger(getClass());

	private URI location;
	private JenkinsEntityType entity;
	private JenkinsArtifactType artifact;
	private Context context;
	private int retries;

	AbstractCrawlingTask(URI location, JenkinsEntityType entity, JenkinsArtifactType artifact) {
		this.location=location;
		this.entity=entity;
		this.artifact=artifact;
		this.retries=0;
	}

	private void setContext(Context context) {
		this.context=context;
	}

	private JenkinsResourceProxy createProxy() {
		return
			JenkinsResourceProxy.
				create(this.location).
					withUseHttps(true).
					withEntity(this.entity);
	}

	private void retryTask(Throwable failure) {
		this.retries++;
		if(this.retries<RETRY_THRESHOLD) {
			if(logger.isInfoEnabled()) {
				logger.info("Retrying {} ({})",this.location,this.retries+1);
			}
			scheduleTask(this);
		} else {
			failSilently(failure,"Could not retrieve '%s' aftet %d intents",this.location,this.retries);
		}
	}

	private void failSilently(Throwable failure, String errorMessage, Object... args) {
		String log=String.format(errorMessage,args);
		logger.error(log,failure);
		throw new JenkinsClientException(log,failure);
	}

	private void persistResource(JenkinsResource resource) {
		try {
			this.context.resourceRepository().saveResource(resource);
		} catch (IOException e) {
			failSilently(e,"Could not persist resource %s",resource);
		}
	}

	private void dispatchResource(JenkinsResource resource) {
		if(Status.AVAILABLE.equals(resource.status())) {
			try {
				processResource(resource);
			} catch (IOException e) {
				failSilently(e,"Could not process resource %s",resource);
			}
		}
	}

	@Override
	public final String id() {
		return String.format("%s:%s:%s:%s",taskPrefix(),entity,artifact,location);
	}

	@Override
	public final void execute(Context context) {
		setContext(context);
		try {
			JenkinsResource resource=
					createProxy().
						get(this.artifact);
			persistResource(resource);
			dispatchResource(resource);
		} catch (IOException e) {
			retryTask(e);
		}
	}

	protected final URI jenkinsInstance() {
		checkState(this.context!=null);
		return this.context.jenkinsInstance();
	}

	protected final URI location() {
		return this.location;
	}

	protected final JenkinsEntityType entityType() {
		return this.entity;
	}

	protected final Build loadBuild(JenkinsResource resource) throws TransformationException {
		checkState(this.context!=null);
		return this.context.modelMapper().loadBuild(resource);
	}

	protected final Run loadRun(JenkinsResource resource) throws TransformationException {
		checkState(this.context!=null);
		return this.context.modelMapper().loadRun(resource);
	}

	protected final Service loadService(JenkinsResource resource) throws TransformationException {
		checkState(this.context!=null);
		return this.context.modelMapper().loadService(resource);
	}

	protected final void scheduleTask(Task task) {
		checkState(this.context!=null);
		this.context.schedule(task);
	}

	protected final void persistEntity(Entity entity, JenkinsEntityType type) throws IOException {
		checkState(this.context!=null);
		this.context.entityRepository().saveEntity(entity,type);
	}

	protected final void fireEvent(JenkinsEvent event) {
		checkState(this.context!=null);
		System.err.println(event);
		this.context.fireEvent(event);
	}

	protected abstract String taskPrefix();

	protected abstract void processResource(JenkinsResource resource) throws IOException;

	public <T extends Entity> T entityOfId(URI id, JenkinsEntityType entityType, Class<? extends T> entityClass) throws IOException {
		checkState(this.context!=null);
		return this.context.entityRepository().entityOfId(id,entityType,entityClass);
	}
}