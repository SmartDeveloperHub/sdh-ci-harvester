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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-core:0.2.0
 *   Bundle      : ci-backend-core-0.2.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.database;

import org.smartdeveloperhub.harvesters.ci.backend.spi.DatabaseProvider;

public final class DatabaseManager {

	private DatabaseManager() {
	}

	/**
	 * Load a database instance for the given configuration
	 *
	 * @param config
	 *            The configuration to use for loading the database
	 * @return A database or null if database cannot be loaded
	 * @throws DatabaseLifecycleException
	 *             if a failure prevents the creation of a database
	 */
	public static Database load(DatabaseConfig config) {
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
