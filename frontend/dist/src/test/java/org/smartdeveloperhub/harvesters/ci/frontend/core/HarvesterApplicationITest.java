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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.4.0-SNAPSHOT
 *   Bundle      : ci-frontend-dist-0.4.0-SNAPSHOT.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assume.assumeThat;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.client.JenkinsResourceProxy;

@RunWith(Arquillian.class)
public class HarvesterApplicationITest extends SmokeTest {

	private static final String SIMPLE_BUILD    = "service/builds/1/";
	private static final String COMPOSITE_BUILD = "service/builds/2/";
	private static int expectedResources;
	private static boolean available;

	private static boolean checkAvailability(final String str) {
		boolean available=false;
		try {
			final JenkinsResourceProxy proxy = JenkinsResourceProxy.create(URI.create(str));
			final JenkinsResource resource = proxy.get(JenkinsArtifactType.RESOURCE);
			available=!resource.status().isFailure();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return available;
	}

	@Deployment(name="default",testable=false)
	@TargetsContainer("tomcat")
	public static WebArchive createDeployment() throws Exception {
		String target = "https://ci.jenkins-ci.org/";
//		HarvesterApplicationITest.available = checkAvailability(target);
//		HarvesterApplicationITest.expectedResources = 5;
		if(!HarvesterApplicationITest.available) {
			target = "http://vps164.cesvima.upm.es:8000/";
			HarvesterApplicationITest.available=checkAvailability(target);
			HarvesterApplicationITest.expectedResources = 0;
		}
		if(!HarvesterApplicationITest.available) {
			target = "http://www.notfound.org/";
		}
		System.setProperty("ci.harvester.target",target);
		return SmokeTest.createWebArchive("default-harvester.war");
	}

	@Test
	@OperateOnDeployment("default")
	public void testService(@ArquillianResource final URL contextURL) throws Exception {
		assumeThat(HarvesterApplicationITest.available,equalTo(true));
		TimeUnit.MINUTES.sleep(1);
		final List<String> builds = getServiceBuilds(contextURL);
		assertThat(builds,hasSize(greaterThan(HarvesterApplicationITest.expectedResources)));
		assertThat(builds,
			hasItems(
				TestingUtil.resolve(contextURL,SIMPLE_BUILD),
				TestingUtil.resolve(contextURL,COMPOSITE_BUILD)));
	}

}
