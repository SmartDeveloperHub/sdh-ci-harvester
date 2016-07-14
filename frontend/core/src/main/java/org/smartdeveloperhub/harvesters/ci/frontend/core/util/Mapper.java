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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.4.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.util;

import java.net.URI;
import java.util.Set;

import org.ldp4j.application.data.DataSetUtils;
import org.ldp4j.application.data.ExternalIndividual;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.IndividualHelper;

import com.google.common.base.Optional;

public final class Mapper {

	private final IndividualHelper helper;

	private Mapper(final IndividualHelper helper) {
		this.helper = helper;
	}

	private Mapper(final Individual<?,?> individual) {
		this(DataSetUtils.newHelper(individual));
	}

	public Set<URI> types() {
		return this.helper.types();
	}

	public <T> T literal(final String propertyURI, final Class<? extends T> aClazz) {
		return
			this.helper.
				property(propertyURI).
					firstValue(aClazz);
	}

	public Optional<URI> individual(final String propertyURI) {
		return
			Optional.
				fromNullable(
					this.helper.
						property(propertyURI).
							firstIndividual(ExternalIndividual.class));
	}

	public Mapper individualMapper(final String propertyURI) {
		return new Mapper(this.helper.property(propertyURI).firstIndividual());
	}

	public static String toStringOrNull(final Optional<URI> individual) {
		return individual.isPresent()?individual.get().toString():null;
	}

	public static Mapper create(final Individual<?,?> individual) {
		return new Mapper(individual);
	}

}