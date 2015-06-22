package org.sakaiproject.gradebookng.business;

/**
 * Tracks the direction of a sort
 *
 */
public enum SortDirection {
	
	ASCENDING,
	DESCENDING;
	
	// cycles the values
	public SortDirection toggle() {
		return values()[(ordinal() + 1) % values().length];
	}
}
