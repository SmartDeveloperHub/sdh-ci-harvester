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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public final class OperationStrategy {

	private static final class DefaultOperationDecissionPoint implements OperationDecissionPoint {

		private static final class DefaultCrawlingDelay implements Delayed {

			private static final long DELAY_TIME = 60L;

			private static final TimeUnit DELAY_UNIT = TimeUnit.SECONDS;

			@Override
			public int compareTo(Delayed o) {
				return (int)(o.getDelay(DELAY_UNIT)-DELAY_TIME);
			}

			@Override
			public long getDelay(TimeUnit unit) {
				return unit.convert(DELAY_TIME, DELAY_UNIT);
			}

		}

		@Override
		public Delayed getCrawlingDelay(CrawlerInformationPoint cip) {
			return new DefaultCrawlingDelay();
		}

		@Override
		public boolean canContinueCrawling(CrawlerInformationPoint cip) {
			return true;
		}
	}

	OperationDecissionPoint decissionPoint() {
		return new DefaultOperationDecissionPoint();
	}

}
