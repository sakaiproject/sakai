package org.sakaiproject.component.app.scheduler.jobs;

/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationCommand;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.evaluation.logic.externals.EvalTransition;
import org.sakaiproject.evaluation.model.constant.EvalConstants;

/**
 * Carry out work at a scheduled time, calling methods of EvalTransition.
 * 
 * @author rwellis
 *
 */
public class EvalScheduledInvocation implements ScheduledInvocationCommand {
	
	private static Log log = LogFactory.getLog(EvalScheduledInvocation.class);
	
	public EvalScheduledInvocation() {
		
	}
	
	public void execute(String opaqueContext) {
		
		if(opaqueContext == null || opaqueContext.equals("")) {
			log.warn(this + " opaqueContext is null or empty");
			return;
		}
		
		//opaqueContext provides evaluation id and job type
		String[] parts = opaqueContext.split("/");
		if(parts.length != 2) {
			log.warn(this + " opaqueContext parts != 2 " + opaqueContext);
		}
		String id = parts[0];
		String jobType = parts[1];
		Long evalId = Long.valueOf(id);
		
		//call EvalTransition method
		EvalTransition evalTransition = (EvalTransition)ComponentManager.get("org.sakaiproject.evaluation.logic.externals.EvalTransition");
		if(EvalConstants.SCHEDULED_CMD_FIX_STATE.equals(jobType)) {
			evalTransition.fixState(evalId);
		}
		else if(EvalConstants.SCHEDULED_CMD_SEND_ACTIVE.equals(jobType)) {
			evalTransition.sendActive(evalId);
		}
		else if(EvalConstants.SCHEDULED_CMD_SEND_CREATED.equals(jobType)) {
			evalTransition.sendCreated(evalId);
		}
		else if(EvalConstants.SCHEDULED_CMD_SEND_REMINDER.equals(jobType)) {
			evalTransition.sendReminder(evalId);
		}
		else if(EvalConstants.SCHEDULED_CMD_SEND_VIEWABLE.equals(jobType)) {
			evalTransition.sendViewable(evalId);
		}
	}
}

