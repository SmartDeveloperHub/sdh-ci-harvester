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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.execution;

import org.smartdeveloperhub.harvesters.ci.backend.Result;

import com.google.common.base.MoreObjects;

final class ResultMapping {

	private final String verdict;
	private final String state;
	private final String resultType;

	private ResultMapping(String state, String verdict, String resultType) {
		this.state = state;
		this.verdict = verdict;
		this.resultType = resultType;
	}

	String verdict() {
		return this.verdict;
	}

	String state() {
		return this.state;
	}

	String resultType() {
		return this.resultType;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("state",this.state).
					add("verdict",this.verdict).
					add("resultType",this.resultType).
					toString();
	}

	static ResultMapping newInstance(Result result) {
		String state=null;
		String verdict=null;
		String resultType=null;
		switch(result.status()) {
			case UNAVAILABLE:
				state=ExecutionVocabulary.STATE_IN_PROGRESS;
				verdict=ExecutionVocabulary.VERDICT_UNAVAILABLE;
				resultType=ExecutionVocabulary.CI_UNAVAILABLE_EXECUTION_RESULT;
				break;
			case ABORTED:
				state=ExecutionVocabulary.STATE_CANCELED;
				verdict=ExecutionVocabulary.VERDICT_UNAVAILABLE;
				resultType=ExecutionVocabulary.CI_UNAVAILABLE_EXECUTION_RESULT;
				break;
			case PASSED:
				state=ExecutionVocabulary.STATE_COMPLETE;
				verdict=ExecutionVocabulary.VERDICT_PASSED;
				resultType=ExecutionVocabulary.CI_AVAILABLE_EXECUTION_RESULT;
				break;
			case FAILED:
				state=ExecutionVocabulary.STATE_COMPLETE;
				verdict=ExecutionVocabulary.VERDICT_FAILED;
				resultType=ExecutionVocabulary.CI_AVAILABLE_EXECUTION_RESULT;
				break;
			case WARNING:
				state=ExecutionVocabulary.STATE_COMPLETE;
				verdict=ExecutionVocabulary.VERDICT_WARNING;
				resultType=ExecutionVocabulary.CI_AVAILABLE_EXECUTION_RESULT;
				break;
			case NOT_BUILT:
				state=ExecutionVocabulary.STATE_COMPLETE;
				verdict=ExecutionVocabulary.VERDICT_ERROR;
				resultType=ExecutionVocabulary.CI_AVAILABLE_EXECUTION_RESULT;
				break;
			default:
				throw new AssertionError("Unknown result status "+result.status());
		}
		return new ResultMapping(state,verdict,resultType);
	}

}