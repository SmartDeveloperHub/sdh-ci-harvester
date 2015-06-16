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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-crawler:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-crawler-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins.crawler.event;

import java.net.URI;
import java.util.Date;

import org.smartdeveloperhub.jenkins.crawler.xml.ci.Build;

import com.google.common.base.MoreObjects.ToStringHelper;

public final class BuildCreatedEvent extends JenkinsEvent {

	private Build build;

	private BuildCreatedEvent(URI service, Date date) {
		super(service,date);
	}

	BuildCreatedEvent withBuild(Build build) {
		this.build = build;
		return this;
	}

	Build build() {
		return this.build;
	}

	@Override
	void accept(JenkinsEventVisitor visitor) {
		if(visitor!=null) {
			visitor.visitBuildCreationEvent(this);
		}
	}

	public URI buildId() {
		return this.build.getUrl();
	}

	@Override
	protected void toString(ToStringHelper helper) {
		helper.add("buildId", buildId());
	}

	static BuildCreatedEvent create(URI service) {
		return new BuildCreatedEvent(service, new Date());
	}

}