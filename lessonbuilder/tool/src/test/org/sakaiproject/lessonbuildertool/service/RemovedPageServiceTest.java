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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = LessonBuilderServiceTestConfiguration.class)
@Transactional
public class RemovedPageServiceTest {

    private static final String SITE_ID = "site";

    @Autowired private RemovedPageService service;
    @Autowired private SimplePageToolDao dao;
    @Autowired private SiteService siteService;
    @Autowired private SecurityService securityService;
    @Autowired private ContentHostingService contentHostingService;
    @Autowired private GradingService gradingService;

    private Site site;

    @Before
    public void setUp() throws Exception {
        reset(siteService, securityService, contentHostingService, gradingService);
        site = mock(Site.class);
        when(siteService.getSite(SITE_ID)).thenReturn(site);
        when(securityService.unlock(any(String.class), any(String.class))).thenReturn(true);
        configurePlacements();
    }

    @Test
    public void detachLessonsPlacementsPreservesPageContent() throws Exception {
        SitePage lessonsPage = mock(SitePage.class);
        SitePage otherPage = mock(SitePage.class);
        ToolConfiguration lessonsTool = mock(ToolConfiguration.class);
        ToolConfiguration otherTool = mock(ToolConfiguration.class);
        when(lessonsTool.getToolId()).thenReturn(LessonBuilderConstants.TOOL_ID);
        when(otherTool.getToolId()).thenReturn("sakai.resources");
        when(lessonsPage.getTools()).thenReturn(List.of(lessonsTool));
        when(otherPage.getTools()).thenReturn(List.of(otherTool));
        when(site.getPages()).thenReturn(List.of(lessonsPage, otherPage));

        assertEquals(1, service.detachLessonsPlacements(SITE_ID));

        verify(site).removePage(lessonsPage);
        verify(site, never()).removePage(otherPage);
        verify(siteService).save(site);
    }

    @Test
    public void deleteRejectsEntireSelectionWhenAnyPageIsActive() {
        SimplePage active = savePage("active-tool", "Active", null);
        SimplePage removed = savePage("removed-tool", "Removed", null);
        savePageItem(0, active, false);
        configurePlacements(placement("active-tool"));

        DeleteResult result = service.deleteRemovedPages(
                SITE_ID, List.of(active.getPageId(), removed.getPageId()));

        assertEquals(OperationStatus.INVALID, result.status());
        assertEquals(0, result.deletedCount());
        assertTrue(dao.getPage(removed.getPageId()) != null);
    }

    @Test
    public void restoreRejectsPageThatIsAlreadyActive() throws Exception {
        SimplePage active = savePage("active-tool", "Active", null);
        savePageItem(0, active, false);
        configurePlacements(placement("active-tool"));

        Optional<RestoredPage> result = service.restorePage(SITE_ID, active.getPageId());

        assertEquals(Optional.empty(), result);
        verify(site, never()).addPage();
    }

