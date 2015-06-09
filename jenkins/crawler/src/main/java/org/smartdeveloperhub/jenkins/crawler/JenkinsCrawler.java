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
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.ResourceRepository;
import org.smartdeveloperhub.jenkins.Status;
import org.smartdeveloperhub.jenkins.client.JenkinsClientException;
import org.smartdeveloperhub.jenkins.client.JenkinsResourceProxy;
import org.smartdeveloperhub.jenkins.crawler.application.ModelMappingService;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence.FileBasedStorage;
import org.smartdeveloperhub.jenkins.crawler.infrastructure.transformation.TransformationManager;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeBuild;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.EntityRepository;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Service;
import org.smartdeveloperhub.jenkins.util.xml.XmlUtils;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class JenkinsCrawler {

	interface Task {

		String id();

		void execute(Context context);

	}

	interface Context {

		ModelMappingService modelMapper();

		EntityRepository entityRepository();

		ResourceRepository resourceRepository();

		TaskScheduler scheduler();

	}

	interface TaskScheduler {

		void schedule(Task task);

		void start(Task first);

	}

	static final class SingleThreadedTaskScheduler implements TaskScheduler {

		private final class LocalContext implements Context {

			@Override
			public EntityRepository entityRepository() {
				return entityManager;
			}

			@Override
			public ModelMappingService modelMapper() {
				return modelMapper;
			}

			@Override
			public TaskScheduler scheduler() {
				return SingleThreadedTaskScheduler.this;
			}

			@Override
			public ResourceRepository resourceRepository() {
				return resourceRepository;
			}

		}

		private final EntityRepository entityManager;
		private final ModelMappingService modelMapper;
		private final Queue<Task> tasks;
		private final LocalContext context;
		private ResourceRepository resourceRepository;

		private SingleThreadedTaskScheduler(EntityRepository entityManager, ResourceRepository resourceRepository, ModelMappingService manager) {
			this.entityManager = entityManager;
			this.resourceRepository = resourceRepository;
			this.modelMapper = manager;
			this.tasks=Lists.newLinkedList();
			this.context = new LocalContext();
		}

		@Override
		public void schedule(Task task) {
			LOGGER.debug("Scheduled: "+task.id());
			this.tasks.offer(task);
		}

		@Override
		public void start(Task first) {
			this.tasks.add(first);
			while(!this.tasks.isEmpty()) {
				Task task = tasks.poll();
				LOGGER.debug("Dispatched: "+task.id());
				try {
					task.execute(context);
				} catch (Exception e) {
					LOGGER.error(String.format("Task %s died",task.id()),e);
				}
			}
		}

	}

	static final class MultiThreadedTaskScheduler implements TaskScheduler {

		private final class LocalContext implements Context {
			@Override
			public ModelMappingService modelMapper() {
				return modelMapper;
			}

			@Override
			public EntityRepository entityRepository() {
				return entityRepository;
			}

			@Override
			public TaskScheduler scheduler() {
				return MultiThreadedTaskScheduler.this;
			}

			@Override
			public ResourceRepository resourceRepository() {
				return resourceRepository;
			}
		}

		private final EntityRepository entityRepository;
		private final ModelMappingService modelMapper;
		private final LocalContext context;
		private final AtomicLong taskCounter=new AtomicLong();

		private final ExecutorService pool;
		private final ResourceRepository resourceRepository;

		private MultiThreadedTaskScheduler(EntityRepository entityRepository, ResourceRepository resourceRepository, ModelMappingService modelMapper, int threads) {
			this.entityRepository = entityRepository;
			this.resourceRepository = resourceRepository;
			this.modelMapper = modelMapper;
			this.context = new LocalContext();
			this.pool =
				Executors.
					newFixedThreadPool(
						threads,
						new ThreadFactoryBuilder().
							setNameFormat("JenkinsCrawler-thread-%d").
							setUncaughtExceptionHandler(
								new UncaughtExceptionHandler() {
									@Override
									public void uncaughtException(Thread t, Throwable e) {
										LOGGER.error(String.format("Thread %s died",t.getName()),e);
										taskCounter.decrementAndGet();
									}
								}).
							build());
		}

		@Override
		public void schedule(final Task task) {
			LOGGER.debug("Scheduled [{}]",task.id());
			taskCounter.incrementAndGet();
			pool.execute(new Runnable() {
				@Override
				public void run() {
					LOGGER.debug("Started   [{}]",task.id());
					task.execute(context);
					taskCounter.decrementAndGet();
					LOGGER.debug("Completed [{}]",task.id());
				}
			});
		}

		public void start(Task first) {
			schedule(first);
			while(taskCounter.get()>0) {
				LOGGER.info("Awaiting for "+taskCounter.get()+" tasks...");
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
				}
			}
			LOGGER.info("Finished.");
			this.pool.shutdown();
			while(!this.pool.isTerminated()) {
				try {
					this.pool.awaitTermination(1,TimeUnit.SECONDS);
				} catch (InterruptedException e) {
				}
			}
		}

	}

	private static abstract class AbstractCrawlingTask implements Task {

		private static final int RETRY_THREASHOLD = 5;

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
			if(this.retries<RETRY_THREASHOLD) {
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
				URI location=ref.getValue();
				scheduleTask(new LoadProjectTask(location));
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
			scheduleTask(new LoadProjectConfigurationTask(location(),build,resource.entity()));
			scheduleTask(new LoadProjectSCMTask(location(),build));
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
				scheduleTask(new LoadRunArtifactsTask(location(),run));
			}
		}

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

	private static class LoadProjectSCMTask extends AbstractCrawlingSubTask<Build> {

		private LoadProjectSCMTask(URI location, Build build) {
			super(location,JenkinsEntityType.JOB,JenkinsArtifactType.SCM,build);
		}

		@Override
		protected String taskPrefix() {
			return "lpst";
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

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsCrawler.class);

	public static void main(String[] args) throws IOException {
		File tmpDirectory = new File("target","jenkins"+new Date().getTime());
		tmpDirectory.deleteOnExit();
		String location = "http://localhost:8080/";
		if(true) {
			location="http://ci.jenkins-ci.org/";
		}

		FileBasedStorage storageManager=
			FileBasedStorage.
				builder().
					withWorkingDirectory(tmpDirectory).
					withConfigFile(new File(tmpDirectory,"repository.xml")).
					build();

		ModelMappingService mappingService =
			ModelMappingService.
				newInstance(
					TransformationManager.
						newInstance());

		TaskScheduler scheduler=
			new MultiThreadedTaskScheduler(
				storageManager,
				storageManager,
				mappingService,
				Runtime.
					getRuntime().
						availableProcessors());
//		TaskScheduler scheduler=
//			new SingleThreadedTaskScheduler(
//				storageManager,
//				storageManager,
//				mappingService);

		long started = System.currentTimeMillis();
		scheduler.start(new LoadServiceTask(URI.create(location)));
		long finished=System.currentTimeMillis();
		LOGGER.info("Execution took {}",durationToString(started, finished));
		storageManager.save();

		FileBasedStorage tmp=
			FileBasedStorage.
				builder().
					withConfigFile(storageManager.configFile()).
					build();

		JenkinsResource resource=
			tmp.
				findResource(
					URI.create("http://ci.jenkins-ci.org/job/jenkins_rc_branch/423/"),
					JenkinsArtifactType.RESOURCE);
		LOGGER.info(resource.toString());
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

}