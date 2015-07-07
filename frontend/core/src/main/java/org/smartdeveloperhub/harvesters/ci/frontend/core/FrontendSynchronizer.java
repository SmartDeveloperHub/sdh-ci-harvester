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
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.Build;
import org.smartdeveloperhub.harvesters.ci.backend.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class FrontendSynchronizer implements EntityLifecycleEventListener {

	private static final Logger LOGGER=LoggerFactory.getLogger(FrontendSynchronizer.class);

	private final EntityIndex index;
	private final BackendModelPublisher publisher;

	FrontendSynchronizer(EntityIndex index, BackendModelPublisher publisher) {
		this.index = index;
		this.publisher = publisher;
	}

	@Override
	public void onEvent(EntityLifecycleEvent event) {
		try {
			WriteSession session = ApplicationContext.getInstance().createSession();
			actOnSession(event, session);
		} catch (ApplicationContextException e) {
			LOGGER.warn("Could not create session for processing event {}",event,e);
		}
	}

	private void actOnSession(EntityLifecycleEvent event, WriteSession session) {
		try {
			if(processEvent(event, session)) {
				session.saveChanges();
				LOGGER.info("Updated frontend: {} {} {}",event.state(),event.entityType(),event.entityId());
			} else {
				session.discardChanges();
				LOGGER.debug("Nothing to do ({} {} {})",event.state(),event.entityType(),event.entityId());
			}
		} catch (WriteSessionException e) {
			LOGGER.warn("Could not process event {}",event,e);
		}
	}

	private boolean processEvent(EntityLifecycleEvent event, WriteSession session) {
		boolean result=false;
		switch(event.state()) {
			case CREATED:
				result=update(event, session);
				break;
			case MODIFIED:
				result=modify(event, session);
				break;
			case DELETED:
				result=delete(event, session);
				break;
			default:
				break;
		}
		return result;
	}

	private boolean update(EntityLifecycleEvent event, WriteSession session) {
		boolean result=false;
		switch(event.entityType()) {
			case BUILD:
				result=updateBuild(event, session);
				break;
			case EXECUTION:
				result=updateExecution(event, session);
				break;
			default:
				break;
		}
		return result;
	}

	private boolean updateExecution(EntityLifecycleEvent event, WriteSession session) {
		Execution execution=this.index.findExecution(event.entityId());
		if(execution!=null) {
			this.publisher.publish(session, execution);
		}
		return execution!=null;
	}

	private boolean updateBuild(EntityLifecycleEvent event, WriteSession session) {
		Build build=this.index.findBuild(event.entityId());
		if(build!=null) {
			this.publisher.publish(session, build);
		}
		return build!=null;
	}

	private boolean modify(EntityLifecycleEvent event, WriteSession session) {
		ResourceSnapshot resource = findResource(event, session);
		if(resource!=null) {
			session.modify(resource);
		}
		return resource!=null;
	}

	private boolean delete(EntityLifecycleEvent event, WriteSession session) {
		ResourceSnapshot resource = findResource(event, session);
		if(resource!=null) {
			session.delete(resource);
		}
		return resource!=null;
	}

	private ResourceSnapshot findResource(EntityLifecycleEvent event, WriteSession session) {
		Name<URI> name=null;
		switch(event.entityType()) {
			case BUILD:
				name=IdentityUtil.buildName(event.entityId());
				break;
			case EXECUTION:
				name=IdentityUtil.executionName(event.entityId());
				break;
			default:
				break;
		}
		return session.find(ResourceSnapshot.class,name,BuildHandler.class);
	}

}