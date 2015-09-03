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

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class ListenerManager<T> {

	private static final Logger LOGGER=LoggerFactory.getLogger(ListenerManager.class);

	private final CopyOnWriteArrayList<T> listeners; // NOSONAR

	private ListenerManager() {
		this.listeners=Lists.newCopyOnWriteArrayList();
	}

	private void logListenerLifecycle(T listener, String action) {
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("%s %08X (%s)",action,listener.hashCode(),listener.getClass().getName()));
		}
	}

	public void registerListener(T listener) {
		if(this.listeners.addIfAbsent(listener)) {
			logListenerLifecycle(listener, "Registered");
		}
	}

	public void deregisterListener(T listener) {
		if(this.listeners.remove(listener)) {
			logListenerLifecycle(listener,"Deregistered");
		}
	}

	public void notify(Notification<T> notification) {
		for(T listener:this.listeners) {
			try {
				notification.propagate(listener);
			} catch (Exception e) {
				if(LOGGER.isWarnEnabled()) {
					LOGGER.warn(
						"Propagation of notification {} to listener {} ({}) failed",
						notification,
						Integer.toHexString(listener.hashCode()).toUpperCase(),
						listener.getClass().getName(),
						e);
					LOGGER.debug("Notification failure stacktrace follows",e);
				}
			}
		}
	}

	public static <T> ListenerManager<T> newInstance() {
		return new ListenerManager<T>();
	}

}