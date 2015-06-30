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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.backend:ci-backend-hsqldb:1.0.0-SNAPSHOT
 *   Bundle      : ci-backend-hsqldb-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.backend.persistence.hsqldb;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

abstract class Utils {

	public static final class URLBuilder {

		private boolean mustExist;
		private String file;
		private boolean close;
		private String remote;

		public URLBuilder mustExist() {
			this.mustExist=true;
			return this;
		}

		public URLBuilder close() {
			this.close=true;
			return this;
		}

		public URLBuilder persistent(String file) {
			this.file = file;
			return this;
		}

		public URLBuilder remote(String location) {
			this.remote = location;
			return this;
		}

		public String build() {
			StringBuilder builder=new StringBuilder();
			builder.append("jdbc:hsqldb:");
			if(this.file!=null) {
				builder.append("file:").append(this.file);
			} else if(this.remote!=null) {
				builder.append(this.remote);
			}
			if(this.close) {
				builder.append(";shutdown=true");
			} else if(this.mustExist) {
				builder.append(";ifexists=true");
			} else {
				builder.append(";sql.enforce_strict_size=true;hsqldb.tx=locks;hsqldb.write_delay=false;hsqldb.default_table_type=CACHED");
			}
			return builder.toString();
		}

	}

	private static final String[] DATABASE_FILES={".properties",".script",".data",".backup",".log",".lck"};

	static URLBuilder urlBuilder() {
		return new URLBuilder();
	}

	static List<File> dbResources(String location) throws IOException {
		List<File> result=Lists.newArrayList();
		for(String dbFile:DATABASE_FILES) {
			String targetFile = location+dbFile;
			result.add(new File(targetFile));
		}
		return result;
	}

}
