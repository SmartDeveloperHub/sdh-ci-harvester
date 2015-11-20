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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.util;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class CustomScheduledFutureTask<V> implements RunnableScheduledFuture<V> {

	private final RunnableScheduledFuture<V> delegate;
	private final Object task;

	CustomScheduledFutureTask(final Object task, final RunnableScheduledFuture<V> delegate) {
		this.delegate=delegate;
		this.task=task;
	}

	Object task() {
		return this.task;
	}

	@Override
	public void run() {
		this.delegate.run();  // NOSONAR
		if(!this.delegate.isCancelled() && this.delegate.isDone()) {
			try {
				this.delegate.get();
			} catch (final InterruptedException e) { // NOSONAR
				// IGNORE
			} catch (final ExecutionException e) {
				final Thread thread = Thread.currentThread();
				thread.
					getUncaughtExceptionHandler().
						uncaughtException(thread, e.getCause());
			}
		}
	}

	@Override
	public boolean cancel(final boolean mayInterruptIfRunning) {
		return this.delegate.cancel(mayInterruptIfRunning);
	}

	@Override
	public boolean isCancelled() {
		return this.delegate.isCancelled();
	}

	@Override
	public boolean isDone() {
		return this.delegate.isDone();
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		return this.delegate.get();
	}

	@Override
	public V get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		return this.delegate.get(timeout, unit);
	}

	@Override
	public long getDelay(final TimeUnit unit) {
		return this.delegate.getDelay(unit);
	}

	@Override
	public int compareTo(final Delayed o) {
		return this.delegate.compareTo(o);
	}

	@Override
	public boolean isPeriodic() {
		return this.delegate.isPeriodic();
	}

	@Override
	public String toString() {
		return this.delegate.toString();
	}

 }