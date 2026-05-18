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
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
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
        eventTrackingService = mock(EventTrackingService.class);
        toolSession = mock(ToolSession.class);
        site = mock(Site.class);

        handler.setSiteService(siteService);
        handler.setToolManager(toolManager);
        handler.setSessionManager(sessionManager);
        handler.setServerConfigurationService(serverConfigurationService);
        handler.setContentHostingService(contentHostingService);
        handler.setEventTrackingService(eventTrackingService);

        when(sessionManager.getCurrentToolSession()).thenReturn(toolSession);
        when(toolSession.getAttribute("sakai.tool.helper.id.siteId")).thenReturn("site1");
        when(siteService.getSite("site1")).thenReturn(site);
        when(siteService.allowUpdateSite("site1")).thenReturn(true);
        when(site.getId()).thenReturn("site1");
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
        ToolConfiguration tool = tool("sakai.foo");
        SitePage page = page("page1", Collections.singletonList(tool));
        Event event = mock(Event.class);
        when(site.getPage("page1")).thenReturn(page);
        when(tool.getPlacementConfig()).thenReturn(placementConfig);
        when(tool.getConfig()).thenReturn(placementConfig);
        when(toolManager.getRequiredPermissions(tool)).thenReturn(Collections.emptyList());
        when(toolManager.isFirstToolVisibleToAnyNonMaintainerRole(page)).thenReturn(true);
        when(eventTrackingService.newEvent("pageorder.hide", "/site/site1/page/page1", false)).thenReturn(event);

        handler.setPageVisible("page1", false);

        assertEquals("false", placementConfig.getProperty(ToolManager.PORTAL_VISIBLE));
        verify(tool).save();
        verify(toolSession).setAttribute(SitePageEditHandler.ATTR_TOP_REFRESH, Boolean.TRUE);
        verify(eventTrackingService).post(event);
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
}
