package org.sakaiproject.gradebookng.business;

/**
 * Tracks the direction of a sort and provides a convenience method to allow a quick toggle to the opposite direction.
 */
public enum SortDirection {

	ASCENDING,
	DESCENDING;

	/**
	 * Get the next sort direction
	 *
	 * @return the next SortDirection
	 */
	public SortDirection toggle() {
		return values()[(ordinal() + 1) % values().length];
	}
}
