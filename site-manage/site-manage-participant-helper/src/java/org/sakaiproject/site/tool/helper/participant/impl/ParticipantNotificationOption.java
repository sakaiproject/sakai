/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

/** Whether newly added participants should receive notification email. */
public enum ParticipantNotificationOption {
    SEND("true", true),
    DO_NOT_SEND("false", false);

    private final String formValue;
    private final boolean sendsNotification;

    ParticipantNotificationOption(String formValue, boolean sendsNotification) {
        this.formValue = formValue;
        this.sendsNotification = sendsNotification;
    }

    public String getFormValue() {
        return formValue;
    }

    public boolean sendsNotification() {
        return sendsNotification;
    }

    public static ParticipantNotificationOption fromFormValue(String formValue) {
        for (ParticipantNotificationOption value : values()) {
            if (value.formValue.equals(formValue)) return value;
        }
        return null;
    }
}
