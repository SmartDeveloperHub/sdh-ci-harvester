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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-dist-1.0.0-SNAPSHOT.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.frontend.core.QueryHelper.ResultProcessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;

@RunWith(Arquillian.class)
@RunAsClient
public class HarvesterApplicationITest {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterApplicationITest.class);

	private static final String TEXT_TURTLE = "text/turtle";
	private static final int    OK          = 200;

	private static final String SERVICE         = "service/";
	private static final String SIMPLE_BUILD    = "service/builds/1/";
	private static final String COMPOSITE_BUILD = "service/builds/2/";
	private static final String SUB_BUILD       = "service/builds/2/child/1/";

	@Deployment(testable=false)
	public static WebArchive createDeployment() {
		try {
			File[] files =
				Maven.
					configureResolver().
						loadPomFromFile("src/test/resources/pom.xml").
						importCompileAndRuntimeDependencies().
						resolve().
						withTransitivity().
						asFile();
			return
				ShrinkWrap.
					create(WebArchive.class,"harvester.war").
						addAsLibraries(files).
						addAsResource("ldp4j-server-frontend.cfg").
						addAsResource("log4j.properties").
						setWebXML(new File("src/main/webapp/WEB-INF/web.xml"));
		} catch (Exception e) {
			LOGGER.error("Could not create archive",e);
			throw e;
		}
	}

	@Test
	public void testService(@ArquillianResource URL contextURL) throws Exception {
		Response response=
			given().
				accept(TEXT_TURTLE).
				baseUri(contextURL.toString()).
			expect().
				statusCode(OK).
				contentType(TEXT_TURTLE).
			when().
				get(SERVICE);

		List<String> builds=
			QueryHelper.
				newInstance().
					withModel(
						TestingUtil.
							asModel(response,contextURL,SERVICE)).
					withQuery().
						fromResource("queries/service_builds.sparql").
						withURIRefParam("service",TestingUtil.resolve(contextURL,SERVICE)).
					select(
						new ResultProcessor<List<String>>() {
							private List<String> builds=Lists.newArrayList();
							@Override
							protected void processSolution() {
								this.builds.add(resource("build").getURI());
							}
							@Override
							public List<String> getResult() {
								return ImmutableList.copyOf(this.builds);
							}
						}
					);

		assertThat(builds,hasSize(2));
		assertThat(builds,
			hasItems(
				TestingUtil.resolve(contextURL,SIMPLE_BUILD),
				TestingUtil.resolve(contextURL,COMPOSITE_BUILD)));
	}

	@Test
	public void testSimpleBuild(@ArquillianResource URL contextURL) throws Exception {
		given().
			accept(TEXT_TURTLE).
			baseUri(contextURL.toString()).
		expect().
			statusCode(OK).
			contentType(TEXT_TURTLE).
		when().
			get(SIMPLE_BUILD);
		testExecutions(TestingUtil.resolve(contextURL,SIMPLE_BUILD));
	}

	@Test
	public void testCompositeBuild(@ArquillianResource URL contextURL) throws Exception {
		given().
			accept(TEXT_TURTLE).
			baseUri(contextURL.toString()).
		expect().
			statusCode(OK).
			contentType(TEXT_TURTLE).
		when().
			get(COMPOSITE_BUILD);
		testExecutions(TestingUtil.resolve(contextURL,COMPOSITE_BUILD));
	}

	@Test
	public void testSubBuild(@ArquillianResource URL contextURL) throws Exception {
		given().
			accept(TEXT_TURTLE).
			baseUri(contextURL.toString()).
		expect().
			statusCode(OK).
			contentType(TEXT_TURTLE).
		when().
			get(SUB_BUILD);
		testExecutions(TestingUtil.resolve(contextURL,SUB_BUILD));
	}

	public void testExecutions(String base) throws Exception {
		for(int i=1;i<7;i++) {
			LOGGER.info("Trying {}executions/{}/",base,i);
			given().
				accept(TEXT_TURTLE).
				baseUri(base).
			expect().
				statusCode(OK).
				contentType(TEXT_TURTLE).
			when().
				get("executions/"+i+"/");
		}
	}

}
