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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.4.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.4.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.client.JenkinsResourceProxy;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Instance;

public class FileBasedStorageTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Rule
	public TestName testName = new TestName();

	@Test
	public void testEntityManagement() throws Exception {
		final URI instanceId=URI.create("http://www.example.org/");

		final Instance in=new Instance();
		in.setUrl(instanceId);
		in.setId(instanceId.toString());

		final FileBasedStorage write = storage();
		write.saveEntity(in,JenkinsEntityType.INSTANCE);
		write.save();

		final FileBasedStorage read = storage();
		final Instance out=read.entityOfId(instanceId,JenkinsEntityType.INSTANCE,Instance.class);

		assertThat(out,equalTo(in));
	}

	@Test
	public void testBadEncoding() throws Exception {
		final String location="https://ci.jenkins-ci.org/job/infra_changelog_refresh/27821/";
		final JenkinsResourceProxy sut =
			JenkinsResourceProxy.
				create(URI.create(location)).
					withEntity(JenkinsEntityType.RUN);

		try {
			final JenkinsResource representation=sut.get(JenkinsArtifactType.RESOURCE);
			System.out.println("Resource from '"+location+"':");
			System.out.println(representation);
			storage().saveResource(representation);
		} catch (final IOException e) {
			System.err.println("Could not retrieve service: "+e.getMessage());
			e.printStackTrace(System.err);
		}
	}

	private FileBasedStorage storage() throws IOException {
		return
			FileBasedStorage.
				builder().
					withWorkingDirectory(this.testFolder.getRoot()).
					build();
	}

}
