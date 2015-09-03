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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.1.0
 *   Bundle      : ci-jenkins-crawler-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.ResourceRepository;
import org.smartdeveloperhub.jenkins.crawler.application.ModelMappingService;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventDispatcher;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.transformation.TransformationManager;
import org.smartdeveloperhub.jenkins.crawler.util.ListenerManager;
import org.smartdeveloperhub.jenkins.crawler.util.Notification;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.EntityRepository;

import com.google.common.base.Optional;

public final class JenkinsCrawler {

	public static final class Builder {

		private URI location;
		private File directory;
		private OperationStrategy operationStrategy;
		private CrawlingStrategy crawlingStrategy;
		private int numberOfWorkers;

		private Builder() {
			this.numberOfWorkers=Runtime.getRuntime().availableProcessors();
		}

		public Builder withLocation(URI location) {
			this.location = location;
			return this;
		}

		public Builder withLocation(String location) {
			this.location = URI.create(location);
			return this;
		}

		public Builder withDirectory(File directory) {
			this.directory = directory;
			return this;
		}

		public Builder withWorkerCount(int numberOfWorkers) {
			checkArgument(0<numberOfWorkers,"At least one worker is required (%s)",numberOfWorkers);
			checkArgument(numberOfWorkers<=Runtime.getRuntime().availableProcessors(),"No more than %s workers can be used (%s)",Runtime.getRuntime().availableProcessors(),numberOfWorkers);
			this.numberOfWorkers=numberOfWorkers;
			return this;
		}

		public Builder withOperationStrategy(OperationStrategy operationStrategy) {
			this.operationStrategy = operationStrategy;
			return this;
		}

		public Builder withCrawlingStrategy(CrawlingStrategy crawlingStrategy) {
			this.crawlingStrategy = crawlingStrategy;
			return this;
		}

		public JenkinsCrawler build() throws JenkinsCrawlerException {
			checkNotNull(this.location,"Service instance cannot be null");
			try {
				return
					new JenkinsCrawler(
						this.location,
						createFileBasedStorage(),
						ModelMappingService.
							newInstance(TransformationManager.newInstance()),
						Optional.fromNullable(this.operationStrategy).or(OperationStrategy.builder().build()),
						Optional.fromNullable(this.crawlingStrategy).or(CrawlingStrategy.builder().build()),
						this.numberOfWorkers);
			} catch (IOException e) {
				String errorMessage = "Could not setup persistency layer";
				LOGGER.debug(errorMessage+". Full stacktrace follows",e);
				throw new JenkinsCrawlerException(errorMessage,e);
			}
		}

		private FileBasedStorage createFileBasedStorage() throws IOException {
			return
				FileBasedStorage.
					builder().
						withWorkingDirectory(this.directory).
						withConfigFile(
							this.directory==null?
								null:
								new File(this.directory,"repository.xml")).
						build();
		}

	}

	private static final class JenkinsEventNotification implements Notification<JenkinsEventListener> {

		private JenkinsEvent event;

		private JenkinsEventNotification(JenkinsEvent event) {
			this.event = event;
		}

		@Override
		public void propagate(JenkinsEventListener listener) {
			JenkinsEventDispatcher.
				create(listener).
					fireEvent(this.event);
		}

	}

	private final class LocalContext implements Context {

		@Override
		public URI jenkinsInstance() {
			return JenkinsCrawler.this.instance;
		}

		@Override
		public ModelMappingService modelMapper() {
			return JenkinsCrawler.this.mappingService;
		}

		@Override
		public EntityRepository entityRepository() {
			return JenkinsCrawler.this.storage;
		}

		@Override
		public ResourceRepository resourceRepository() {
			return JenkinsCrawler.this.storage;
		}

		@Override
		public void fireEvent(JenkinsEvent event) {
			JenkinsCrawler.this.jenkinsEventListeners.notify(new JenkinsEventNotification(event));
		}

