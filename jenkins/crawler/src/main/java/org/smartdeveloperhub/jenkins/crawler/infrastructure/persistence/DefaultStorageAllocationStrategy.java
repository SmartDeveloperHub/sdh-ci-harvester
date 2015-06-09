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
package org.smartdeveloperhub.jenkins.crawler.infrastructure.persistence;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.smartdeveloperhub.jenkins.JenkinsArtifactType;
import org.smartdeveloperhub.jenkins.JenkinsEntityType;
import org.smartdeveloperhub.jenkins.JenkinsURI;

final class DefaultStorageAllocationStrategy implements StorageAllocationStrategy {

	/**
	 *
	 */
	private static final long serialVersionUID = 4007002457509004501L;

	private File workingDirectory;

	private void getServiceBase(JenkinsURI id, PathBuilder builder) {
		try {
			builder.addSegment(URLEncoder.encode(id.service(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError("UTF-8 encoding should be supported");
		}
	}

	private void getBuildBase(JenkinsURI id,PathBuilder builder) {
		getServiceBase(id,builder);
		builder.addSegment("builds").addSegment(id.job());
		if(!id.isSimple()) {
			builder.addSegment(id.subJob());
		}
	}

	private void getRunBase(JenkinsURI id,PathBuilder builder) {
		getBuildBase(id,builder);
		builder.addSegment("runs").addSegment(id.run());
	}

	private File getEntityBaseDirectory(URI location, JenkinsEntityType entity) {
		JenkinsURI id = JenkinsURI.create(location);
		PathBuilder builder=new PathBuilder();
		if(entity.isService()) {
			getServiceBase(id, builder);
		} else if(entity.isJob()) {
			getBuildBase(id, builder);
		} else if(entity.isRun()) {
			getRunBase(id, builder);
		} else {
			throw new AssertionError("Invalid entity type '"+entity+"'");
		}
		return new File(workingDirectory,builder.build());
	}

	private String getArtifactFilename(JenkinsEntityType entity, JenkinsArtifactType type) throws AssertionError {
		String filename = getFileName(entity);
		switch(type) {
			case RESOURCE:
				break;
			case ARTIFACTS:
				filename+=".artifacts";
				break;
			case CONFIGURATION:
				filename+=".config";
				break;
			case SCM:
				filename+=".scm";
				break;
			default:
				throw new AssertionError("Unsupported artifact type '"+type+"'");
		}
		return filename;
	}

	private String getFileName(JenkinsEntityType entity) throws AssertionError {
		String filename=null;
		if(entity.isService()) {
			filename="service";
		} else if(entity.isJob()) {
			filename="build";
		} else if(entity.isRun()) {
			filename="run";
		} else {
			throw new AssertionError("Invalid entity type '"+entity+"'");
		}
		return filename;
	}

	@Override
	public File workingDirectory() {
		checkState(this.workingDirectory!=null,"Strategy has not been initialized yet");
		return this.workingDirectory;
	}

	@Override
	public File allocateArtifact(URI location, JenkinsEntityType entity, JenkinsArtifactType artifact) {
		File base=getEntityBaseDirectory(location,entity);
		String filename = getArtifactFilename(entity, artifact);
		return new File(base,filename+".xml");
	}

	@Override
	public File allocateResource(URI location, JenkinsEntityType entity, JenkinsArtifactType artifact) {
		File base=getEntityBaseDirectory(location,entity);
		String filename = getArtifactFilename(entity, artifact);
		return new File(base,filename+".resource.xml");
	}

	@Override
	public File allocateEntity(URI location, JenkinsEntityType entity, JenkinsArtifactType artifact) {
		File base=getEntityBaseDirectory(location,entity);
		String filename = getArtifactFilename(entity, artifact);
		return new File(base,filename+".entity.xml");
	}

	@Override
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory=workingDirectory;
	}

}