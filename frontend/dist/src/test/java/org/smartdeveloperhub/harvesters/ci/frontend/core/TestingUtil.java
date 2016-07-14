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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.jayway.restassured.response.Response;

final class TestingUtil {

	static String loadResource(String resourceName) {
		try {
			InputStream resourceAsStream =
				Thread.
					currentThread().
						getContextClassLoader().
							getResourceAsStream(resourceName);
			if(resourceAsStream==null) {
				throw new AssertionError("Could not find resource '"+resourceName+"'");
			}
			return IOUtils.toString(resourceAsStream, Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new AssertionError("Could not load resource '"+resourceName+"'");
		}
	}

	static String resolve(URL base, String path) {
		return base.toString()+path;
	}

	static Model asModel(Response response, URL base, String path) {
		String language="TURTLE";
		return
			ModelFactory.
				createDefaultModel().
					read(
						new StringReader(response.asString()),
						resolve(base,path),
						language);
	}

	static String interpolate(String input, String parameter, String value) {
		return input.replaceAll("\\$\\{"+parameter+"\\}", value);
	}

}
