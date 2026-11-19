/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

/** The role-assignment mode selected for an Add Participants operation. */
public enum ParticipantRoleMode {
    SAME_ROLE("sameRole"),
    DIFFERENT_ROLE("differentRole");

    private final String formValue;

    ParticipantRoleMode(String formValue) {
        this.formValue = formValue;
    }

    public String getFormValue() {
        return formValue;
    }

    public static ParticipantRoleMode fromFormValue(String formValue) {
        for (ParticipantRoleMode value : values()) {
            if (value.formValue.equals(formValue)) return value;
        }
        return null;
    }
}
