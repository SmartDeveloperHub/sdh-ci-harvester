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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.3.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;

import org.ldp4j.application.ext.Configuration;
import org.ldp4j.application.ext.Namespaces;
import org.ldp4j.application.util.ImmutableNamespaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HarvesterConfiguration extends Configuration {

	private static final Logger LOGGER=LoggerFactory.getLogger(HarvesterConfiguration.class);

	private static final String CI_HARVESTER_CONFIG_PATH = "ci.harvester.config";
	private static final String CI_HARVESTER_PROVIDER    = "ci.harvester.provider";
	private static final String CI_HARVESTER_TARGET      = "ci.harvester.target";
	private static final String CI_HARVESTER_BASE        = "ci.harvester.base";
	private static final String CI_BROKER_PORT           = "ci.broker.port";
	private static final String CI_BROKER_HOST           = "ci.broker.host";

	private final String provider;

	private final URI target;

	private final String databaseConfigPath;

	private final String canonicalBase;

	private final String brokerHost;

	private final int brokerPort;

	public HarvesterConfiguration() {
		this.provider = System.getProperty(CI_HARVESTER_PROVIDER);
		this.target = URI.create(System.getProperty(CI_HARVESTER_TARGET,"https://ci.jenkins-ci.org/"));
		this.databaseConfigPath = System.getProperty(CI_HARVESTER_CONFIG_PATH);
		this.canonicalBase = System.getProperty(CI_HARVESTER_BASE, "http://localhost/harvester/");
		this.brokerHost = System.getProperty(CI_BROKER_HOST,"localhost");
		this.brokerPort=getBrokerPort();
	}

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
		return this.provider;
	}

	public URI target() {
		return this.target;
	}

	public String databaseConfigPath() {
		return this.databaseConfigPath;
	}

	public String canonicalBase() {
		return this.canonicalBase;
	}

	public String brokerHost() {
		return this.brokerHost;
	}

	public int brokerPort() {
		return this.brokerPort;
	}

	private static int getBrokerPort() {
		final String rawBrokerPort = System.getProperty(CI_BROKER_PORT,"5672");
		int port=5672;
		try {
			port=Integer.parseInt(rawBrokerPort);
		} catch (final NumberFormatException e) {
			LOGGER.error("{} is not a valid broker port. Using default port.",rawBrokerPort,e);
		}
		return port;
	}

}
