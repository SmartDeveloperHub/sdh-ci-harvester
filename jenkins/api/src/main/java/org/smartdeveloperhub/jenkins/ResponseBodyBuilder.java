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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class ResponseBodyBuilder {

	private static final class ImmutableResponseBody implements ResponseBody {

		private final String content;
		private final String contentType;
		private final String encoding;
		private final Digest digest;

		private ImmutableResponseBody(String content, String contentType, String encoding, Digest digest) {
			this.contentType = contentType;
			this.encoding = encoding;
			this.digest = digest;
			this.content = content;
		}

		@Override
		public Digest digest() {
			return this.digest;
		}

		@Override
		public String contentType() {
			return this.contentType;
		}

		@Override
		public String encoding() {
			return this.encoding;
		}

		@Override
		public String content() {
			return this.content;
		}

		@Override
		public int hashCode() {
			return this.digest.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if(obj instanceof ResponseBody) {
				ResponseBody that=(ResponseBody)obj;
				result=Objects.equal(this.digest,that.digest());
			}
			return result;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(getClass()).
						add("digest",this.digest).
						add("contentType",this.contentType).
						add("encoding",this.encoding).
						add("content",this.content).
						toString();
		}
	}

	private String content;
	private String contentType;
	private String encoding;
	private Digest tmpDigest;

	public ResponseBodyBuilder withContent(String content) {
		this.content=content;
		return this;
	}

	public ResponseBodyBuilder withContentType(String contentType) {
		this.contentType=contentType;
		return this;
	}

	public ResponseBodyBuilder withEncoding(String encoding) {
		this.encoding=encoding;
		return this;
	}

	public ResponseBodyBuilder withDigest(Digest digest) {
		this.tmpDigest = digest;
		return this;
	}

	public ResponseBody build() {
		checkNotNull(this.content,"Response body content cannot be null");
		checkNotNull(this.contentType,"Response body content type cannot be null");
		checkNotNull(this.encoding,"Response body encoding cannot be null");
		Digest digest=this.tmpDigest;
		if(digest==null) {
			digest=DigestService.digestContents(this.content);
		}
		return new ImmutableResponseBody(this.content,this.contentType,this.encoding,digest);
	}

}
