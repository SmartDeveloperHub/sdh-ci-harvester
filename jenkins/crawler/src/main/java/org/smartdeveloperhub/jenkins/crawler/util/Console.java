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
 * Abstraction representing a text input/output console.
 */
public interface Console {

	/**
	 * Writes a formatted string to this console's output stream using the
	 * specified format string and arguments.
	 *
	 * @param fmt
	 *            A format string as described in {@link java.util.Formatter} *
	 * @param args
	 *            Arguments referenced by the format specifiers in the format
	 *            string. If there are more arguments than format specifiers,
	 *            the extra arguments are ignored. The number of arguments is
	 *            variable and may be zero. The maximum number of arguments is
	 *            limited by the maximum dimension of a Java array as defined by
	 *            <cite>The Java&trade; Virtual Machine Specification</cite>.
	 *            The behaviour on a <tt>null</tt> argument depends on the
	 *            conversion.
	 *
	 * @throws ConsoleException
	 *             If a format string contains an illegal syntax, a format
	 *             specifier that is incompatible with the given arguments,
	 *             insufficient arguments given the format string, or other
	 *             illegal conditions.
	 *
	 * @return This console
	 */
	Console printf(String fmt, Object... params);

	/**
	 * Reads a single line of text from the console.
	 *
	 * @throws ConsoleException
	 *             If an I/O error occurs.
	 *
	 * @return A string containing the line read from the console, not including
	 *         any line-termination characters, or <tt>null</tt> if an end of
	 *         stream has been reached.
	 */
	String readLine();

	/**
	 * Reads a password or passphrase from the console.
	 *
	 * @throws ConsoleException
	 *             If an I/O error occurs.
	 *
	 * @return A character array containing the password or passphrase read from
	 *         the console, not including any line-termination characters, or
	 *         <tt>null</tt> if an end of stream has been reached.
	 */
	char[] readPassword();

	/**
	 * Retrieves the {@link java.io.Reader Reader} associated with this console.
	 * <p>
	 * This method is intended to be used by sophisticated applications, for
	 * example, a {@link java.util.Scanner} object which utilizes the rich
	 * parsing/scanning functionality provided by the <tt>Scanner</tt>:
	 * <blockquote>
	 *
	 * <pre>
	 * Console con = System.console();
	 * if (con != null) {
	 *     Scanner sc = new Scanner(con.reader());
	 *     ...
	 * }
	 * </pre>
	 *
	 * </blockquote>
	 * <p>
	 * For simple applications requiring only line-oriented reading, use
	 * <tt>{@link #readLine}</tt>.
	 * <p>
	 * The bulk read operations {@link java.io.Reader#read(char[]) read(char[])
	 * * }, {@link java.io.Reader#read(char[], int, int) read(char[], int, int)
	 * } and {@link java.io.Reader#read(java.nio.CharBuffer)
	 * read(java.nio.CharBuffer)} on the returned object will not read in
	 * characters beyond the line bound for each invocation, even if the
	 * destination buffer has space for more characters. A line bound is
	 * considered to be any one of a line feed (<tt>'\n'</tt>), a carriage
	 * return (<tt>'\r'</tt>), a carriage return followed immediately by a
	 * linefeed, or an end of stream.
	 *
	 * @return The reader associated with this console
	 */
	Reader reader();

	/**
	 * Retrieves the unique {@link java.io.PrintWriter PrintWriter} object
	 * associated with this console.
	 *
	 * @return The printwriter associated with this console
	 */
	PrintWriter writer();

}
