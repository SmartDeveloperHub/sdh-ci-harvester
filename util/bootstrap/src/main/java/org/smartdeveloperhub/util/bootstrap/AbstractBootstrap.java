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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:0.1.0
 *   Bundle      : ci-util-bootstrap-0.1.0.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager;

public abstract class AbstractBootstrap<T> {

	private static final TimeUnit INITIALIZATION_WAIT_TIMEUNIT = TimeUnit.MILLISECONDS;
	private static final int INITIALIZATION_WAIT_TIMEOUT = 500;
	private static final int MAX_RETRIES = 5;

	private final Logger logger; // NOSONAR

	private final String applicationName;
	private final String bootstrapId;
	private final Class<T> configType;

	private ServiceManager serviceManager;
	private BootstrapListener listener;

	protected AbstractBootstrap(String name, Class<T> configType) {
		this.applicationName=checkNotNull(name,"Application name cannot be null");
		this.configType=checkNotNull(configType,"Application configuration type cannot be null");
		this.bootstrapId=BootstrapTracker.track(getClass(),this.applicationName);
		this.logger=LoggerFactory.getLogger(getClass());
	}

	private T loadConfiguration(String pathname) throws BootstrapException {
		try {
			this.logger.info("Loading {} configuration {} from {}...",this.bootstrapId,this.configType.getName(),pathname);
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			T configuration = mapper.readValue(new File(pathname), this.configType);
			this.logger.info("{} configuration loaded: {}",this.bootstrapId,configuration);
			return configuration;
		} catch (Exception e) {
			String errorMessage = String.format("Could not load %s configuration",this.bootstrapId);
			logger.warn(errorMessage+". Full stacktrace follows: ",e);
			throw new BootstrapException(errorMessage,e);
		}
	}

	private Iterable<? extends Service> prepareServices(T config) throws BootstrapException {
		this.logger.info("Preparing {} services...",this.bootstrapId);
		try {
			this.logger.info("Configuring {} services using {}...",this.bootstrapId,config);
			Iterable<? extends Service> services = getServices(config);
			this.logger.info("{} services prepared.",this.bootstrapId);
			return services;
		} catch (BootstrapException e) {
			logger.warn(
				String.format("Could not prepare %s services. Full stacktrace follows:",this.bootstrapId),
				e);
			throw e;
		} catch (Exception e) {
			String errorMessage = String.format("Could not prepare %s services",this.bootstrapId);
			logger.warn(errorMessage+". Full stacktrace follows: ",e);
			throw new ApplicationInitializationException(errorMessage,e);
		}
	}

	private void initializeServices(Iterable<? extends Service> services) {
		this.serviceManager=new ServiceManager(services);
		this.listener=BootstrapListener.newInstance(this);
		this.serviceManager.addListener(this.listener);
		this.serviceManager.startAsync();
	}

	private void registerBootstrapCleaner() {
		Runtime.
			getRuntime().
				addShutdownHook(BootstrapCleaner.newInstance(this));
	}

	private void awaitInitialization() throws BootstrapException {
		this.logger.info("Awaiting {} service start up...",this.bootstrapId);
		int retries=0;
		BootstrapException failure=null;
		Stopwatch timer=Stopwatch.createStarted();
		while(!this.serviceManager.isHealthy() && failure==null) {
			try {
				this.serviceManager.awaitHealthy(INITIALIZATION_WAIT_TIMEOUT,INITIALIZATION_WAIT_TIMEUNIT);
			} catch (TimeoutException e) {
				retries++;
				if(retries>MAX_RETRIES) {
					timer.stop();
					failure=
						new ApplicationInitializationException(
							String.format("Could not start %s services after %d seconds",this.bootstrapId,timer.elapsed(TimeUnit.SECONDS)),
							e);
				}
			} catch(IllegalStateException e) {
				failure=
					new ApplicationInitializationException(
						String.format("Start up of %s services failed",this.bootstrapId),
						e,
						extractFailures(this.serviceManager.servicesByState().get(State.FAILED)));
			}
		}
		if(failure!=null) {
			this.logger.warn(failure.getMessage()+". Full stacktrace follows:",failure.getCause());
			this.logger.info("Aborting {} bootstrap...",this.bootstrapId);
			doTerminate(false);
			this.logger.info("Aborted {} bootstrap.",this.bootstrapId);
			throw failure;
		}
		this.logger.info("{} services started up succesfully.",this.bootstrapId);
	}

