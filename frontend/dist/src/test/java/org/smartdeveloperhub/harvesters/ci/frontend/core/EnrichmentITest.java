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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-dist-0.2.0-SNAPSHOT.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.frontend.curator.Action;
import org.smartdeveloperhub.harvesters.ci.frontend.curator.HarvesterCurator;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.client.JenkinsResourceProxy;

@RunWith(Arquillian.class)
public class EnrichmentITest extends SmokeTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(EnrichmentITest.class);

	private static boolean available;

	private static HarvesterCurator curator;

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
		EnrichmentITest.available = checkAvailability(target);
		if(!EnrichmentITest.available) {
			target = "http://vps164.cesvima.upm.es:8000/";
			EnrichmentITest.available=checkAvailability(target);
		}
		if(!EnrichmentITest.available) {
			target = "http://www.notfound.org/";
		}
		System.setProperty("ci.harvester.target",target);
		curator = new HarvesterCurator();
		curator.start();
		return SmokeTest.createWebArchive("default-harvester.war");
	}

	@Test
	@OperateOnDeployment("default")
	public void testService(@ArquillianResource final URL contextURL) throws Exception {
		assumeThat(EnrichmentITest.available,equalTo(true));
		LOGGER.info("Warming up...");
		TimeUnit.SECONDS.sleep(60);
		curator.stop();
		final List<Action> actionsUndertaken = curator.actionsUndertaken();
		LOGGER.info("Warm up completed. {} enrichment requests processed.",actionsUndertaken.size());
		if(actionsUndertaken.isEmpty()) {
			LOGGER.info("No enrichment requests processed. Aborted testing...");
		} else {
			LOGGER.info("Awaiting for the processing of the enrichment responses...");
			TimeUnit.SECONDS.sleep(30);
			LOGGER.info("Starting verification...");
			int applied=0;
			for(final Action action:actionsUndertaken) {
				final boolean hasBeenApplied = hasBeenApplied(contextURL, action);
				if(hasBeenApplied) {
					applied++;
				} else {
					LOGGER.warn("Enrichment response for {} was not applied",action.targetResource());
				}
			}
			assertThat(applied,greaterThan(0));
			LOGGER.info("{} enrichment responses where applied from a total of {}.",applied,actionsUndertaken.size());
		}
	}

}
