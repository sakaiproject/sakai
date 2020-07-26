/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.gradebookng.business;

public enum GbEvent {
    ADD_ASSIGNMENT("gradebook.newItem"),
    UPDATE_ASSIGNMENT("gradebook.updateAssignment"),
    DELETE_ASSIGNMENT("gradebook.deleteItem"),
    UPDATE_GRADE("gradebook.updateItemScore"),
    UPDATE_UNGRADED("gradebook.updateUngradedScores"),
    UPDATE_COMMENT("gradebook.comment"),
    STUDENT_VIEW("gradebook.studentView"),
    EXPORT("gradebook.export"),
    IMPORT_BEGIN("gradebook.importBegin"),
    IMPORT_COMPLETED("gradebook.importCompleted"),
    OVERRIDE_COURSE_GRADE("gradebook.overrideCourseGrade"),
    UPDATE_SETTINGS("gradebook.updateSettings");

    private String event;

    GbEvent(final String event) {
        this.event = event;
    }

    public String getEvent() {
        return this.event;
    }
}
