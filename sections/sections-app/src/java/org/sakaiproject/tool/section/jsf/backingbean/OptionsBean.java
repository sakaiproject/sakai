/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the options page.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class OptionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(OptionsBean.class);
	
	private boolean selfRegister;
	private boolean selfSwitch;

	public void init() {
		Course course = getCourse();
		selfRegister = course.isSelfRegistrationAllowed();
		selfSwitch = course.isSelfSwitchingAllowed();
		
	}

	public String update() {
		if(!isSectionOptionsManagementEnabled()) {
			// This should never happen
			log.warn("Updating section options not permitted for user " + getUserUid());
			return "overview";
		}
		Course course = getCourse();
		getSectionManager().setSelfRegistrationAllowed(course.getUuid(), selfRegister);
		getSectionManager().setSelfSwitchingAllowed(course.getUuid(), selfSwitch);
		
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage("options_update_successful"));
		return "overview";
	}
	
	public boolean isSelfRegister() {
		return selfRegister;
	}

	public void setSelfRegister(boolean selfRegister) {
		this.selfRegister = selfRegister;
	}

	public boolean isSelfSwitch() {
		return selfSwitch;
	}

	public void setSelfSwitch(boolean selfSwitch) {
		this.selfSwitch = selfSwitch;
	}

}
