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
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class JenkinsCrawler {

	private final class JenkinsCrawlerBootstrap implements Runnable {

		@Override
		public void run() {
			LOGGER.info("Starting crawling...");
			long started=System.currentTimeMillis();
			taskScheduler().schedule(new LoadServiceTask(URI.create(location)));
			taskScheduler().awaitTaskCompletion(1,TimeUnit.SECONDS);
			long finished=System.currentTimeMillis();
			LOGGER.info("Crawling completed. Execution took {}",durationToString(started, finished));
			try {
				JenkinsCrawler.this.storageManager.save();
			} catch (IOException e) {
				LOGGER.warn("Could not persist crawler state",e);
			}
		}

		private MultiThreadedTaskScheduler taskScheduler() {
			return JenkinsCrawler.this.taskScheduler;
		}

	}

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
			try {
				return
					new JenkinsCrawler(
						this.location,
						this.listener==null?
							NullEventListener.create():
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
			return JenkinsCrawler.this.storageManager;
		}

		@Override
		public ResourceRepository resourceRepository() {
			return JenkinsCrawler.this.storageManager;
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

	private static final PeriodFormatter PERIOD_FORMATTER =
		new PeriodFormatterBuilder().
				appendYears().
				appendSuffix(" year", " years").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMonths().
				appendSuffix(" month", " months").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendDays().
				appendSuffix(" day", " days").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendHours().
				appendSuffix(" hour", " hours").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMinutes().
				appendSuffix(" minute", " minutes").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendSeconds().
				appendSuffix(" second", " seconds").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMillis().
				appendSuffix(" millisecond", " milliseconds").
				appendSeparator(" and ").
				printZeroRarelyLast().
				toFormatter();

	private final ScheduledExecutorService adminPool;
	private final MultiThreadedTaskScheduler taskScheduler;

	private String location;
	private FileBasedStorage storageManager;

	private ModelMappingService mappingService;

	private URI instance;

	private JenkinsEventListener listener;


	private JenkinsCrawler(String location, JenkinsEventListener listener, FileBasedStorage storage, ModelMappingService mappingService) {
		setListener(listener);
		setLocation(location);
		setInstance(URI.create(location));
		setFileBasedStorage(storage);
		setMappingService(mappingService);
		this.adminPool=createAdminPool();
		this.taskScheduler=
			new MultiThreadedTaskScheduler(
				new LocalContext(),
				Runtime.getRuntime().availableProcessors());
	}

	private void setListener(JenkinsEventListener listener) {
		this.listener = listener;
	}

	private void setMappingService(ModelMappingService mappingService) {
		this.mappingService=mappingService;
	}

	private void setInstance(URI instance) {
		checkNotNull(instance,"Service instance cannot be null");
		this.instance=instance;
	}

	private void setLocation(String location) {
		checkNotNull(location,"Service location cannot be null");
		this.location = location;
	}

	private void setFileBasedStorage(FileBasedStorage storage) {
		checkNotNull(storage,"Storage cannot be null");
		this.storageManager = storage;
	}

	public void start() {
		LOGGER.info("Starting crawler...");
		this.taskScheduler.start();
		this.adminPool.
			scheduleWithFixedDelay(
				new JenkinsCrawlerBootstrap(),
				1,
				60,
				TimeUnit.SECONDS);
		LOGGER.info("Crawler started.");
	}

	public void stop() throws IOException {
		LOGGER.info("Stopping crawler...");
		this.taskScheduler.stop();
		this.adminPool.shutdown();
		while(!this.adminPool.isTerminated()) {
			try {
				this.adminPool.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		LOGGER.info("Crawler stopped.");
	}

	private static ScheduledExecutorService createAdminPool() {
		return
			Executors.
				newScheduledThreadPool(
					1,
					new ThreadFactoryBuilder().
						setNameFormat("JenkinsCrawler-admin-%d").
						setUncaughtExceptionHandler(
							new UncaughtExceptionHandler() {
								@Override
								public void uncaughtException(Thread t, Throwable e) {
									LOGGER.error(String.format("Thread %s died",t.getName()),e);
								}
							}).
							build()
					);
	}

	private static String durationToString(long started, long finished) {
		StringWriter writer = new StringWriter();
		try {
			PERIOD_FORMATTER.
				getPrinter().
					printTo(
						writer,
						new Duration(started,finished).toPeriod(),
						Locale.ENGLISH);
			return writer.toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
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