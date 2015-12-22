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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0
 *   Bundle      : ci-jenkins-crawler-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.util;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public final class Timer {

	private static final String TIMER_HAS_NOT_BEEN_STARTED     = "Timer has not been started";
	private static final String TIMER_HAS_ALREADY_BEEN_STOPPED = "Timer has already been stopped";
	private static final String TIMER_HAS_ALREADY_BEEN_STARTED = "Timer has already been started";

	private static final String MILLISECOND = " millisecond";
	private static final String SECOND      = " second";
	private static final String MINUTE      = " minute";
	private static final String HOUR        = " hour";
	private static final String DAY         = " day";
	private static final String MONTH       = " month";
	private static final String YEAR        = " year";
	private static final String AND         = " and ";

	private static final PeriodFormatter PERIOD_FORMATTER =
		new PeriodFormatterBuilder().
				appendYears().
				appendSuffix(YEAR, plural(YEAR)).
				appendSeparator(AND).
				printZeroRarelyLast().
				appendMonths().
				appendSuffix(MONTH, plural(MONTH)).
				appendSeparator(AND).
				printZeroRarelyLast().
				appendDays().
				appendSuffix(DAY, plural(DAY)).
				appendSeparator(AND).
				printZeroRarelyLast().
				appendHours().
				appendSuffix(HOUR, plural(HOUR)).
				appendSeparator(AND).
				printZeroRarelyLast().
				appendMinutes().
				appendSuffix(MINUTE, plural(MINUTE)).
				appendSeparator(AND).
				printZeroRarelyLast().
				appendSeconds().
				appendSuffix(SECOND, plural(SECOND)).
				appendSeparator(AND).
				printZeroRarelyLast().
				appendMillis().
				appendSuffix(MILLISECOND, plural(MILLISECOND)).
				appendSeparator(AND).
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
		checkState(this.started<0,TIMER_HAS_ALREADY_BEEN_STARTED);
		checkState(this.finished<0,TIMER_HAS_ALREADY_BEEN_STOPPED);
		this.started=now;
	}

	public void stop() {
		long now = System.currentTimeMillis();
		checkState(this.started>0,TIMER_HAS_NOT_BEEN_STARTED);
		checkState(this.finished<0,TIMER_HAS_ALREADY_BEEN_STOPPED);
		this.finished=now;
	}

	public Date startedOn() {
		return new Date(this.started);
	}

	public Date stoppedOn() {
		return new Date(this.finished);
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
			long end=this.finished;
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

	private static String plural(String string) {
		return string+"s";
	}

}
