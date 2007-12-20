package org.sakaiproject.gradebook.tool.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class AddGradebookItemViewParams extends SimpleViewParameters {
	
	public Long gradebookItemId;
	
	public AddGradebookItemViewParams() {}
	
	public AddGradebookItemViewParams(String viewId, Long gradebookItemId){
		super(viewId);
		this.gradebookItemId = gradebookItemId;
	}
	
}