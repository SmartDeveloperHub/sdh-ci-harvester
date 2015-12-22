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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-concurrent:0.2.0
 *   Bundle      : ci-util-concurrent-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.util.concurrent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.harvesters.util.concurrent.CustomScheduledThreadPoolExecutor;

@RunWith(JMockit.class)
public class CustomScheduledThreadPoolExecutorTest {

	private static class RuntimeExceptionRunnable implements Runnable {

		private final RuntimeException failure;

		private RuntimeExceptionRunnable(final RuntimeException failure) {
			this.failure = failure;

		}

		@Override
		public void run() {
			throw this.failure;
		}

	}

	private MockThreadFactory factory;

	private CustomScheduledThreadPoolExecutor sut;

	private void completeSafeExecution(final Future<?> future) throws Exception {
		future.get();
		assertThat(this.factory.counter.get(),equalTo(1));
		assertThat(MockThreadFactory.EXCEPTION_HANDLER.invocations.isEmpty(),equalTo(true));
	}

	@Before
	public void setUp() {
		this.factory=new MockThreadFactory();
		this.sut=new CustomScheduledThreadPoolExecutor(1,this.factory);
	}

	@Test
	public void testGetCommand$happyPath() throws Exception {
		final Runnable r=new Runnable() {
			@Override
			public void run() {
				// Do nothing
			}
		};
		final Future<?> future = this.sut.submit(r);
		final Runnable task = this.sut.getCommand(future, r.getClass());
		assertThat(task,sameInstance(r));
		completeSafeExecution(future);
	}

	@Test
	public void testGetCommand$invalidFuture(@Mocked final Future<?> invalidFuture) throws Exception {
		final Runnable task = this.sut.getCommand(invalidFuture, Runnable.class);
		assertThat(task,nullValue());
	}

	@Test
	public void testGetCommand$notMatchingRunnableClass() throws Exception {
		final Runnable r=new Runnable() {
			@Override
			public void run() {
				// Do nothing
			}
		};
		final Future<?> future = this.sut.submit(r);
		final Runnable task = this.sut.getCommand(future, RuntimeExceptionRunnable.class);
		assertThat(task,nullValue());
		completeSafeExecution(future);
	}

	@Test
	public void testUnwrapCommand$happyPath() throws Exception {
		final Runnable r=new Runnable() {
			@Override
			public void run() {
				// Do nothing
			}
		};
		final Future<?> future = this.sut.schedule(r, 3, TimeUnit.SECONDS);
		final Runnable scheduledTask = this.sut.getCommand(future, r.getClass());
		assertThat(scheduledTask,sameInstance(r));

		final List<Runnable> runnables = this.sut.shutdownNow();
		assertThat(runnables,hasSize(1));
		final Runnable canceledTask=this.sut.unwrap(runnables.get(0),r.getClass());
		assertThat(canceledTask,sameInstance(r));
	}

	@Test
	public void testUnwrapCommand$invalidRunnable(@Mocked final Runnable invalidRunnable) throws Exception {
		final Runnable task = this.sut.unwrap(invalidRunnable, Runnable.class);
		assertThat(task,nullValue());
	}

}
