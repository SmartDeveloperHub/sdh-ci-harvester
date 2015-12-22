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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class FileBasedStorageBuilderTest {

	private static final String DEFAULT_CONFIG_FILENAME = "repository.xml";
	private static final File DEFAULT_WORKING_DIRECTORY = new File(".crawler");
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testDefault() throws Exception {
		FileBasedStorage storage =
			FileBasedStorage.
				builder().
					build();
		assertThat(storage.configFile().getParentFile().getAbsoluteFile(),equalTo(DEFAULT_WORKING_DIRECTORY.getAbsoluteFile()));
		assertThat(storage.configFile().getName(),equalTo(DEFAULT_CONFIG_FILENAME));
	}

	@Test
	public void testCustomWorkingDirectory$valid() throws Exception {
		FileBasedStorage storage =
			FileBasedStorage.
				builder().
					withWorkingDirectory(testFolder.getRoot()).
					build();
		assertThat(storage.configFile().getParentFile().getAbsoluteFile(),equalTo(testFolder.getRoot().getAbsoluteFile()));
		assertThat(storage.configFile().getName(),equalTo(DEFAULT_CONFIG_FILENAME));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCustomWorkingDirectory$invalid$file() throws Exception {
		File badWorkingDirectory = testFolder.newFile();
		FileBasedStorage.
			builder().
				withWorkingDirectory(badWorkingDirectory).
				build();
		fail("Should not build the storage with a using a file as working directory");
	}

	@Test
	public void testCustomWorkingDirectory$invalid$null() throws Exception {
		FileBasedStorage storage=
			FileBasedStorage.
				builder().
					withWorkingDirectory(null).
					build();
		assertThat(storage.configFile().getParentFile().getAbsoluteFile(),equalTo(DEFAULT_WORKING_DIRECTORY.getAbsoluteFile()));
		assertThat(storage.configFile().getName(),equalTo(DEFAULT_CONFIG_FILENAME));
	}

}