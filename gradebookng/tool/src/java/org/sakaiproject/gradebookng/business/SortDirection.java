package org.sakaiproject.gradebookng.business;

/**
 * Tracks a directional sort and provides convenience methods to get the default and allow a quick toggle to the opposite direction.
 */
public enum SortDirection {

	ASCENDING,
	DESCENDING;

	/**
	 * Get the default sort direction
	 *
	 * @return the default SortDirection, which is SortDirection.ASCENDING
	 */
	public static SortDirection getDefault() {
		return ASCENDING;
	}

	/**
	 * Get the next sort direction
	 *
	 * @return the next SortDirection
	 */
	public SortDirection toggle() {
		return values()[(ordinal() + 1) % values().length];
	}

}
