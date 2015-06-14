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

import java.io.PrintWriter;
import java.io.Reader;

/**
 * {@link Console} implementation that wraps a {@link java.io.Console}.
 */
final class NativeConsole implements Console {
	private final java.io.Console console;

	NativeConsole(java.io.Console console) {
		this.console = console;
	}

	@Override
	public Console printf(String fmt, Object... params) throws ConsoleException {
		console.format(fmt, params);
		return this;
	}

	@Override
	public Reader reader() throws ConsoleException {
		return console.reader();
	}

	@Override
	public String readLine() throws ConsoleException {
		return console.readLine();
	}

	@Override
	public char[] readPassword() throws ConsoleException {
		return console.readPassword();
	}

	@Override
	public PrintWriter writer() throws ConsoleException {
		return console.writer();
	}

}