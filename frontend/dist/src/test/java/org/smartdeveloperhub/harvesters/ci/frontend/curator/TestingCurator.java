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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-dist:0.3.0
 *   Bundle      : ci-frontend-dist-0.3.0.war
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.curator;

import org.smartdeveloperhub.curator.Notifier;
import org.smartdeveloperhub.curator.connector.CuratorConfiguration;
import org.smartdeveloperhub.curator.connector.ResponseProvider;
import org.smartdeveloperhub.curator.connector.SimpleCurator;
import org.smartdeveloperhub.curator.connector.io.ConversionContext;
import org.smartdeveloperhub.curator.protocol.Agent;
import org.smartdeveloperhub.curator.protocol.DeliveryChannel;

import com.google.common.base.Optional;

public final class TestingCurator {

	public static final class Builder {

		private CuratorConfiguration curatorConfiguration;
		private ConversionContext conversionContext;
		private Notifier notifier;
		private DeliveryChannel connectorConfiguration;
		private ResponseProvider provider;

		public TestingCurator.Builder withCuratorConfiguration(final CuratorConfiguration curatorConfiguration) {
			this.curatorConfiguration = curatorConfiguration;
			return this;
		}

		public TestingCurator.Builder withConversionContext(final ConversionContext conversionContext) {
			this.conversionContext = conversionContext;
			return this;
		}

		public TestingCurator.Builder withNotifier(final Notifier notifier) {
			this.notifier = notifier;
			return this;
		}

		public TestingCurator.Builder withConnectorConfiguration(final DeliveryChannel connectorConfiguration) {
			this.connectorConfiguration = connectorConfiguration;
			return this;
		}

		public TestingCurator.Builder withResponseProvider(final ResponseProvider provider) {
			this.provider=provider;
			return this;
		}

		private CuratorConfiguration curatorConfiguration() {
			return Optional.fromNullable(this.curatorConfiguration).or(CuratorConfiguration.newInstance());
		}

		private ConversionContext conversionContext() {
			return Optional.fromNullable(this.conversionContext).or(ConversionContext.newInstance());
		}

		public TestingCurator build() {
			return new TestingCurator(this.connectorConfiguration,curatorConfiguration(),this.notifier,conversionContext(),this.provider);
		}

	}

	private final SimpleCurator delegate;

	private TestingCurator(final DeliveryChannel connector, final CuratorConfiguration curatorConfiguration, final Notifier notifier, final ConversionContext context, final ResponseProvider responseProvider) {
		this.delegate=new SimpleCurator(connector,curatorConfiguration,notifier,responseProvider,context);
	}

	public void connect(final Agent agent) {
		try {
			this.delegate.connect(agent);
		} catch (final Exception e) {
			throw new IllegalStateException("Could not connect curator",e);
		}
	}

	public void disconnect() {
		try {
			this.delegate.disconnect();
		} catch (final Exception e) {
			throw new IllegalStateException("Could not disconnect curator",e);
		}
	}

	public static TestingCurator.Builder builder() {
		return new Builder();
	}

}