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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.util;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;

import com.google.common.collect.Lists;

public final class TaskUtils {

	private TaskUtils() {
	}

	public static List<URI> extractResources(List<Reference> references) {
		List<URI> resources=Lists.newArrayList();
		for(Reference ref:references) {
			resources.add(ref.getValue());
		}
		return resources;
	}

	public static class ReferenceDifference {

		private final List<URI> deletedResources;
		private final List<URI> createdResources;
		private final List<URI> maintainedResources;

		private ReferenceDifference(List<Reference> references, List<Reference> currentReferences) {
			List<URI> resources = TaskUtils.extractResources(references);
			List<URI> currentResources = TaskUtils.extractResources(currentReferences);
			this.deletedResources = Lists.newArrayList(resources);
			this.deletedResources.removeAll(currentResources);
			this.createdResources = Lists.newArrayList(currentResources);
			this.createdResources.removeAll(resources);
			this.maintainedResources = Lists.newArrayList(currentResources);
			this.maintainedResources.removeAll(this.createdResources);
		}

		public List<URI> deleted() {
			return Collections.unmodifiableList(this.deletedResources);
		}

		public List<URI> created() {
			return Collections.unmodifiableList(this.createdResources);
		}

		public List<URI> maintained() {
			return Collections.unmodifiableList(this.maintainedResources);
		}

	}

	public static ReferenceDifference calculate(List<Reference> oldReferences, List<Reference> newReferences) {
		return new ReferenceDifference(oldReferences,newReferences);
	}


}
