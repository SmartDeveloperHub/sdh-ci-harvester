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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.4.0-SNAPSHOT
 *   Bundle      : ci-jenkins-api-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

import java.util.Arrays;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class DigestService {

	private static final class ImmutableDigest implements Digest {

		private final byte[] value;
		private final String algorithm;

		private ImmutableDigest(byte[] value, String algorithm) {
			this.value = value;
			this.algorithm = algorithm;
		}

		@Override
		public String algorithm() {
			return this.algorithm;
		}

		@Override
		public byte[] value() {
			return this.value;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(this.value,this.algorithm);
		}

		@Override
		public boolean equals(Object obj) {
			boolean result = false;
			if(obj instanceof Digest) {
				Digest that=(Digest)obj;
				byte[] a = this.value;
				byte[] b = that.value();
				result=
					Objects.equal(this.algorithm,that.algorithm()) &&
					a == b || (a != null && Arrays.equals(a,b));
			}
			return result;
		}

		@Override
		public String toString() {
			return
				MoreObjects.
					toStringHelper(Digest.class).
						add("algorithm",this.algorithm).
						add("value",DigestUtil.bytesToHex(this.value)).
						toString();
		}


	}

	private DigestService() {
	}

	public static Digest digestContents(String contents) {
		byte[] digest=DigestUtils.sha256(contents);
		String algorithm=MessageDigestAlgorithms.SHA_256;
		return assembleDigest(digest, algorithm);
	}

	public static Digest assembleDigest(byte[] digest, String algorithm) {
		return new ImmutableDigest(digest, algorithm);
	}

}
