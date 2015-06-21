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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.core.commands.Command;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.AbstractExecutionThreadService;

final class CommandProcessingMonitor extends AbstractExecutionThreadService {

	static final class Status {

		private final AtomicLong offered;
		private final AtomicLong taken;

		private Status(long offered, long taken) {
			this.offered=new AtomicLong(offered);
			this.taken=new AtomicLong(taken);
		}

		private Status() {
			this(0,0);
		}

		private Status(Status status) {
			this(status.offered.get(),status.taken.get());
		}

		void offer() {
			this.offered.incrementAndGet();
		}

		void take() {
			this.taken.incrementAndGet();
		}

		@Override
		public String toString() {
			Status tmp = new Status(this);
			return
				String.format(
					"%d commands pending (%d produced / %d processed)",
					tmp.offered.get()-tmp.taken.get(),
					tmp.offered.get(),
					tmp.taken.get());
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
		if(!this.commandQueue.isEmpty()) {
			LOGGER.info("Dismissing {} commands",this.commandQueue.size());
			traceDismissedCommands();
		}
	}

	private void traceDismissedCommands() {
		if(LOGGER.isTraceEnabled()) {
			for(Command command:this.commandQueue) {
				LOGGER.trace("- Dimissed {}",command);
			}
		}
	}

}
