package org.sakaiproject.tool.gradebook.ui.helpers.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class AddGradebookItemViewParams extends SimpleViewParameters {
	
	public Long gradebookItemId;
	public String contextId;
	
	public AddGradebookItemViewParams() {}
	
	public AddGradebookItemViewParams(String viewId, String contextId, Long gradebookItemId){
		super(viewId);
		this.contextId = contextId;
		this.gradebookItemId = gradebookItemId;
	}
	
}