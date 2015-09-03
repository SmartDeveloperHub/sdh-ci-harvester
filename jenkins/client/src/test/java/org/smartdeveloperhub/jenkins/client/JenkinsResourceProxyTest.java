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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.1.0
 *   Bundle      : ci-jenkins-client-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;

public class JenkinsResourceProxyTest {

	@Test
	public void testGet() throws Exception {
		String location="https://ci.jenkins-ci.org/";
		JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withUseHttps(true).
					withEntity(JenkinsEntityType.INSTANCE);
		try {
			JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			System.out.println("Resource from '"+location+"':");
			System.out.println(representation);
		} catch (IOException e) {
			System.err.println("Could not retrieve service: "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	@Test
	public void testBadEncoding() throws Exception {
		String location="http://ci.jenkins-ci.org/job/infra_changelog_refresh/27821/";
		JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withUseHttps(true).
					withEntity(JenkinsEntityType.RUN);
		try {
			JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			System.out.println("Resource from '"+location+"':");
			System.out.println(representation);
			if(representation.failure().isPresent()) {
				representation.failure().get().printStackTrace();
			}
		} catch (IOException e) {
			System.err.println("Could not retrieve service: "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

}
