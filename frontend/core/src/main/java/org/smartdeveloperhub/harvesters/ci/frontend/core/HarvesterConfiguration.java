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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;

import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.Namespaces;
import org.ldp4j.application.util.ImmutableNamespaces;

public final class HarvesterConfiguration extends Configuration {

	private static final String CI_HARVESTER_PROVIDER = "ci.harvester.provider";
	private static final String CI_HARVESTER_TARGET = "ci.harvester.target";

	@Override
	public Namespaces namespaces() {
		return
			new ImmutableNamespaces().
				withPrefix("ci", "http://www.smartdeveloperhub.org/vocabulary/ci#").
				withPrefix("scm", "http://www.smartdeveloperhub.org/vocabulary/scm#").
				withPrefix("platform", "http://www.smartdeveloperhub.org/vocabulary/platform#").
				withPrefix("oslc_auto", "http://open-services.net/ns/auto#").
				withPrefix("dctype", "http://purl.org/dc/dcmitype/").
				withPrefix("dcterms", "http://purl.org/dc/terms/");
	}

	public String provider() {
		return System.getProperty(CI_HARVESTER_PROVIDER);
	}

	public URI target() {
		String target = System.getProperty(CI_HARVESTER_TARGET);
		if(target==null) {
			target="http://ci.jenkins-ci.org/";
		}
		return URI.create(target);
	}

}
