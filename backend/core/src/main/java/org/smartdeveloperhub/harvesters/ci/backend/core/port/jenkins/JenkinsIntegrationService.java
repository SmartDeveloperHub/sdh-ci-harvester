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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;

import org.smartdeveloperhub.harvesters.ci.backend.core.ContinuousIntegrationService;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.Command;
import org.smartdeveloperhub.harvesters.ci.backend.core.transaction.TransactionManager;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawler;
import org.smartdeveloperhub.jenkins.crawler.JenkinsCrawlerException;

import com.google.common.collect.Queues;

public final class JenkinsIntegrationService {

	private final ContinuousIntegrationService service;
	private final TransactionManager transactionManager;
	private final LinkedBlockingQueue<Command> commandQueue;
	private final CommandProducerListener listener;

	private JenkinsCrawler crawler;
	private CommandProcessorService worker;

	public JenkinsIntegrationService(ContinuousIntegrationService service, TransactionManager manager) {
		this.service=service;
		this.transactionManager = manager;
		this.commandQueue=Queues.newLinkedBlockingQueue();
		this.listener=new CommandProducerListener(this.commandQueue);
	}

	public synchronized void connect(URI jenkinsInstance) throws IOException {
		checkState(this.crawler==null,"Already connected");
		try {
			this.worker=new CommandProcessorService(this.commandQueue,this.transactionManager,this.service);
			this.worker.startAsync();
			this.crawler =
				JenkinsCrawler.
					builder().
						withLocation(jenkinsInstance.toString()).
						build();
			this.crawler.registerListener(this.listener);
			this.crawler.start();
		} catch (JenkinsCrawlerException e) {
			this.worker.stopAsync();
			this.worker.awaitTerminated();
			this.worker=null;
			throw new IOException("Cannot create crawler",e);
		}
	}

	public URI connectedTo() {
		return this.crawler.instance();
	}

	public synchronized boolean isConnected() {
		return this.crawler!=null;
	}

	public synchronized void disconnect() throws IOException {
		checkState(this.crawler!=null,"Not connected");
		try {
			this.crawler.stop();
			this.crawler.deregisterListener(this.listener);
			this.crawler=null;
		} finally {
			this.worker.triggerShutdown();
			this.worker.stopAsync();
			this.worker.awaitTerminated();
			this.worker=null;
		}
	}

}