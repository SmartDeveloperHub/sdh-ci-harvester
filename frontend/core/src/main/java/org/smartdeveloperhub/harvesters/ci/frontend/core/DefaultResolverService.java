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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.lifecycle.ApplicationLifecycleListener;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SnapshotResolver;
import org.ldp4j.application.session.WriteSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.ResolverService;
import org.smartdeveloperhub.harvesters.ci.frontend.core.execution.ExecutionHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;

final class DefaultResolverService implements ResolverService, ApplicationLifecycleListener {

	private static final Logger LOGGER=LoggerFactory.getLogger(DefaultResolverService.class);

	private final Lock read;

	private final Lock write;

	private ApplicationContext context;

	DefaultResolverService() {
		final ReadWriteLock lock=new ReentrantReadWriteLock();
		this.read=lock.readLock();
		this.write=lock.writeLock();
	}

	@Override
	public boolean isReady() {
		this.read.lock();
		try {
			return this.context!=null;
		} finally {
			this.read.unlock();
		}
	}

	@Override
	public URI resolveExecution(final Execution execution) {
		URI result=null;
		this.read.lock();
		try(final WriteSession session=this.context.createSession()) {
			final SnapshotResolver resolver=
				SnapshotResolver.
					builder().
						withCanonicalBase(URI.create("http://localhost/harvester/")).
						withReadSession(session).
						build();
			final ResourceSnapshot snapshot=session.find(ResourceSnapshot.class,IdentityUtil.executionName(execution),ExecutionHandler.class);
			if(snapshot!=null) {
				result=resolver.toURI(snapshot);
			}
		} catch (final Exception e) {
			LOGGER.error("Could not resolve the URI of {}. Full stacktrace follows",execution,e);
		} finally {
			this.read.unlock();
		}
		return result;
	}

	@Override
	public void applicationStarted(final ApplicationContext context) {
		this.write.lock();
		try {
			this.context=context;
		} finally {
			this.write.unlock();
		}
	}

	@Override
	public void applicationStopped() {
		this.write.lock();
		try {
			this.context=null;
		} finally {
			this.write.unlock();
		}
	}

}