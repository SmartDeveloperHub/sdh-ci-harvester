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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-console:0.2.0-SNAPSHOT
 *   Bundle      : ci-util-console-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.console;

import java.io.IOError;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.IllegalFormatException;

/**
 * {@link Console} implementation that wraps a {@link java.io.Console}.
 */
final class NativeConsole implements Console {

	private final java.io.Console console;

	NativeConsole(java.io.Console console) {
		this.console = console;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Console printf(String fmt, Object... params) {
		try {
			this.console.format(fmt, params);
			return this;
		} catch (IllegalFormatException e) {
			throw new ConsoleException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Reader reader() {
		return this.console.reader();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String readLine() {
		try {
			return this.console.readLine();
		} catch (IOError e) {
			throw new ConsoleException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public char[] readPassword() {
		try {
			return this.console.readPassword();
		} catch (IOError e) {
			throw new ConsoleException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PrintWriter writer() {
		return this.console.writer();
	}

}