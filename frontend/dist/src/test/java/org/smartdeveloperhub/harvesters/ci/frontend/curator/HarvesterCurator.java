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
package org.smartdeveloperhub.harvesters.ci.frontend.curator;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.curator.Notifier;
import org.smartdeveloperhub.curator.connector.UseCase;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

public final class HarvesterCurator {

	private static DeliveryChannel deliveryChannel() {
		return
			ProtocolFactory.
				newDeliveryChannel().
					withRoutingKey("curator.testing").
					build();
	}

	public static void main(final String... args) throws Exception {
		configureLogger();
		final Logger LOGGER=LoggerFactory.getLogger(HarvesterCurator.class);
		final TestingCurator curator=
			TestingCurator.
				builder().
					withResponseProvider(new TestingResponseProvider(LOGGER)).
					withConnectorConfiguration(deliveryChannel()).
					withConversionContext(ConversionContext.
						newInstance().
							withNamespacePrefix(UseCase.CI_NAMESPACE,"ci").
							withNamespacePrefix(UseCase.SCM_NAMESPACE,"scm").
								withNamespacePrefix(UseCase.DOAP_NAMESPACE,"doap")).
					withNotifier(new Notifier()).
					build();
		final Agent agent = ProtocolFactory.newAgent().withAgentId(UUID.randomUUID()).build();
		LOGGER.info("Starting testing curator [{}]...",agent.agentId());
		curator.connect(agent);
		LOGGER.info("Awaiting for enrichment requests...");
		try {
			TimeUnit.SECONDS.sleep(600);
		} catch (final InterruptedException e) {
			LOGGER.warn("Testing curator interrupted");
		}
		LOGGER.info("Terminating testing curator...");
		curator.disconnect();

	}

	private static void configureLogger() {
		System.setProperty("log4j.configuration",Thread.currentThread().getContextClassLoader().getResource("curator.log4j.properties").toString());
	}

}
