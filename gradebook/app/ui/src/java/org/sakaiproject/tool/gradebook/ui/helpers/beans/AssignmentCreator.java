package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import org.sakaiproject.tool.gradebook.Assignment;

import java.util.Calendar;
import java.util.Date;

public class AssignmentCreator {

	public Assignment create(){
		Assignment togo = new Assignment();
		togo.setCounted(true);
		togo.setReleased(true);
		
		
		//Setting up Dates
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_YEAR, 7);
    	cal.set(Calendar.HOUR_OF_DAY, 17);
    	cal.set(Calendar.MINUTE, 0);
    	Date duedate = cal.getTime();
    	
    	togo.setDueDate(duedate);
		
		return togo;
	}
	
}
