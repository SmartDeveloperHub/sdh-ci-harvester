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


abstract class ExecutionVocabulary {

	static final String TYPE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";

	static final String DC_TERMS_TITLE       = "http://purl.org/dc/terms/title";
	static final String DC_TERMS_CREATED     = "http://purl.org/dc/terms/created";
	static final String DC_TERMS_IDENTIFIER  = "http://purl.org/dc/terms/identifier";
	static final String DC_TERMS_DESCRIPTION = "http://purl.org/dc/terms/description";

	static final String AUTO_AUTOMATION_REQUEST        = "http://open-services.net/ns/auto#AutomationRequest";
	static final String AUTOMATION_RESULT              = "http://open-services.net/ns/auto#AutomationResult";
	static final String EXECUTES_AUTOMATION_PLAN       = "http://open-services.net/ns/auto#executesAutomationPlan";
	static final String PRODUCED_BY_AUTOMATION_REQUEST = "http://open-services.net/ns/auto#producedByAutomationRequest";
	static final String REPORTS_ON_AUTOMATION_PLAN     = "http://open-services.net/ns/auto#reportsOnAutomationPlan";
	static final String STATE                          = "http://open-services.net/ns/auto#state";
	static final String STATE_IN_PROGRESS              = "http://open-services.net/ns/auto#inProgress";
	static final String STATE_CANCELED                 = "http://open-services.net/ns/auto#canceled";
	static final String STATE_COMPLETE                 = "http://open-services.net/ns/auto#complete";
	static final String VERDICT                        = "http://open-services.net/ns/auto#verdict";
	static final String VERDICT_UNAVAILABLE            = "http://open-services.net/ns/auto#unavailable";
	static final String VERDICT_PASSED                 = "http://open-services.net/ns/auto#passed";
	static final String VERDICT_FAILED                 = "http://open-services.net/ns/auto#failed";
	static final String VERDICT_WARNING                = "http://open-services.net/ns/auto#warning";
	static final String VERDICT_ERROR                  = "http://open-services.net/ns/auto#error";

	static final String CI_EXECUTION                    = "http://www.smartdeveloperhub.org/vocabulary/ci#Execution";
	static final String CI_RUNNING_EXECUTION            = "http://www.smartdeveloperhub.org/vocabulary/ci#RunningExecution";
	static final String CI_FINISHED_EXECUTION           = "http://www.smartdeveloperhub.org/vocabulary/ci#FinishedExecution";
	static final String CI_EXECUTION_RESULT             = "http://www.smartdeveloperhub.org/vocabulary/ci#ExecutionResult";
	static final String CI_AVAILABLE_EXECUTION_RESULT   = "http://www.smartdeveloperhub.org/vocabulary/ci#AvailableExecutionResult";
	static final String CI_UNAVAILABLE_EXECUTION_RESULT = "http://www.smartdeveloperhub.org/vocabulary/ci#UnavailableExecutionResult";
	static final String CI_LOCATION                     = "http://www.smartdeveloperhub.org/vocabulary/ci#location";
	static final String CI_FINISHED                     = "http://www.smartdeveloperhub.org/vocabulary/ci#finished";
	static final String CI_HAS_RESULT                   = "http://www.smartdeveloperhub.org/vocabulary/ci#hasResult";

}
