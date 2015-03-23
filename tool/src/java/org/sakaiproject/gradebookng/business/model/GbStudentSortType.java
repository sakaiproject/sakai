package org.sakaiproject.gradebookng.business.model;

/**
 * Types of sort order
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbStudentSortType {

	LAST_NAME,
	FIRST_NAME;
	
	public GbStudentSortType getDefault() {
		return GbStudentSortType.LAST_NAME;
	}
	
}
