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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.EnumSet;
import java.util.Set;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Entity;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Reference;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class CrawlingStrategy {

	private final class DefaultCrawlingDecissionPoint implements CrawlingDecissionPoint {

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
			if(includedJobs.contains(job.getId())) {
				return true;
			} else if(excludedJobs.contains(job.getId())) {
				return false;
			}
			return includedJobs.size()==0;
		}

		@Override
		public boolean canProcessEntityType(JenkinsEntityType entityType, JenkinsInformationPoint jip, CrawlingSession session) {
			if(includedEntityTypes.contains(entityType)) {
				return true;
			} else if(excludedEntityTypes.contains(entityType)) {
				return false;
			}
			return includedEntityTypes.size()==0;
		}

		@Override
		public boolean canProcessArtifactType(JenkinsArtifactType artifactType, JenkinsInformationPoint jip, CrawlingSession session) {
			if(includedArtifactTypes.contains(artifactType)) {
				return true;
			} else if(excludedArtifactTypes.contains(artifactType)) {
				return false;
			}
			return includedArtifactTypes.size()==0;
		}
	}

	public static final class Builder {

		private final Set<String> includedJobs;
		private final Set<String> excludedJobs;

		private final Set<JenkinsEntityType> includedEntityTypes;
		private final Set<JenkinsEntityType> excludedEntityTypes;

		private final Set<JenkinsArtifactType> includedArtifactTypes;
		private final Set<JenkinsArtifactType> excludedArtifactTypes;

		private Builder() {
			this.includedJobs=Sets.newLinkedHashSet();
			this.excludedJobs=Sets.newLinkedHashSet();
			this.includedEntityTypes=EnumSet.allOf(JenkinsEntityType.class);
			this.excludedEntityTypes=EnumSet.noneOf(JenkinsEntityType.class);
			this.includedArtifactTypes=EnumSet.allOf(JenkinsArtifactType.class);
			this.excludedArtifactTypes=EnumSet.noneOf(JenkinsArtifactType.class);
		}

		public Builder includeJob(String name) {
			this.includedJobs.add(name);
			this.excludedJobs.remove(name);
			return this;
		}

		public Builder excludeJob(String name) {
			this.includedJobs.remove(name);
			this.excludedJobs.add(name);
			return this;
		}

		public Builder includeEntityType(JenkinsEntityType type) {
			checkNotNull(type,"Entity type cannot be null");
			this.includedEntityTypes.add(type);
			this.excludedEntityTypes.remove(type);
			return this;
		}

		public Builder excludeEntityType(JenkinsEntityType type) {
			checkNotNull(type,"Entity type cannot be null");
			checkNotNull(!JenkinsEntityType.INSTANCE.equals(type),"Entity type cannot be %s",JenkinsEntityType.INSTANCE);
			this.includedEntityTypes.remove(type);
			this.excludedEntityTypes.add(type);
			return this;
		}

		public Builder includeArtifactType(JenkinsArtifactType type) {
			checkNotNull(type,"Artifact type cannot be null");
			this.includedArtifactTypes.add(type);
			this.excludedArtifactTypes.remove(type);
			return this;
		}

		public Builder excludeArtifactType(JenkinsArtifactType type) {
			checkNotNull(type,"Artifact type cannot be null");
			checkNotNull(!JenkinsArtifactType.RESOURCE.equals(type),"Artifact type cannot be %s",JenkinsArtifactType.RESOURCE);
			this.includedArtifactTypes.remove(type);
			this.excludedArtifactTypes.add(type);
			return this;
		}

		public CrawlingStrategy build() {
			return new CrawlingStrategy(this.includedJobs,this.excludedJobs,this.includedEntityTypes,this.excludedEntityTypes,this.includedArtifactTypes,this.excludedArtifactTypes);
		}

	}

	private final Set<String> includedJobs;
	private final Set<String> excludedJobs;

	private final Set<JenkinsEntityType> includedEntityTypes;
	private final Set<JenkinsEntityType> excludedEntityTypes;

	private final Set<JenkinsArtifactType> includedArtifactTypes;
	private final Set<JenkinsArtifactType> excludedArtifactTypes;

	private CrawlingStrategy(Set<String> includedJobs, Set<String> excludedJobs, Set<JenkinsEntityType> includedEntityTypes, Set<JenkinsEntityType> excludedEntityTypes, Set<JenkinsArtifactType> includedArtifactTypes, Set<JenkinsArtifactType> excludedArtifactTypes) {
		this.includedJobs=ImmutableSet.copyOf(includedJobs);
		this.excludedJobs=ImmutableSet.copyOf(excludedJobs);
		this.includedEntityTypes=ImmutableSet.copyOf(includedEntityTypes);
		this.excludedEntityTypes=ImmutableSet.copyOf(excludedEntityTypes);
		this.includedArtifactTypes=ImmutableSet.copyOf(includedArtifactTypes);
		this.excludedArtifactTypes=ImmutableSet.copyOf(excludedArtifactTypes);
	}

	CrawlingDecissionPoint decissionPoint() {
		return new DefaultCrawlingDecissionPoint();
	}

	public static Builder builder() {
		return new Builder();
	}

}
