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

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageIndex;
import org.sakaiproject.site.api.ToolConfiguration;

public class PageIndexServiceTest {

    private static final String SITE_ID = "site";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    private SimplePageToolDao dao;

    @Mock
    private ToolConfiguration activePlacement;

    private PageIndexService service;

    @Before
    public void setUp() {
        service = new PageIndexService();
        service.setSimplePageToolDao(dao);
    }

    @Test
    public void getPageIndexClassifiesReachableAndRemovedPagesFromOneGraph() {
        configureActivePlacement();
        SimplePage topLevel = page(1, null);
        SimplePage subpage = page(2, null);
        SimplePage nextPage = page(3, null);
        SimplePage removed = page(4, null);
        SimplePage studentPage = page(5, "student");
        SimplePageItem placement = pageItem(101, 0, 1, false);
        SimplePageItem subpageItem = pageItem(102, 1, 2, false);
        SimplePageItem nextPageItem = pageItem(103, 1, 3, true);

        when(dao.getSitePages(SITE_ID)).thenReturn(List.of(topLevel, subpage, nextPage, removed, studentPage));
        when(dao.getOrderedTopLevelPageItems(SITE_ID)).thenReturn(List.of(placement));
        when(dao.findItemsOnPage(1)).thenReturn(List.of(subpageItem, nextPageItem));
        when(dao.findItemsOnPage(2)).thenReturn(List.of());
        when(dao.findItemsOnPage(3)).thenReturn(List.of());

        PageIndex index = service.getPageIndex(SITE_ID);

        assertEquals(List.of(1L, 2L, 3L), index.activePages().stream()
                .map(node -> node.page().getPageId())
                .toList());
        assertEquals(List.of(4L), index.removedPages().stream().map(SimplePage::getPageId).toList());
        assertTrue(index.sharedPageIds().isEmpty());
    }

    @Test
    public void getPageIndexStopsCyclesAndMarksSharedPages() {
        configureActivePlacement();
        SimplePage first = page(1, null);
        SimplePage second = page(2, null);
        when(dao.getSitePages(SITE_ID)).thenReturn(List.of(first, second));
        when(dao.getOrderedTopLevelPageItems(SITE_ID)).thenReturn(List.of(pageItem(101, 0, 1, false)));
        when(dao.findItemsOnPage(1)).thenReturn(List.of(pageItem(102, 1, 2, false)));
        when(dao.findItemsOnPage(2)).thenReturn(List.of(pageItem(103, 2, 1, false)));

        PageIndex index = service.getPageIndex(SITE_ID);

        assertEquals(List.of(1L, 2L, 1L), index.activePages().stream()
                .map(node -> node.page().getPageId())
                .toList());
        assertEquals(List.of(1L), index.sharedPageIds().stream().toList());
        assertTrue(index.removedPages().isEmpty());
    }

    @Test
    public void getPageIndexTreatsFakeTopLevelItemsAsRemovedWhenNoPlacementExists() {
        SimplePage removed = page(1, null);
        when(dao.getSitePages(SITE_ID)).thenReturn(List.of(removed));
        when(dao.getSiteTools(SITE_ID)).thenReturn(List.of());

        PageIndex index = service.getPageIndex(SITE_ID);

        assertTrue(index.activePages().isEmpty());
        assertEquals(List.of(1L), index.removedPages().stream().map(SimplePage::getPageId).toList());
        verify(dao, never()).getOrderedTopLevelPageItems(SITE_ID);
    }

    private void configureActivePlacement() {
        when(dao.getSiteTools(SITE_ID)).thenReturn(List.of(activePlacement));
    }

    private SimplePage page(long pageId, String owner) {
        SimplePage page = new SimplePageImpl("tool-" + pageId, SITE_ID, "Page " + pageId, null, null);
        page.setPageId(pageId);
        page.setOwner(owner);
        return page;
    }

    private SimplePageItem pageItem(long itemId, long parentPageId, long targetPageId, boolean nextPage) {
        SimplePageItem item = new SimplePageItemImpl(
                itemId, parentPageId, 0, SimplePageItem.PAGE, Long.toString(targetPageId), "Page " + targetPageId);
        item.setNextPage(nextPage);
        return item;
    }
}
