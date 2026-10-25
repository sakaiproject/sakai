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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

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
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.portal.api.PortalConstants;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.view.SiteStatsViewService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesEdit;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.webapi.beans.DashboardRestBean;
import org.sakaiproject.webapi.beans.DashboardWidgetTile;
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
    private ServerConfigurationService serverConfigurationService;

    @Mock
    private EntityManager entityManager;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SiteService siteService;

    @Mock
    private UserDirectoryService userDirectoryService;

    @Mock
    private FormattedText formattedText;

    @Mock
    private SiteStatsViewService siteStatsViewService;

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
        ReflectionTestUtils.setField(dashboardController, "serverConfigurationService", serverConfigurationService);
        ReflectionTestUtils.setField(dashboardController, "entityManager", entityManager);
        ReflectionTestUtils.setField(dashboardController, "siteService", siteService);
        ReflectionTestUtils.setField(dashboardController, "userDirectoryService", userDirectoryService);
        ReflectionTestUtils.setField(dashboardController, "formattedText", formattedText);
        ReflectionTestUtils.setField(dashboardController, "siteStatsViewService", siteStatsViewService);
        ReflectionTestUtils.setField(dashboardController, "defaultHomeLayout", List.of());
        ReflectionTestUtils.setField(dashboardController, "defaultHomeLayoutAdmin", List.of());
        ReflectionTestUtils.setField(dashboardController, "defaultCourseLayoutStudent", List.of());
        ReflectionTestUtils.setField(dashboardController, "defaultCourseLayoutInstructor", List.of());
        ReflectionTestUtils.setField(dashboardController, "homeWidgets", List.of());
        ReflectionTestUtils.setField(dashboardController, "courseWidgets", List.of());
        ReflectionTestUtils.setField(dashboardController, "maxNumberMotd", 5);
        ReflectionTestUtils.setField(dashboardController, "sitestatsDashboardEnabled", true);

        when(formattedText.processFormattedText(anyString(), isNull(), any(FormattedText.Level.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));
        when(serverConfigurationService.getBoolean(eq("dashboard.grades.reveal-on-click"), eq(true))).thenReturn(true);
    }

    @After
    public void tearDown() throws Exception {

        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testGetUserDashboardMapsVisibleMotdBodies() throws Exception {

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

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertEquals("I'm here!\nStill here!", bean.getMotd());
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

        ReflectionTestUtils.setField(dashboardController, "homeWidgets", List.of("announcements", "calendar"));

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertEquals(List.of("announcements", "calendar"), bean.getWidgets());
        assertFalse(bean.getWidgetLayout().stream().anyMatch(t -> "courses".equals(t.getId())));
        assertEquals("announcements", bean.getWidgetLayout().get(0).getId());
        assertEquals(1, bean.getWidgetLayout().get(0).getW());
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
        when(serverConfigurationService.getStringList("dashboard.home.widget.layout", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.home.widget.layout.admin", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.course.widget.layout.student", null)).thenReturn(List.of());
        when(serverConfigurationService.getStringList("dashboard.course.widget.layout.instructor", null)).thenReturn(List.of());
        when(serverConfigurationService.getInt("dashboard.home.motd.display", 1)).thenReturn(5);
        when(siteStatsViewService.getHomeDashboardWidgetIds(SiteService.ADMIN_SITE_ID)).thenReturn(List.of());

        dashboardController.init();

        List<String> homeWidgets = (List<String>) ReflectionTestUtils.getField(dashboardController, "homeWidgets");
        List<String> defaultHomeLayout = (List<String>) ReflectionTestUtils.getField(dashboardController, "defaultHomeLayout");
        List<String> defaultCourseLayoutStudent =
                (List<String>) ReflectionTestUtils.getField(dashboardController, "defaultCourseLayoutStudent");

        assertEquals("courses", homeWidgets.get(0));
        assertEquals("courses", defaultHomeLayout.get(0));
        assertTrue(defaultCourseLayoutStudent.contains("sitestats-student-grades"));

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertEquals(List.of("courses", "calendar", "announcements", "grades", "forums"),
            bean.getWidgetLayout().stream().map(DashboardWidgetTile::getId).toList());
        assertEquals("courses", bean.getWidgetLayout().get(0).getId());
        assertEquals(2, bean.getWidgetLayout().get(0).getH());
        assertTrue(bean.getGradesRevealOnClick());
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
        when(site.getReference()).thenReturn("/site/site1");
        when(properties.getProperty("dashboard-config")).thenReturn(null);
        when(securityService.isSuperUser()).thenReturn(false);
        when(securityService.unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString())).thenReturn(true);
        stubApplyCourseLayoutPrefs("admin-user");

        String payload = "<img src=x onerror=alert(1)>";
        String clean = "<img src=\"x\" />";
        when(formattedText.processFormattedText(eq(payload), isNull(), eq(FormattedText.Level.HIGH))).thenReturn(clean);

        DashboardRestBean bean = new DashboardRestBean();
        bean.setOverview(payload);
        bean.setProgramme("Programme");
        bean.setWidgetLayout(List.of(DashboardWidgetTile.of("announcements")));
        bean.setHeroWidth("wide");
        bean.setShowImage(Boolean.TRUE);

        dashboardController.saveSiteDashboard(siteId, bean);

        verify(site).setDescription(clean);
        verify(siteService).save(site);
    }

    @Test
    public void testSaveSiteDashboardMemberDoesNotUpdateSite() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("student");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        String siteId = "site1";
        Site site = mock(Site.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getReference()).thenReturn("/site/site1");
        when(securityService.isSuperUser()).thenReturn(false);
        when(securityService.unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString())).thenReturn(false);
        stubApplyCourseLayoutPrefs("student");

        DashboardRestBean bean = new DashboardRestBean();
        bean.setOverview("Should not save");
        bean.setProgramme("Should not save");
        bean.setWidgetLayout(List.of(DashboardWidgetTile.of("calendar")));
        bean.setHeroWidth("narrow");
        bean.setShowImage(Boolean.FALSE);

        dashboardController.saveSiteDashboard(siteId, bean);

        verify(site, never()).setDescription(anyString());
        verify(siteService, never()).save(site);
    }

    @Test
    public void testGetSiteDashboardIncludesSitestatsAllWidgetsWhenPermitted() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("instructor");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        String siteId = "course1";
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getTitle()).thenReturn("Course 1");
        when(site.getShortDescription()).thenReturn("Programme");
        when(site.getDescription()).thenReturn("Overview");
        when(site.getReference()).thenReturn("/site/course1");
        when(site.getProperties()).thenReturn(properties);
        when(properties.getProperty("dashboard-config")).thenReturn(null);
        when(properties.getProperty(Site.PROP_COURSE_IMAGE_URL)).thenReturn(null);
        when(serverConfigurationService.getString("dashboard.course.hero.width", "wide")).thenReturn("wide");
        when(serverConfigurationService.getBoolean("dashboard.course.hero.showImage", true)).thenReturn(true);
        when(securityService.isSuperUser()).thenReturn(false);
        when(securityService.unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString())).thenReturn(true);
        stubEmptyCourseLayoutPrefs("instructor");

        ReflectionTestUtils.setField(dashboardController, "courseWidgets", List.of("announcements", "calendar"));
        ReflectionTestUtils.setField(dashboardController, "defaultCourseLayoutInstructor",
                List.of("announcements", "calendar", "sitestats-visits", "sitestats-submissions"));

        when(siteStatsViewService.getCourseDashboardWidgetIds(siteId))
                .thenReturn(List.of("visits", "submissions", "grades"));

        DashboardRestBean bean = dashboardController.getSiteDashboard(siteId);

        assertTrue(bean.getWidgets().contains("announcements"));
        assertTrue(bean.getWidgets().contains("sitestats-visits"));
        assertTrue(bean.getWidgets().contains("sitestats-submissions"));
        assertTrue(bean.getWidgets().contains("sitestats-grades"));
        assertFalse(bean.getWidgets().contains("sitestats-platform-health"));
        assertEquals("wide", bean.getHeroWidth());
        assertTrue(bean.getShowImage());
        assertTrue(bean.getSiteEditable());
        assertTrue(bean.getLayoutEditable());
        assertEquals(List.of("announcements", "calendar", "sitestats-visits", "sitestats-submissions"),
                bean.getWidgetLayout().stream().map(DashboardWidgetTile::getId).toList());
    }

    @Test
    public void testGetSiteDashboardUsesUserCourseLayoutAndHeroOverride() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("student");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        String siteId = "course1";
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getTitle()).thenReturn("Course 1");
        when(site.getShortDescription()).thenReturn("Programme");
        when(site.getDescription()).thenReturn("Overview");
        when(site.getReference()).thenReturn("/site/course1");
        when(site.getProperties()).thenReturn(properties);
        when(properties.getProperty("dashboard-config"))
                .thenReturn("{\"showImage\":false,\"heroWidth\":\"full\"}");
        when(properties.getProperty(Site.PROP_COURSE_IMAGE_URL)).thenReturn(null);
        when(securityService.isSuperUser()).thenReturn(false);
        when(securityService.unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString())).thenReturn(false);

        Preferences preferences = mock(Preferences.class);
        ResourceProperties prefProps = mock(ResourceProperties.class);
        when(preferencesService.getPreferences("student")).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(prefProps);
        when(prefProps.getProperty("courseLayouts")).thenReturn(
                "{\"course1\":{\"widgetLayout\":[{\"id\":\"grades\",\"w\":1,\"h\":1}],\"heroWidth\":\"narrow\"}}");

        ReflectionTestUtils.setField(dashboardController, "courseWidgets", List.of("announcements", "grades"));
        ReflectionTestUtils.setField(dashboardController, "defaultCourseLayoutStudent", List.of("announcements"));

        when(siteStatsViewService.getCourseDashboardWidgetIds(siteId)).thenReturn(List.of());

        DashboardRestBean bean = dashboardController.getSiteDashboard(siteId);

        assertFalse(bean.getShowImage());
        assertEquals("narrow", bean.getHeroWidth());
        assertFalse(bean.getSiteEditable());
        assertTrue(bean.getLayoutEditable());
        assertEquals(List.of("grades"),
                bean.getWidgetLayout().stream().map(DashboardWidgetTile::getId).toList());
    }

    @Test
    public void testGetSiteDashboardOmitsSitestatsWhenNotPermitted() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("student");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        String siteId = "course1";
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getTitle()).thenReturn("Course 1");
        when(site.getShortDescription()).thenReturn("Programme");
        when(site.getDescription()).thenReturn("Overview");
        when(site.getReference()).thenReturn("/site/course1");
        when(site.getProperties()).thenReturn(properties);
        when(properties.getProperty("dashboard-config")).thenReturn(null);
        when(properties.getProperty(Site.PROP_COURSE_IMAGE_URL)).thenReturn(null);
        when(serverConfigurationService.getString("dashboard.course.hero.width", "wide")).thenReturn("wide");
        when(serverConfigurationService.getBoolean("dashboard.course.hero.showImage", true)).thenReturn(true);
        when(securityService.isSuperUser()).thenReturn(false);
        when(securityService.unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString())).thenReturn(false);
        stubEmptyCourseLayoutPrefs("student");

        ReflectionTestUtils.setField(dashboardController, "courseWidgets", List.of("announcements"));
        ReflectionTestUtils.setField(dashboardController, "defaultCourseLayoutStudent",
                List.of("announcements", "sitestats-student-visits"));

        when(siteStatsViewService.getCourseDashboardWidgetIds(siteId)).thenReturn(List.of());

        DashboardRestBean bean = dashboardController.getSiteDashboard(siteId);

        assertEquals(List.of("announcements"), bean.getWidgets());
        assertFalse(bean.getWidgets().stream().anyMatch(id -> id.startsWith("sitestats-")));
        assertEquals(List.of("announcements"),
                bean.getWidgetLayout().stream().map(DashboardWidgetTile::getId).toList());
    }

    @Test
    public void testGetSiteDashboardStudentDefaultIncludesOwnSitestatsWhenPermitted() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("student");
        when(sessionManager.getCurrentSession()).thenReturn(session);

        String siteId = "course1";
        Site site = mock(Site.class);
        ResourceProperties properties = mock(ResourceProperties.class);
        when(siteService.getSite(siteId)).thenReturn(site);
        when(site.getTitle()).thenReturn("Course 1");
        when(site.getShortDescription()).thenReturn("Programme");
        when(site.getDescription()).thenReturn("Overview");
        when(site.getReference()).thenReturn("/site/course1");
        when(site.getProperties()).thenReturn(properties);
        when(properties.getProperty("dashboard-config")).thenReturn(null);
        when(properties.getProperty(Site.PROP_COURSE_IMAGE_URL)).thenReturn(null);
        when(serverConfigurationService.getString("dashboard.course.hero.width", "wide")).thenReturn("wide");
        when(serverConfigurationService.getBoolean("dashboard.course.hero.showImage", true)).thenReturn(true);
        when(securityService.isSuperUser()).thenReturn(false);
        when(securityService.unlock(eq(SiteService.SECURE_UPDATE_SITE), anyString())).thenReturn(false);
        stubEmptyCourseLayoutPrefs("student");

        ReflectionTestUtils.setField(dashboardController, "courseWidgets", List.of("announcements", "calendar"));
        ReflectionTestUtils.setField(dashboardController, "defaultCourseLayoutStudent",
                List.of("calendar", "announcements", "sitestats-student-visits", "sitestats-student-submissions"));

        when(siteStatsViewService.getCourseDashboardWidgetIds(siteId))
                .thenReturn(List.of("student-visits", "student-submissions"));

        DashboardRestBean bean = dashboardController.getSiteDashboard(siteId);

        assertTrue(bean.getWidgets().contains("sitestats-student-visits"));
        assertTrue(bean.getWidgets().contains("sitestats-student-submissions"));
        assertFalse(bean.getWidgets().contains("sitestats-visits"));
        assertFalse(bean.getSiteEditable());
        assertEquals(List.of("calendar", "announcements", "sitestats-student-visits", "sitestats-student-submissions"),
                bean.getWidgetLayout().stream().map(DashboardWidgetTile::getId).toList());
    }

    @Test
    public void testGetUserDashboardAdminDefaultIncludesSitestatsWhenPermitted() throws Exception {

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

        ReflectionTestUtils.setField(dashboardController, "homeWidgets", List.of("courses", "announcements"));
        ReflectionTestUtils.setField(dashboardController, "defaultHomeLayout",
                List.of("courses", "announcements"));
        ReflectionTestUtils.setField(dashboardController, "defaultHomeLayoutAdmin",
                List.of("courses", "announcements", "sitestats-platform-health"));

        when(siteStatsViewService.getHomeDashboardWidgetIds(SiteService.ADMIN_SITE_ID))
                .thenReturn(List.of("platform-health"));

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertTrue(bean.getWidgets().contains("sitestats-platform-health"));
        assertEquals(List.of("courses", "announcements", "sitestats-platform-health"),
                bean.getWidgetLayout().stream().map(DashboardWidgetTile::getId).toList());
    }

    private void stubEmptyCourseLayoutPrefs(String userId) {

        Preferences preferences = mock(Preferences.class);
        when(preferencesService.getPreferences(userId)).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(null);
    }

    @SuppressWarnings("unchecked")
    private void stubApplyCourseLayoutPrefs(String userId) {

        doAnswer(invocation -> {
            Consumer<PreferencesEdit> consumer = invocation.getArgument(1);
            PreferencesEdit edit = mock(PreferencesEdit.class);
            ResourcePropertiesEdit props = mock(ResourcePropertiesEdit.class);
            when(edit.getPropertiesEdit("dashboard-config")).thenReturn(props);
            when(props.getProperty("courseLayouts")).thenReturn(null);
            consumer.accept(edit);
            return true;
        }).when(preferencesService).applyEditWithAutoCommit(eq(userId), any(Consumer.class));
    }

    @Test
    public void testGetUserDashboardIncludesSitestatsAdminWidgetsWhenPermitted() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("admin-user");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.isSuperUser()).thenReturn(true);

        Type userMutableType = mock(Type.class);
        when(sakaiPersonManager.getUserMutableType()).thenReturn(userMutableType);
        when(sakaiPersonManager.getSakaiPerson("admin-user", userMutableType)).thenReturn(Optional.empty());
        when(userDirectoryService.getOptionalUser("admin-user")).thenReturn(Optional.empty());

        Preferences preferences = mock(Preferences.class);
        when(preferencesService.getPreferences("admin-user")).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(null);
        when(announcementService.getVisibleMessagesOfTheDay(null, 5, false)).thenReturn(List.of());

        ReflectionTestUtils.setField(dashboardController, "homeWidgets", List.of("courses", "announcements"));
        when(siteStatsViewService.getHomeDashboardWidgetIds(SiteService.ADMIN_SITE_ID))
                .thenReturn(List.of("platform-health", "client-environment"));

        DashboardRestBean bean = dashboardController.getUserDashboard("admin-user");

        assertTrue(bean.getWidgets().contains("courses"));
        assertTrue(bean.getWidgets().contains("sitestats-platform-health"));
        assertTrue(bean.getWidgets().contains("sitestats-client-environment"));
        assertFalse(bean.getWidgets().contains("sitestats-visits"));
    }

    @Test
    public void testGetUserDashboardOmitsSitestatsAdminWidgetsWithoutPermission() throws Exception {

        Session session = mock(Session.class);
        when(session.getUserId()).thenReturn("user1");
        when(sessionManager.getCurrentSession()).thenReturn(session);
        when(securityService.isSuperUser()).thenReturn(false);

        Type userMutableType = mock(Type.class);
        when(sakaiPersonManager.getUserMutableType()).thenReturn(userMutableType);
        when(sakaiPersonManager.getSakaiPerson("user1", userMutableType)).thenReturn(Optional.empty());
        when(userDirectoryService.getOptionalUser("user1")).thenReturn(Optional.empty());

        Preferences preferences = mock(Preferences.class);
        when(preferencesService.getPreferences("user1")).thenReturn(preferences);
        when(preferences.getProperties("dashboard-config")).thenReturn(null);
        when(announcementService.getVisibleMessagesOfTheDay(null, 5, false)).thenReturn(List.of());

        ReflectionTestUtils.setField(dashboardController, "homeWidgets", List.of("courses", "announcements"));
        when(siteStatsViewService.getHomeDashboardWidgetIds(SiteService.ADMIN_SITE_ID)).thenReturn(List.of());

        DashboardRestBean bean = dashboardController.getUserDashboard("user1");

        assertEquals(List.of("courses", "announcements"), bean.getWidgets());
        assertFalse(bean.getWidgets().stream().anyMatch(id -> id.startsWith("sitestats-")));
    }
}
