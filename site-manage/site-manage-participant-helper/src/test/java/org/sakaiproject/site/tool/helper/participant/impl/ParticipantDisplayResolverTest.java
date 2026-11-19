/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Verifies participant display resolution through its Spring-wired service contract. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParticipantHelperTestConfiguration.class)
public class ParticipantDisplayResolverTest {

    @Autowired private ParticipantDisplayResolver resolver;
    @Autowired private UserDirectoryService userDirectoryService;

    @Before
    public void setUp() {
        reset(userDirectoryService);
    }

    @Test
    public void resolvesSingleParticipant() throws Exception {
        User user = mock(User.class);
        when(userDirectoryService.getUserByEid("student0011")).thenReturn(user);
        when(user.getDisplayId()).thenReturn("student0011");
        when(user.getSortName()).thenReturn("Student, One");

        List<ParticipantDisplayResolver.ParticipantDisplay> displays =
                resolver.displays(List.of(new UserRoleEntry("student0011", "access")));

        assertEquals(1, displays.size());
        assertEquals("student0011 (Student, One)", displays.get(0).displayName());
    }
}
