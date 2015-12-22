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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.2.0
 *   Bundle      : ci-frontend-dist-0.2.0.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.smartdeveloperhub.harvesters.ci.frontend.core.QueryHelper.ResultProcessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.query.QuerySolution;
import com.jayway.restassured.response.Response;

@RunWith(Arquillian.class)
public class HarvesterApplicationTest extends SmokeTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterApplicationTest.class);

	private static final String SIMPLE_BUILD    = "service/builds/1/";
	private static final String COMPOSITE_BUILD = "service/builds/2/";
	private static final String SUB_BUILD       = "service/builds/2/child/1/";

	@Deployment(name="testing",testable=false)
	@TargetsContainer("tomcat")
	public static WebArchive createDeployment() throws Exception {
		return SmokeTest.createWebArchive("testing-harvester.war");
	}

	@Test
	@OperateOnDeployment("testing")
	public void testService(@ArquillianResource final URL contextURL) throws Exception {
		final List<String> builds = getServiceBuilds(contextURL);
		assertThat(builds,hasSize(2));
		assertThat(builds,
			hasItems(
				TestingUtil.resolve(contextURL,SIMPLE_BUILD),
				TestingUtil.resolve(contextURL,COMPOSITE_BUILD)));
	}

	@Test
	@OperateOnDeployment("testing")
	public void testSimpleBuild(@ArquillianResource final URL contextURL) throws Exception {
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
	@OperateOnDeployment("testing")
	public void testCompositeBuild(@ArquillianResource final URL contextURL) throws Exception {
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
	@OperateOnDeployment("testing")
	public void testSubBuild(@ArquillianResource final URL contextURL) throws Exception {
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

	public void testExecutions(final String base) throws Exception {
		for(int i=1;i<7;i++) {
			LOGGER.info("Trying {}executions/{}/",base,i);
			final String path = "executions/"+i+"/";
			final Response response =
				given().
					accept(TEXT_TURTLE).
					baseUri(base).
				expect().
					statusCode(OK).
					contentType(TEXT_TURTLE).
				when().
					get(path);
			final String body = response.getBody().asString();
			LOGGER.trace(body);
			final List<Map<String, String>> enrichments = getEnrichments(response, base, path);
			assertThat(enrichments,hasSize(1));
			LOGGER.debug("Found: \n{}",toString(base+path,enrichments));
		}
	}

	private List<Map<String, String>> getEnrichments(final Response response, final String base, final String path) throws IOException {
		return
			QueryHelper.
				newInstance().
					withModel(
						TestingUtil.
							asModel(response,new URL(base),path)).
					withQuery().
						fromResource("queries/execution_scm.sparql").
					select(
						new ResultProcessor<List<Map<String,String>>>() {
							private final List<Map<String,String>> results=Lists.newArrayList();
							@Override
							protected void processSolution() {
								final QuerySolution solution = solution();
								final Iterator<String> names=solution.varNames();
								final Map<String,String> item=Maps.newLinkedHashMap();
								for(;names.hasNext();) {
									final String name = names.next();
									item.put(name,solution.get(name).asNode().toString(false));
								}
								this.results.add(item);
							}
							@Override
							public List<Map<String,String>> getResult() {
								return ImmutableList.copyOf(this.results);
							}
						}
					);
	}

	private String toString(final String executionId,final List<Map<String, String>> builds) {
		final StringBuilder builder=new StringBuilder();
		builder.append(executionId).append(" {").append(System.lineSeparator());
		for(final Map<String,String> item:builds) {
			builder.append("  ").append("- ExecutionEnrichment {").append(System.lineSeparator());
			for(final Entry<String,String> entry:item.entrySet()) {
				builder.append("      + ").append(entry.getKey()).append(" : ").append(entry.getValue()).append(System.lineSeparator());
			}
			builder.append("    }").append(System.lineSeparator());
		}
		builder.append("}").append(System.lineSeparator());
		return builder.toString();
	}

}
