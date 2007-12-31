package org.sakaiproject.gradebook.tool.helper;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class PermissionsErrorProducer implements ViewComponentProducer{
	
	public static final String VIEW_ID = "permissions-error";
    public String getViewID() {
        return VIEW_ID;
    }
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	
    	//really do nothing
    }
}