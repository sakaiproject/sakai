/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Verifies participant parsing against its public Spring-wired service contract. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParticipantHelperTestConfiguration.class)
public class ParticipantAccountParserTest {

    @Autowired private ParticipantAccountParser parser;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private UserDirectoryService userDirectoryService;

    @Before
    public void setUp() {
        reset(serverConfigurationService, userDirectoryService);
    }

    @Test
    public void parsesKnownOfficialAccountIntoServerOwnedEntry() throws Exception {
        User user = user("student0011", "user-1");
        Site site = mock(Site.class);
        when(userDirectoryService.getUserByEid("student0011")).thenReturn(user);

        ParticipantAccountParser.Result result = parser.parse(site, "student0011", new ArrayList<>(), null);

        assertEquals(1, result.entries().size());
        assertEquals("student0011", result.entries().get(0).getEid());
        assertTrue(result.messages().isEmpty());
    }

    @Test
    public void reportsExistingSiteMember() throws Exception {
        User user = user("student0011", "user-1");
        Site site = mock(Site.class);
        when(userDirectoryService.getUserByEid("student0011")).thenReturn(user);
        when(site.getUserRole("user-1")).thenReturn(mock(Role.class));

        ParticipantAccountParser.Result result = parser.parse(site, "student0011", new ArrayList<>(), null);

        assertTrue(result.entries().isEmpty());
        assertTrue(messageCodes(result).contains("add.existingpart.1"));
        assertTrue(messageCodes(result).contains("java.guest"));
    }

    @Test
    public void reportsDuplicateOfficialAccount() throws Exception {
        User user = user("student0011", "user-1");
        Site site = mock(Site.class);
        when(userDirectoryService.getUserByEid("student0011")).thenReturn(user);

        ParticipantAccountParser.Result result = parser.parse(site, "student0011\r\nstudent0011", new ArrayList<>(), null);

        assertEquals(1, result.entries().size());
        assertTrue(messageCodes(result).contains("add.duplicatedpart.single"));
    }

    @Test
    public void rejectsNonOfficialAccountFromBlockedDomain() {
        when(serverConfigurationService.getStrings(SiteAddParticipantHandler.SAK_PROP_INVALID_EMAIL_DOMAINS))
                .thenReturn(new String[] {"blocked.example.org"});

        ParticipantAccountParser.Result result = parser.parse(mock(Site.class), null, new ArrayList<>(),
                "guest@blocked.example.org,Guest,User");

        assertTrue(result.entries().isEmpty());
        assertTrue(messageCodes(result).contains("nonOfficialAccount.invalidEmailDomain"));
    }

    private User user(String eid, String id) {
        User user = mock(User.class);
        when(user.getEid()).thenReturn(eid);
        when(user.getId()).thenReturn(id);
        return user;
    }

    private List<String> messageCodes(ParticipantAccountParser.Result result) {
        return result.messages().stream().map(ParticipantMessage::getCode).collect(Collectors.toList());
    }
}
