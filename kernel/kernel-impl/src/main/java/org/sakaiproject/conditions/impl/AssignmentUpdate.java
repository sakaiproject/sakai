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
