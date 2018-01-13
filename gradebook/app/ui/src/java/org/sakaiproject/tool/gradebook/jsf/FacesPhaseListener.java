/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation, The MIT Corporation
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
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook.jsf;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FacesPhaseListener implements PhaseListener {
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	public void beforePhase(PhaseEvent e) {
		if (log.isDebugEnabled()) log.debug("BEFORE " + e.getPhaseId());
	}
	public void afterPhase(PhaseEvent e) {
		if (log.isDebugEnabled()) log.debug("AFTER " + e.getPhaseId());
	}
}
