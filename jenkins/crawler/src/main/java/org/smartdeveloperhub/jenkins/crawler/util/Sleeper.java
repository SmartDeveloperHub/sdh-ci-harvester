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
package org.smartdeveloperhub.jenkins.crawler.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.TimeUnit;

public final class Sleeper {

	private final Object witness;

	public Sleeper() {
		this.witness=new Object();
	}

	private void doSleep(long millis) throws InterruptedException {
		synchronized(this.witness) {
			this.witness.wait(millis); // NOSONAR
		}
	}

	public void sleep(Delay delay) throws InterruptedException {
		checkNotNull(delay,"Delay cannot be null");
		doSleep(delay.getTime(TimeUnit.MILLISECONDS));
	}

	public void sleep(long timeout, TimeUnit unit) throws InterruptedException {
		checkNotNull(timeout,"Time out cannot be null");
		checkNotNull(unit,"Time unit cannot be null");
		doSleep(TimeUnit.MILLISECONDS.convert(timeout, unit));
	}

	public void wakeUp() {
		synchronized(this.witness) {
			this.witness.notify();
		}
	}

}