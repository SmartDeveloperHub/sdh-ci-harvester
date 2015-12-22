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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.2.0
 *   Bundle      : ci-jenkins-api-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

final class RangeFactory {

	private RangeFactory() {
	}

	static Range equalTo(final int value) {
		return new Range() {
			private static final long serialVersionUID = -966084429887367866L;
			@Override
			public boolean contains(int candidate) {
				return value==candidate;
			}
		};
	}

	static Range greaterThan(final int value) {
		return new Range() {
			private static final long serialVersionUID = 7322706219195976509L;
			@Override
			public boolean contains(int candidate) {
				return candidate>value;
			}
		};
	}

	static Range greaterOrEqualThan(final int value) {
		return new Range() {
			private static final long serialVersionUID = 6766720239232784840L;
			@Override
			public boolean contains(int candidate) {
				return candidate>=value;
			}
		};
	}

	static Range lowerThan(final int value) {
		return new Range() {
			private static final long serialVersionUID = 2179357896204605497L;
			@Override
			public boolean contains(int candidate) {
				return candidate<value;
			}
		};
	}

	static Range lowerOrEqualThan(final int value) {
		return new Range() {
			private static final long serialVersionUID = 8094478819732773588L;
			@Override
			public boolean contains(int candidate) {
				return candidate<=value;
			}
		};
	}

	static Range not(final Range range) {
		return new Range() {
			private static final long serialVersionUID = -2499220525374469940L;
			@Override
			public boolean contains(int candidate) {
				return !range.contains(candidate);
			}
		};
	}

	static Range and(final Range r1, final Range r2) {
		return new Range() {
			private static final long serialVersionUID = 2000177500725768440L;
			@Override
			public boolean contains(int candidate) {
				return r1.contains(candidate) && r2.contains(candidate);
			}
		};
	}

	static Range or(final Range r1, final Range r2) {
		return new Range() {
			private static final long serialVersionUID = -7518747584459506831L;
			@Override
			public boolean contains(int candidate) {
				return r1.contains(candidate) || r2.contains(candidate);
			}
		};
	}

	static Range empty() {
		return new Range() {
			private static final long serialVersionUID = -4665007847244997659L;
			@Override
			public boolean contains(int candidate) {
				return false;
			}
		};
	}

}