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
package org.smartdeveloperhub.jenkins.crawler.infrastructure.transformation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.CompositeJob;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.JobType;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunStatus;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunType;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.SimpleJob;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.SubJob;
import org.smartdeveloperhub.util.xml.XmlProcessingException;
import org.smartdeveloperhub.util.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.common.collect.ImmutableMap;

public class TransformationManagerTest {

	private static final String INSTANCE_BASE = "http://ci.jenkins-ci.org/";

	private TransformationManager sut;

	@Before
	public void setUp() {
		this.sut=TransformationManager.newInstance();
	}

	@Test
	public void testFreeStyleProject() throws Exception {
		final String jobName = "infra_backend_crawler";
		final String url = jobURL(jobName);
		final Job result =
			doTransform(
				"responses/freeStyleProject/job.xml",
				url,
				Job.class);
		final SimpleJob job = verifyType(result,SimpleJob.class);
		assertThat(job.getUrl(),equalTo(URI.create(url)));
		assertThat(job.getInstance(),equalTo(URI.create(INSTANCE_BASE)));
		assertThat(job.getType(),equalTo(JobType.FREE_STYLE_PROJECT));
		assertThat(job.getCodebase(),equalTo(emptyCodebase()));
		assertThat(job.getId(),equalTo(jobName));
		assertThat(job.getRuns().getRuns(),hasSize(19));
	}

	@Test
	public void testFreeStyleRun() throws Exception {
		final String jobName = "infra_backend_crawler";
		final String runName = "10";
		final String url = runURL(jobName,runName);
		final Run result =
			doTransform(
				"responses/freeStyleProject/run.xml",
				url,
				Run.class);
		final Run job = verifyType(result,Run.class);
		assertThat(job.getUrl(),equalTo(URI.create(url)));
		assertThat(job.getType(),equalTo(RunType.FREE_STYLE_BUILD));
		assertThat(job.getId(),equalTo(runName));
		assertThat(job.getJob(),equalTo(URI.create(jobURL(jobName))));
		assertThat(job.getStatus(),equalTo(RunStatus.FINISHED));
		assertThat(job.getResult().getDuration(),equalTo(482341L));
		assertThat(job.getResult().getStatus(),equalTo(RunResult.SUCCESS));
		assertThat(job.getCodebase().getLocation(),equalTo(URI.create("git://github.com/jenkinsci/backend-crawler.git")));
		assertThat(job.getCodebase().getBranch(),equalTo("refs/remotes/origin/master"));
		assertThat(job.getCommit(),equalTo("537854615082e00fc444ea29caf49d9bf9fb4135"));
	}

	@Test
	public void testMavenModuleSet() throws Exception {
		final String jobName = "infra_backend-plugin-report-card";
		final String url = jobURL(jobName);
		final Job result =
			doTransform(
				"responses/mavenModuleSet/job.xml",
				url,
				Job.class);
		final CompositeJob job = verifyType(result,CompositeJob.class);
		assertThat(job.getUrl(),equalTo(URI.create(url)));
		assertThat(job.getInstance(),equalTo(URI.create(INSTANCE_BASE)));
		assertThat(job.getType(),equalTo(JobType.MAVEN_MODULE_SET));
		assertThat(job.getCodebase(),equalTo(emptyCodebase()));
		assertThat(job.getId(),equalTo(jobName));
		assertThat(job.getRuns().getRuns(),hasSize(100));
		assertThat(job.getSubJobs().getJobs(),hasSize(1));
	}

