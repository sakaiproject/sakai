package org.sakaiproject.gradebookng.business;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the category types allowed in the gradebook. Must be kept in sync with GradebookService if that ever changes.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbCategoryType {

	NO_CATEGORY(1),
	ONLY_CATEGORY(2),
	WEIGHTED_CATEGORY(3);

	private int value;

	GbCategoryType(final int value) {
		this.value = value;
	}

	/**
	 * Get the value for the type
	 *
	 * @return
	 */
	public int getValue() {
		return this.value;
	}

	// also need to maintain a map of the types so we can lookup the enum based on type
	private static Map<Integer, GbCategoryType> map = new HashMap<Integer, GbCategoryType>();

	static {
		for (final GbCategoryType type : GbCategoryType.values()) {
			map.put(type.value, type);
		}
	}

	public static GbCategoryType valueOf(final int value) {
		return map.get(value);
	}

}
