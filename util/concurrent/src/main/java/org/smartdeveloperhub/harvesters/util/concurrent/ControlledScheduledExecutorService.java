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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-concurrent:0.3.0-SNAPSHOT
 *   Bundle      : ci-util-concurrent-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.util.concurrent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public final class ControlledScheduledExecutorService extends ScheduledThreadPoolExecutor {

	public static class Builder {

		private int poolSize;
		private ThreadFactory threadFactory;

		private Builder() {
			this.poolSize=1;
			this.threadFactory=new ThreadFactoryBuilder().build();
		}

		public Builder withPoolSize(final int poolSize) {
			checkArgument(poolSize>0,"Pool size must be greatern that 0 (%s)",this.poolSize);
			this.poolSize = poolSize;
			return this;
		}

		public Builder withThreadFactory(final ThreadFactory threadFactory) {
			checkNotNull(threadFactory,"Thread factory cannot be null");
			this.threadFactory = threadFactory;
			return this;
		}

		public ScheduledExecutorService build() {
			return new ControlledScheduledExecutorService(this.poolSize,this.threadFactory);
		}

	}

	private static final Logger LOGGER=LoggerFactory.getLogger(ControlledScheduledExecutorService.class);

	private ControlledScheduledExecutorService(final int corePoolSize, final ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	@Override
	protected void afterExecute(final Runnable r, final Throwable t) {
		super.afterExecute(r,t);
		Throwable failure=t;
		if(failure == null && r instanceof Future<?>) {
			try {
				final Future<?> future = (Future<?>) r;
				if(future.isDone()) {
					future.get();
				}
			} catch (final CancellationException ce) {
				failure = ce;
			} catch (final ExecutionException ee) { // NOSONAR
				failure = ee.getCause();
			} catch (final InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			}
		}
		if(failure!=null) {
			LOGGER.error("Runnable {} died",r.getClass().getName(),failure);
		}
	}

	public static Builder builder() {
		return new Builder();
	}
}