	@Test
	public void testMavenModule() throws Exception {
		final String subJobName = "org.jenkins.ci.backend:backend-plugin-report-card";
		final String jobName = "infra_backend-plugin-report-card";
		final String url = subJobURL(jobName,subJobName);
		final Job result =
			doTransform(
				"responses/mavenModule/job.xml",
				url,
				Job.class);
		final SubJob job = verifyType(result,SubJob.class);
		assertThat(job.getUrl(),equalTo(URI.create(url)));
		assertThat(job.getInstance(),equalTo(URI.create(INSTANCE_BASE)));
		assertThat(job.getParent(),equalTo(URI.create(jobURL(jobName))));
		assertThat(job.getType(),equalTo(JobType.MAVEN_MODULE));
		assertThat(job.getCodebase(),equalTo(emptyCodebase()));
		assertThat(job.getId(),equalTo(subJobName));
		assertThat(job.getRuns().getRuns(),hasSize(100));
	}

	@Test
	public void testMatrixProject() throws Exception {
		final String jobName = "test-matrix";
		final String url = jobURL(jobName);
		final Job result =
			doTransform(
				"responses/matrixProject/job.xml",
				url,
				Job.class);
		final CompositeJob job = verifyType(result,CompositeJob.class);
		assertThat(job.getUrl(),equalTo(URI.create(url)));
		assertThat(job.getInstance(),equalTo(URI.create(INSTANCE_BASE)));
		assertThat(job.getType(),equalTo(JobType.MATRIX_PROJECT));
		assertThat(job.getCodebase(),equalTo(emptyCodebase()));
		assertThat(job.getId(),equalTo(jobName));
		assertThat(job.getRuns().getRuns(),hasSize(0));
		assertThat(job.getSubJobs().getJobs(),hasSize(1));
	}

	private Codebase emptyCodebase() {
		return new Codebase().withBranch("").withLocation(URI.create(""));
	}

	@Test
	public void testMatrixConfiguration() throws Exception {
		final String subJobName = "default";
		final String jobName = "test-matrix";
		final String url = subJobURL(jobName,subJobName);
		final Job result =
			doTransform(
				"responses/matrixConfiguration/job.xml",
				url,
				Job.class);
		final SubJob job = verifyType(result,SubJob.class);
		assertThat(job.getUrl(),equalTo(URI.create(url)));
		assertThat(job.getInstance(),equalTo(URI.create(INSTANCE_BASE)));
		assertThat(job.getParent(),equalTo(URI.create(jobURL(jobName))));
		assertThat(job.getType(),equalTo(JobType.MATRIX_CONFIGURATION));
		assertThat(job.getCodebase(),equalTo(emptyCodebase()));
		assertThat(job.getId(),equalTo(subJobName));
		assertThat(job.getRuns().getRuns(),hasSize(0));
	}

	private String subJobURL(final String jobName, final String subJobName) {
		return jobURL(jobName)+subJobName.replace(':', '$')+"/";
	}

	private <S,T extends S> T verifyType(final S result, final Class<? extends T> clazz) {
		assertThat(result,instanceOf(clazz));
		return clazz.cast(result);
	}

	private String jobURL(final String jobName) {
		return INSTANCE_BASE+"job/"+jobName+"/";
	}

	private String runURL(final String jobName, final String runName) {
		return jobURL(jobName)+runName+"/";
	}

	private <T> T doTransform(final String localResource, final String resourceLocation, final Class<? extends T> clazz) throws Exception {
		final Document content = loadResource(localResource);
		final String localName =
			getFirstChildElement(content).
				getNodeName();
		final Source data = new DOMSource(content);
		return
			this.sut.transform(
				localName,
				data,
				createParameters(resourceLocation),
				clazz
			);
	}

	private static Element getFirstChildElement(Node node) {
		node = node.getFirstChild();
		while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
			node = node.getNextSibling();
		}
		return (Element) node;
	}

	private Map<String, Object> createParameters(final String location) {
		return
			ImmutableMap.
				<String,Object>builder().
					put("serviceUrl",location).
					build();
	}

	private Document loadResource(final String resource)
			throws XmlProcessingException, IOException {
		return XmlUtils.toDocument(IOUtils.toString(ClassLoader.getSystemResourceAsStream(resource)));
	}
}
