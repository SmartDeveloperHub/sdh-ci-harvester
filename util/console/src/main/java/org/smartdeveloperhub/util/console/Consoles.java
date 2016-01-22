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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Convenience class for providing {@link Console} implementations.
 */
public final class Consoles {

	private static final Console DEFAULT =
		(System.console() == null) ?
			streamConsole(System.in, System.out) : // NOSONAR
			new NativeConsole(System.console());

	private Consoles() {
	}

	/**
	 * The default system console.
	 *
	 * @return the default device
	 */
	public static Console defaultConsole() {
		return DEFAULT;
	}

	/**
	 * Returns a console wrapping the given streams. The default system
	 * encoding is used to decode/encode data.
	 *
	 * @param in
	 *            an input source
	 * @param out
	 *            an output target
	 * @return a new console
	 */
	public static Console streamConsole(InputStream in, OutputStream out) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		PrintWriter writer = new PrintWriter(out, true);
		return new CharacterConsole(reader, writer);
	}

	/**
	 * Returns a console wrapping the given streams.
	 *
	 * @param reader
	 *            an input source
	 * @param writer
	 *            an output target
	 * @return a new console
	 */
	public static Console characterConsole(BufferedReader reader, PrintWriter writer) {
		return new CharacterConsole(reader, writer);
	}

}
