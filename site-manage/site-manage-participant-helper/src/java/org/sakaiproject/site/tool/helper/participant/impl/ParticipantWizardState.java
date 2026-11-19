/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * The user-entered state for one Add Participants operation. Stored in the current
 * Sakai tool session so it never resides in a singleton controller or service.
 */
@Getter
@Setter
public class ParticipantWizardState implements Serializable {

    private static final long serialVersionUID = 1L;

    private String officialAccountParticipant;
    private List<String> officialAccountEidOnly = new ArrayList<>();
    private String nonOfficialAccountParticipant;
    private ParticipantRoleMode roleMode = ParticipantRoleMode.SAME_ROLE;
    private String sameRoleChoice;
    private ParticipantStatus status = ParticipantStatus.ACTIVE;
    private ParticipantNotificationOption notificationOption = ParticipantNotificationOption.DO_NOT_SEND;
    private List<UserRoleEntry> userRoleEntries = new ArrayList<>();

    public ParticipantWizardState() {
    }

    private ParticipantWizardState(ParticipantWizardState source) {
        officialAccountParticipant = source.officialAccountParticipant;
        officialAccountEidOnly = new ArrayList<>(source.officialAccountEidOnly);
        nonOfficialAccountParticipant = source.nonOfficialAccountParticipant;
        roleMode = source.roleMode;
        sameRoleChoice = source.sameRoleChoice;
        status = source.status;
        notificationOption = source.notificationOption;
        userRoleEntries = new ArrayList<>(source.userRoleEntries);
    }

    public ParticipantWizardState copy() {
        return new ParticipantWizardState(this);
    }
}