	private Map<String, Throwable> extractFailures(Iterable<? extends Service> failedServices) {
		Map<String,Throwable> failures=Maps.newLinkedHashMap();
		for(Service service:failedServices) {
			this.logger.trace(service.toString()+" : ",service.failureCause());
			failures.put(service.toString(),service.failureCause());
		}
		return failures;
	}

	private ApplicationShutdownException shutdownBootstrap() {
		ApplicationShutdownException shutdownFailure=null;
		try {
			this.logger.info("Shutting down {}...",this.bootstrapId);
			shutdown();
			this.logger.info("{} shutdown completed.",this.bootstrapId);
		} catch(Exception e) {
			String errorMessage = String.format("%s shutdown failed",this.bootstrapId);
			this.logger.warn(errorMessage+". Full stacktrace follows:",e);
			shutdownFailure=new ApplicationShutdownException(errorMessage,e);
		}
		return shutdownFailure;
	}

	private boolean verifyTermination(boolean fail, BootstrapException serviceFailure, BootstrapException shutdownFailure) throws BootstrapException {
		BootstrapException failure=serviceFailure;
		if(failure==null) {
			failure=shutdownFailure;
		}
		if(failure!=null) {
			this.logger.warn("{} terminated abruptly.",this.bootstrapId);
			if(fail) {
				throw failure;
			}
		}
		return failure==null;
	}

	private ApplicationShutdownException shutdownServices() {
		this.logger.info("Waiting for {} service shutdown...",this.bootstrapId);

		ImmutableCollection<Service> preFailedServices=
			this.serviceManager.servicesByState().get(State.FAILED);

		this.serviceManager.stopAsync();
		this.serviceManager.awaitStopped();

		ApplicationShutdownException serviceFailure=null;
		ImmutableCollection<Service> postFailedServices=
			this.serviceManager.servicesByState().get(State.FAILED);
		List<Service> failedServices=Lists.newArrayList(postFailedServices);
		failedServices.removeAll(preFailedServices);
		if(failedServices.isEmpty()) {
			this.logger.info("{} services shutdown succesfully.",this.bootstrapId);
		} else {
			String errorMessage=String.format("%s service shutdown failed.",this.bootstrapId);
			this.logger.warn(errorMessage,this.applicationName);
			Map<String, Throwable> failures = extractFailures(failedServices);
			serviceFailure = new ApplicationShutdownException(errorMessage,failures);
		}
		return serviceFailure;
	}

	public final String id() {
		return this.bootstrapId;
	}

	public final String applicationName() {
		return this.applicationName;
	}

	public final synchronized void run(String[] args) throws BootstrapException {
		checkArgument(args.length==1,"Usage: %s <configuration.yml>",this.applicationName);
		checkState(this.serviceManager==null,"%s already started",this.bootstrapId);
		this.logger.info("Bootstrapping {}...",this.bootstrapId);
		T config = loadConfiguration(args[0]);
		Iterable<? extends Service> services = prepareServices(config);
		initializeServices(services);
		awaitInitialization();
		registerBootstrapCleaner();
		this.logger.info("{} bootstrap completed.",this.bootstrapId);
	}

	public final synchronized void terminate() throws BootstrapException {
		if(this.serviceManager!=null) {
			doTerminate(true);
		}
	}

	private void doTerminate(boolean fail) throws BootstrapException {
		this.logger.info("Terminating {}...",this.bootstrapId);
		BootstrapException serviceFailure = shutdownServices();
		BootstrapException shutdownFailure = shutdownBootstrap();
		this.serviceManager=null;
		if(verifyTermination(fail,serviceFailure,shutdownFailure)) {
			this.logger.info("{} terminated succesfully.",this.bootstrapId);
		}
	}

	/**
	 * Handle any global resource deallocation.
	 */
	protected abstract void shutdown();

	/**
	 * @param config
	 * @return
	 * @throws JAXBException
	 * @throws IOException
	 */
	protected abstract <S extends Service> Iterable<S> getServices(T config) throws BootstrapException;

}