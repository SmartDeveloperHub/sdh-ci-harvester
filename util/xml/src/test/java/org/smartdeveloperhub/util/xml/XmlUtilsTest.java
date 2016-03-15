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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-xml:0.3.0-SNAPSHOT
 *   Bundle      : ci-util-xml-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.xml;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class XmlUtilsTest {

	private static final String EMPTY_STRING = "";

	private final int[][] badRanges={
		{0x0,0x8},
		{0xB,0xC},
		{0xE,0x1F},
		{0xD800,0xDFFF}, // High and low surrogates
		{0xFFFE,0xFFFF} //Process-internal codepoints.
	};

	private final int[][] goodRanges={
		{0x9,0xA},
		{0xD,0xD},
		{0x20,0xD7FF},
		{0xE000,0xFFFD},
		{0x10000,0x10FFFF}
	};

	@Test
	public void nullStringsAreNotStripped() throws Exception {
		assertThat(XmlUtils.stripNonValidXMLCharacters(null),equalTo(EMPTY_STRING));
	}

	@Test
	public void emptyStringsAreNotStripped() throws Exception {
		assertThat(XmlUtils.stripNonValidXMLCharacters(EMPTY_STRING),equalTo(EMPTY_STRING));
	}

	@Test
	public void stripsInvalidCharacters() throws Exception {
		for(int i=0;i<this.badRanges.length;i++) {
			final int[] range=this.badRanges[i];
			for(int c=range[0];c<=range[1];c++) {
				final String in = new String(Character.toChars(c));
				assertThat(in.length(),equalTo(Character.charCount(c)));
				final String out = XmlUtils.stripNonValidXMLCharacters(in);
				assertThat(String.format("Should strip \\u%X (%s)",c,in),out.length(),equalTo(0));
			}
		}
	}

	@Test
	public void acceptsValidCharacters() throws Exception {
		for(int i=0;i<this.goodRanges.length;i++) {
			final int[] range=this.goodRanges[i];
			for(int c=range[0];c<=range[1];c++) {
				final String in = new String(Character.toChars(c));
				final String out = XmlUtils.stripNonValidXMLCharacters(in);
				assertThat(String.format("Should accept \\u%X (%s)",c,in),out,equalTo(in));
			}
		}
	}

}
