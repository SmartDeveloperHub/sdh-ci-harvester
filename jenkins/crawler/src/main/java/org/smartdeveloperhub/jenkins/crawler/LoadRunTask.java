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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.2.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsResource;
import org.smartdeveloperhub.jenkins.JenkinsURI;
import org.smartdeveloperhub.jenkins.crawler.event.JenkinsEventFactory;
import org.smartdeveloperhub.jenkins.crawler.util.GitUtil;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Run;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.RunResult;

import com.google.common.collect.Lists;

final class LoadRunTask extends AbstractEntityCrawlingTask<Run> {

	private static final Logger LOGGER=LoggerFactory.getLogger(LoadRunTask.class);

	private static final String SEPARATOR = "/";

	LoadRunTask(URI location) {
		super(location,Run.class,JenkinsEntityType.RUN,JenkinsArtifactType.RESOURCE);
	}

	@Override
	protected String taskPrefix() {
		return "lrt";
	}

	@Override
	protected void processEntity(Run run, JenkinsResource resource) throws IOException {
		List<Codebase> codebases=Lists.newArrayList();
		codebases.add(getNormalizedCodebase(run));

		if(JenkinsEntityType.MAVEN_MODULE_RUN.isCompatible(resource.entity())) {
			// We know for sure that this type of runs do not bear any SCM information
			addSCMInformationFromAggregatorBuild(run, codebases);
		}

		codebases.add(getCodebaseFromJob(run));

		run.setCodebase(SCMUtil.mergeCodebases(codebases));

		super.persistEntity(run,resource.entity());
		super.fireEvent(
				JenkinsEventFactory.
					newRunCreatedEvent(
						super.jenkinsInstance(),
						run));

		if(JenkinsEntityType.MAVEN_RUN.isCompatible(resource.entity()) && run.getResult().equals(RunResult.SUCCESS)) {
			scheduleTask(new LoadRunArtifactsTask(super.location(),run));
		}

	}

	private Codebase getNormalizedCodebase(Run run) {
		Codebase result=new Codebase();
		if(run.isSetCodebase()) {
			final Codebase codebase = run.getCodebase();
			if(codebase.getLocation()!=null && codebase.getLocation().toString().isEmpty()) {
				result.setLocation(null);
			}
			result.setBranch(GitUtil.normalizeBranchName(codebase.getBranch()));
		}
		return result;
	}

	private Codebase getCodebaseFromJob(Run run) {
		Codebase result=null;
		try {
			Job job = super.entityOfId(run.getJob(), JenkinsEntityType.JOB,Job.class);
			if(job!=null) {
				result=job.getCodebase();
			}
		} catch (IOException e) {
			LOGGER.warn("Could not retrieve job "+run.getJob()+" for run "+run.getUrl(),e);
		}
		return result;
	}

	private void addSCMInformationFromAggregatorBuild(Run run, List<Codebase> codebases) {
		JenkinsURI breakdown = JenkinsURI.create(run.getUrl());
		final URI parentURI = URI.create(breakdown.instance()+"job/"+breakdown.job()+SEPARATOR+breakdown.run()+SEPARATOR);
		try {
			Run parent = super.entityOfId(parentURI,JenkinsEntityType.MAVEN_MULTIMODULE_RUN,Run.class);
			if(parent!=null) {
				codebases.add(parent.getCodebase());
				run.setCommit(parent.getCommit());
			}
		} catch (IOException e) {
			LOGGER.warn("Could not retrieve aggregator build "+parentURI+" for run "+run.getUrl(),e);
		}
	}

}