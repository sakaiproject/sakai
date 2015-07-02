package org.sakaiproject.gradebookng.business;

/**
 * Tracks the direction of a sort
 *
 */
public enum SortDirection {
	
	ASCENDING,
	DESCENDING;
	
	/**
	 * Get the next sort type
	 * @return
	 */
	public SortDirection toggle() {
		return values()[(ordinal() + 1) % values().length];
	}
}
