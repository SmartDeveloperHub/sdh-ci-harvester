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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence;

import org.smartdeveloperhub.harvesters.ci.backend.persistence.spi.DatabaseProvider;

public final class DatabaseManager {

	private DatabaseManager() {
	}

	public static Database load(DatabaseConfig config) throws DatabaseLifecycleException {
		try {
			Class<?> clazz = Class.forName(config.getProvider());
			if(!DatabaseProvider.class.isAssignableFrom(clazz)) {
				throw new DatabaseLifecycleException("Invalid provider class: "+config.getProvider()+" is not a valid "+DatabaseProvider.class.getCanonicalName());
			}
			Class<? extends DatabaseProvider> providerClass = clazz.asSubclass(DatabaseProvider.class);
			DatabaseProvider provider=providerClass.newInstance();
			return provider.create(config);
		} catch (InstantiationException | IllegalAccessException e) {
		throw new DatabaseLifecycleException("Could not instantiate provider",e);
		} catch (ClassNotFoundException e) {
			throw new DatabaseLifecycleException("Could not load provider",e);
		}
	}

}
