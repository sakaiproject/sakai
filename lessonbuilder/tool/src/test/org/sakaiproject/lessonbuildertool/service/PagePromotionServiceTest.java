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
package org.sakaiproject.lessonbuildertool.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.PagePromotionService.PagePromotionResult;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
@Transactional
public class PagePromotionServiceTest {

    private static final String SITE_ID = "site";
    private static final String ACTIVE_TOOL = "active-tool";
    private static final String REMOVED_TOOL = "removed-tool";
    private static final String NEW_TOOL = "restored-tool";

    @Autowired private PagePromotionService service;
    @Autowired private SimplePageToolDao dao;
    @Autowired private SiteService siteService;
    @Autowired private SecurityService securityService;

    private Site site;

    @Before
    public void setUp() throws Exception {
        reset(siteService, securityService);
        site = mock(Site.class);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(securityService.unlock(anyString(), anyString())).thenReturn(true);
        configurePlacements(placement(ACTIVE_TOOL));
    }

    @Test
    public void promoteRejectsPageAlreadyInSiteNavigation() {
        SimplePage active = savePage(ACTIVE_TOOL, "Active", null);
        savePageItem(0, active, false);

        assertEquals(PagePromotionResult.ALREADY_TOP_LEVEL,
                service.promote(Long.toString(active.getPageId()), SITE_ID));
    }

    @Test
    public void promoteRestoresRemovedTopLevelPageReusingOrphanItem() throws Exception {
        SimplePage active = savePage(ACTIVE_TOOL, "Active", null);
        savePageItem(0, active, false);
        SimplePage removed = savePage(REMOVED_TOOL, "Removed", null);
        SimplePageItem orphanItem = savePageItem(0, removed, false);
        long orphanItemId = orphanItem.getId();

        SitePage newSitePage = mock(SitePage.class);
        ToolConfiguration newTool = mock(ToolConfiguration.class);
        when(site.addPage()).thenReturn(newSitePage);
        when(newSitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(newTool);
        when(newTool.getPageId()).thenReturn(NEW_TOOL);

        assertEquals(PagePromotionResult.SUCCESS,
                service.promote(Long.toString(removed.getPageId()), SITE_ID));

        dao.flush();
        dao.clear();

        SimplePage restored = dao.getPage(removed.getPageId());
        assertNotNull(restored);
        assertEquals(NEW_TOOL, restored.getToolId());
        assertNull(restored.getParent());
        assertNull(restored.getTopParent());

        SimplePageItem topLevelItem = dao.findTopLevelPageItemBySakaiId(Long.toString(removed.getPageId()));
        assertNotNull(topLevelItem);
        assertEquals(orphanItemId, topLevelItem.getId());
        assertEquals("Removed", topLevelItem.getName());

        verify(siteService).save(site);
        verify(newSitePage).setTitle("Removed");
        verify(newSitePage).setTitleCustom(true);
        verify(newTool).setTitle("Removed");
    }

    @Test
    public void promoteCreatesTopLevelItemForSubpage() throws Exception {
        SimplePage active = savePage(ACTIVE_TOOL, "Active", null);
        savePageItem(0, active, false);
        SimplePage subpage = savePage(ACTIVE_TOOL, "Subpage", active.getPageId());
        savePageItem(active.getPageId(), subpage, false);

        SitePage newSitePage = mock(SitePage.class);
        ToolConfiguration newTool = mock(ToolConfiguration.class);
        when(site.addPage()).thenReturn(newSitePage);
        when(newSitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(newTool);
        when(newTool.getPageId()).thenReturn(NEW_TOOL);

        assertEquals(PagePromotionResult.SUCCESS,
                service.promote(Long.toString(subpage.getPageId()), SITE_ID));

        dao.flush();
        dao.clear();

        SimplePage promoted = dao.getPage(subpage.getPageId());
        assertEquals(NEW_TOOL, promoted.getToolId());
        assertNull(promoted.getParent());
        assertNotNull(dao.findTopLevelPageItemBySakaiId(Long.toString(subpage.getPageId())));
    }

    private void configurePlacements(ToolConfiguration... placements) {
        List<ToolConfiguration> siteTools = List.of(placements);
        when(site.getTools(any(String[].class))).thenReturn(siteTools);
        when(site.getTools(LessonBuilderConstants.TOOL_ID)).thenReturn(siteTools);
        List<SitePage> sitePages = siteTools.stream().map(tool -> {
            String pageId = tool.getPageId();
            SitePage sitePage = mock(SitePage.class);
            when(sitePage.getId()).thenReturn(pageId);
            return sitePage;
        }).toList();
        when(site.getOrderedPages()).thenReturn(sitePages);
    }

    private ToolConfiguration placement(String pageId) {
        ToolConfiguration placement = mock(ToolConfiguration.class);
        when(placement.getToolId()).thenReturn(LessonBuilderConstants.TOOL_ID);
        when(placement.getPageId()).thenReturn(pageId);
        return placement;
    }

    private SimplePage savePage(String toolId, String title, Long parent) {
        SimplePage page = dao.makePage(toolId, SITE_ID, title, parent, parent);
        assertTrue(dao.quickSaveItem(page));
        dao.flush();
        return page;
    }

    private SimplePageItem savePageItem(long pageId, SimplePage target, boolean nextPage) {
        SimplePageItem item = dao.makeItem(pageId, 1, SimplePageItem.PAGE,
                Long.toString(target.getPageId()), target.getTitle());
        item.setNextPage(nextPage);
        assertTrue(dao.quickSaveItem(item));
        dao.flush();
        return item;
    }
}
