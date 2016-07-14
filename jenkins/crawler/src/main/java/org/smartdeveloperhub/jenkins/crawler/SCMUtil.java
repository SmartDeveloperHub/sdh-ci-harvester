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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0
 *   Bundle      : ci-jenkins-crawler-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.util.xml.XmlProcessingException;
import org.smartdeveloperhub.util.xml.XmlUtils;
import org.w3c.dom.Document;

final class SCMUtil {

	private static final Logger LOGGER=LoggerFactory.getLogger(SCMUtil.class);

	private SCMUtil() {
	}

	static boolean isDefined(final Codebase codebase) {
		return codebase!=null && (codebase.isSetBranch() || codebase.isSetLocation());
	}

	static Codebase createCodebase(final JenkinsResource resource) {
		final Document document = resource.content().get();
		return
			new Codebase().
				withLocation(getLocation(document)).
				withBranch(getBranch(document));
	}

	static Codebase mergeCodebases(final List<Codebase> codebases) {
		final Codebase result=new Codebase();
		for(final Codebase codebase:codebases) {
			if(codebase!=null) {
				result.setLocation(firstNonNull(result.getLocation(),codebase.getLocation()));
				result.setBranch(firstNonNull(result.getBranch(),codebase.getBranch()));
			}
		}
		return result;
	}

	private static <V> V firstNonNull(final V v1, final V v2) {
		return v1!=null?v1:v2;
	}

	private static URI getLocation(final Document document) {
		URI location=null;
		try {
			final String rawLocation=
				XmlUtils.
					evaluateXPath(
						"//scm[@class='hudson.plugins.git.GitSCM']/userRemoteConfigs//url",
						document);
			if(rawLocation!=null && !rawLocation.isEmpty()) {
				location = URI.create(rawLocation);
			}
		} catch (final XmlProcessingException e) {
			LOGGER.error("Could not recover codebase location information from {}",toStringQuietly(document),e);
		}
		return location;
	}

	private static String getBranch(final Document document) {
		String branch=null;
		try {
			final String rawBranch=
				XmlUtils.
					evaluateXPath(
						"//scm[@class='hudson.plugins.git.GitSCM']/branches//name",
						document);
			if(rawBranch!=null && !rawBranch.isEmpty()) {
				branch=rawBranch;
			}
		} catch (final XmlProcessingException e) {
			LOGGER.error("Could not recover codebase branch information from {}",toStringQuietly(document),e);
		}
		return branch;
	}

	private static String toStringQuietly(final Document document) {
		try {
			return XmlUtils.toString(document);
		} catch (final XmlProcessingException e) {
			LOGGER.error("Could not process {}",document,e);
			return "<not available>";
		}
	}

}
