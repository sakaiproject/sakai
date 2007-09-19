package org.sakaiproject.tool.resetpass;


import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.components.UIOutput;

import org.sakaiproject.tool.resetpass.RetUser;

public class ConfirmProducer implements ViewComponentProducer {
	public static final String VIEW_ID = "confirm";

	public String getViewID() {
		// TODO Auto-generated method stub
		return VIEW_ID;
	}

	
	private RetUser userBean;
	public void setUserBean(RetUser u){
		this.userBean = u;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters arg1,
			ComponentChecker arg2) {
		
		String[] parms = new String[] {userBean.getEmail()};
		UIMessage.make(tofill,"message","confirm",parms);
	}

}