		@Override
		public void schedule(Task task) {
			JenkinsCrawler.this.scheduler.schedule(task);
		}

		@Override
		public CrawlingDecissionPoint crawlingDecissionPoint() {
			return JenkinsCrawler.this.crawlingStrategy.decissionPoint();
		}

		@Override
		public JenkinsInformationPoint jenkinsInformationPoint() {
			return JenkinsCrawler.this.cip.jenkinsInformationPoint();
		}

		@Override
		public CrawlingSession currentSession() {
			return JenkinsCrawler.this.cip.currentCrawlingSession();
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsCrawler.class);

	private final ListenerManager<JenkinsEventListener> jenkinsEventListeners;
	private final ListenerManager<CrawlerEventListener> crawlerEventListeners;

	private final URI instance;
	private final TaskScheduler scheduler;
	private final CrawlingController controller;
	private final FileBasedStorage storage;
	private final ModelMappingService mappingService;

	private final CrawlerInformationPoint cip;

	private final OperationStrategy operationStrategy;
	private final CrawlingStrategy crawlingStrategy;

	private JenkinsCrawler(URI instance, FileBasedStorage storage, ModelMappingService mappingService, OperationStrategy operationStrategy, CrawlingStrategy crawlingStrategy, int numberOfWorkers) {
		this.operationStrategy = operationStrategy;
		this.crawlingStrategy = crawlingStrategy;
		this.instance=instance;
		this.storage=storage;
		this.mappingService=mappingService;
		this.cip=new CrawlerInformationPoint();
		this.jenkinsEventListeners=ListenerManager.newInstance();
		this.crawlerEventListeners=ListenerManager.newInstance();
		this.jenkinsEventListeners.registerListener(this.cip);
		this.crawlerEventListeners.registerListener(this.cip);
		this.scheduler=
			MultiThreadedTaskScheduler.
				builder().
					withNumberOfThreads(numberOfWorkers).
					withContext(new LocalContext()).
					build();
		this.controller=
			CrawlingController.
				builder().
					withStorage(this.storage).
					withCrawlingEventDispatcher(
						new DefaultCrawlerEventManager(
							this.crawlerEventListeners)).
					withJenkinsInstance(this.instance).
					withTaskScheduler(this.scheduler).
					withCrawlerInformationPoint(this.cip).
					withOperationDecissionPoint(this.operationStrategy.decissionPoint()).
					build();
	}

	public URI instance() {
		return this.instance;
	}

	public File workingDirectory() {
		return this.storage.workingDirectory();
	}

	public void start() {
		LOGGER.info("Starting Jenkins Crawler ({})...",this.instance);
		this.scheduler.start();
		this.controller.startAsync();
		LOGGER.info("Jenkins Crawler ({}) started.",this.instance);
	}

	public void awaitCompletion() {
		this.controller.awaitTerminated();
	}

	public void stop() throws IOException {
		LOGGER.info("Stopping Jenkins Crawler ({})...",this.instance);
		this.controller.stopAsync();
		this.controller.awaitTerminated();
		this.scheduler.stop();
		this.storage.save();
		LOGGER.info("Jenkins Crawler ({}) stopped.",this.instance);
	}

	public JenkinsCrawler registerListener(JenkinsEventListener listener) {
		this.jenkinsEventListeners.registerListener(listener);
		return this;
	}

	public JenkinsCrawler registerListener(CrawlerEventListener listener) {
		this.crawlerEventListeners.registerListener(listener);
		return this;
	}

	public JenkinsCrawler deregisterListener(JenkinsEventListener listener) {
		this.jenkinsEventListeners.deregisterListener(listener);
		return this;
	}

	public JenkinsCrawler deregisterListener(CrawlerEventListener listener) {
		this.crawlerEventListeners.deregisterListener(listener);
		return this;
	}

	public static Builder builder() {
		return new Builder();
	}

}