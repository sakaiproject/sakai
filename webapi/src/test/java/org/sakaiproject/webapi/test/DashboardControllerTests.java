/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.webapi.beans.DashboardRestBean;
import org.sakaiproject.webapi.controllers.DashboardController;
import org.springframework.test.util.ReflectionTestUtils;

public class DashboardControllerTests {

    @Mock
    private AnnouncementService announcementService;

    @Mock
    private PreferencesService preferencesService;

    @Mock
    private SakaiPersonManager sakaiPersonManager;

    @Mock
    private SecurityService securityService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private UserDirectoryService userDirectoryService;

    private DashboardController dashboardController;
    private AutoCloseable mocks;

    @Before
    public void setup() {

        mocks = MockitoAnnotations.openMocks(this);

        dashboardController = new DashboardController();
        dashboardController.setSessionManager(sessionManager);

        ReflectionTestUtils.setField(dashboardController, "announcementService", announcementService);
        ReflectionTestUtils.setField(dashboardController, "preferencesService", preferencesService);
        ReflectionTestUtils.setField(dashboardController, "sakaiPersonManager", sakaiPersonManager);
        ReflectionTestUtils.setField(dashboardController, "securityService", securityService);
        ReflectionTestUtils.setField(dashboardController, "userDirectoryService", userDirectoryService);
        ReflectionTestUtils.setField(dashboardController, "defaultHomeLayout", List.of());
        ReflectionTestUtils.setField(dashboardController, "homeWidgets", List.of());
        ReflectionTestUtils.setField(dashboardController, "maxNumberMotd", 5);
    }

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetUserDashboardExcludesDraftMotd() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("admin-user");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.isSuperUser()).thenReturn(false);

        Type userMutableType = mock(Type.class);
        when(sakaiPersonManager.getUserMutableType()).thenReturn(userMutableType);
        when(sakaiPersonManager.getSakaiPerson("admin-user", userMutableType)).thenReturn(Optional.empty());
        when(userDirectoryService.getOptionalUser("admin-user")).thenReturn(Optional.empty());

        Preferences preferences = mock(Preferences.class);
        when(preferencesService.getPreferences("admin-user")).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(null);

        AnnouncementMessage visibleMessage = mock(AnnouncementMessage.class);
        MessageHeader visibleHeader = mock(MessageHeader.class);
        when(visibleHeader.getDraft()).thenReturn(false);
        when(visibleMessage.getHeader()).thenReturn(visibleHeader);
        when(visibleMessage.getBody()).thenReturn("I'm here!");

        when(announcementService.getVisibleMessagesOfTheDay(null, 5, false)).thenReturn(List.of(visibleMessage));

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertEquals("I'm here!", bean.getMotd());
        assertFalse(bean.getMotd().contains("can't see me"));
    }

    @Test
    public void testGetUserDashboardWithNegativeMotdLimit() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("admin-user");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.isSuperUser()).thenReturn(false);

        Type userMutableType = mock(Type.class);
        when(sakaiPersonManager.getUserMutableType()).thenReturn(userMutableType);
        when(sakaiPersonManager.getSakaiPerson("admin-user", userMutableType)).thenReturn(Optional.empty());
        when(userDirectoryService.getOptionalUser("admin-user")).thenReturn(Optional.empty());

        Preferences preferences = mock(Preferences.class);
        when(preferencesService.getPreferences("admin-user")).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(null);

        when(announcementService.getVisibleMessagesOfTheDay(null, -1, false)).thenReturn(List.of());

        ReflectionTestUtils.setField(dashboardController, "maxNumberMotd", -1);

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertEquals("", bean.getMotd());
        verify(announcementService).getVisibleMessagesOfTheDay(null, -1, false);
    }
}
