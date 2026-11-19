/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

/** Membership status selected for newly added participants. */
public enum ParticipantStatus {
    ACTIVE("active", true),
    INACTIVE("inactive", false);

    private final String formValue;
    private final boolean active;

    ParticipantStatus(String formValue, boolean active) {
        this.formValue = formValue;
        this.active = active;
    }

    public String getFormValue() {
        return formValue;
    }

    public boolean isActive() {
        return active;
    }

    public static ParticipantStatus fromFormValue(String formValue) {
        for (ParticipantStatus value : values()) {
            if (value.formValue.equals(formValue)) return value;
        }
        return null;
    }
}
