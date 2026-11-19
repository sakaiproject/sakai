/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import java.util.List;

/** Read-only view of the current Add Participants wizard state. */
public record ParticipantWizardSnapshot(
        List<UserRoleEntry> participants,
        String officialAccountParticipant,
        String nonOfficialAccountParticipant,
        String statusChoice,
        String roleChoice,
        String sameRoleChoice,
        String emailNotiChoice,
        boolean active) {

    static ParticipantWizardSnapshot from(ParticipantWizardState state) {
        return new ParticipantWizardSnapshot(
                List.copyOf(state.getUserRoleEntries()),
                state.getOfficialAccountParticipant(),
                state.getNonOfficialAccountParticipant(),
                state.getStatus().getFormValue(),
                state.getRoleMode().getFormValue(),
                state.getSameRoleChoice(),
                state.getNotificationOption().getFormValue(),
                state.getStatus().isActive());
    }
}
