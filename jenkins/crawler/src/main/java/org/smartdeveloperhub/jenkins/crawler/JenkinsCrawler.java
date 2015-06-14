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

import java.io.File;
import java.io.IOException;
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
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.Status;
import org.smartdeveloperhub.jenkins.client.JenkinsClientException;
import org.smartdeveloperhub.jenkins.client.JenkinsResourceProxy;
import org.smartdeveloperhub.jenkins.crawler.application.ModelMappingService;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.transformation.TransformationManager;
import org.smartdeveloperhub.jenkins.crawler.util.Consoles;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeBuild;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Service;
import org.smartdeveloperhub.jenkins.util.xml.XmlUtils;

import static com.google.common.base.Preconditions.*;

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

	private static abstract class AbstractCrawlingTask implements Task {

		private static final int RETRY_THRESHOLD = 5;

		private URI location;
		private JenkinsEntityType entity;
		private JenkinsArtifactType artifact;
		private Context context;
		private int retries;

		private AbstractCrawlingTask(URI location, JenkinsEntityType entity, JenkinsArtifactType artifact) {
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
				if(LOGGER.isInfoEnabled()) {
					LOGGER.info("Retrying {} ({})",this.location,this.retries+1);
				}
				scheduleTask(this);
			} else {
				failSilently(failure,"Could not retrieve '%s' aftet %d intents",this.location,this.retries);
			}
		}

		private void failSilently(Throwable failure, String errorMessage, Object... args) {
			String log=String.format(errorMessage,args);
			LOGGER.error(log,failure);
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

		protected final URI location() {
			return this.location;
		}

		protected final JenkinsEntityType entityType() {
			return this.entity;
		}

		protected final void scheduleTask(Task task) {
			checkState(context!=null);
			this.context.scheduler().schedule(task);
		}

		protected final void persistEntity(Entity entity, JenkinsEntityType type) throws IOException {
			checkState(context!=null);
			this.context.entityRepository().saveEntity(entity,type);
		}

		protected final ModelMappingService modelMapper() {
			checkState(context!=null);
			return this.context.modelMapper();
		}

		protected abstract String taskPrefix();

		protected abstract void processResource(JenkinsResource resource) throws IOException;

	}

	private static abstract class AbstractCrawlingSubTask<T extends Entity> extends AbstractCrawlingTask {

		private T parent;

		private AbstractCrawlingSubTask(URI location, JenkinsEntityType entity, JenkinsArtifactType artifact, T parent) {
			super(location,entity, artifact);
			this.parent = parent;
		}

		@Override
		protected final void processResource(JenkinsResource resource) throws IOException {
			processSubresource(this.parent,resource);
		}

		protected void processSubresource(T parent, JenkinsResource resource) throws IOException {
			// To be extended by subclasses if necessary
		}

	}

	private static class LoadServiceTask extends AbstractCrawlingTask {

		private LoadServiceTask(URI location) {
			super(location,JenkinsEntityType.SERVICE,JenkinsArtifactType.RESOURCE);
		}

		@Override
		protected String taskPrefix() {
			return "lst";
		}

		@Override
		protected void processResource(JenkinsResource resource) throws IOException {
			Service service = modelMapper().loadService(resource);
			persistEntity(service,resource.entity());
			for(Reference ref:service.getBuilds().getBuilds()) {
				scheduleTask(new LoadProjectTask(ref.getValue()));
			}
		}

	}

	private static class LoadProjectTask extends AbstractCrawlingTask {

		private LoadProjectTask(URI location) {
			super(location,JenkinsEntityType.JOB,JenkinsArtifactType.RESOURCE);
		}

		@Override
		protected String taskPrefix() {
			return "lpt";
		}

		@Override
		protected void processResource(JenkinsResource resource) throws IOException {
			Build build = modelMapper().loadBuild(resource);
			persistEntity(build,resource.entity());
			scheduleTask(new LoadProjectConfigurationTask(super.location(),build,resource.entity()));
			scheduleTask(new LoadProjectSCMTask(super.location(),build));
			if(build instanceof CompositeBuild) {
				CompositeBuild cBuild=(CompositeBuild)build;
				for(Reference ref:cBuild.getSubBuilds().getBuilds()) {
					scheduleTask(new LoadProjectTask(ref.getValue()));
				}
			}
			boolean first=true;
			for(Reference ref:build.getRuns().getRuns()) {
				if(first) {
					first=false;
					scheduleTask(new LoadRunTask(ref.getValue()));
				}
			}
		}

	}

	private static class LoadProjectSCMTask extends AbstractCrawlingSubTask<Build> {

		private LoadProjectSCMTask(URI location, Build build) {
			super(location,JenkinsEntityType.JOB,JenkinsArtifactType.SCM,build);
		}

		@Override
		protected String taskPrefix() {
			return "lpst";
		}

	}

	private static class LoadProjectConfigurationTask extends AbstractCrawlingSubTask<Build> {

		private LoadProjectConfigurationTask(URI location, Build build, JenkinsEntityType type) {
			super(location,type,JenkinsArtifactType.CONFIGURATION,build);
		}

		@Override
		protected String taskPrefix() {
			return "lpct";
		}

		@Override
		protected void processSubresource(Build parent, JenkinsResource resource) throws IOException {
			try {
				String rawURI=
					XmlUtils.
						evaluateXPath(
							"//scm[@class='hudson.plugins.git.GitSCM']/userRemoteConfigs//url",
							resource.content().get());
				parent.withCodebase(URI.create(rawURI));
				persistEntity(parent, entityType());
			} catch (Exception e) {
				LOGGER.error("Could not recover SCM information",e);
			}
		}

	}

	private static class LoadRunTask extends AbstractCrawlingTask {

		private LoadRunTask(URI location) {
			super(location,JenkinsEntityType.RUN,JenkinsArtifactType.RESOURCE);
		}

		@Override
		protected String taskPrefix() {
			return "lrt";
		}

		@Override
		protected void processResource(JenkinsResource resource) throws IOException {
			Run run = modelMapper().loadRun(resource);
			persistEntity(run,resource.entity());
			if(JenkinsEntityType.MAVEN_RUN.isCompatible(resource.entity()) && run.getResult().equals(RunResult.SUCCESS)) {
				scheduleTask(new LoadRunArtifactsTask(super.location(),run));
			}
		}

	}

	private static class LoadRunArtifactsTask extends AbstractCrawlingSubTask<Run> {

		private LoadRunArtifactsTask(URI location, Run run) {
			super(location,JenkinsEntityType.RUN,JenkinsArtifactType.ARTIFACTS,run);
		}

		@Override
		protected String taskPrefix() {
			return "lrat";
		}

	}

	public static class JenkinsCrawlerBuilder {

		private String location;
		private File directory;

		private JenkinsCrawlerBuilder() {
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

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsCrawler.class);

	private final ScheduledExecutorService adminPool;

	private final MultiThreadedTaskScheduler taskScheduler;

	private String location;
	private FileBasedStorage storageManager;

	private JenkinsCrawler(String location, FileBasedStorage storage, ModelMappingService mappingService) {
		setLocation(location);
		setFileBasedStorage(storage);
		this.adminPool =createAdminPool();
		this.taskScheduler=createTaskScheduler(storage, mappingService);
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

	private static MultiThreadedTaskScheduler createTaskScheduler(FileBasedStorage storage, ModelMappingService mappingService) {
		return
			new MultiThreadedTaskScheduler(
				storage,
				storage,
				mappingService,
				Runtime.getRuntime().availableProcessors());
	}

	private static String durationToString(long started, long finished) {
		Duration duration=new Duration(started,finished);
		PeriodFormatter formatter =
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
		StringBuffer buffer = new StringBuffer();
		formatter.getPrinter().printTo(buffer, duration.toPeriod(), Locale.ENGLISH);
		String durationStr=buffer.toString();
		return durationStr;
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