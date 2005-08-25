/**********************************************************************************
*
* $Id: $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The Regents of the University of Michigan,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.section.decorator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Decorates CourseSections for display in the UI.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSectionDecorator {

	protected CourseSection section;
	protected String categoryForDisplay;

	public CourseSectionDecorator(CourseSection section, String categoryForDisplay) {
		this.section = section;
		this.categoryForDisplay = categoryForDisplay;
	}
	
	public CourseSectionDecorator() {
		// Needed for serialization
	}

	// Decorator methods
	public String getCategoryForDisplay() {
		return categoryForDisplay;
	}
	
	public String getMeetingTimes() {
		String am = JsfUtil.getLocalizedMessage("time_of_day_am");
		String pm = JsfUtil.getLocalizedMessage("time_of_day_pm");
		String daySepChar = JsfUtil.getLocalizedMessage("day_of_week_sep_char");
		String timeSepChar = JsfUtil.getLocalizedMessage("time_sep_char");
		
		StringBuffer sb = new StringBuffer();
		
		// Days of the week
		for(Iterator iter = getDayList().iterator(); iter.hasNext();) {
			String day = (String)iter.next();
			sb.append(JsfUtil.getLocalizedMessage(day));
			if(iter.hasNext()) {
				sb.append(daySepChar);
			}
		}

		// Start time
		sb.append(" ");
		sb.append(section.getStartTime());
		if(section.isStartTimeAm()) {
			sb.append(am);
		} else {
			sb.append(pm);
		}

		// End time
		sb.append(timeSepChar);
		sb.append(section.getEndTime());
		if(section.isEndTimeAm()) {
			sb.append(am);
		} else {
			sb.append(pm);
		}
		
		return sb.toString();
	}

	private List getDayList() {
		List list = new ArrayList();
		if(section.isMonday())
			list.add("day_of_week_monday_abbrev");
		if(section.isTuesday())
			list.add("day_of_week_tuesday_abbrev");
		if(section.isWednesday())
			list.add("day_of_week_wednesday_abbrev");
		if(section.isThursday())
			list.add("day_of_week_thursday_abbrev");
		if(section.isFriday())
			list.add("day_of_week_friday_abbrev");
		if(section.isSaturday())
			list.add("day_of_week_saturday_abbrev");
		if(section.isSunday())
			list.add("day_of_week_sunday_abbrev");
		return list;
	}
		
	// Delegate methods
	public String getCategory() {
		return section.getCategory();
	}

	public Course getCourse() {
		return section.getCourse();
	}

	public String getEndTime() {
		return section.getEndTime();
	}

	public String getLocation() {
		return section.getLocation();
	}

	public int getMaxEnrollments() {
		return section.getMaxEnrollments();
	}

	public String getStartTime() {
		return section.getStartTime();
	}

	public String getTitle() {
		return section.getTitle();
	}

	public String getUuid() {
		return section.getUuid();
	}

	public boolean isEndTimeAm() {
		return section.isEndTimeAm();
	}

	public boolean isFriday() {
		return section.isFriday();
	}

	public boolean isMonday() {
		return section.isMonday();
	}

	public boolean isSaturday() {
		return section.isSaturday();
	}

	public boolean isStartTimeAm() {
		return section.isStartTimeAm();
	}

	public boolean isSunday() {
		return section.isSunday();
	}

	public boolean isThursday() {
		return section.isThursday();
	}

	public boolean isTuesday() {
		return section.isTuesday();
	}

	public boolean isWednesday() {
		return section.isWednesday();
	}
	
}



/**********************************************************************************
 * $Id: $
 *********************************************************************************/
