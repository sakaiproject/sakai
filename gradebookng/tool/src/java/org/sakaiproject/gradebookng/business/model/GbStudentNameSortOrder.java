package org.sakaiproject.gradebookng.business.model;

/**
 * Types of sort order
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbStudentNameSortOrder {

	LAST_NAME,
	FIRST_NAME;

	/**
	 * Get the default sort type
	 * 
	 * @return
	 */
	public GbStudentNameSortOrder getDefault() {
		return GbStudentNameSortOrder.LAST_NAME;
	}

	/**
	 * Get the next sort type
	 * 
	 * @return
	 */
	public GbStudentNameSortOrder toggle() {
		return values()[(ordinal() + 1) % values().length];
	}

}
