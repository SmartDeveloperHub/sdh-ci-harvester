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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.4.0-SNAPSHOT
 *   Bundle      : ci-jenkins-client-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.JenkinsResource.Metadata.Filter;
import org.smartdeveloperhub.jenkins.Status;
import org.w3c.dom.Document;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class InMemoryJenkinsResource implements JenkinsResource {

	private final InMemoryMetadata metadata;

	private Document document;
	private JenkinsEntityType entity;
	private Status status;
	private RuntimeException failure;
	private URI location;

	private JenkinsArtifactType artifact;

	InMemoryJenkinsResource(final Date retrievedOn) {
		this.metadata = new InMemoryMetadata(retrievedOn);
	}

	InMemoryJenkinsResource withFilters(final List<Filter> filters) {
		this.metadata.withFilters(filters);
		return this;
	}

	private InMemoryJenkinsResource withStatus(final Status status, final Throwable failure) {
		this.status = status;
		if(failure instanceof RuntimeException) {
			this.failure = (RuntimeException)failure;
		} else if(failure!=null){
			this.failure = new RuntimeException(failure);
		} else {
			this.failure=null;
		}
		return this;
	}

	InMemoryJenkinsResource withLocation(final URI location) {
		this.location = location;
		return this;
	}

	InMemoryJenkinsResource withEntity(final JenkinsEntityType entity) {
		this.entity = entity;
		return this;
	}

	InMemoryJenkinsResource withArtifact(final JenkinsArtifactType artifact) {
		this.artifact = artifact;
		return this;
	}

	InMemoryJenkinsResource withStatus(final Status status) {
		return withStatus(status,null);
	}

	InMemoryJenkinsResource withStatus(final Status status, final String message, final Object... args) {
		return withStatus(status,new RuntimeException(String.format(message,args)));
	}

	InMemoryJenkinsResource withStatus(final Status status, final Throwable cause, final String message, final Object... args) {
		return withStatus(status,new RuntimeException(String.format(message,args),cause));
	}

	InMemoryJenkinsResource withContent(final Document document) {
		this.document=document;
		return this;
	}

	@Override
	public URI location() {
		return this.location;
	}

	public URI effectiveLocation() {
		URI targetLocation = this.artifact.locate(this.location);
		if(JenkinsArtifactType.RESOURCE.equals(this.artifact)) {
			targetLocation=targetLocation.resolve(applyFilters(this.metadata.filters()));
		}
		return targetLocation;
	}

	@Override
	public JenkinsEntityType entity() {
		return this.entity;
	}

	@Override
	public JenkinsArtifactType artifact() {
		return this.artifact;
	}

	@Override
	public InMemoryMetadata metadata() {
		return this.metadata;
	}

	@Override
	public Status status() {
		return this.status;
	}

	@Override
	public Optional<Document> content() {
		return Optional.fromNullable(this.document);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("location",this.location).
					add("entity",this.entity).
					add("artifact",this.artifact).
					add("status",this.status).
					add("failure",this.failure).
					add("metadata",this.metadata).
					toString();
	}

	@Override
	public Optional<Throwable> failure() {
		return Optional.<Throwable>fromNullable(this.failure);
	}

	private String applyFilters(final List<Filter> filters) {
		if(filters.isEmpty()) {
			return "xml";
		}
		final StringBuilder builder=new StringBuilder();
		builder.append("xml?");
		for(final Iterator<Filter> it=filters.iterator();it.hasNext();) {
			builder.append(it.next().expression());
			if(it.hasNext()) {
				builder.append("&");
			}
		}
		return builder.toString();
	}

}