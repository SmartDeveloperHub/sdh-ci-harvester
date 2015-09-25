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
package org.smartdeveloperhub.harvesters.ci.frontend.core.util;

import java.util.concurrent.atomic.AtomicReference;

import org.ldp4j.application.data.FormatUtils;
import org.ldp4j.application.data.Individual;
import org.ldp4j.application.data.Literal;
import org.ldp4j.application.data.ManagedIndividualId;
import org.ldp4j.application.data.Value;
import org.ldp4j.application.data.ValueVisitor;

public final class FormatUtil {

	private FormatUtil() {
	}

	public static String toString(ManagedIndividualId id) {
		return String.format("%s {Managed by: %s}",id.name(),id.managerId());
	}

	public static String toString(Value value) {
		final AtomicReference<String> result=new AtomicReference<String>();
		value.accept(
			new ValueVisitor() {
				@Override
				public void visitLiteral(Literal<?> value) {
					result.set(String.format("%s [%s]",value.get(),value.get().getClass().getCanonicalName()));
				}
				@Override
				public void visitIndividual(Individual<?, ?> value) {
					result.set(FormatUtils.formatId(value));
				}
			}
		);
		return result.get();
	}

}
