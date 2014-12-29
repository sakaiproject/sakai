package org.sakaiproject.tool.gradebook.ui.helpers.params;

import java.util.Date;

import uk.org.ponder.rsf.viewstate.SimpleViewParameters;

public class GradebookItemViewParams extends SimpleViewParameters {
	
	public Long assignmentId;
	public String contextId;
	public String finishURL;
	public String name;
	public boolean requireDueDate;
	public Date dueDate;
	/**
	 * This value will be a long representing the due date as a time (but passed as a string) sent from the caller
	 * to auto-populate the due date on the gradebook item if this is a new item
	 */
	public String dueDateTime;
	
	public GradebookItemViewParams() {}
	
	public GradebookItemViewParams(String viewId) {
		super(viewId);
	}
	
	public GradebookItemViewParams(String viewId, Long assignmentId) {
		super(viewId);
		this.assignmentId = assignmentId;
	}
	
	public GradebookItemViewParams(String viewId, String contextId, Long assignmentId){
		super(viewId);
		this.contextId = contextId;
		this.assignmentId = assignmentId;
	}
	
	public String getParseSpec(){
		return super.getParseSpec() + ",@1:contextId,@2:assignmentId,finishURL,name,dueDateTime";
	}
	
}