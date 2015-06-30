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
package org.smartdeveloperhub.harvesters.ci.backend.integration;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.command.Command;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class CommandProcessingMonitor extends AbstractExecutionThreadService {

	static final class Status {

		private final AtomicLong offered;
		private final AtomicLong taken;
		private final AtomicLong retries;

		private Status(long offered, long taken, long retries) {
			this.offered=new AtomicLong(offered);
			this.taken=new AtomicLong(taken);
			this.retries=new AtomicLong(retries);
		}

		private Status() {
			this(0,0,0);
		}

		private Status(Status status) {
			this(status.offered.get(),status.taken.get(),status.retries.get());
		}

		void offer() {
			this.offered.incrementAndGet();
		}

		void take() {
			this.taken.incrementAndGet();
		}

		void retryLater() {
			this.taken.decrementAndGet();
			this.retries.incrementAndGet();
		}

		@Override
		public String toString() {
			Status tmp = new Status(this);
			return
				String.format(
					"%d commands pending (%d produced / %d processed (%d retries))",
					tmp.offered.get()-tmp.taken.get(),
					tmp.offered.get(),
					tmp.taken.get(),
					tmp.retries.get());
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(CommandProcessingMonitor.class);

	private final LinkedBlockingQueue<Command> commandQueue;
	private final Status status;

	private volatile boolean shuttingDown;

	CommandProcessingMonitor() {
		this.commandQueue=Queues.newLinkedBlockingQueue();
		this.status=new Status();
		this.shuttingDown=false;
	}

	void offer(Command command) {
		this.commandQueue.offer(command);
		this.status.offer();
	}

	void retryLater(Command command) {
		this.commandQueue.offer(command);
		this.status.retryLater();
	}

	Command take() throws InterruptedException {
		Command taken=this.commandQueue.take();
		this.status.take();
		return taken;
	}

	@Override
	protected void run() throws Exception {
		LOGGER.info("Command processing monitor started.");
		do {
			TimeUnit.SECONDS.sleep(5);
			if(!this.shuttingDown) {
				LOGGER.info(this.status.toString());
			}
		} while(!this.shuttingDown);
		LOGGER.info("Command processing monitor terminated.");
		debugDismissedDetails();
	}

	@Override
	protected void triggerShutdown() {
		this.shuttingDown=true;
		LOGGER.info("Requested command processing monitor termination...");
	}

	private void debugDismissedDetails() {
		List<Command> abandoned=Lists.newArrayList();
		this.commandQueue.drainTo(abandoned);
		if(!abandoned.isEmpty()) {
			LOGGER.info("Dismissing {} commands",abandoned.size());
			for(Command command:abandoned) {
				LOGGER.info("- Dismissed {}",command);
			}
		}
	}

}
