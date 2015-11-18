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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.frontend:ci-frontend-core:0.2.0-SNAPSHOT
 *   Bundle      : ci-frontend-core-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.harvesters.ci.frontend.core;

import java.net.URI;

import org.ldp4j.application.ApplicationContext;
import org.ldp4j.application.ApplicationContextException;
import org.ldp4j.application.data.Name;
import org.ldp4j.application.session.ResourceSnapshot;
import org.ldp4j.application.session.SessionTerminationException;
import org.ldp4j.application.session.WriteSession;
import org.ldp4j.application.session.WriteSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Build;
import org.smartdeveloperhub.harvesters.ci.backend.domain.Execution;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEvent;
import org.smartdeveloperhub.harvesters.ci.backend.event.EntityLifecycleEventListener;
import org.smartdeveloperhub.harvesters.ci.frontend.core.build.BuildHandler;
import org.smartdeveloperhub.harvesters.ci.frontend.core.util.IdentityUtil;
import org.smartdeveloperhub.harvesters.ci.frontend.spi.EntityIndex;

final class FrontendSynchronizer implements EntityLifecycleEventListener {

	private static final Logger LOGGER=LoggerFactory.getLogger(FrontendSynchronizer.class);

	private final EntityIndex index;
	private final BackendModelPublisher publisher;

	FrontendSynchronizer(final EntityIndex index, final BackendModelPublisher publisher) {
		this.index = index;
		this.publisher = publisher;
	}

	@Override
	public void onEvent(final EntityLifecycleEvent event) {
		try(WriteSession session = ApplicationContext.getInstance().createSession()) {
			actOnSession(event, session);
		} catch (ApplicationContextException | SessionTerminationException e) {
			LOGGER.warn("Session failure while processing event {}",event,e);
		}
	}

	private void actOnSession(final EntityLifecycleEvent event, final WriteSession session) {
		try {
			if(processEvent(event, session)) {
				session.saveChanges();
				LOGGER.info("Updated frontend: {} {} {}",event.state(),event.entityType(),event.entityId());
			} else {
				session.discardChanges();
				LOGGER.debug("Nothing to do ({} {} {})",event.state(),event.entityType(),event.entityId());
			}
		} catch (final WriteSessionException e) {
			LOGGER.warn("Could not process event {}",event,e);
		}
	}

	private boolean processEvent(final EntityLifecycleEvent event, final WriteSession session) {
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

	private boolean update(final EntityLifecycleEvent event, final WriteSession session) {
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

	private boolean updateExecution(final EntityLifecycleEvent event, final WriteSession session) {
		final Execution execution=this.index.findExecution(event.entityId());
		if(execution!=null) {
			this.publisher.publish(session, execution);
		}
		return execution!=null;
	}

	private boolean updateBuild(final EntityLifecycleEvent event, final WriteSession session) {
		final Build build=this.index.findBuild(event.entityId());
		if(build!=null) {
			this.publisher.publish(session, build);
		}
		return build!=null;
	}

	private boolean modify(final EntityLifecycleEvent event, final WriteSession session) {
		final ResourceSnapshot resource = findResource(event, session);
		if(resource!=null) {
			session.modify(resource);
		}
		return resource!=null;
	}

	private boolean delete(final EntityLifecycleEvent event, final WriteSession session) {
		final ResourceSnapshot resource = findResource(event, session);
		if(resource!=null) {
			session.delete(resource);
		}
		return resource!=null;
	}

	private ResourceSnapshot findResource(final EntityLifecycleEvent event, final WriteSession session) {
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