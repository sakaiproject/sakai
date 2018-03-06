/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
