/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitemanage.api.UserNotificationProvider;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Verifies participant wizard outcomes through the public handler contract. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParticipantHelperTestConfiguration.class)
public class SiteAddParticipantHandlerTest {

    private static final String CSRF_TOKEN = "csrf-token";
    private static final String SITE_ID = "site-1";
    private static final String SITE_REFERENCE = "/site/" + SITE_ID;

    @Autowired private SiteAddParticipantHandler handler;
    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private SiteService siteService;
    @Autowired private UserNotificationProvider notificationProvider;
    @Autowired private SessionManager sessionManager;
    @Autowired private UserDirectoryService userDirectoryService;

    private Site site;
    private AuthzGroup realm;
    private Role role;
    private User firstUser;
    private User secondUser;

    @Before
    public void setUp() throws Exception {
        reset(authzGroupService, siteService, notificationProvider, sessionManager, userDirectoryService);

        site = mock(Site.class);
        realm = mock(AuthzGroup.class);
        role = mock(Role.class);
        firstUser = user("student0011", "user-1");
        secondUser = user("student0012", "user-2");
        Session session = mock(Session.class);

        when(session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE)).thenReturn(CSRF_TOKEN);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(site.getId()).thenReturn(SITE_ID);
        when(site.getReference()).thenReturn(SITE_REFERENCE);
        when(realm.getId()).thenReturn(SITE_REFERENCE);
        when(realm.getRole("access")).thenReturn(role);
        when(role.getId()).thenReturn("access");
        when(authzGroupService.getAuthzGroup(SITE_REFERENCE)).thenReturn(realm);
        when(authzGroupService.allowUpdate(SITE_REFERENCE)).thenReturn(true);
        when(userDirectoryService.getUserByEid("student0011")).thenReturn(firstUser);
        when(userDirectoryService.getUserByEid("student0012"))
                .thenReturn(secondUser)
                .thenThrow(new UserNotDefinedException("student0012"));
        when(userDirectoryService.userReference("user-1")).thenReturn("/user/user-1");

        handler.startNewOperation();
        handler.site = site;
        handler.siteId = SITE_ID;
        handler.realm = realm;
        handler.setRoles(new ArrayList<>(List.of(role)));
    }

    @Test
    public void retriesOnlyParticipantsRejectedByAPartialCommit() throws Exception {
        assertTrue(handler.submitAdd(CSRF_TOKEN, "student0011\r\nstudent0012", null, ParticipantStatus.ACTIVE));
        assertTrue(handler.submitRoles(CSRF_TOKEN, ParticipantRoleMode.SAME_ROLE, "access", List.of()));

        assertFalse(handler.finish(CSRF_TOKEN, ParticipantNotificationOption.SEND));
        assertEquals(List.of("student0012"), handler.getParticipants().stream().map(UserRoleEntry::getEid).toList());

        assertFalse(handler.finish(CSRF_TOKEN, ParticipantNotificationOption.SEND));
        verify(realm, times(1)).addMember("user-1", "access", true, false);
        verify(authzGroupService, times(1)).save(realm);
        verify(notificationProvider, times(1)).notifyAddedParticipant(false, firstUser, site);
        verify(notificationProvider, times(1)).notifyAddedParticipant(anyBoolean(), any(), any());
    }

    private User user(String eid, String id) {
        User user = mock(User.class);
        when(user.getEid()).thenReturn(eid);
        when(user.getId()).thenReturn(id);
        return user;
    }
}
