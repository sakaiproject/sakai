/**
 * Copyright (c) 2006-2016 The Apereo Foundation
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
package org.sakaiproject.tool.resetpass;


import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class ConfirmProducer implements ViewComponentProducer {
	public static final String VIEW_ID = "confirm";
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	public String getViewID() {
		return VIEW_ID;
	}

	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}
	
	private RetUser userBean;
	public void setUserBean(RetUser u){
		this.userBean = u;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2) {
		
		String[] parms = new String[] {userBean.getEmail()};
		boolean validatingAccounts = serverConfigurationService.getBoolean("siteManage.validateNewUsers", true);
		
		if (!validatingAccounts) {
			UIMessage.make(tofill,"message","confirm",parms);
		} else {
			UIMessage.make(tofill,"message","confirm.validate",parms);
		}
		// Get the instructions from the tool placement.
		Placement placement = toolManager.getCurrentPlacement();
		String supportInstructions = placement == null ? "" :  placement.getConfig().getProperty("supportInstructions");
		if(supportInstructions != null && !"".equals(supportInstructions)){
			UIVerbatim.make(tofill, "supportMessage", supportInstructions);
		}else if (serverConfigurationService.getString("mail.support", null) != null) {
			UIMessage.make(tofill, "supportMessage", "supportMessage");
			UILink.make(tofill, "supportEmail",serverConfigurationService.getString("mail.support", ""),"mailto:" + serverConfigurationService.getString("mail.support", ""));
		}
	}

}
