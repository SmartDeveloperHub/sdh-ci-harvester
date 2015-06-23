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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:1.0.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-1.0.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core.util;

import java.io.Serializable;
import java.net.URI;

import org.ldp4j.application.data.Name;
import org.ldp4j.application.data.NamingScheme;
import org.ldp4j.application.session.ResourceSnapshot;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.CompositeBuild;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.Service;
import org.smartdeveloperhub.harvesters.ci.backend.SubBuild;

import static com.google.common.base.Preconditions.*;

public final class IdentityUtil {

	private IdentityUtil() {
	}
/*
	public static Individual<?, ?> personIndividual(DataSet content, Person person) {
		Individual<?,?> result=null;
		if(person!=null) {
			ManagedIndividualId id=
				ManagedIndividualId.
					createId(
						name(person),
						PersonHandler.ID);
			result=content.individual(id,ManagedIndividual.class);
		} else {
			result=DataSetUtils.newHelper(content).self();
		}
		return result;
	}

	public static Individual<?, ?> contactIndividual(DataSet content, Contact contact) {
		Individual<?,?> result=null;
		if(contact!=null) {
			ManagedIndividualId id=
				ManagedIndividualId.
					createId(
						name(contact),
						ContactHandler.ID);
			result=content.individual(id,ManagedIndividual.class);
		} else {
			result=DataSetUtils.newHelper(content).self();
		}
		return result;
	}

	public static String personId(ResourceSnapshot resource) {
		Serializable id = resource.name().id();
		checkState(id instanceof String,"Person identifier should be a string not a %s",id.getClass().getCanonicalName());
		return (String)id;
	}

	public static ContactId contactId(ResourceSnapshot resource) {
		Serializable contactId = resource.name().id();
		checkState(contactId instanceof String,"Contact identifier should be a string not a %s",contactId.getClass().getCanonicalName());
		ResourceSnapshot contactsResource = resource.parent();
		checkState(contactsResource!=null,"Could not find contact's parent resource");
		ResourceSnapshot personResource = contactsResource.parent();
		checkState(personResource!=null,"Could not find contact's related person resource");
		Serializable personId = personResource.name().id();
		checkState(personId instanceof String,"Person identifier should be a string not a %s",personId.getClass().getCanonicalName());
		return ContactId.create((String)personId,(String)contactId);
	}

	public static Name<String> name(Contact contact, String... subKeys) {
		return NamingScheme.getDefault().name(contact.getEmail(),subKeys);
	}

	public static Name<String> name(Person person, String... subKeys) {
		return NamingScheme.getDefault().name(person.getEmail(),subKeys);
	}

 */

	public static URI serviceId(ResourceSnapshot resource) {
		Serializable serviceId=resource.name().id();
		checkState(serviceId instanceof URI,"Service identifier should be a URI not a %s",serviceId.getClass().getCanonicalName());
		return (URI)serviceId;
	}

	public static URI buildId(ResourceSnapshot resource) {
		Serializable buildId=resource.name().id();
		checkState(buildId instanceof URI,"Build identifier should be a URI not a %s",buildId.getClass().getCanonicalName());
		return (URI)buildId;
	}

	public static URI executionId(ResourceSnapshot resource) {
		Serializable executionId=resource.name().id();
		checkState(executionId instanceof URI,"Execution identifier should be a URI not a %s",executionId.getClass().getCanonicalName());
		return (URI)executionId;
	}

	public static Name<URI> name(Service service) {
		return NamingScheme.getDefault().name(service.serviceId());
	}

	public static Name<URI> name(Service service, URI buildId) {
		return NamingScheme.getDefault().name(buildId);
	}

	public static Name<URI> name(Build build) {
		return NamingScheme.getDefault().name(build.buildId());
	}

	public static Name<URI> name(Build build, URI executionId) {
		return NamingScheme.getDefault().name(executionId);
	}

	public static Name<URI> name(Execution execution) {
		return NamingScheme.getDefault().name(execution.executionId());
	}

	public static Name<URI> buildName(Execution execution) {
		return NamingScheme.getDefault().name(execution.buildId());
	}

	public static Name<URI> parentBuild(SubBuild subBuild) {
		return NamingScheme.getDefault().name(subBuild.parentId());
	}

	public static Name<?> subBuild(CompositeBuild compositeBuild, URI subBuild) {
		return NamingScheme.getDefault().name(subBuild);
	}

}
