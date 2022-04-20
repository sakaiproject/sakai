/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
 * Represents the category types allowed in the gradebook.
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public enum GradingCategoryType {

    NO_CATEGORY(1),
    ONLY_CATEGORY(2),
    WEIGHTED_CATEGORY(3);

    private int value;

    GradingCategoryType(int value) {
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
    private static Map<Integer, GradingCategoryType> map = new HashMap<>();

    static {
        for (final GradingCategoryType type : GradingCategoryType.values()) {
            map.put(type.value, type);
        }
    }

    public static GradingCategoryType valueOf(int value) {
        return map.get(value);
    }
}
