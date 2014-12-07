package org.sakaiproject.gradebookng.business;

/**
 * List of possible sort orders
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum SortOrder {

	LAST_NAME (1),
	FIRST_NAME (2),
	USER_ID (3);
	
	private int sort;
	
	SortOrder(int sort) {
		this.sort = sort;
	}
	
	public int getValue() {
		return this.sort;
	}
	
	public SortOrder getDefault() {
		return SortOrder.LAST_NAME;
	}
}
