/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** Verifies that session state does not share mutable collections with a request. */
public class ParticipantWizardStateTest {

    @Test
    public void copyIsIndependentFromSourceState() {
        ParticipantWizardState source = new ParticipantWizardState();
        source.setOfficialAccountEidOnly(new ArrayList<>(List.of("student0011")));
        source.setUserRoleEntries(new ArrayList<>(List.of(new UserRoleEntry("student0011", "access"))));

        ParticipantWizardState copy = source.copy();
        copy.getOfficialAccountEidOnly().add("student0012");
        copy.getUserRoleEntries().set(0, copy.getUserRoleEntries().get(0).withRole("maintain"));

        assertEquals(List.of("student0011"), source.getOfficialAccountEidOnly());
        assertEquals("access", source.getUserRoleEntries().get(0).getRole());
    }
}
