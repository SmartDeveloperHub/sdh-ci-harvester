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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.4.0-SNAPSHOT
 *   Bundle      : ci-jenkins-client-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;

public class JenkinsResourceProxyTest {

	@Test
	public void testGet() throws Exception {
		final String location="https://ci.jenkins-ci.org/";
		final JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withEntity(JenkinsEntityType.INSTANCE);
		try {
			final JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			assertThat(representation,notNullValue());
		} catch (final IOException e) {
		}
	}

	@Test
	public void testGet$withDepth() throws Exception {
		final String location="https://ci.jenkins-ci.org/job/jenkins_main_trunk/";
		final JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withDepth(1).
					withEntity(JenkinsEntityType.FREE_STYLE_BUILD);
		try {
			final JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			assertThat(representation,notNullValue());
		} catch (final IOException e) {
		}
	}

	@Test
	public void testGet$withTree() throws Exception {
		final String location="https://ci.jenkins-ci.org/job/jenkins_main_trunk/";
		final JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withTree("allBuilds[url,number,building,duration,result,timestamp]").
					withEntity(JenkinsEntityType.FREE_STYLE_BUILD);
		try {
			final JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			assertThat(representation,notNullValue());
		} catch (final IOException e) {
		}
	}

	@Test
	public void testBadEncoding() throws Exception {
		final String location="https://ci.jenkins-ci.org/job/infra_changelog_refresh/27821/";
		final JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withEntity(JenkinsEntityType.RUN);
		try {
			final JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			assertThat(representation,notNullValue());
			if(representation.failure().isPresent()) {
				assertThat(representation.failure().get(),notNullValue());
			}
		} catch (final IOException e) {
		}
	}

}
