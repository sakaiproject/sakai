package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import org.sakaiproject.tool.gradebook.Assignment;
import java.util.Date;

public class AssignmentCreator {

	public Assignment create(){
		Assignment togo = new Assignment();
		togo.setCounted(false);
		togo.setReleased(false);
		return togo;
	}
	
}
