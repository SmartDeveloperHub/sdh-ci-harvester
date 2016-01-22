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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-client:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-client-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.client;

import java.util.Date;

import org.smartdeveloperhub.jenkins.ResponseBody;
import org.smartdeveloperhub.jenkins.ResponseExcerpt;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

final class InMemoryResponseExcerpt implements ResponseExcerpt {

	private int statusCode;
	private String etag;
	private Date lastModified;
	private ResponseBody body;

	InMemoryResponseExcerpt() {
	}

	InMemoryResponseExcerpt withStatusCode(int statusCode) {
		this.statusCode=statusCode;
		return this;
	}

	InMemoryResponseExcerpt withLastModified(Date lastModified) {
		this.lastModified=lastModified;
		return this;
	}

	InMemoryResponseExcerpt withEtag(String etag) {
		this.etag=etag;
		return this;
	}

	InMemoryResponseExcerpt withBody(ResponseBody body) {
		this.body=body;
		return this;
	}

	@Override
	public int statusCode() {
		return this.statusCode;
	}

	@Override
	public Date lastModified() {
		return this.lastModified;
	}

	@Override
	public String etag() {
		return this.etag;
	}

	@Override
	public Optional<ResponseBody> body() {
		return Optional.fromNullable(this.body);
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					omitNullValues().
					add("statusCode",this.statusCode).
					add("etag",this.etag).
					add("lastModified",this.lastModified).
					add("body",this.body).
					toString();
	}

}