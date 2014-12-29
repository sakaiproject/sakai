/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/kernel/trunk/api/src/main/java/org/sakaiproject/antivirus/api/VirusFoundException.java $
 * $Id: VirusFoundException.java 68335 2009-10-29 08:18:43Z david.horwitz@uct.ac.za $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.conditions.impl;

import java.util.Date;

/**
 * @author Zach A. Thomas <zach@aeroplanesoftware.com>
 *
 */
public class AssignmentUpdate {
	
	private Date dueDate;
	private boolean releasedToStudents;
	private boolean includedInCourseGrade;
	private Double pointValue;
	private String title;
	
	public Date getDueDate() {
		return dueDate;
	}
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public boolean isReleasedToStudents() {
		return releasedToStudents;
	}
	
	public boolean isNotReleasedToStudents() {
		return !releasedToStudents;
	}
	public void setReleasedToStudents(boolean releasedToStudents) {
		this.releasedToStudents = releasedToStudents;
	}
	public boolean isIncludedInCourseGrade() {
		return includedInCourseGrade;
	}
	
	public boolean isNotIncludedInCourseGrade() {
		return !includedInCourseGrade;
	}
	public void setIncludedInCourseGrade(boolean includedInCourseGrade) {
		this.includedInCourseGrade = includedInCourseGrade;
	}
	public Double getPointValue() {
		return pointValue;
	}
	public void setPointValue(Double pointValue) {
		this.pointValue = pointValue;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean dueDateHasNotPassed() {
		if (this.dueDate == null) {
			return true;
		} else {
			return ! new Date().after(this.dueDate);
		}
	}
	
	public boolean dueDateHasPassed() {
		if (this.dueDate == null) {
			return false;
		} else {
			return new Date().after(this.dueDate);
		}
	}

}
