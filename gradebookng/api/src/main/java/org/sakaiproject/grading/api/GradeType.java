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
package org.sakaiproject.grading.api;

import java.util.HashMap;
import java.util.Map;

/**
 * The grading types that a gradebook could be configured as
 */
public enum GradeType {

    POINTS,
    PERCENTAGE,
    LETTER;

    /*
    private int value;

    GradeType(int value) {
        this.value = value;
    }
    */

    /**
     * Get the value for the type
     *
     * @return
     */
    /*
    public int getValue() {
        return this.value;
    }

    // maintain a map of the types so we can lookup the enum based on type
    private static Map<Integer, GradeType> map = new HashMap<>();

    static {
        for (final GradeType type : GradeType.values()) {
            map.put(type.value, type);
        }
    }

    public static GradeType valueOf(final int value) {
        return map.get(value);
    }
    */
}
