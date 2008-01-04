package org.sakaiproject.tool.gradebook.ui.helpers.params;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class GradeGradebookItemViewParams extends SimpleViewParameters {
	
	public Long assignmentId;
	public String contextId;
	public String userId;
	
	public GradeGradebookItemViewParams(){}
	
	public GradeGradebookItemViewParams(String viewId, String contextId, Long assignmentId, String userId){
		super(viewId);
		this.assignmentId = assignmentId;
		this.contextId = contextId;
		this.userId = userId;
	}
}