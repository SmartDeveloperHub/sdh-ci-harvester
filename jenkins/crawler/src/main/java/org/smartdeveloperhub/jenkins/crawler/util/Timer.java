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
package org.smartdeveloperhub.jenkins.crawler.util;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public final class Timer {

	private static final PeriodFormatter PERIOD_FORMATTER =
		new PeriodFormatterBuilder().
				appendYears().
				appendSuffix(" year", " years").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMonths().
				appendSuffix(" month", " months").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendDays().
				appendSuffix(" day", " days").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendHours().
				appendSuffix(" hour", " hours").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMinutes().
				appendSuffix(" minute", " minutes").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendSeconds().
				appendSuffix(" second", " seconds").
				appendSeparator(" and ").
				printZeroRarelyLast().
				appendMillis().
				appendSuffix(" millisecond", " milliseconds").
				appendSeparator(" and ").
				printZeroRarelyLast().
				toFormatter();

	private long started;
	private long finished;

	public Timer() {
		this.started=-1;
		this.finished=-1;
	}

	public void start() {
		long now = System.currentTimeMillis();
		checkState(this.started<0,"Timer has already been started");
		checkState(this.finished<0,"Timer has already been stopped");
		this.started=now;
	}

	public void stop() {
		long now = System.currentTimeMillis();
		checkState(this.started>0,"Timer has not been started");
		checkState(this.finished<0,"Timer has already been stopped");
		this.finished=now;
	}

	public long duration() {
		long result = this.finished-this.started;
		if(result<0) {
			result=0;
		}
		return result;
	}

	@Override
	public String toString() {
		StringWriter writer = new StringWriter();
		try {
			long init=this.started;
			if(init<0) {
				init=0;
			}
			long end =this.finished;
			if(end<init) {
				end=init;
			}
			PERIOD_FORMATTER.
				getPrinter().
					printTo(
						writer,
						new Duration(init,end).toPeriod(),
						Locale.ENGLISH);
			return writer.toString();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
