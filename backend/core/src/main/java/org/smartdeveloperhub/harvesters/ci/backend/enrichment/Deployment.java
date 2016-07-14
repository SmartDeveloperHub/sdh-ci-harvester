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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.4.0-SNAPSHOT
 *   Bundle      : ci-backend-core-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.enrichment;

import java.net.URI;

import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory;
import org.smartdeveloperhub.curator.connector.protocol.ProtocolFactory.BrokerBuilder;
import org.smartdeveloperhub.curator.connector.protocol.ValidationException;
import org.smartdeveloperhub.curator.protocol.Broker;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentConfig;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public final class Deployment {

	public static final class Builder {

		private final BrokerBuilder brokerBuilder;

		private URI base;

		private Builder() {
			this.brokerBuilder=ProtocolFactory.newBroker();
		}

		public Builder withBase(final URI canonicalBase) {
			this.base=canonicalBase;
			return this;
		}

		public Builder withBrokerHost(final String host) {
			this.brokerBuilder.withHost(host);
			return this;
		}

		public Builder withBrokerPort(final int port) {
			this.brokerBuilder.withPort(port);
			return this;
		}

		public Deployment build() {
			try {
				return new Deployment(this.base!=null?this.base:DEFAULT_BASE,this.brokerBuilder.build());
			} catch (final ValidationException e) {
				throw new IllegalStateException("Could not create broker",e);
			}
		}

	}

	private static final URI DEFAULT_BASE = URI.create("http://locahost/harvester/");

	private final Broker broker;
	private final URI base;

	private Deployment(final URI base, final Broker broker) {
		this.base = base;
		this.broker = broker;
	}

	public Broker broker() {
		return this.broker;
	}

	public URI base() {
		return this.base;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("base",this.base).
					add("broker",this.broker).
					toString();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Deployment fromConfiguration(final EnrichmentConfig enrichment) {
		return
			builder().
				withBase(URI.create(Optional.fromNullable(enrichment.getBase()).or(DEFAULT_BASE.toString()))).
				withBrokerHost(enrichment.getBroker().getHost()).
				withBrokerPort(enrichment.getBroker().getPort()).
				build();
	}

}
