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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.core.port.jenkins;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionManager;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawler;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawlerException;

public final class JenkinsIntegrationService {

	private interface ServiceState {

		void setWorkingDirectory(File directory);
		void connectTo(URI instance) throws IOException;
		void disconnect() throws IOException;

		boolean isConnected();

		File workingDirectory();
		URI connectedTo();

	}

	private final class ServiceConnected implements ServiceState {

		private final File workingDirectory;

		private ServiceConnected(File workingDirectory) {
			this.workingDirectory = workingDirectory;
		}

		@Override
		public void setWorkingDirectory(File directory) {
			throw new IllegalStateException("Cannot change working directory while connected");
		}

		@Override
		public void connectTo(URI instance) {
			throw new IllegalStateException("Cannot change working directory while connected");
		}

		@Override
		public void disconnect() throws IOException {
			try {
				doDisconnect();
			} catch (Exception e) {
				state=new ServiceDisconnected(this.workingDirectory);
				throw e;
			}
		}

		@Override
		public File workingDirectory() {
			return crawler.workingDirectory();
		}

		@Override
		public URI connectedTo() {
			return crawler.instance();
		}

		@Override
		public boolean isConnected() {
			return true;
		}
	}

	private final class ServiceDisconnected implements ServiceState {

		private File workingDirectory;

		private ServiceDisconnected() {
		}

		private ServiceDisconnected(File workingDirectory) {
			this.workingDirectory = workingDirectory;
		}

		@Override
		public void setWorkingDirectory(File directory) {
			this.workingDirectory = directory;
		}

		@Override
		public void connectTo(URI instance) throws IOException {
			doConnect(instance, this.workingDirectory);
			state=new ServiceConnected(this.workingDirectory);
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

	}


	private static final Logger LOGGER=LoggerFactory.getLogger(JenkinsIntegrationService.class);

	private final ContinuousIntegrationService service;
	private final TransactionManager transactionManager;
	private final CommandProducerListener listener;
	private final CommandProcessingMonitor monitor;

	private final Lock read;
	private final Lock write;

	private JenkinsCrawler crawler;
	private CommandProcessorService worker;
	private ServiceState state;

	public JenkinsIntegrationService(ContinuousIntegrationService service, TransactionManager manager) {
		this.service=service;
		this.transactionManager = manager;
		this.monitor=new CommandProcessingMonitor();
		this.listener=new CommandProducerListener(this.monitor);
		ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
		this.state=new ServiceDisconnected();
	}

	private void doConnect(URI jenkinsInstance, File workingDirectory) throws IOException {
		try {
			this.monitor.startAsync();
			this.worker=new CommandProcessorService(this.monitor,this.transactionManager,this.service);
			this.worker.startAsync();
			this.crawler =
				JenkinsCrawler.
					builder().
						withLocation(jenkinsInstance.toString()).
						withDirectory(workingDirectory).
						build();
			this.crawler.registerListener(this.listener);
			LOGGER.info("Connecting to {}...",jenkinsInstance);
			this.crawler.start();
			LOGGER.info("Connected to {}.",jenkinsInstance);
		} catch (JenkinsCrawlerException e) {
			this.worker.stopAsync();
			this.worker.awaitTerminated();
			this.worker=null;
			this.monitor.stopAsync();
			this.monitor.awaitTerminated();
			LOGGER.error("Could not create crawler. Full stacktrace follows:",e);
			throw new IOException("Cannot create crawler",e);
		}
	}

	private void doDisconnect() throws IOException {
		try {
			this.crawler.stop();
			LOGGER.info("Disconnected from {}",this.crawler.instance());
		} finally {
			this.crawler.deregisterListener(listener);
			this.crawler=null;
			this.worker.stopAsync();
			this.worker.awaitTerminated();
			this.monitor.stopAsync();
			this.monitor.awaitTerminated();
			this.worker=null;
		}
	}

	public JenkinsIntegrationService setWorkingDirectory(File directory) {
		this.write.lock();
		try {
			this.state.setWorkingDirectory(directory);
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

	public void connect(URI jenkinsInstance) throws IOException {
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

}