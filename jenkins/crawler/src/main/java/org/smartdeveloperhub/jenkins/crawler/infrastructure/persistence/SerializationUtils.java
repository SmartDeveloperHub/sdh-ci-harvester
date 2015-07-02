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
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class SerializationUtils {

	private static final Logger LOGGER=LoggerFactory.getLogger(SerializationUtils.class);

	private SerializationUtils() {
	}

	static <T extends Serializable> T deserialize(byte[] binaryFailure, Class<? extends T> clazz) throws IOException {
		try {
			ByteArrayInputStream is = new ByteArrayInputStream(binaryFailure);
			ObjectInputStream in = new ObjectInputStream(is);
			Object readObject = in.readObject();
			return clazz.cast(readObject);
		} catch (ClassNotFoundException e) {
			LOGGER.error("Could not deserialize {} from {}. Fullstacktrace follows",clazz.getName(),Arrays.asList(binaryFailure),e);
			throw new IOException("Deserialization failure",e);
		} catch (IOException e) {
			LOGGER.error("Could not deserialize {} from {}. Fullstacktrace follows",clazz.getName(),Arrays.asList(binaryFailure),e);
			throw e;
		}
	}

	static byte[] serialize(Serializable failure) throws IOException {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(os);
			out.writeObject(failure);
			out.close();
			return os.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Could not serialize {} ({}). Fullstacktrace follows",failure,failure.getClass().getName(),e);
			throw e;
		}
	}

}
