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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:0.3.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-0.3.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Codebase;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.Job;
import org.smartdeveloperhub.jenkins.crawler.xml.ci.SubJob;

import com.google.common.base.Optional;
import com.google.common.base.MoreObjects.ToStringHelper;

public final class JobCreatedEvent extends JenkinsEvent {

	public enum Type {
		SIMPLE,
		COMPOSITE,
		SUB_JOB
	}

	private Job job;
	private Type type;
	private Codebase codebase;

	private JobCreatedEvent(URI instanceId, Date date) {
		super(instanceId,date);
	}

	JobCreatedEvent withJob(Job job) {
		this.job = job;
		this.codebase=Optional.fromNullable(this.job.getCodebase()).or(new Codebase());
		return this;
	}

	Job job() {
		return this.job;
	}

	@Override
	void accept(JenkinsEventVisitor visitor) {
		if(visitor!=null) {
			visitor.visitJobCreatedEvent(this);
		}
	}

	public URI jobId() {
		return this.job.getUrl();
	}

	public String title() {
		return job.getTitle();
	}

	public String description() {
		return this.job.getDescription();
	}

	public URI codebase() {
		return this.codebase.getLocation();
	}

	public String branchName() {
		return this.codebase.getBranch();
	}

	public Type type() {
		if(this.type==null) {
			switch(this.job.getType()) {
				case FREE_STYLE_PROJECT:
					this.type=Type.SIMPLE;
					break;
				case MATRIX_CONFIGURATION:
					this.type=Type.SUB_JOB;
					break;
				case MATRIX_PROJECT:
					this.type=Type.COMPOSITE;
					break;
				case MAVEN_MODULE:
					this.type=Type.SUB_JOB;
					break;
				case MAVEN_MODULE_SET:
					this.type=Type.COMPOSITE;
					break;
				default:
					throw new AssertionError("Unknown job type '"+this.job.getType());
			}
		}
		return this.type;
	}

	public URI parent() {
		URI result=null;
		if(this.job instanceof SubJob) {
			result=((SubJob)this.job).getParent();
		}
		return result;
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.
			add("jobId",jobId()).
			add("title",title()).
			add("description",description()).
			add("codebase", codebase()).
			add("branchName", branchName()).
			add("type",type()).
			add("parent",parent());
	}

	static JobCreatedEvent create(URI instanceId) {
		return new JobCreatedEvent(instanceId, new Date());
	}

}
