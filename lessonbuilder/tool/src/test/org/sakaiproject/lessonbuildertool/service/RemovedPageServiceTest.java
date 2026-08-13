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
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageImpl;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.RemovedPageService.DeleteResult;
import org.sakaiproject.lessonbuildertool.service.RemovedPageService.OperationStatus;
import org.sakaiproject.lessonbuildertool.service.RemovedPageService.RemovedPageOperationException;
import org.sakaiproject.lessonbuildertool.service.RemovedPageService.RestoredPage;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;

public class RemovedPageServiceTest {

    private static final String SITE_ID = "site";

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock private SimplePageToolDao dao;
    @Mock private SiteService siteService;
    @Mock private Site site;
    @Mock private SitePage sitePage;
    @Mock private ToolConfiguration tool;
    @Mock private GradebookIfc gradebookIfc;
    @Mock private ContentHostingService contentHostingService;
    @Mock private LessonEntity assignmentEntity;
    @Mock private LessonEntity quizEntity;
    @Mock private LessonEntity forumEntity;

    private RemovedPageService service;

    @Before
    public void setUp() {
        PageIndexService pageIndexService = new PageIndexService();
        pageIndexService.setSimplePageToolDao(dao);

        service = new RemovedPageService();
        service.setSimplePageToolDao(dao);
        service.setPageIndexService(pageIndexService);
        service.setSiteService(siteService);
        service.setGradebookIfc(gradebookIfc);
        service.setContentHostingService(contentHostingService);
        service.setAssignmentEntity(assignmentEntity);
        service.setQuizEntity(quizEntity);
        service.setForumEntity(forumEntity);
    }

    @Test
    public void detachLessonsPlacementsPreservesPageContent() throws Exception {
        SitePage lessonsPage = org.mockito.Mockito.mock(SitePage.class);
        SitePage otherPage = org.mockito.Mockito.mock(SitePage.class);
        ToolConfiguration lessonsTool = org.mockito.Mockito.mock(ToolConfiguration.class);
        ToolConfiguration otherTool = org.mockito.Mockito.mock(ToolConfiguration.class);
        when(lessonsTool.getToolId()).thenReturn(LessonBuilderConstants.TOOL_ID);
        when(otherTool.getToolId()).thenReturn("sakai.resources");
        when(lessonsPage.getTools()).thenReturn(List.of(lessonsTool));
        when(otherPage.getTools()).thenReturn(List.of(otherTool));
        when(site.getPages()).thenReturn(List.of(lessonsPage, otherPage));
        when(siteService.getSite(SITE_ID)).thenReturn(site);

        assertEquals(1, service.detachLessonsPlacements(SITE_ID));

        verify(site).removePage(lessonsPage);
        verify(site, never()).removePage(otherPage);
        verify(siteService).save(site);
        verify(dao, never()).deleteItem(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void deleteRejectsEntireSelectionWhenAnyPageIsActive() {
        SimplePage active = page(1);
        SimplePage removed = page(2);
        configureIndex(List.of(active, removed), List.of(pageItem(101, 1)));

        DeleteResult result = service.deleteRemovedPages(SITE_ID, List.of(1L, 2L));

        assertEquals(OperationStatus.INVALID, result.status());
        assertEquals(0, result.deletedCount());
        verify(dao, never()).deleteItem(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void restoreRejectsPageThatIsAlreadyActive() throws Exception {
        SimplePage active = page(1);
        configureIndex(List.of(active), List.of(pageItem(101, 1)));

        Optional<RestoredPage> result = service.restorePage(SITE_ID, 1L);

        assertEquals(Optional.empty(), result);
        verify(siteService, never()).getSite(SITE_ID);
        verify(dao, never()).quickUpdate(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void restoreCreatesPlacementAndTopLevelItemForRemovedPage() throws Exception {
        SimplePage removed = page(2);
        configureRemovedPage(removed);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(site.addPage()).thenReturn(sitePage);
        when(sitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(tool);
        when(tool.getPageId()).thenReturn("restored-tool");
        when(dao.quickUpdate(removed)).thenReturn(true);
        SimplePageItem topLevelItem = pageItem(202, 2);
        when(dao.makeItem(0, 0, SimplePageItem.PAGE, "2", removed.getTitle())).thenReturn(topLevelItem);
        when(dao.quickSaveItem(topLevelItem)).thenReturn(true);

        Optional<RestoredPage> result = service.restorePage(SITE_ID, 2L);

        assertEquals(Optional.of(new RestoredPage(2L, "Page 2")), result);
        assertEquals("restored-tool", removed.getToolId());
        verify(siteService).save(site);
    }

    @Test
    public void restorePropagatesSiteSaveFailureInsteadOfReportingSuccess() throws Exception {
        SimplePage removed = page(2);
        configureRemovedPage(removed);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(site.addPage()).thenReturn(sitePage);
        when(sitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(tool);
        when(tool.getPageId()).thenReturn("restored-tool");
        when(dao.quickUpdate(removed)).thenReturn(true);
        when(dao.findTopLevelPageItemBySakaiId("2")).thenReturn(pageItem(202, 2));
        doThrow(new PermissionException("user", "site.upd", SITE_ID)).when(siteService).save(site);

        assertThrows(PermissionException.class, () -> service.restorePage(SITE_ID, 2L));
    }

    @Test
    public void deleteSignalsPersistenceFailure() {
        SimplePage removed = page(2);
        configureRemovedPage(removed);
        when(dao.findItemsOnPage(2)).thenReturn(List.of());
        when(dao.deleteItem(removed)).thenReturn(false);

        assertThrows(RemovedPageOperationException.class,
                () -> service.deleteRemovedPages(SITE_ID, List.of(2L)));
    }

    @Test
    public void deleteRemovesValidatedPage() {
        SimplePage removed = page(2);
        configureRemovedPage(removed);
        when(dao.findItemsOnPage(2)).thenReturn(List.of());
        when(dao.deleteItem(removed)).thenReturn(true);

        DeleteResult result = service.deleteRemovedPages(SITE_ID, List.of(2L));

        assertEquals(OperationStatus.SUCCESS, result.status());
        assertEquals(1, result.deletedCount());
        verify(dao).deleteItem(removed);
    }

    private void configureRemovedPage(SimplePage removed) {
        configureIndex(List.of(removed), List.of());
        when(dao.getPage(removed.getPageId())).thenReturn(removed);
    }

    private void configureIndex(List<SimplePage> pages, List<SimplePageItem> placements) {
        when(dao.getSitePages(SITE_ID)).thenReturn(pages);
        if (placements.isEmpty()) {
            when(dao.getSiteTools(SITE_ID)).thenReturn(List.of());
        } else {
            when(dao.getSiteTools(SITE_ID)).thenReturn(List.of(tool));
            when(dao.getOrderedTopLevelPageItems(SITE_ID)).thenReturn(placements);
        }
    }

    private SimplePage page(long pageId) {
        SimplePage page = new SimplePageImpl("old-tool-" + pageId, SITE_ID, "Page " + pageId, null, null);
        page.setPageId(pageId);
        return page;
    }

    private SimplePageItem pageItem(long itemId, long targetPageId) {
        return new SimplePageItemImpl(
                itemId, 0, 0, SimplePageItem.PAGE, Long.toString(targetPageId), "Page " + targetPageId);
    }
}