    @Test
    public void restoreCreatesPlacementAndPropagatesToolIdToDescendants() throws Exception {
        SimplePage removed = savePage("old-tool", "Removed", null);
        SimplePage child = savePage("old-tool", "Child", removed.getPageId());
        savePageItem(removed.getPageId(), child, false);
        SitePage sitePage = mock(SitePage.class);
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(site.addPage()).thenReturn(sitePage);
        when(sitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(tool);
        when(tool.getPageId()).thenReturn("restored-tool");

        Optional<RestoredPage> result = service.restorePage(SITE_ID, removed.getPageId());
        dao.flush();
        dao.clear();

        assertEquals(Optional.of(new RestoredPage(removed.getPageId(), "Removed")), result);
        assertEquals("restored-tool", dao.getPage(removed.getPageId()).getToolId());
        assertEquals("restored-tool", dao.getPage(child.getPageId()).getToolId());
        assertTrue(dao.findTopLevelPageItemBySakaiId(Long.toString(removed.getPageId())) != null);
        verify(siteService).save(site);
    }

    @Test
    public void restoreDoesNotRetargetLinkedActiveTopLevelPage() throws Exception {
        SimplePage removed = savePage("old-tool", "Removed", null);
        SimplePage active = savePage("active-tool", "Active", null);
        savePageItem(removed.getPageId(), active, false);
        savePageItem(0, active, false);
        configurePlacements(placement("active-tool"));
        SitePage sitePage = mock(SitePage.class);
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(site.addPage()).thenReturn(sitePage);
        when(sitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(tool);
        when(tool.getPageId()).thenReturn("restored-tool");

        Optional<RestoredPage> result = service.restorePage(SITE_ID, removed.getPageId());
        dao.flush();
        dao.clear();

        assertEquals(Optional.of(new RestoredPage(removed.getPageId(), "Removed")), result);
        assertEquals("restored-tool", dao.getPage(removed.getPageId()).getToolId());
        assertEquals("active-tool", dao.getPage(active.getPageId()).getToolId());
    }

    @Test
    public void restorePropagatesSiteSaveFailureInsteadOfReportingSuccess() throws Exception {
        SimplePage removed = savePage("old-tool", "Removed", null);
        SitePage sitePage = mock(SitePage.class);
        ToolConfiguration tool = mock(ToolConfiguration.class);
        when(site.addPage()).thenReturn(sitePage);
        when(sitePage.addTool(LessonBuilderConstants.TOOL_ID)).thenReturn(tool);
        when(tool.getPageId()).thenReturn("restored-tool");
        doThrow(new PermissionException("user", "site.upd", SITE_ID)).when(siteService).save(site);

        assertThrows(PermissionException.class, () -> service.restorePage(SITE_ID, removed.getPageId()));
    }

    @Test
    public void deleteSignalsGradebookFailure() {
        SimplePage removed = savePage("removed-tool", "Removed", null);
        removed.setGradebookPoints(10.0);
        assertTrue(dao.quickUpdate(removed));
        doThrow(new RuntimeException("gradebook unavailable")).when(gradingService)
                .removeExternalAssignment(null, "lesson-builder:" + removed.getPageId(), LessonBuilderConstants.TOOL_ID);

        assertThrows(RemovedPageOperationException.class,
                () -> service.deleteRemovedPages(SITE_ID, List.of(removed.getPageId())));
        assertTrue(dao.getPage(removed.getPageId()) != null);
    }

    @Test
    public void deleteRemovesValidatedPage() {
        SimplePage removed = savePage("removed-tool", "Removed", null);

        DeleteResult result = service.deleteRemovedPages(SITE_ID, List.of(removed.getPageId()));
        dao.flush();
        dao.clear();

        assertEquals(OperationStatus.SUCCESS, result.status());
        assertEquals(1, result.deletedCount());
        assertNull(dao.getPage(removed.getPageId()));
    }

    @Test
    public void deleteCancelsResourceEditWhenVisibilityRestoreFails() throws Exception {
        SimplePage removed = savePage("removed-tool", "Removed", null);
        SimplePageItem resourceItem = dao.makeItem(
                removed.getPageId(), 1, SimplePageItem.RESOURCE, "/content/resource", "Resource");
        resourceItem.setPrerequisite(true);
        assertTrue(dao.quickSaveItem(resourceItem));
        ContentResource resource = mock(ContentResource.class);
        ContentResourceEdit edit = mock(ContentResourceEdit.class);
        when(contentHostingService.getResource("/content/resource")).thenReturn(resource);
        when(resource.isHidden()).thenReturn(true);
        when(contentHostingService.editResource("/content/resource")).thenReturn(edit);
        when(edit.isActiveEdit()).thenReturn(true);
        doThrow(new RuntimeException("availability update failed"))
                .when(edit).setAvailability(false, null, null);

        DeleteResult result = service.deleteRemovedPages(SITE_ID, List.of(removed.getPageId()));

        assertEquals(OperationStatus.SUCCESS, result.status());
        verify(contentHostingService).cancelResource(edit);
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
