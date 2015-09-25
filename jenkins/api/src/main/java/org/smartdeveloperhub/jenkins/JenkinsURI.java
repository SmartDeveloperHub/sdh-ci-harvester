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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:0.2.0-SNAPSHOT
 *   Bundle      : ci-jenkins-api-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;



import com.google.common.base.MoreObjects;

public final class JenkinsURI {

	private static final String JENKINS_ENTITY_URI_MARKER = "/job/";

	private final URI location;

	private String instance;
	private String job;
	private String run;
	private String subJob;

	private JenkinsURI(URI location) {
		this.location = location;
	}

	private void setSubJob(String subJob) {
		this.subJob=subJob;
	}

	private void setJob(String job) {
		this.job=job;
	}

	private void setRun(String run) {
		this.run=run;
	}

	private void setService(String service) {
		this.instance=service;
	}

	public URI location() {
		return this.location;
	}

	public String subJob() {
		return this.subJob;
	}

	public String instance() {
		return this.instance;
	}

	public String run() {
		return this.run;
	}

	public String job() {
		return this.job;
	}

	public boolean isInstance() {
		return this.job==null;
	}

	public boolean isJob() {
		return this.job!=null && this.run==null;
	}

	public boolean isRun() {
		return this.job!=null && this.run!=null;
	}

	public boolean isSimple() {
		return this.job!=null && this.subJob==null;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(JenkinsURI.class).
					omitNullValues().
					add("location",this.location).
					add("instance",this.instance).
					add("job",this.job).
					add("subJob",this.subJob).
					add("run",this.location).
					toString();
	}

	public static JenkinsURI create(URI location) {
		checkNotNull(location,"Location cannot be null");
		checkArgument(location.isAbsolute(),"Location must be absolute (%s)",location);
		checkArgument(!location.isOpaque(),"Location must be hierarchical (%s)",location);
		checkArgument("http".equals(location.getScheme()) || "https".equals(location.getScheme()),"Location must be an HTTP(S) url (%s)",location);

		JenkinsURI result=new JenkinsURI(location);
		String strLocation = location.toString();
		int sMarker = strLocation.indexOf(JENKINS_ENTITY_URI_MARKER);
		if(sMarker>0) {
			result.setService(strLocation.substring(0,sMarker+1));
			String[] path=strLocation.substring(sMarker+JENKINS_ENTITY_URI_MARKER.length()).split("/");
			result.setJob(path[0]);
			result.setSubJob(path.length>1?path[1]:null);
			result.setRun(path.length>2?path[2]:null);
			if(result.subJob()!=null) {
				verifySubresource(result);
			}
		} else {
			result.setService(strLocation);
		}
		return result;
	}

	private static void verifySubresource(JenkinsURI result) {
		String oldRun = result.run();
		if(oldRun==null) {
			verifySubJob(result, oldRun);
		} else {
			verifyRun(result, oldRun);
		}
	}

	private static void verifyRun(JenkinsURI result, String oldRun) {
		try {
			Long.parseLong(oldRun);
			// Good guess
		} catch (NumberFormatException e) {
			swapRunAndSubJob(result, oldRun);
		}
	}

	private static void verifySubJob(JenkinsURI result, String oldRun) {
		try {
			Long.parseLong(result.subJob());
			swapRunAndSubJob(result, oldRun);
		} catch (NumberFormatException e) {
			// Good guess
		}
	}

	private static void swapRunAndSubJob(JenkinsURI result, String oldRun) {
		result.setRun(result.subJob());
		result.setSubJob(oldRun);
	}

}