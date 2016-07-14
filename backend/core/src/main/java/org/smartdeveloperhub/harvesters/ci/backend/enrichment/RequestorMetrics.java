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

import java.util.Arrays;

import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentRequestor.RequestJob;

final class RequestorMetrics {

	private static final int CREATED_JOBS  =0;
	private static final int PENDING_JOBS  =1;
	private static final int PROCESSED_JOBS=2;
	private static final int FAILED_JOBS   =3;
	private static final int COMPLETED_JOBS=4;

	private final Counter[] metrics=new Counter[5];

	RequestorMetrics() {
		this.metrics[CREATED_JOBS]  =new Counter("createdJobs");
		this.metrics[PENDING_JOBS]  =new Counter("pendingJobs");
		this.metrics[PROCESSED_JOBS]=new Counter("processedJobs");
		this.metrics[FAILED_JOBS]   =new Counter("failedJobs");
		this.metrics[COMPLETED_JOBS]=new Counter("completedJobs");
	}

	synchronized long createJob(final EnrichmentContext context) {
		this.metrics[PENDING_JOBS].incrementAndGet();
		return this.metrics[CREATED_JOBS].incrementAndGet();
	}

	synchronized void jobProcessed(final RequestJob job, final boolean completed) {
		this.metrics[PENDING_JOBS].decrementAndGet();
		this.metrics[PROCESSED_JOBS].incrementAndGet();
		if(completed) {
			this.metrics[COMPLETED_JOBS].incrementAndGet();
		} else {
			this.metrics[FAILED_JOBS].incrementAndGet();
		}
	}

	synchronized long pendingJobs() {
		return this.metrics[PENDING_JOBS].get();
	}

	synchronized long processedJobs() {
		return this.metrics[PROCESSED_JOBS].get();
	}

	synchronized long completedJobs() {
		return this.metrics[COMPLETED_JOBS].get();
	}

	synchronized long failedJobs() {
		return this.metrics[FAILED_JOBS].get();
	}

	@Override
	public synchronized String toString() {
		return Arrays.toString(this.metrics);
	}

}