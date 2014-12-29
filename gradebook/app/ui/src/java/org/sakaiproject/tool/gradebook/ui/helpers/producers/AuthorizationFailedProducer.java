package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class AuthorizationFailedProducer implements ViewComponentProducer{
	
	public static final String VIEW_ID = "authorizationFailed";
    public String getViewID() {
        return VIEW_ID;
    }
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	
    	//really do nothing
    	UIMessage.make(tofill, "permissions_error", "gradebook.authorizationFailed.permissions_error");
    }
}