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

import java.net.URI;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.Maps;

final class LockManager {

	interface Operation {

	}

	interface RunnableOperation<E extends Throwable> extends Operation {

		void execute() throws E;

	}

	interface CallableOperation<T,E extends Throwable> extends Operation {

		T execute() throws E;

	}

	private final ConcurrentMap<URI,ReadWriteLock> locks;

	LockManager() {
		this.locks=Maps.newConcurrentMap();
	}

	private ReadWriteLock register(URI uri) {
		ReadWriteLock newLock = new ReentrantReadWriteLock();
		ReadWriteLock result=this.locks.put(uri, newLock);
		if(result==null) {
			result=newLock;
		}
		return result;
	}

	<E extends Throwable> void read(URI uri, RunnableOperation<E> operation) throws E{
		ReadWriteLock lock = register(uri);
		lock.readLock().lock();
		try {
			operation.execute();
		} finally {
			lock.readLock().unlock();
		}
	}

	<T, E extends Throwable> T read(URI uri, CallableOperation<T,E> operation) throws E {
		ReadWriteLock lock = register(uri);
		lock.readLock().lock();
		try {
			return operation.execute();
		} finally {
			lock.readLock().unlock();
		}
	}

	<E extends Throwable> void write(URI uri, RunnableOperation<E> operation) throws E{
		ReadWriteLock lock = register(uri);
		lock.writeLock().lock();
		try {
			operation.execute();
		} finally {
			lock.writeLock().unlock();
		}
	}

	<T, E extends Throwable> T write(URI uri, CallableOperation<T,E> operation) throws E {
		ReadWriteLock lock = register(uri);
		lock.readLock().lock();
		try {
			return operation.execute();
		} finally {
			lock.writeLock().unlock();
		}
	}

}