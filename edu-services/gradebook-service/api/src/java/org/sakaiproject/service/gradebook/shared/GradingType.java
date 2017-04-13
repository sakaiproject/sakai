package org.sakaiproject.service.gradebook.shared;

import java.util.HashMap;
import java.util.Map;

/**
 * The grading types that a gradebook could be configured as
 */
public enum GradingType {

	POINTS(1),
	PERCENTAGE(2),
	LETTER(3);
	
	private int value;

	GradingType(int value) {
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

	// maintain a map of the types so we can lookup the enum based on type
	private static Map<Integer, GradingType> map = new HashMap<Integer, GradingType>();

	static {
		for (final GradingType type : GradingType.values()) {
			map.put(type.value, type);
		}
	}

	public static GradingType valueOf(final int value) {
		return map.get(value);
	}
	

}
