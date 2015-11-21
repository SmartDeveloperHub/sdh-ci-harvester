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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-concurrent:0.2.0-SNAPSHOT
 *   Bundle      : ci-util-concurrent-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.util.concurrent;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import org.junit.Test;
import org.ldp4j.commons.testing.Utils;
import org.smartdeveloperhub.harvesters.util.concurrent.CustomScheduledThreadPoolExecutor;
import org.smartdeveloperhub.harvesters.util.concurrent.MemoizingScheduledExecutorService;
import org.smartdeveloperhub.harvesters.util.concurrent.MoreExecutors;

public class MoreExecutorsTest  {

	@Test
	public void verifyIsValidUtilityClass() {
		assertThat(Utils.isUtilityClass(MoreExecutors.class),equalTo(true));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreation$invalidCorePoolSize() {
		MoreExecutors.newMemoizingScheduledExecutorService(-1,null);
	}

	@Test(expected=NullPointerException.class)
	public void testCreation$invalidThreadFactory() {
		MoreExecutors.newMemoizingScheduledExecutorService(1,null);
	}

	@Test
	public void testCreation$happyPath() {
		final MockThreadFactory threadFactory = new MockThreadFactory();
		final MemoizingScheduledExecutorService executor = MoreExecutors.newMemoizingScheduledExecutorService(1,threadFactory);
		assertThat(executor,instanceOf(CustomScheduledThreadPoolExecutor.class));
	}

}
