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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageIndex;
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
public class PageIndexServiceTest {

    private static final String SITE_ID = "site";

    @Autowired private PageIndexService service;
    @Autowired private SimplePageToolDao dao;
    @Autowired private SiteService siteService;

    private Site site;

    @Before
    public void setUp() throws Exception {
        reset(siteService);
        site = mock(Site.class);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        configurePlacements();
    }

    @Test
    public void getPageIndexBuildsActiveHierarchyAndRemovedPages() {
        SimplePage root = savePage("active-tool", "Root", null);
        SimplePage child = savePage("active-tool", "Child", root.getPageId());
        SimplePage next = savePage("active-tool", "Next", root.getPageId());
        SimplePage removed = savePage("removed-tool", "Removed", null);
        savePageItem(0, root, false);
        savePageItem(root.getPageId(), child, false);
        savePageItem(child.getPageId(), next, true);
        configurePlacements(placement("active-tool"));

        PageIndex index = service.getPageIndex(SITE_ID);

        assertEquals(List.of(root.getPageId(), child.getPageId(), next.getPageId()), index.activePages().stream()
                .map(node -> node.page().getPageId())
                .toList());
        assertEquals(List.of(removed.getPageId()), index.removedPages().stream()
                .map(SimplePage::getPageId)
                .toList());
        assertTrue(index.sharedPageIds().isEmpty());
    }

    @Test
    public void getPageIndexStopsCyclesAndMarksSharedPages() {
        SimplePage first = savePage("active-tool", "First", null);
        SimplePage second = savePage("active-tool", "Second", null);
        savePageItem(0, first, false);
        savePageItem(first.getPageId(), second, false);
        savePageItem(second.getPageId(), first, false);
        configurePlacements(placement("active-tool"));

        PageIndex index = service.getPageIndex(SITE_ID);

        assertEquals(List.of(first.getPageId(), second.getPageId(), first.getPageId()), index.activePages().stream()
                .map(node -> node.page().getPageId())
                .toList());
        assertEquals(List.of(first.getPageId()), index.sharedPageIds().stream().toList());
        assertTrue(index.removedPages().isEmpty());
    }

    @Test
    public void getPageIndexTreatsFakeTopLevelItemsAsRemovedWhenNoPlacementExists() {
        SimplePage removed = savePage("removed-tool", "Removed", null);
        savePageItem(0, removed, false);

        PageIndex index = service.getPageIndex(SITE_ID);

        assertTrue(index.activePages().isEmpty());
        assertEquals(List.of(removed.getPageId()), index.removedPages().stream()
                .map(SimplePage::getPageId)
                .toList());
    }

    @Test
    public void getPageTreeIdsFollowsParentAndItemRelationshipsWithoutLooping() {
        SimplePage root = savePage("removed-tool", "Root", null);
        SimplePage child = savePage("removed-tool", "Child", root.getPageId());
        SimplePage linked = savePage("removed-tool", "Linked", null);
        savePageItem(child.getPageId(), linked, false);
        savePageItem(linked.getPageId(), root, false);

        assertEquals(List.of(root.getPageId(), child.getPageId(), linked.getPageId()),
                service.getPageTreeIds(SITE_ID, root.getPageId()).stream().toList());
    }

    private void configurePlacements(ToolConfiguration... placements) {
        List<ToolConfiguration> siteTools = Arrays.asList(placements);
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
