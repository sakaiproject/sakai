package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import org.sakaiproject.tool.gradebook.GradebookAssignment;

public class AssignmentCreator {

	public GradebookAssignment create(){
		GradebookAssignment togo = new GradebookAssignment();
		togo.setCounted(false); // default to false
		togo.setReleased(false); // default to false
		togo.setDueDate(null); // default to no due date
		togo.setExtraCredit(Boolean.FALSE);
		return togo;
	}
	
}
