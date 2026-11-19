/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 */
package org.sakaiproject.site.tool.helper.participant.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/** Verifies participant wizard request handling with Spring-wired internal services. */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ParticipantHelperTestConfiguration.class)
public class SiteAddParticipantHandlerTest {

    @Autowired private AuthzGroupService authzGroupService;
    @Autowired private ParticipantAccountParser participantAccountParser;
    @Autowired private ParticipantRealmUpdater participantRealmUpdater;
    @Autowired private ServerConfigurationService serverConfigurationService;
    @Autowired private SessionManager sessionManager;
    @Autowired private SiteService siteService;

    private SiteAddParticipantHandler handler;

    @Before
    public void setUp() {
        reset(authzGroupService, serverConfigurationService, sessionManager, siteService);
        handler = new SiteAddParticipantHandler(authzGroupService, mock(CourseManagementService.class),
                serverConfigurationService, sessionManager, siteService, mock(ToolManager.class),
                participantRealmUpdater, participantAccountParser);
    }

    @Test
    public void rejectsInvalidCsrfTokenWithoutChangingWizardState() {
        Session session = mock(Session.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE)).thenReturn("valid-token");

        boolean accepted = handler.submitAdd("invalid-token", "student0011", "guest@example.org",
                ParticipantStatus.INACTIVE);

        assertFalse(accepted);
        assertNull(handler.snapshot().officialAccountParticipant());
        assertNull(handler.snapshot().nonOfficialAccountParticipant());
        assertEquals(ParticipantStatus.ACTIVE.getFormValue(), handler.snapshot().statusChoice());
        assertTrue(hasError("java.badcsrftoken"));
        verify(sessionManager, never()).getCurrentToolSession();
    }

    @Test
    public void initializationFailureBlocksParticipantOperations() throws Exception {
        Session session = mock(Session.class);
        ToolSession toolSession = mock(ToolSession.class);
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        when(session.getAttribute(UsageSessionService.SAKAI_CSRF_SESSION_ATTRIBUTE)).thenReturn("csrf-token");
        when(toolSession.getAttribute(ParticipantConstants.HELPER_SITE_ID_ATTRIBUTE)).thenReturn("missing-site");
        when(siteService.getSite("missing-site")).thenThrow(new IllegalStateException("Site unavailable"));

        handler.beginStep();

        assertTrue(hasError("java.realm"));
        assertFalse(handler.canAddParticipant());
        assertFalse(handler.submitAdd("csrf-token", "student0011", null, ParticipantStatus.ACTIVE));
        assertFalse(handler.submitRoles("csrf-token", ParticipantRoleMode.SAME_ROLE, "access", null));
        assertFalse(handler.finish("csrf-token", ParticipantNotificationOption.DO_NOT_SEND));
        assertTrue(hasError("java.realm"));
        verify(siteService, times(1)).getSite("missing-site");
        verify(siteService, never()).allowUpdateSiteMembership("missing-site");
    }

    private boolean hasError(String code) {
        return handler.getMessages().stream().anyMatch(message -> code.equals(message.getCode())
                && message.getSeverity() == ParticipantMessage.Severity.ERROR);
    }
}
