package org.sakaiproject.tool.resetpass;


import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.components.UIOutput;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.resetpass.RetUser;

public class ConfirmProducer implements ViewComponentProducer {
	public static final String VIEW_ID = "confirm";

	public String getViewID() {
		// TODO Auto-generated method stub
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
		UIMessage.make(tofill,"message","confirm",parms);
		if (serverConfigurationService.getString("support.email", null) != null) {
			UIMessage.make(tofill, "supportMessage", "supportMessage");
			UILink.make(tofill, "supportEmail",serverConfigurationService.getString("support.email", ""));
		}
	}

}
