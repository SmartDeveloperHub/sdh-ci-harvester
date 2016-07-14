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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.4.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.4.0-SNAPSHOT.jar
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
import org.smartdeveloperhub.jenkins.crawler.application.ModelMappingService;
import org.smartdeveloperhub.jenkins.crawler.application.spi.TransformationException;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEvent;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Instance;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

import com.google.common.base.Optional;

abstract class AbstractCrawlingTask implements Task {

	private static final int RETRY_THRESHOLD = 5;

	private final Logger logger=LoggerFactory.getLogger(getClass()); // NOSONAR

	private final URI location;
	private final JenkinsEntityType entity;
	private final JenkinsArtifactType artifact;
	private Context context;
	private int retries;


	AbstractCrawlingTask(final URI location, final JenkinsEntityType entity, final JenkinsArtifactType artifact) {
		this.location=location;
		this.entity=entity;
		this.artifact=artifact;
		this.retries=0;
	}

	private void setContext(final Context context) {
		this.context=context;
	}

	private JenkinsResourceProxy createProxy() {
		JenkinsResourceProxy result = JenkinsResourceProxy.
			create(this.location).
				withEntity(this.entity);
		final Optional<Long> depth=getDepth();
		if(depth.isPresent()) {
			result=result.withDepth(depth.get());
		}
		final Optional<String> tree=getTree();
		if(tree.isPresent()) {
			result=result.withTree(tree.get());
		}
		final Optional<String> wrapper=getWrapper();
		if(wrapper.isPresent()) {
			result=result.withWrapper(wrapper.get());
		}
		final Optional<String> xpath=getXPath();
		if(xpath.isPresent()) {
			result=result.withXPath(xpath.get());
		}
		return result;
	}

	protected Optional<Long> getDepth() {
		return Optional.absent();
	}

	protected Optional<String> getTree() {
		return Optional.absent();
	}

	protected Optional<String> getWrapper() {
		return Optional.absent();
	}

	protected Optional<String> getXPath() {
		return Optional.absent();
	}

	private void retryTask(final Throwable failure) {
		this.retries++;
		if(this.retries<RETRY_THRESHOLD) {
			if(this.logger.isInfoEnabled()) {
				this.logger.info("Retrying {} ({})",this.location,this.retries+1);
			}
			scheduleTask(this);
		} else {
			failSilently(failure,"Could not retrieve '%s' after %d intents",this.location,this.retries);
		}
	}

	private void failSilently(final Throwable failure, final String errorMessage, final Object... args) {
		final String log=String.format(errorMessage,args);
		this.logger.error(log,failure);
		throw new JenkinsClientException(log,failure);
	}

	private void persistResource(final JenkinsResource resource) {
		try {
			this.context.resourceRepository().saveResource(resource);
		} catch (final IOException e) {
			failSilently(e,"Could not persist resource %s",resource);
		}
	}

	private void dispatchResource(final JenkinsResource resource) {
		if(Status.AVAILABLE.equals(resource.status())) {
			try {
				processResource(resource);
			} catch (final IOException e) {
				failSilently(e,"Could not process resource %s",resource);
			}
		} else {
			this.logger.warn("Resource {} not available ({})",resource.location(),resource.status());
		}
	}

	@Override
	public final String id() {
		return String.format("%s:%s:%s:%s",taskPrefix(),this.entity,this.artifact,this.location);
	}

	@Override
	public final void execute(final Context context) {
		if(!context.
			crawlingDecissionPoint().
				canProcessEntityType(
					this.entity,
					context.jenkinsInformationPoint(),
					context.currentSession())) {
			this.logger.info("Dismissing execution of task {}: not allowed",id());
			return;
		}

		setContext(context);
		try {
			final JenkinsResource resource=
				createProxy().
					get(this.artifact);
			persistResource(resource);

			if(!this.entity.isCompatible(resource.entity())) {
				this.logger.warn("Aborted processing of task {}: incompatible returned entity type ({} not compatible with {})",id(),resource.entity(),this.entity);
				return;
			}
			dispatchResource(resource);
		} catch (final IOException e) {
			retryTask(e);
		}
	}

	protected final Logger logger() {
		return this.logger;
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

	protected final  void persistEntity(final Entity entity, final JenkinsEntityType type) throws IOException {
		checkState(this.context!=null);
		this.context.entityRepository().saveEntity(entity,type);
	}

	protected final Job loadJob(final JenkinsResource resource) throws TransformationException {
		checkState(this.context!=null);
		return this.context.modelMapper().loadJob(resource);
	}

	protected final Run loadRun(final JenkinsResource resource) throws TransformationException {
		checkState(this.context!=null);
		return this.context.modelMapper().loadRun(resource);
	}

	protected final Instance loadInstance(final JenkinsResource resource) throws TransformationException {
		checkState(this.context!=null);
		return this.context.modelMapper().loadInstance(resource);
	}

	protected final void scheduleTask(final Task task) {
		checkState(this.context!=null);
		this.context.schedule(task);
	}

	protected final void fireEvent(final JenkinsEvent event) {
		checkState(this.context!=null);
		this.context.fireEvent(event);
	}

	protected final <S extends Entity> S entityOfId(final URI id, final JenkinsEntityType entityType, final Class<? extends S> entityClass) throws IOException {
		checkState(this.context!=null);
		return this.context.entityRepository().entityOfId(id,entityType,entityClass);
	}

	protected final ModelMappingService modelMapper() {
		return this.context.modelMapper();
	}

	protected final CrawlingDecissionPoint crawlingDecissionPoint() {
		checkState(this.context!=null);
		return this.context.crawlingDecissionPoint();
	}

	protected final JenkinsInformationPoint jenkinsInformationPoint() {
		checkState(this.context!=null);
		return this.context.jenkinsInformationPoint();
	}

	protected final CrawlingSession currentCrawlingSession() {
		checkState(this.context!=null);
		return this.context.currentSession();
	}

	protected abstract String taskPrefix();

	protected abstract void processResource(JenkinsResource resource) throws IOException;

}