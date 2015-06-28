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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

public final class CrawlingStrategy {

	private static final class DefaultCrawlingDecissionPoint implements CrawlingDecissionPoint {

		@Override
		public boolean canProcessRun(Run run, JenkinsInformationPoint jip, CrawlingSession session) {
			return true;
		}

		@Override
		public boolean canProcessReference(Entity entity, Reference reference, JenkinsInformationPoint jip, CrawlingSession session) {
			return true;
		}

		@Override
		public boolean canProcessJob(Job job, JenkinsInformationPoint jip, CrawlingSession session) {
			return true;
		}

		@Override
		public boolean canProcessEntityType(JenkinsEntityType entityType, JenkinsInformationPoint jip, CrawlingSession session) {
			return true;
		}

		@Override
		public boolean canProcessArtifactType(JenkinsArtifactType artifactType, JenkinsInformationPoint jip, CrawlingSession session) {
			return true;
		}
	}

	CrawlingDecissionPoint decissionPoint() {
		return new DefaultCrawlingDecissionPoint();
	}

}
