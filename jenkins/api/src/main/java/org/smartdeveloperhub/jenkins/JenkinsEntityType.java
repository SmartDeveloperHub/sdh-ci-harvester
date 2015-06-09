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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.jenkins:ci-jenkins-api:1.0.0-SNAPSHOT
 *   Bundle      : ci-jenkins-api-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.jenkins;


public enum JenkinsEntityType {

	SERVICE("hudson",null,null) {
		@Override
		public boolean isService() {
			return true;
		}
	},
	JOB("build",null,SERVICE) {
		@Override
		public boolean isJob() {
			return true;
		}
	},
	MULTI_JOB("build",JOB,null) ,
	RUN("run",null,JOB) {
		@Override
		public boolean isRun() {
			return true;
		}
	},
	FREE_STYLE_BUILD("freeStyleProject",JOB,null),
	MAVEN_BUILD("abstractMavenProject",JOB,null),
	MAVEN_MULTIMODULE_BUILD("mavenModuleSet",MAVEN_BUILD,null),
	MAVEN_MODULE_BUILD("mavenModule",MAVEN_BUILD,MAVEN_MULTIMODULE_BUILD),
	MATRIX_BUILD("matrixProject",JOB,SERVICE),
	CONFIGURATION_BUILD("matrixConfiguration",JOB,MATRIX_BUILD),
	FREE_STYLE_RUN("freeStyleBuild",RUN,FREE_STYLE_BUILD),
	MAVEN_RUN("abstractMavenBuild",RUN,MAVEN_BUILD),
	MAVEN_MULTIMODULE_RUN("mavenModuleSetBuild",MAVEN_RUN,MAVEN_MULTIMODULE_BUILD),
	MAVEN_MODULE_RUN("mavenBuild",MAVEN_RUN,MAVEN_MULTIMODULE_RUN),
	MATRIX_RUN("matrixBuild",RUN,MATRIX_BUILD),
	CONFIGURATION_RUN("matrixConfigurationBuild",RUN,MATRIX_RUN),
	;

	private final JenkinsEntityType parent;
	private final JenkinsEntityType owner;
	private final String documentRootElementName;

	private JenkinsEntityType(String documentRootElementName, JenkinsEntityType parent, JenkinsEntityType owner) {
		this.documentRootElementName = documentRootElementName;
		this.parent = parent;
		this.owner = owner;
	}

	public JenkinsEntityType category() {
		return
			this.parent!=null?
				this.parent.category():
				this;
	}

	public boolean isService() {
		return
			this.parent!=null?
				this.parent.isService():
				false;
	}

	public boolean isJob() {
		return
			this.parent!=null?
				this.parent.isJob():
				false;
	}

	public boolean isRun() {
		return
			this.parent!=null?
				this.parent.isRun():
				false;
	}

	public JenkinsEntityType getOwner() {
		return
			this.owner!=null?
				this.owner:
				this.parent!=null?
					this.parent.getOwner():
					null;
	}

	public boolean isOwnedBy(JenkinsEntityType resource) {
		return
			resource==null?
				false:
				getOwner()==resource;
	}

	public boolean isOwnerOf(JenkinsEntityType resource) {
		return
			resource==null?
				false:
				resource.isOwnedBy(this);
	}

	public static JenkinsEntityType fromNode(String name) {
		for(JenkinsEntityType type:values()) {
			if(type.documentRootElementName.equals(name)) {
				return type;
			}
		}
		return null;
	}

	public boolean isCompatible(JenkinsEntityType entity) {
		JenkinsEntityType tmp=entity;
		boolean compatible=false;
		while(!compatible && tmp!=null) {
			if(tmp==this) {
				compatible=true;
			} else {
				tmp=tmp.parent;
			}
		}
//		System.err.println(""+this+".isCompatible("+entity+")="+compatible);
		return compatible;
	}

	public String toNode() {
		return this.documentRootElementName;
	}

}