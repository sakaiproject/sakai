package org.sakaiproject.tool.gradebook.ui.helpers.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class HelperAwareViewParams extends SimpleViewParameters{
	
	public String finishURL;
	
	public HelperAwareViewParams() {}
	
	public HelperAwareViewParams(String viewID) {
		super(viewID);
	}
	
	public HelperAwareViewParams(String viewID, String finishURL) {
		super(viewID);
		this.finishURL = finishURL;
	}
}