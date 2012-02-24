/**********************************************************************************
*
* $Id$
*
***********************************************************************************
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
**********************************************************************************/

package org.sakaiproject.jsf.model;

/**
 * Beans that wish to be notified of interesting transitions in the
 * JSF request life cycle should implement this interface.
 */
public interface PhaseAware {
	/**
	 * Called after the component has finished the Process Validations
	 * phase. If validations are processed but the Update Model Values
	 * phase is never reached, then validations probably failed.
	 */
	public void endProcessValidators();

	/**
	 * Called after the component has finished the Update Model Values
	 * phase.
	 */
	public void endProcessUpdates();

	/**
	 * Called when the component is about to begin rendering.
	 */
	public void startRenderResponse();
}


