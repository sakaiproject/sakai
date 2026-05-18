/**
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
package org.sakaiproject.site.tool.helper.order.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.order.model.ToolOrderPage;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;

public class SitePageEditHandlerTest {

    private SitePageEditHandler handler;
    private SiteService siteService;
    private ToolManager toolManager;
    private SessionManager sessionManager;
    private ServerConfigurationService serverConfigurationService;
    private ContentHostingService contentHostingService;
    private AuthzGroupService authzGroupService;
    private EventTrackingService eventTrackingService;
    private ToolSession toolSession;
    private Site site;

    @Before
    public void setUp() throws Exception {
        handler = new SitePageEditHandler();
        siteService = mock(SiteService.class);
        toolManager = mock(ToolManager.class);
        sessionManager = mock(SessionManager.class);
        serverConfigurationService = mock(ServerConfigurationService.class);
        contentHostingService = mock(ContentHostingService.class);
        authzGroupService = mock(AuthzGroupService.class);
        eventTrackingService = mock(EventTrackingService.class);
        toolSession = mock(ToolSession.class);
        site = mock(Site.class);

        handler.setSiteService(siteService);
        handler.setToolManager(toolManager);
        handler.setSessionManager(sessionManager);
        handler.setServerConfigurationService(serverConfigurationService);
        handler.setContentHostingService(contentHostingService);
        handler.setAuthzGroupService(authzGroupService);
        handler.setEventTrackingService(eventTrackingService);

        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        when(toolSession.getAttribute("sakai.tool.helper.id.siteId")).thenReturn("site1");
        when(siteService.getSite("site1")).thenReturn(site);
        when(siteService.allowUpdateSite("site1")).thenReturn(true);
        when(site.getId()).thenReturn("site1");
        when(site.getReference()).thenReturn("/site/site1");
        when(site.getType()).thenReturn("course");
        when(siteService.isUserSite("site1")).thenReturn(false);
        when(serverConfigurationService.getCommaSeparatedListAsSet(anyString())).thenReturn(Collections.emptySet());
        when(serverConfigurationService.getBoolean(anyString(), anyBoolean())).thenAnswer(invocation -> invocation.getArgument(1));
        when(serverConfigurationService.getToolsRequired("course")).thenReturn(Collections.emptyList());
    }

    @Test
    public void getCurrentSiteRejectsMissingContextSiteId() throws Exception {
        when(toolSession.getAttribute("sakai.tool.helper.id.siteId")).thenReturn(" ");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> handler.getCurrentSite());

        assertEquals("No current site in context", exception.getMessage());
        verify(siteService, never()).getSite(anyString());
    }

    @Test
    public void getDoneUrlUsesPageOrderHelperDoneUrl() {
        Tool currentTool = mock(Tool.class);
        when(currentTool.getId()).thenReturn("sakai.siteinfo");
        when(toolManager.getCurrentTool()).thenReturn(currentTool);
        when(toolSession.getAttribute("sakai.siteinfo" + Tool.HELPER_DONE_URL)).thenReturn("/");
        when(toolSession.getAttribute("sakai-site-pageorder-helper" + Tool.HELPER_DONE_URL))
                .thenReturn("/site-info?sakai.tool.helper.idMain=done&panel=Main");

        assertEquals("/site-info?sakai.tool.helper.idMain=done&panel=Main", handler.getDoneUrl());
    }

    @Test
    public void getDoneUrlFallsBackWithoutToolSession() {
        when(sessionManager.getCurrentToolSession()).thenReturn(null);

        assertEquals("/", handler.getDoneUrl());
    }

    @Test
    public void getPageReturnsRealEdgeMetadata() {
        SitePage page1 = page("page1");
        SitePage page2 = page("page2");
        when(site.getOrderedPages()).thenReturn(Arrays.asList(page1, page2));
        when(site.getPage("page1")).thenReturn(page1);
        when(site.getPage("page2")).thenReturn(page2);

        ToolOrderPage firstPage = handler.getPage("page1");
        ToolOrderPage lastPage = handler.getPage("page2");

        assertTrue(firstPage.isFirst());
        assertFalse(firstPage.isLast());
        assertFalse(lastPage.isFirst());
        assertTrue(lastPage.isLast());
    }

    @Test
    public void reorderPagesPersistsPositionsAndMarksTopRefresh() throws Exception {
        SitePage page1 = page("page1");
        SitePage page2 = page("page2");
        when(site.getOrderedPages()).thenReturn(Arrays.asList(page1, page2));
        when(site.getPage("page1")).thenReturn(page1);
        when(site.getPage("page2")).thenReturn(page2);
        Event event = mock(Event.class);
        when(eventTrackingService.newEvent("pageorder.reorder", "/site/site1", false)).thenReturn(event);

        handler.reorderPages(Arrays.asList("page2", "page1"));

        verify(page2).setPosition(0);
        verify(page1).setPosition(1);
        verify(site).setCustomPageOrdered(true);
        verify(siteService).save(site);
        verify(toolSession).setAttribute(SitePageEditHandler.ATTR_TOP_REFRESH, Boolean.TRUE);
        verify(eventTrackingService).post(event);
    }

    @Test
    public void reorderPagesRejectsMissingPages() throws Exception {
        SitePage page1 = page("page1");
        SitePage page2 = page("page2");
        when(site.getOrderedPages()).thenReturn(Arrays.asList(page1, page2));

        assertThrows(IllegalArgumentException.class, () -> handler.reorderPages(Collections.singletonList("page1")));

        verify(siteService, never()).save(site);
    }

    @Test
    public void reorderPagesRejectsNullOrder() throws Exception {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.reorderPages(null));

        assertEquals("Page order must include at least one page id", exception.getMessage());
        verify(siteService, never()).save(site);
    }

    @Test
    public void reorderPagesRejectsEmptyOrder() throws Exception {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.reorderPages(Collections.emptyList()));

        assertEquals("Page order must include at least one page id", exception.getMessage());
        verify(siteService, never()).save(site);
    }

    @Test
    public void deletePageRejectsRequiredTool() throws Exception {
        ToolConfiguration tool = tool("sakai.required");
        Properties placementConfig = new Properties();
        SitePage page = page("page1", Collections.singletonList(tool));
        when(site.getPage("page1")).thenReturn(page);
        when(tool.getConfig()).thenReturn(placementConfig);
        when(tool.getPlacementConfig()).thenReturn(placementConfig);
        when(serverConfigurationService.getToolsRequired("course")).thenReturn(Collections.singletonList("sakai.required"));
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.emptyList());
        when(toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page)).thenReturn(true);

        assertThrows(SecurityException.class, () -> handler.deletePage("page1"));

        verify(site, never()).removePage(page);
        verify(siteService, never()).save(site);
    }

    @Test
    public void setPageVisibleUpdatesPortalVisibility() throws Exception {
        Properties placementConfig = new Properties();
        SitePage page0 = page("page0");
        ToolConfiguration tool = tool("sakai.foo");
        SitePage page = page("page1", Collections.singletonList(tool));
        Event event = mock(Event.class);
        when(site.getPage("page1")).thenReturn(page);
        when(site.getOrderedPages()).thenReturn(Arrays.asList(page0, page));
        when(tool.getPlacementConfig()).thenReturn(placementConfig);
        when(tool.getConfig()).thenReturn(placementConfig);
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.emptyList());
        when(toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page)).thenReturn(true);
        when(eventTrackingService.newEvent("pageorder.hide", "/site/site1/page/page1", false)).thenReturn(event);

        ToolOrderPage row = handler.setPageVisible("page1", false);

        assertEquals("false", placementConfig.getProperty(ToolManager.PORTAL_VISIBLE));
        assertFalse(row.isFirst());
        assertTrue(row.isLast());
        verify(tool).save();
        verify(toolSession).setAttribute(SitePageEditHandler.ATTR_TOP_REFRESH, Boolean.TRUE);
        verify(eventTrackingService).post(event);
        InOrder inOrder = inOrder(tool, eventTrackingService);
        inOrder.verify(tool).save();
        inOrder.verify(eventTrackingService).post(event);
    }

    @Test
    public void setPageEnabledPostsEventAfterAuthzAndVisibilityUpdates() throws Exception {
        Properties placementConfig = new Properties();
        ToolConfiguration tool = tool("sakai.foo");
        SitePage page = page("page1", Collections.singletonList(tool));
        Role role = roleWithoutSiteUpdate();
        AuthzGroup authzGroup = authzGroupWithRole(role);
        Event event = mock(Event.class);
        when(site.getPage("page1")).thenReturn(page);
        when(site.getOrderedPages()).thenReturn(Collections.singletonList(page));
        when(tool.getPlacementConfig()).thenReturn(placementConfig);
        when(tool.getConfig()).thenReturn(placementConfig);
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.singletonList(new HashSet<>(
                Arrays.asList(SiteService.SECURE_UPDATE_SITE, "foo.submit"))));
        when(eventTrackingService.newEvent("pageorder.disable", "/site/site1/page/page1", false)).thenReturn(event);

        ToolOrderPage row = handler.setPageEnabled("page1", false);

        assertTrue(row.isFirst());
        assertTrue(row.isLast());
        verify(role).disallowFunctions(Collections.singletonList("foo.submit"));
        verify(authzGroupService).save(authzGroup);
        verify(tool).save();
        verify(eventTrackingService).post(event);
        InOrder inOrder = inOrder(authzGroupService, tool, eventTrackingService);
        inOrder.verify(authzGroupService).save(authzGroup);
        inOrder.verify(tool).save();
        inOrder.verify(eventTrackingService).post(event);
    }

    @Test
    public void setPageVisibleDoesNotPostShowEventWhenAuthzSaveFails() throws Exception {
        Properties placementConfig = new Properties();
        placementConfig.setProperty(ToolManager.PORTAL_VISIBLE, "false");
        ToolConfiguration tool = tool("sakai.foo");
        SitePage page = page("page1", Collections.singletonList(tool));
        AuthzGroup authzGroup = authzGroupWithRole(roleWithoutSiteUpdate());
        when(site.getPage("page1")).thenReturn(page);
        when(tool.getPlacementConfig()).thenReturn(placementConfig);
        when(tool.getConfig()).thenReturn(placementConfig);
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.singletonList(new HashSet<>(
                Arrays.asList(SiteService.SECURE_UPDATE_SITE, "foo.submit"))));
        doThrow(new AuthzPermissionException("user1", "realm.upd", "/site/site1"))
                .when(authzGroupService).save(authzGroup);

        assertThrows(IllegalStateException.class, () -> handler.setPageVisible("page1", true));

        assertEquals("true", placementConfig.getProperty(ToolManager.PORTAL_VISIBLE));
        verify(tool).save();
        verify(eventTrackingService, never()).newEvent(anyString(), anyString(), anyBoolean());
        verify(eventTrackingService, never()).post(any());
    }

    @Test
    public void addPageAddsAvailableTool() throws Exception {
        Tool registeredTool = registeredTool("sakai.allowed");
        SitePage page = page("page1");
        ToolConfiguration placement = tool("sakai.allowed");
        Event event = mock(Event.class);
        when(toolManager.findTools(any(), any())).thenReturn(Collections.singleton(registeredTool));
        when(site.addPage()).thenReturn(page);
        when(page.addTool("sakai.allowed")).thenReturn(placement);
        when(placement.getId()).thenReturn("placement1");
        when(eventTrackingService.newEvent("pageorder.add",
                "/site/site1/page/page1/tool/sakai.allowed/placement/placement1", false)).thenReturn(event);

        assertEquals(page, handler.addPage("sakai.allowed", "New Page"));

        verify(page).setTitle("New Page");
        verify(siteService).save(site);
        verify(toolSession).setAttribute(SitePageEditHandler.ATTR_TOP_REFRESH, Boolean.TRUE);
        verify(eventTrackingService).post(event);
    }

    @Test
    public void addPageRejectsUnavailableTool() throws Exception {
        Tool registeredTool = registeredTool("sakai.allowed");
        when(toolManager.findTools(any(), any())).thenReturn(Collections.singleton(registeredTool));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> handler.addPage("sakai.disallowed", "New Page"));

        assertEquals("Tool is not available for site site1: sakai.disallowed", exception.getMessage());
        verify(site, never()).addPage();
        verify(siteService, never()).save(site);
    }

    @Test
    public void allowDisableAllowsPagesWithNonInstructorPermissions() {
        ToolConfiguration tool = tool("sakai.foo");
        SitePage page = page("page1", Collections.singletonList(tool));
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.singletonList(new HashSet<>(
                Arrays.asList(SiteService.SECURE_UPDATE_SITE, "foo.submit"))));

        assertTrue(handler.allowDisable(page));
    }

    @Test
    public void allowDisableRejectsInstructorOnlyPermissions() {
        ToolConfiguration tool = tool("sakai.foo");
        SitePage page = page("page1", Collections.singletonList(tool));
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.singletonList(new HashSet<>(
                Arrays.asList(SiteService.SECURE_UPDATE_SITE, SiteService.SITE_VISIT))));

        assertFalse(handler.allowDisable(page));
    }

    @Test
    public void allowDisableRejectsEmptyPermissions() {
        assertFalse(handler.allowDisable(page("page1")));
    }

    private SitePage page(String id) {
        return page(id, Collections.emptyList());
    }

    private SitePage page(String id, List<ToolConfiguration> tools) {
        SitePage page = mock(SitePage.class);
        when(page.getId()).thenReturn(id);
        when(page.getTitle()).thenReturn(id);
        when(page.getTools()).thenReturn(tools);
        return page;
    }

    private ToolConfiguration tool(String toolId) {
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(tool.getToolId()).thenReturn(toolId);
        return tool;
    }

    private AuthzGroup authzGroupWithRole(Role role) throws Exception {
        AuthzGroup authzGroup = mock(AuthzGroup.class);
        when(authzGroup.getRoles()).thenReturn(Collections.singleton(role));
        when(authzGroupService.getAuthzGroup("/site/site1")).thenReturn(authzGroup);
        return authzGroup;
    }

    private Role roleWithoutSiteUpdate() {
        Role role = mock(Role.class);
        when(role.isAllowed(SiteService.SECURE_UPDATE_SITE)).thenReturn(false);
        return role;
    }

    private Tool registeredTool(String toolId) {
        Tool tool = mock(Tool.class);
        when(tool.getId()).thenReturn(toolId);
        when(tool.getTitle()).thenReturn(toolId);
        when(tool.getRegisteredConfig()).thenReturn(new Properties());
        return tool;
    }
}
