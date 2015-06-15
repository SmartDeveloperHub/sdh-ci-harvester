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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.ResourceRepository;
import org.smartdeveloperhub.jenkins.crawler.application.ModelMappingService;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEvent;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventDispatcher;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventListener;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.transformation.TransformationManager;
import org.smartdeveloperhub.jenkins.crawler.util.Consoles;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.EntityRepository;

public final class JenkinsCrawler {

	public static final class JenkinsCrawlerBuilder {

		private String location;
		private File directory;
		private JenkinsEventListener listener;

		private JenkinsCrawlerBuilder() {
		}

		public JenkinsCrawlerBuilder withListener(JenkinsEventListener listener) {
			this.listener = listener;
			return this;
		}

		public JenkinsCrawlerBuilder withLocation(String location) {
			this.location = location;
			return this;
		}

		public JenkinsCrawlerBuilder withDirectory(File directory) {
			this.directory = directory;
			return this;
		}

		public JenkinsCrawler build() throws JenkinsCrawlerException {
			checkNotNull(this.location,"Service instance cannot be null");
			try {
				return
					new JenkinsCrawler(
						URI.create(this.location),
						this.listener==null?
							NullEventListener.getInstance():
							this.listener,
						createFileBasedStorage(),
						ModelMappingService.
							newInstance(TransformationManager.newInstance()));
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
							new File(this.directory,"repository.xml")).build();
		}

	}

	private final class LocalContext implements Context {

		private final JenkinsEventDispatcher dispatcher;

		private LocalContext() {
			this.dispatcher=JenkinsEventDispatcher.create(JenkinsCrawler.this.listener);
		}

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
			this.dispatcher.fireEvent(event);
		}

		@Override
		public void schedule(Task task) {
			JenkinsCrawler.this.taskScheduler.schedule(task);
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsCrawler.class);


	private URI instance;
	private TaskScheduler taskScheduler;
	private CrawlingController crawlingStrategy;
	private FileBasedStorage storage;
	private ModelMappingService mappingService;
	private JenkinsEventListener listener;

	private JenkinsCrawler(URI instance, JenkinsEventListener listener, FileBasedStorage storage, ModelMappingService mappingService) {
		this.listener=listener;
		this.instance=instance;
		this.storage=storage;
		this.mappingService=mappingService;
		this.taskScheduler=
			MultiThreadedTaskScheduler.
				builder().
					withContext(new LocalContext()).
					build();
		this.crawlingStrategy=
			CrawlingController.
				builder().
					withStorage(this.storage).
					withJenkinsInstance(this.instance).
					withTaskScheduler(this.taskScheduler).
					build();
	}

	public void start() {
		LOGGER.info("Starting Jenkins Crawler ({})...",this.instance);
		this.taskScheduler.start();
		this.crawlingStrategy.start();
		LOGGER.info("Jenkins Crawler ({}) started.",this.instance);
	}

	public void stop() throws IOException {
		LOGGER.info("Stopping Jenkins Crawler ({})...",this.instance);
		this.crawlingStrategy.stop();
		this.taskScheduler.stop();
		this.storage.save();
		LOGGER.info("Jenkins Crawler ({}) stopped.",this.instance);
	}

	public static JenkinsCrawlerBuilder builder() {
		return new JenkinsCrawlerBuilder();
	}

	public static void main(String[] args) {
		final File tmpDirectory = new File("target","jenkins"+new Date().getTime());
		tmpDirectory.deleteOnExit();
		String location="http://ci.jenkins-ci.org/";
		try {
			final JenkinsCrawler crawler=
				JenkinsCrawler.
					builder().
						withDirectory(tmpDirectory).
						withLocation(location).
						build();
			crawler.start();
			LOGGER.info("<<HIT ENTER TO STOP THE CRAWLER>>");
			Consoles.defaultConsole().readLine();
			crawler.stop();
			FileBasedStorage tmp=
					FileBasedStorage.
						builder().
							withConfigFile(new File(tmpDirectory,"repository.xml")).
							build();
			JenkinsResource resource=
				tmp.
					findResource(
						URI.create("http://ci.jenkins-ci.org/job/jenkins_rc_branch/423/"),
						JenkinsArtifactType.RESOURCE);
			if(resource!=null) {
				LOGGER.info(resource.toString());
			}
		} catch (JenkinsCrawlerException e) {
			LOGGER.error("Could not create crawler",e);
		} catch (IOException e) {
			LOGGER.error("Unexpected failure",e);
		}
	}

}