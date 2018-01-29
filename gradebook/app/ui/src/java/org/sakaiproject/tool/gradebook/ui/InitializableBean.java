/**
 * Copyright (c) 2003-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.tool.gradebook.ui;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.jsf.model.PhaseAware;

@Slf4j
public abstract class InitializableBean implements PhaseAware {
	private transient boolean notValidated;

	/**
	 * JSF doesn't provide a way to configure an initialization method which will
	 * be called after the contructor and all framework setters. By convention,
	 * our backing beans use this method. It's triggered either by a Faces configuration
	 * file setting "configured" to true, or by a JSF component directly calling "startRenderResponse".
	 *
	 * For greater subclassing flexibility, the init method is not declared to be
	 * abstract.
	 */
	protected void init() {
	}

	/**
	 * Remember if JSF entered the Validations phase. If so, and if we never
	 * reach the Update Model Values phase, then validation failed. That may
	 * be of interest to the backing bean. For example, the backing bean
	 * may choose not to requery and reload data on a validation error.
	 */
	public void endProcessValidators() {
		setNotValidated(true);
		if (log.isDebugEnabled()) log.debug("endProcessValidators");
	}

	public void endProcessUpdates() {
		setNotValidated(false);
		if (log.isDebugEnabled()) log.debug("endProcessUpdates");
	}

	/**
	 * Call init() at the beginning of every request rendering.
	 * (This should also work to refresh session-scoped beans, but it's
	 * only been tested with request scope.)
	 */
	public void startRenderResponse() {
		if (log.isDebugEnabled()) log.debug("startRenderResponse notValidated=" + isNotValidated());
		init();
	}

	public boolean isNotValidated() {
		return notValidated;
	}
	public void setNotValidated(boolean notValidated) {
		this.notValidated = notValidated;
	}

	/**
	 * Signals that configuration is finished.
	 */
	public void setConfigured(boolean isConfigured) {
		if (log.isDebugEnabled()) log.debug("setConfigured " + isConfigured);
		if (isConfigured) {
			init();
		}
	}
}
