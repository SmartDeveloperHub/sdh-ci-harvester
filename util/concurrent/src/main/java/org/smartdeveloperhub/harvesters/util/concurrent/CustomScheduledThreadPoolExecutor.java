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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

final class CustomScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor implements MemoizingScheduledExecutorService {

	CustomScheduledThreadPoolExecutor(final int corePoolSize, final ThreadFactory threadFactory) {
		super(corePoolSize, threadFactory);
	}

	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(final Runnable r, final RunnableScheduledFuture<V> task) {
		return new CustomScheduledFutureTask<V>(r, task);
	}

	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(final Callable<V> c, final RunnableScheduledFuture<V> task) {
		return new CustomScheduledFutureTask<V>(c, task);
	}

	private <S> S safeUnwrap(final CustomScheduledFutureTask<?> csft, final Class<? extends S> clazz) {
		S result=null;
		final Object task = csft.task();
		if(clazz.isInstance(task)) {
			result=clazz.cast(task);
		}
		return result;
	}

	@Override
	public <S> S unwrap(final Runnable runnable, final Class<? extends S> clazz) {
		S result=null;
		if(runnable instanceof CustomScheduledFutureTask<?>) {
			result=safeUnwrap((CustomScheduledFutureTask<?>)runnable, clazz);
		}
		return result;
	}

	@Override
	public <S,V> S getCommand(final Future<V> future, final Class<? extends S> clazz) {
		S result=null;
		if(future instanceof CustomScheduledFutureTask<?>) {
			result=safeUnwrap((CustomScheduledFutureTask<?>)future, clazz);
		}
		return result;
	}

}