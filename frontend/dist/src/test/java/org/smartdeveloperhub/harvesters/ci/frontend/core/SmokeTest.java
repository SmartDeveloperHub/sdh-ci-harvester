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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.1.0
 *   Bundle      : ci-frontend-dist-0.1.0.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import static com.jayway.restassured.RestAssured.given;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.frontend.core.QueryHelper.ResultProcessor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jayway.restassured.response.Response;

public class SmokeTest {

	private static final Logger LOGGER=LoggerFactory.getLogger(SmokeTest.class);

	protected static final String TEXT_TURTLE = "text/turtle";
	protected static final int    OK          = 200;

	protected static final String SERVICE     = "service/";

	protected static WebArchive createWebArchive(String archiveName) throws Exception {
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
					create(WebArchive.class,archiveName).
						addAsLibraries(files).
						addAsResource("ldp4j-server-frontend.cfg").
						addAsResource("log4j.properties").
						setWebXML(new File("src/main/webapp/WEB-INF/web.xml"));
		} catch (Exception e) {
			LOGGER.error("Could not create archive",e);
			throw e;
		}
	}

	protected final List<String> getServiceBuilds(URL contextURL) throws IOException {
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
		return builds;
	}


}
