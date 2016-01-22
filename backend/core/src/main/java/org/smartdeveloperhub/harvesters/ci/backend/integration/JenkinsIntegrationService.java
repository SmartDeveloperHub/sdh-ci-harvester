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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentService;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ResolverService;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.backend.transaction.TransactionManager;
import org.smartdeveloperhub.jenkins.crawler.CrawlingStrategy;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawler;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawlerException;
import org.smartdeveloperhub.jenkins.crawler.OperationStrategy;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerEventListener;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerStartedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlerStoppedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingAbortedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingCompletedEvent;
import org.smartdeveloperhub.jenkins.crawler.event.CrawlingStartedEvent;
import org.smartdeveloperhub.jenkins.crawler.util.ListenerManager;

import com.google.common.util.concurrent.Service;

public final class JenkinsIntegrationService {

	private interface ServiceState {

		void connectTo(URI instance) throws IOException;
		void disconnect() throws IOException;
		void setWorkingDirectory(File directory);
		void setCrawlingStrategy(CrawlingStrategy strategy);
		void setOperationStrategy(OperationStrategy strategy);
		void setExecutionResolver(ResolverService resolver);

		boolean isConnected();

		File workingDirectory();
		URI connectedTo();
		void awaitCrawlingCompletion();

	}

	private final class ServiceConnected implements ServiceState {

		private final File workingDirectory;

		private ServiceConnected(final File workingDirectory) {
			this.workingDirectory = workingDirectory;
		}

		@Override
		public void setWorkingDirectory(final File directory) {
			throw new IllegalStateException("Cannot change working directory while connected");
		}

		@Override
		public void connectTo(final URI instance) {
			throw new IllegalStateException("Already connected");
		}

		@Override
		public void setCrawlingStrategy(final CrawlingStrategy strategy) {
			throw new IllegalStateException("Cannot change crawling strategy while connected");
		}

		@Override
		public void setOperationStrategy(final OperationStrategy strategy) {
			throw new IllegalStateException("Cannot change operation strategy while connected");
		}

		@Override
		public void setExecutionResolver(final ResolverService resolver) {
			throw new IllegalStateException("Cannot execution resolver directory while connected");
		}

		@Override
		public void disconnect() throws IOException {
			try {
				doDisconnect();
			} catch (final Exception e) {
				JenkinsIntegrationService.this.state=new ServiceDisconnected(this.workingDirectory);
				throw e;
			}
		}

		@Override
		public File workingDirectory() {
			return JenkinsIntegrationService.this.crawler.workingDirectory();
		}

		@Override
		public URI connectedTo() {
			return JenkinsIntegrationService.this.crawler.instance();
		}

		@Override
		public boolean isConnected() {
			return true;
		}

		@Override
		public void awaitCrawlingCompletion() {
			JenkinsIntegrationService.this.crawler.awaitCompletion();
		}

	}

	private final class ServiceDisconnected implements ServiceState {

		private File workingDirectory;
		private CrawlingStrategy crawlingStrategy;
		private OperationStrategy operationStrategy;
		private ResolverService executionResolver;

		private ServiceDisconnected() {
		}

		private ServiceDisconnected(final File workingDirectory) {
			this.workingDirectory = workingDirectory;
		}

		@Override
		public void connectTo(final URI instance) throws IOException {
			doConnect(instance,this.workingDirectory,this.crawlingStrategy,this.operationStrategy,this.executionResolver);
			JenkinsIntegrationService.this.state=new ServiceConnected(this.workingDirectory);
		}

		@Override
		public void disconnect() {
			// Nothing to do
		}

		@Override
		public File workingDirectory() {
			return this.workingDirectory;
		}

		@Override
		public URI connectedTo() {
			return null;
		}

		@Override
		public boolean isConnected() {
			return false;
		}

		@Override
		public void setWorkingDirectory(final File directory) {
			this.workingDirectory = directory;
		}

		@Override
		public void setCrawlingStrategy(final CrawlingStrategy strategy) {
			this.crawlingStrategy=strategy;
		}

		@Override
		public void setOperationStrategy(final OperationStrategy strategy) {
			this.operationStrategy=strategy;
		}

		@Override
		public void setExecutionResolver(final ResolverService resolver) {
			this.executionResolver=resolver;
		}

		@Override
		public void awaitCrawlingCompletion() {
			throw new IllegalStateException("Not connected");
		}

	}

	private final class CrawlerLifecycleLogger implements CrawlerEventListener {
		@Override
		public void onCrawlerStartUp(final CrawlerStartedEvent event) {
			LOGGER.info("Crawler started");
		}

		@Override
		public void onCrawlerShutdown(final CrawlerStoppedEvent event) {
			LOGGER.info("Crawler completed");
		}

		@Override
		public void onCrawlingStartUp(final CrawlingStartedEvent event) {
			LOGGER.info("Started crawling ({})",event.sessionId());
		}

		@Override
		public void onCrawlingCompletion(final CrawlingCompletedEvent event) {
			LOGGER.info("Finished crawling ({})",event.sessionId());
		}

