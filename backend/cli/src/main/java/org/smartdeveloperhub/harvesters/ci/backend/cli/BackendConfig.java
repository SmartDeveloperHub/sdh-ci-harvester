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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-cli:0.3.0-SNAPSHOT
 *   Bundle      : ci-backend-cli-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.cli;

import java.io.File;

import org.smartdeveloperhub.harvesters.ci.backend.database.DatabaseConfig;
import org.smartdeveloperhub.harvesters.ci.backend.enrichment.EnrichmentConfig;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

public final class BackendConfig {

	private String workingDirectory;
	private DatabaseConfig database;
	private EnrichmentConfig enrichment;

	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public String getWorkingDirectory() {
		return this.workingDirectory;
	}

	public DatabaseConfig getDatabase() {
		return this.database;
	}

	public void setDatabase(final DatabaseConfig dbConfig) {
		this.database=dbConfig;
	}

	public String targetDatabase() {
		String base=
			Optional.
				fromNullable(this.workingDirectory).
				or("");
		if(!base.isEmpty() && !base.endsWith(File.separator)) {
			base+=File.separator;
		}
		return base+this.database.getLocation();
	}

	public void setEnrichment(final EnrichmentConfig enrichment) {
		this.enrichment = enrichment;
	}

	public EnrichmentConfig getEnrichment() {
		return this.enrichment;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("workingDirectory",this.workingDirectory).
					add("database",this.database).
					add("enrichment",this.enrichment).
					toString();
	}

}
