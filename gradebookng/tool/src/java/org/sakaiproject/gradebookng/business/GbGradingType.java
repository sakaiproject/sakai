package org.sakaiproject.gradebookng.business;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the grading types allowed in the gradebook. Must be kept in sync with GradebookService if that ever changes.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GbGradingType {

	POINTS(1),
	PERCENTAGE(2);

	private int value;

	GbGradingType(final int value) {
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
	private static Map<Integer, GbGradingType> map = new HashMap<Integer, GbGradingType>();

	static {
		for (final GbGradingType type : GbGradingType.values()) {
			map.put(type.value, type);
		}
	}

	public static GbGradingType valueOf(final int value) {
		return map.get(value);
	}

}