		@Override
		public void onCrawlingAbortion(final CrawlingAbortedEvent event) {
			LOGGER.info("Aborted crawling ({})",event.sessionId());
		}
	}

	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsIntegrationService.class);

	private final ContinuousIntegrationService ciService;
	private final TransactionManager transactionManager;
	private final CommandProducerListener listener;
	private final CommandProcessingMonitor monitor;

	private final Lock read;
	private final Lock write;

	private final ListenerManager<EntityLifecycleEventListener> listeners;

	private JenkinsCrawler crawler;
	private Service worker;
	private ServiceState state;

	private final EnrichmentService erService;

	public JenkinsIntegrationService(final ContinuousIntegrationService ciService, final EnrichmentService erService, final TransactionManager manager) {
		this.ciService=ciService;
		this.erService=erService;
		this.transactionManager = manager;
		this.monitor=new CommandProcessingMonitor();
		this.listener=new CommandProducerListener(this.monitor,false);
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.state=new ServiceDisconnected();
		this.listeners=ListenerManager.newInstance();
	}

	private void doConnect(
			final URI jenkinsInstance,
			final File workingDirectory,
			final CrawlingStrategy crawlingStrategy,
			final OperationStrategy operationStrategy,
			final ResolverService resolver) throws IOException {
		this.erService.withResolverService(resolver).connect();
		try {
			LOGGER.info("Connecting to {}...",jenkinsInstance);
			this.monitor.startAsync();
			this.worker=new SimpleCommandProcessorService(this.monitor,this.transactionManager,this.ciService,this.erService,this.listeners);
			this.worker.startAsync();
			this.crawler =
				JenkinsCrawler.
					builder().
						withLocation(jenkinsInstance.toString()).
						withDirectory(workingDirectory).
						withCrawlingStrategy(crawlingStrategy).
						withOperationStrategy(operationStrategy).
						build();
			this.crawler.registerListener(this.listener);
			this.crawler.registerListener(new CrawlerLifecycleLogger());
			this.crawler.start();
			LOGGER.info("Connected to {}.",jenkinsInstance);
		} catch (final JenkinsCrawlerException e) {
			this.worker.stopAsync();
			this.worker.awaitTerminated();
			this.worker=null;
			this.monitor.stopAsync();
			this.monitor.awaitTerminated();
			disconnectQuietly(this.erService);
			LOGGER.error("Could not connect to {}. Full stacktrace follows:",jenkinsInstance,e);
			throw new IOException("Could not connect to "+jenkinsInstance,e);
		}
	}

	private void disconnectQuietly(final EnrichmentService service) {
		try {
			service.disconnect();
		} catch (final IOException ignore) {
			LOGGER.warn("Could not disconnect enrichment service. Full stacktrace follows:",ignore);
		}
	}

	private void doDisconnect() throws IOException {
		try {
			this.crawler.stop();
			LOGGER.info("Disconnected from {}",this.crawler.instance());
		} finally {
			this.crawler.deregisterListener(this.listener);
			this.crawler=null;
			this.worker.stopAsync();
			this.worker.awaitTerminated();
			this.monitor.stopAsync();
			this.monitor.awaitTerminated();
			this.worker=null;
			disconnectQuietly(this.erService);
		}
	}

	public JenkinsIntegrationService setCrawlingStrategy(final CrawlingStrategy strategy) {
		this.write.lock();
		try {
			this.state.setCrawlingStrategy(strategy);
			return this;
		} finally {
			this.write.unlock();
		}
	}

	public JenkinsIntegrationService setOperationStrategy(final OperationStrategy strategy) {
		this.write.lock();
		try {
			this.state.setOperationStrategy(strategy);
			return this;
		} finally {
			this.write.unlock();
		}
	}

	public JenkinsIntegrationService setWorkingDirectory(final File directory) {
		this.write.lock();
		try {
			this.state.setWorkingDirectory(directory);
			return this;
		} finally {
			this.write.unlock();
		}
	}

	public JenkinsIntegrationService setResolverService(final ResolverService resolver) {
		this.write.lock();
		try {
			this.state.setExecutionResolver(resolver);
			return this;
		} finally {
			this.write.unlock();
		}
	}

	public File workingDirectory() {
		this.read.lock();
		try {
			return this.state.workingDirectory();
		} finally {
			this.read.unlock();
		}
	}

	public boolean isConnected() {
		this.read.lock();
		try {
			return this.state.isConnected();
		} finally {
			this.read.unlock();
		}
	}

	public void awaitCrawlingCompletion() {
		this.state.awaitCrawlingCompletion();
	}

	public void connect(final URI jenkinsInstance) throws IOException {
		this.write.lock();
		try {
			this.state.connectTo(jenkinsInstance);
		} finally {
			this.write.unlock();
		}
	}

	public URI connectedTo() {
		this.read.lock();
		try {
			return this.state.connectedTo();
		} finally {
			this.read.unlock();
		}
	}

	public void disconnect() throws IOException {
		this.write.lock();
		try {
			this.state.disconnect();
		} finally {
			this.write.unlock();
		}
	}

	public JenkinsIntegrationService registerListener(final EntityLifecycleEventListener listener) {
		this.erService.registerListener(listener);
		this.listeners.registerListener(listener);
		return this;
	}

	public JenkinsIntegrationService deregisterListener(final EntityLifecycleEventListener listener) {
		this.erService.deregisterListener(listener);
		this.listeners.deregisterListener(listener);
		return this;
	}

}