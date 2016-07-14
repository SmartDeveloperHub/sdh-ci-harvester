/**
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   This file is part of the Smart Developer Hub Project:
 *     http://www.smartdeveloperhub.org/
 *
 *   Center for Open Middleware
 *     http://www.centeropenmiddleware.com/
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 *   Copyright (C) 2015-2016 Center for Open Middleware.
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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-concurrent:0.3.0
 *   Bundle      : ci-util-concurrent-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.util.concurrent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.harvesters.util.concurrent.CustomScheduledFutureTask;

@RunWith(JMockit.class)
public class CustomScheduledFutureTaskTest {

	interface Threaded {

		void execute(CustomScheduledFutureTask<?> sut, Thread thread, MockExceptionHandler handler) throws Exception;

	}

	@Mocked private RunnableScheduledFuture<String> delegate;

	private CustomScheduledFutureTask<String> newSut() {
		return new CustomScheduledFutureTask<String>(null, this.delegate);
	}

	private void verify(final Threaded r) throws Exception {
		final Thread thread = Thread.currentThread();
		final UncaughtExceptionHandler defaultExceptionHandler = thread.getUncaughtExceptionHandler();
		final MockExceptionHandler handler = new MockExceptionHandler();
		thread.setUncaughtExceptionHandler(handler);
		try {
			r.execute(newSut(),thread,handler);
		} finally {
			thread.setUncaughtExceptionHandler(defaultExceptionHandler);
		}
	}

	@Test
	public void testRun$cancelled() throws Exception {
		verify(
			new Threaded() {
				@Override
				public void execute(final CustomScheduledFutureTask<?> sut, final Thread thread, final MockExceptionHandler handler) throws Exception {
					new Expectations() {{
						CustomScheduledFutureTaskTest.this.delegate.isCancelled();this.result=true;
					}};
					sut.run();
					assertThat(handler.invocations.isEmpty(),equalTo(true));
				}
			}
		);
	}

	@Test
	public void testRun$notDone() throws Exception {
		verify(
			new Threaded() {
				@Override
				public void execute(final CustomScheduledFutureTask<?> sut, final Thread thread, final MockExceptionHandler handler) throws Exception {
					new Expectations() {{
						CustomScheduledFutureTaskTest.this.delegate.isCancelled();this.result=false;
						CustomScheduledFutureTaskTest.this.delegate.isDone();this.result=false;
					}};
					sut.run();
					assertThat(handler.invocations.isEmpty(),equalTo(true));
				}
			}
		);
	}

	@Test
	public void testRun$executionException() throws Exception {
		verify(
			new Threaded() {
				@Override
				public void execute(final CustomScheduledFutureTask<?> sut, final Thread thread, final MockExceptionHandler handler) throws Exception {
					final Throwable cause = new RuntimeException("cause");
					final ExecutionException failure = new ExecutionException("failure",cause);
					new Expectations() {{
						CustomScheduledFutureTaskTest.this.delegate.isCancelled();this.result=false;
						CustomScheduledFutureTaskTest.this.delegate.isDone();this.result=true;
						CustomScheduledFutureTaskTest.this.delegate.get();this.result=failure;
					}};
					sut.run();
					assertThat(handler.invocations,hasEntry(thread.getName(),cause));
				}
			}
		);
	}

	@Test
	public void testRun$InterruptedException() throws Exception {
		verify(
			new Threaded() {
				@Override
				public void execute(final CustomScheduledFutureTask<?> sut, final Thread thread, final MockExceptionHandler handler) throws Exception {
					final InterruptedException failure = new InterruptedException("failure");
					new Expectations() {{
						CustomScheduledFutureTaskTest.this.delegate.isCancelled();this.result=false;
						CustomScheduledFutureTaskTest.this.delegate.isDone();this.result=true;
						CustomScheduledFutureTaskTest.this.delegate.get();this.result=failure;
					}};
					sut.run();
					assertThat(handler.invocations.isEmpty(),equalTo(true));
				}
			}
		);
	}

	@Test
	public void testCancel() throws Exception {
		final boolean expectation=true;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.cancel(expectation);
		}};
		newSut().cancel(expectation);
	}

	@Test
	public void testIsCancelled() throws Exception {
		final boolean expectation=true;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.isCancelled();this.result=expectation;
		}};
		assertThat(newSut().isCancelled(),equalTo(expectation));
	}

	@Test
	public void testIsDone() throws Exception {
		final boolean expectation=true;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.isDone();this.result=expectation;
		}};
		assertThat(newSut().isDone(),equalTo(expectation));
	}

	@Test
	public void testGet() throws Exception {
		final String expectation="true";
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.get();this.result=expectation;
		}};
		assertThat(newSut().get(),equalTo(expectation));
	}

	@Test
	public void testGetTimed() throws Exception {
		final String expectation="true";
		final long timeout=123L;
		final TimeUnit unit=TimeUnit.SECONDS;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.get(timeout,unit);this.result=expectation;
		}};
		assertThat(newSut().get(timeout,unit),equalTo(expectation));
	}

	@Test
	public void testGetDelay() throws Exception {
		final long expectation=123L;
		final TimeUnit unit=TimeUnit.SECONDS;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.getDelay(unit);this.result=expectation;
		}};
		assertThat(newSut().getDelay(unit),equalTo(expectation));
	}

	@Test
	public void testCompareTo(@Mocked final Delayed delayed) throws Exception {
		final int expectation=123;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.compareTo(delayed);this.result=expectation;
		}};
		assertThat(newSut().compareTo(delayed),equalTo(expectation));
	}

	@Test
	public void testIsPeriodic() throws Exception {
		final boolean expectation=true;
		new Expectations() {{
			CustomScheduledFutureTaskTest.this.delegate.isPeriodic();this.result=expectation;
		}};
		assertThat(newSut().isPeriodic(),equalTo(expectation));
	}

	@Test
	public void testToString() throws Exception {
		assertThat(newSut().toString(),equalTo(this.delegate.toString()));
	}

}
