/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
import org.sakaiproject.section.api.SectionManager;
import org.sakaiproject.section.api.SectionManager.ExternalIntegrationConfig;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the options page.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class OptionsBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String EXTERNAL = "external";
	private static final String INTERNAL = "internal";

	private static final Log log = LogFactory.getLog(OptionsBean.class);
	
	private boolean selfRegister;
	private boolean selfSwitch;
	private String management;
	private boolean confirmMode;
	private boolean managementToggleEnabled;

	public void init() {
		// We don't need to initialize the bean when we're in confirm mode
		if(confirmMode) {
			return;
		}
		
		// The management toggle is not available in mandatory configurations
		ExternalIntegrationConfig config = getApplicationConfiguration();
		if(config == ExternalIntegrationConfig.AUTOMATIC_DEFAULT ||
				config == ExternalIntegrationConfig.MANUAL_DEFAULT) {
			managementToggleEnabled = true;
		}
		
		if(log.isDebugEnabled()) log.debug("OptionsBean.init()");
		String courseUuid = getCourse().getUuid();
		SectionManager sm = getSectionManager();
		this.selfRegister =  sm.isSelfRegistrationAllowed(courseUuid);
		this.selfSwitch =  sm.isSelfSwitchingAllowed(courseUuid);
		if(sm.isExternallyManaged(courseUuid)) {
			management = EXTERNAL;
		} else {
			management = INTERNAL;
		}
	}

	public String confirmExternallyManaged() {
		return update(false);
	}

	public String update() {
		return update(managementToggleEnabled);
	}
	
	public String update(boolean checkForConfirmation) {
		if(!isSectionOptionsManagementEnabled()) {
			// This should never happen
			log.warn("Updating section options not permitted for user " + getUserUid());
			return "overview";
		}
		
		String courseUuid = getCourse().getUuid();

		if(checkForConfirmation) {
			boolean oldExternallyManagedSetting = getSectionManager().isExternallyManaged(courseUuid);
			if(EXTERNAL.equals(management) && ! oldExternallyManagedSetting) {
				// The user is switching from manual to automatic.  Switch to confirm mode.
				confirmMode = true;
				return null;
			}
		}
		if(managementToggleEnabled) {
			if(log.isInfoEnabled()) log.info("*** Persisting externallyManaged as " + management);
			getSectionManager().setExternallyManaged(courseUuid, EXTERNAL.equals(management));
		}
		
		// If we're externally managed, these will automatically be set to false
		if(INTERNAL.equals(management) || management == null) {
			getSectionManager().setJoinOptions(courseUuid, selfRegister, selfSwitch);
		}
		
		// TODO Customize the message depending on the action taken
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

	/**
	 * See http://issues.apache.org/jira/browse/MYFACES-570 for the reason for this boolean/String hack
	 * @return
	 */
	public String getManagement() {
		if(log.isDebugEnabled()) log.debug("---- management = " + management);
		return management;
	}

	/**
	 * See http://issues.apache.org/jira/browse/MYFACES-570 for the reason for this boolean/String hack
	 * @return
	 */
	public void setManagement(String management) {
		if(log.isDebugEnabled()) log.debug("---- setting management to " + management);
		this.management = management;
	}

	public boolean isConfirmMode() {
		return confirmMode;
	}

	public void setConfirmMode(boolean confirmMode) {
		this.confirmMode = confirmMode;
	}
	
	public boolean isManagementToggleEnabled() {
		return managementToggleEnabled;
	}

}
