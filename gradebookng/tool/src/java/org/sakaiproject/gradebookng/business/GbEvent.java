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
