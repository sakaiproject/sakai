/**
 * Copyright (c) 2003-2015 The Apereo Foundation
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

import java.util.List;

/**
 * The list of permissions that can be assigned to a grader
 */
public enum GraderPermission {

    VIEW,
    GRADE,
    VIEW_COURSE_GRADE,
    NONE;

    /**
     * Return a lowercase version of the enum
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    /**
     * Helper to get the view and grade permissions as a list
     * Used in a few places
     * @return
     */
    public static List<String> getStandardPermissions() {
        return List.of(GraderPermission.VIEW.toString(), GraderPermission.GRADE.toString());
    }

}
