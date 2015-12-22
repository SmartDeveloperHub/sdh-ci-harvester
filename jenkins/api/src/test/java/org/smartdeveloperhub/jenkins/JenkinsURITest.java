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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-api-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.net.URI;

import org.junit.Test;

public class JenkinsURITest {

	private static final String DEFAULT_SERVICE = "http://ci.jenkins-ci.org/";

	@Test
	public void testCreate$service() throws Exception {
		JenkinsURI breakdown =
			JenkinsURI.
				create(
					URI.create(DEFAULT_SERVICE));
		assertThat(breakdown,notNullValue());
		assertThat(breakdown.instance(),equalTo(DEFAULT_SERVICE));
		assertThat(breakdown.job(),nullValue());
		assertThat(breakdown.subJob(),nullValue());
		assertThat(breakdown.run(),nullValue());

		assertThat(breakdown.isInstance(),equalTo(true));
		assertThat(breakdown.isJob(),equalTo(false));
		assertThat(breakdown.isRun(),equalTo(false));
		assertThat(breakdown.isSimple(),equalTo(false));
	}

	@Test
	public void testCreate$job() throws Exception {
		JenkinsURI breakdown =
			JenkinsURI.
				create(
					URI.create("http://ci.jenkins-ci.org/job/config-provider-model/"));
		assertThat(breakdown,notNullValue());
		assertThat(breakdown.instance(),equalTo(DEFAULT_SERVICE));
		assertThat(breakdown.job(),equalTo("config-provider-model"));
		assertThat(breakdown.subJob(),nullValue());
		assertThat(breakdown.run(),nullValue());

		assertThat(breakdown.isInstance(),equalTo(false));
		assertThat(breakdown.isJob(),equalTo(true));
		assertThat(breakdown.isRun(),not(equalTo(breakdown.isJob())));
		assertThat(breakdown.isSimple(),equalTo(true));
	}

	@Test
	public void testCreate$run() throws Exception {
		JenkinsURI breakdown =
			JenkinsURI.
				create(
					URI.create("http://ci.jenkins-ci.org/job/config-provider-model/14/"));
		assertThat(breakdown,notNullValue());
		assertThat(breakdown.instance(),equalTo(DEFAULT_SERVICE));
		assertThat(breakdown.job(),equalTo("config-provider-model"));
		assertThat(breakdown.subJob(),nullValue());
		assertThat(breakdown.run(),equalTo("14"));

		assertThat(breakdown.isInstance(),equalTo(false));
		assertThat(breakdown.isJob(),equalTo(false));
		assertThat(breakdown.isRun(),not(equalTo(breakdown.isJob())));
		assertThat(breakdown.isSimple(),equalTo(true));
	}

	@Test
	public void testCreate$subJob() throws Exception {
		JenkinsURI breakdown =
			JenkinsURI.
				create(
					URI.create("http://ci.jenkins-ci.org/job/config-provider-model/org.jenkins-ci.lib$config-provider-model/"));
		assertThat(breakdown,notNullValue());
		assertThat(breakdown.instance(),equalTo(DEFAULT_SERVICE));
		assertThat(breakdown.job(),equalTo("config-provider-model"));
		assertThat(breakdown.subJob(),equalTo("org.jenkins-ci.lib$config-provider-model"));
		assertThat(breakdown.run(),nullValue());

		assertThat(breakdown.isInstance(),equalTo(false));
		assertThat(breakdown.isJob(),equalTo(true));
		assertThat(breakdown.isRun(),not(equalTo(breakdown.isJob())));
		assertThat(breakdown.isSimple(),equalTo(false));
	}

	@Test
	public void testCreate$mavenModuleRun$runFirst() throws Exception {
		JenkinsURI breakdown =
			JenkinsURI.
				create(
					URI.create("http://ci.jenkins-ci.org/job/config-provider-model/org.jenkins-ci.lib$config-provider-model/14/"));
		assertThat(breakdown,notNullValue());
		assertThat(breakdown.instance(),equalTo(DEFAULT_SERVICE));
		assertThat(breakdown.job(),equalTo("config-provider-model"));
		assertThat(breakdown.subJob(),equalTo("org.jenkins-ci.lib$config-provider-model"));
		assertThat(breakdown.run(),equalTo("14"));

		assertThat(breakdown.isInstance(),equalTo(false));
		assertThat(breakdown.isJob(),equalTo(false));
		assertThat(breakdown.isRun(),not(equalTo(breakdown.isJob())));
		assertThat(breakdown.isSimple(),equalTo(false));
	}

	@Test
	public void testCreate$mavenModuleRun$moduleFirst() throws Exception {
		JenkinsURI breakdown =
			JenkinsURI.
				create(
					URI.create("http://ci.jenkins-ci.org/job/config-provider-model/14/org.jenkins-ci.lib$config-provider-model/"));
		assertThat(breakdown,notNullValue());
		assertThat(breakdown.instance(),equalTo(DEFAULT_SERVICE));
		assertThat(breakdown.job(),equalTo("config-provider-model"));
		assertThat(breakdown.subJob(),equalTo("org.jenkins-ci.lib$config-provider-model"));
		assertThat(breakdown.run(),equalTo("14"));

		assertThat(breakdown.isInstance(),equalTo(false));
		assertThat(breakdown.isJob(),equalTo(false));
		assertThat(breakdown.isRun(),not(equalTo(breakdown.isJob())));
		assertThat(breakdown.isSimple(),equalTo(false));
	}

}