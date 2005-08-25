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
		// TODO Does this need to be internationalized?
		
		StringBuffer sb = new StringBuffer();
		for(Iterator iter = getDayList().iterator(); iter.hasNext();) {
			String day = (String)iter.next();
			sb.append(day);
			if(iter.hasNext()) {
				sb.append(",");
			}
		}
		
		sb.append(" ");
		sb.append(section.getStartTime());
		if(section.isStartTimeAm()) {
			sb.append("am");
		} else {
			sb.append("pm");
		}
		sb.append("-");
		sb.append(section.getEndTime());
		if(section.isEndTimeAm()) {
			sb.append("am");
		} else {
			sb.append("pm");
		}
		
		return sb.toString();
	}

	private List getDayList() {
		List list = new ArrayList();
		if(section.isMonday())
			list.add("M");
		if(section.isTuesday())
			list.add("T");
		if(section.isWednesday())
			list.add("W");
		if(section.isThursday())
			list.add("Th");
		if(section.isFriday())
			list.add("F");
		if(section.isSaturday())
			list.add("Sa");
		if(section.isSunday())
			list.add("Su");
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
