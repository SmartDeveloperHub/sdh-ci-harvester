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
 *   Artifact    : org.smartdeveloperhub.harvesters.ci.util:ci-util-bootstrap:0.2.0-SNAPSHOT
 *   Bundle      : ci-util-bootstrap-0.2.0-SNAPSHOT.jar
 * #-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=#
 */
package org.smartdeveloperhub.util.bootstrap;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

final public class CustomConfig {

	static final String FAILED_BOOTSTRAP_START_UP_MESSAGE="failedBootstrapStartUp";
	static final String FAILED_BOOTSTRAP_SHUTDOWN_MESSAGE="failedBootstrapShutdown";

	static final String FAILED_SERVICE_START_UP_MESSAGE="failedServiceStartUp";
	static final String FAILED_SERVICE_SHUTDOWN_MESSAGE="failedServiceShutdown";

	private int setting;

	private boolean failServiceStartUp;
	private boolean failServiceShutdown;

	private boolean failBootstrapStartUp;
	private boolean failBootstrapShutdown;
	private boolean delayInitialization;

	public CustomConfig() {
	}

	public void setSetting(int mySetting) {
		this.setting = mySetting;
	}

	public int getSetting() {
		return setting;
	}

	public void setFailServiceStartUp(boolean failInitialization) {
		this.failServiceStartUp = failInitialization;
	}

	public boolean isFailServiceStartUp() {
		return this.failServiceStartUp;
	}

	public void setFailServiceShutdown(boolean failShutdown) {
		this.failServiceShutdown = failShutdown;
	}

	public boolean isFailServiceShutdown() {
		return this.failServiceShutdown;
	}

	public void setFailBootstrapStartUp(boolean failBootstrapStartUp) {
		this.failBootstrapStartUp = failBootstrapStartUp;
	}

	public boolean isFailBootstrapStartUp() {
		return this.failBootstrapStartUp;
	}

	public void setFailBootstrapShutdown(boolean failBootstrapShutdown) {
		this.failBootstrapShutdown = failBootstrapShutdown;
	}

	public boolean isFailBootstrapShutdown() {
		return this.failBootstrapShutdown;
	}

	public void setDelayInitialization(boolean delayInitialization) {
		this.delayInitialization = delayInitialization;
	}

	public boolean isDelayInitialization() {
		return this.delayInitialization;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.setting);
	}

	@Override
	public boolean equals(Object obj) {
		boolean result=false;
		if(obj instanceof CustomConfig) {
			CustomConfig that=(CustomConfig)obj;
			result=Objects.equal(this.setting,that.setting);
		}
		return result;
	}

	@Override
	public String toString() {
		return
			MoreObjects.
				toStringHelper(getClass()).
					add("setting", this.setting).
					add("delayInitialization", this.delayInitialization).
					add("failBootstrapStartUp", this.failBootstrapStartUp).
					add("failBootstrapShutdown", this.failBootstrapShutdown).
					add("failServiceStartup", this.failServiceStartUp).
					add("failServiceShutdown", this.failServiceShutdown).
					toString();
	}

}