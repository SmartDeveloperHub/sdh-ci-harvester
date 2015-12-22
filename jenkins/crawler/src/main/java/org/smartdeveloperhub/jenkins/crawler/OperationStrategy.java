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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.util.concurrent.TimeUnit;

import org.smartdeveloperhub.harvesters.util.concurrent.Delay;

import static com.google.common.base.Preconditions.*;

public final class OperationStrategy {

	public static final class Builder {

		public static final class MultiRunMode {

			private final Builder builder;
			private final long iterations;

			private MultiRunMode(Builder builder, long iterations) {
				this.builder = builder;
				this.iterations = iterations;
			}

			public Builder withDelay(Delay delay) {
				this.builder.setDelay(delay);
				this.builder.setIterations(this.iterations);
				return this.builder;
			}

			public Builder withDelay(long time, TimeUnit unit) {
				return withDelay(Delay.create(time, unit));
			}

		}

		public static final class RunMode {

			private final Builder builder;

			private RunMode(Builder builder) {
				this.builder = builder;
			}

			public Builder once() {
				this.builder.setIterations(1);
				this.builder.setDelay(null);
				return this.builder;
			}

			public MultiRunMode times(long nIterations) {
				checkArgument(nIterations>1,"Number of iterations must be greatern than 1 (%s)",nIterations);
				return new MultiRunMode(this.builder,nIterations);
			}

			public MultiRunMode continuously() {
				return new MultiRunMode(this.builder,-1);
			}

		}

		private long iterations;
		private Delay delay;

		private Builder() {
			this.iterations=-1;
			this.delay=Delay.create(DELAY_TIME, DELAY_UNIT);
		}

		private void setIterations(long iterations) {
			this.iterations=iterations;
		}

		private void setDelay(Delay delay) {
			this.delay=delay;
		}

		public RunMode run() {
			return new RunMode(this);
		}

		public OperationStrategy build() {
			return new OperationStrategy(iterations,delay);
		}

	}
	private final class DefaultOperationDecissionPoint implements OperationDecissionPoint {

		@Override
		public Delay getCrawlingDelay(CrawlerInformationPoint cip) {
			return delay;
		}

		@Override
		public boolean canContinueCrawling(CrawlerInformationPoint cip) {
			if(iterations<0) {
				return true;
			} else {
				return iterations>cip.totalCrawlingSessions();
			}
		}

	}

	private static final long DELAY_TIME = 60L;

	private static final TimeUnit DELAY_UNIT = TimeUnit.SECONDS;

	private final long iterations;

	private final Delay delay;

	private OperationStrategy(long iterations, Delay delay) {
		this.iterations = iterations;
		this.delay = delay;
	}

	OperationDecissionPoint decissionPoint() {
		return new DefaultOperationDecissionPoint();
	}

	public static Builder builder() {
		return new Builder();
	}

}
