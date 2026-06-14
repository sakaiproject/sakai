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
package org.sakaiproject.webapi.controllers.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.any;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.portal.api.PortalConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.webapi.beans.DashboardRestBean;
import org.sakaiproject.webapi.controllers.DashboardController;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { WebApiTestConfiguration.class, DashboardController.class })
public class DashboardControllerTests extends BaseControllerTests {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private PreferencesService preferencesService;

    @Autowired
    private SakaiPersonManager sakaiPersonManager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private ServerConfigurationService serverConfigurationService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private SiteService siteService;

    @Autowired
    private UserDirectoryService userDirectoryService;

    @Autowired
    private FormattedText formattedText;

    @Autowired
    private DashboardController controller;

    @Before
    public void setup() {

        buildMockMvc(controller);

        //when(formattedText.processFormattedText(anyString(), isNull(), any(FormattedText.Level.class)))
        //    .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    public void testGetUserDashboardMapsVisibleMotdBodies() throws Exception {

        reset(announcementService, preferencesService, serverConfigurationService);

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
        when(visibleMessage.getBody()).thenReturn("I'm here!");

        AnnouncementMessage secondVisibleMessage = mock(AnnouncementMessage.class);
        when(secondVisibleMessage.getBody()).thenReturn("Still here!");

        when(announcementService.getVisibleMessagesOfTheDay(null, 5, false))
            .thenReturn(List.of(visibleMessage, secondVisibleMessage));

        ReflectionTestUtils.setField(controller, "maxNumberMotd", 5);

        mockMvc.perform(get("/users/admin-user/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.motd").value("I'm here!\nStill here!"))
            .andDo(document("get-user-dashboard"));
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

        ReflectionTestUtils.setField(controller, "maxNumberMotd", -1);

        mockMvc.perform(get("/users/admin-user/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.motd").value(""));

        verify(announcementService).getVisibleMessagesOfTheDay(null, -1, false);
    }

    @Test
    public void testGetUserDashboardReturnsSavedLayoutUnchangedWhenConfiguredWidgetsExcludeCourses() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("admin-user");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.isSuperUser()).thenReturn(false);

        Type userMutableType = mock(Type.class);
        when(sakaiPersonManager.getUserMutableType()).thenReturn(userMutableType);
        when(sakaiPersonManager.getSakaiPerson("admin-user", userMutableType)).thenReturn(Optional.empty());
        when(userDirectoryService.getOptionalUser("admin-user")).thenReturn(Optional.empty());

        Preferences preferences = mock(Preferences.class);
        ResourceProperties props = mock(ResourceProperties.class);
        when(preferencesService.getPreferences("admin-user")).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(props);
        when(props.getProperty("widgetLayout")).thenReturn("[\"announcements\",\"calendar\"]");
        when(announcementService.getVisibleMessagesOfTheDay(null, 5, false)).thenReturn(List.of());

        ReflectionTestUtils.setField(controller, "homeWidgets", List.of("announcements", "calendar"));

        mockMvc.perform(get("/users/admin-user/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.widgets", is(List.of("announcements", "calendar"))))
            .andExpect(jsonPath("$.widgetLayout", not(hasItem("courses"))));

        DashboardRestBean bean = controller.getUserDashboard("admin-user");
        assertEquals("announcements", bean.getWidgetLayout().get(0));
    }

    @Test
    public void testGetUserDashboardWithNoSavedLayoutUsesDefaultHomeLayout() throws Exception {

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
        when(announcementService.getVisibleMessagesOfTheDay(null, 5, false)).thenReturn(List.of());

        when(serverConfigurationService.getBoolean(PortalConstants.PROP_DASHBOARD_TASKS_ENABLED, false)).thenReturn(false);
        when(serverConfigurationService.getStringList("dashboard.course.widgets", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.home.widgets", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.course.widget.layout1", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.course.widget.layout2", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.course.widget.layout3", null)).thenReturn(List.of());
        when(serverConfigurationService.getInt("dashboard.home.motd.display", 1)).thenReturn(5);

        controller.init();

        List<String> homeWidgets = (List<String>) ReflectionTestUtils.getField(controller, "homeWidgets");
        List<String> defaultHomeLayout = (List<String>) ReflectionTestUtils.getField(controller, "defaultHomeLayout");

        assertEquals("courses", homeWidgets.get(0));
        assertEquals("courses", defaultHomeLayout.get(0));

        mockMvc.perform(get("/users/admin-user/dashboard"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.widgetLayout", is(List.of("courses", "announcements", "calendar", "grades", "forums"))));

        DashboardRestBean bean = controller.getUserDashboard("admin-user");
        assertEquals("courses", bean.getWidgetLayout().get(0));
    }

    @Test
    public void testSaveSiteDashboardSanitizesOverviewOnSave() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("admin-user");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        String siteId = "site1";
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getProperties()).thenReturn(properties);

        String payload = "<img src=x onerror=alert(1)>";
        String clean = "<img src=\"x\" />";
        when(formattedText.processFormattedText(eq(payload), isNull(), eq(FormattedText.Level.HIGH))).thenReturn(clean);

        DashboardRestBean bean = new DashboardRestBean();
        bean.setOverview(payload);
        bean.setProgramme("Programme");
        bean.setWidgetLayout(List.of("announcements"));
        bean.setTemplate(1);

        ObjectMapper jsonMapper = new ObjectMapper();
        String json = jsonMapper.writeValueAsString(bean);

        mockMvc.perform(put("/sites/" + siteId + "/dashboard").contentType("application/json").content(json))
          .andExpect(status().isOk())
          .andDo(document("save-site-dashboard"));

        verify(site).setDescription(clean);
    }
}
