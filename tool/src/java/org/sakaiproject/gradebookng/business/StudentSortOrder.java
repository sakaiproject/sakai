package org.sakaiproject.gradebookng.business;

/**
 * List of possible sort orders
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum StudentSortOrder {

	LAST_NAME (1),
	FIRST_NAME (2),
	USER_EID (3);
	
	private int sort;
	
	StudentSortOrder(int sort) {
		this.sort = sort;
	}
	
	public int getValue() {
		return this.sort;
	}
	
	public StudentSortOrder getDefault() {
		return StudentSortOrder.LAST_NAME;
	}
	
}
