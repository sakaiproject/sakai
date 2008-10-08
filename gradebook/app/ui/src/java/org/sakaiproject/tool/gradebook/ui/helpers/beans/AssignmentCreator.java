package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import org.sakaiproject.tool.gradebook.Assignment;

public class AssignmentCreator {

	public Assignment create(){
		Assignment togo = new Assignment();
		togo.setCounted(true);
		togo.setReleased(true);
		togo.setDueDate(null); // default to no due date
		return togo;
	}
	
}
