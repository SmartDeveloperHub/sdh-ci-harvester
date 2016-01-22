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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-console:0.3.0-SNAPSHOT
 *   Bundle      : ci-util-console-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * @{link Console} implementation that wraps character streams.
 */
final class CharacterConsole implements Console {

	private final BufferedReader in;
	private final PrintWriter out;

	CharacterConsole(BufferedReader reader, PrintWriter writer) {
		this.in = reader;
		this.out = writer;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CharacterConsole printf(String fmt, Object... params) {
		try {
			this.out.printf(fmt, params);
			return this;
		} catch (Exception e) {
			throw new ConsoleException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String readLine() {
		try {
			return this.in.readLine();
		} catch (IOException e) {
			throw new ConsoleException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char[] readPassword() {
		return readLine().toCharArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Reader reader() {
		return this.in;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrintWriter writer() {
		return this.out;
	}

}
