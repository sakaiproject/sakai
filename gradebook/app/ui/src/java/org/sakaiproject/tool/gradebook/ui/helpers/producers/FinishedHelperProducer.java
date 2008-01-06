package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import org.sakaiproject.tool.gradebook.ui.helpers.params.FinishedHelperViewParams;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.htmlutil.HTMLUtil;

public class FinishedHelperProducer implements ViewComponentProducer, ViewParamsReporter
{
	  public static final String VIEWID = "FinishedHelper";
	  
	  public String getViewID() {
	    return VIEWID;
	  }
	
	  public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
		  FinishedHelperViewParams params = (FinishedHelperViewParams) viewparams;
		  
		  //Really do nothing, let the JS do it all, call thickbox close window and Ajax call
		 
	  }
	  
	  public ViewParameters getViewParameters() {
	        return new FinishedHelperViewParams();
	    }
	
}