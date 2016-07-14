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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0
 *   Bundle      : ci-jenkins-crawler-0.3.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import java.net.URI;
import java.util.EnumSet;
import java.util.Set;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.crawler.xml.persistence.StorageEntryType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

final class StorageEntry {

	private final URI location;
	private final JenkinsEntityType type;
	private final Set<JenkinsArtifactType> persistedArtifacts;
	private boolean persistEntity;

	StorageEntry(URI location, JenkinsEntityType type) {
		this.location = location;
		this.type = type;
		this.persistedArtifacts=EnumSet.noneOf(JenkinsArtifactType.class);
	}

	URI location() {
		return this.location;
	}

	JenkinsEntityType type() {
		return this.type;
	}

	void persistEntity() {
		this.persistEntity=true;
	}

	boolean isEntityPersisted() {
		return this.persistEntity;
	}

	boolean isArtifactPersisted(JenkinsArtifactType artifact) {
		return this.persistedArtifacts.contains(artifact);
	}

	void persistArtifact(JenkinsArtifactType artifact) {
		this.persistedArtifacts.add(artifact);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.location,this.type);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof StorageEntry) {
			StorageEntry that=(StorageEntry)obj;
			result=
				Objects.equal(this.location, that.location) &&
				Objects.equal(this.type, that.type);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("location",this.location).
					add("type",this.type).
					add("persistEntity",this.persistEntity).
					add("persistedArtifacts",this.persistedArtifacts).
					toString();
	}

	private JenkinsArtifactType[] persistedArtifacts() {
		return this.persistedArtifacts.toArray(new JenkinsArtifactType[this.persistedArtifacts.size()]);
	}

	StorageEntryType toDescriptor() {
		return
			new StorageEntryType().
				withResource(this.location).
				withType(this.type).
				withEntity(this.persistEntity).
				withArtifacts(persistedArtifacts());
	}

	static StorageEntry fromDescriptor(StorageEntryType descriptor) {
		StorageEntry entry=new StorageEntry(descriptor.getResource(),descriptor.getType());
		if(descriptor.isEntity()) {
			entry.persistEntity();
		}
		for(JenkinsArtifactType artifact:descriptor.getArtifacts()) {
			entry.persistArtifact(artifact);
		}
		return entry;
	}

}