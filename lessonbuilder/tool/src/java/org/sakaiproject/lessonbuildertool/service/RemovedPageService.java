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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Owns the state transitions into and out of the Lessons removed-page state. */
@Slf4j
@Setter
public class RemovedPageService {

    private SimplePageToolDao simplePageToolDao;
    private PageIndexService pageIndexService;
    private SiteService siteService;
    private GradebookIfc gradebookIfc;
    private ContentHostingService contentHostingService;
    private LessonEntity assignmentEntity;
    private LessonEntity quizEntity;
    private LessonEntity forumEntity;

    /**
     * Removes Lessons placements during a replace import while deliberately retaining
     * their page trees. Those trees become recoverable removed pages.
     */
    @Transactional(rollbackFor = Exception.class)
    public int detachLessonsPlacements(String siteId) throws IdUnusedException, PermissionException {
        Site site = siteService.getSite(siteId);
        List<SitePage> pagesToRemove = new ArrayList<>();

        for (SitePage sitePage : site.getPages()) {
            for (ToolConfiguration tool : sitePage.getTools()) {
                if (LessonBuilderConstants.TOOL_ID.equals(tool.getToolId())) {
                    pagesToRemove.add(sitePage);
                    break;
                }
            }
        }

        for (SitePage sitePage : pagesToRemove) {
            site.removePage(sitePage);
        }
        siteService.save(site);
        return pagesToRemove.size();
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public Optional<RestoredPage> restorePage(String siteId, Long pageId)
            throws IdUnusedException, PermissionException {
        if (pageId == null || !pageIndexService.getRemovedPageIds(siteId).contains(pageId)) {
            return Optional.empty();
        }

        SimplePage page = simplePageToolDao.getPage(pageId);
        if (page == null || !siteId.equals(page.getSiteId())) {
            return Optional.empty();
        }

        Site site = siteService.getSite(siteId);
        SitePage sitePage = site.addPage();
        ToolConfiguration tool = sitePage.addTool(LessonBuilderConstants.TOOL_ID);

        page.setToolId(tool.getPageId());
        page.setParent(null);
        page.setTopParent(null);
        if (!simplePageToolDao.quickUpdate(page)) {
            throw new RemovedPageOperationException("Unable to update removed Lessons page " + pageId);
        }

        SimplePageItem topLevelItem = simplePageToolDao.findTopLevelPageItemBySakaiId(pageId.toString());
        if (topLevelItem == null) {
            topLevelItem = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, pageId.toString(), page.getTitle());
            if (!simplePageToolDao.quickSaveItem(topLevelItem)) {
                throw new RemovedPageOperationException("Unable to create top-level item for Lessons page " + pageId);
            }
        }

        tool.setTitle(page.getTitle());
        sitePage.setTitle(page.getTitle());
        sitePage.setTitleCustom(true);
        siteService.save(site);

        return Optional.of(new RestoredPage(pageId, page.getTitle()));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public DeleteResult deleteRemovedPages(String siteId, Collection<Long> requestedPageIds) {
        if (requestedPageIds == null || requestedPageIds.isEmpty()) {
            return new DeleteResult(OperationStatus.INVALID, 0);
        }

        Set<Long> pageIds = new LinkedHashSet<>(requestedPageIds);
        Set<Long> removedPageIds = pageIndexService.getRemovedPageIds(siteId);
        if (!removedPageIds.containsAll(pageIds)) {
            return new DeleteResult(OperationStatus.INVALID, 0);
        }

        for (Long pageId : pageIds) {
            SimplePage page = simplePageToolDao.getPage(pageId);
            if (page == null || !siteId.equals(page.getSiteId())) {
                throw new RemovedPageOperationException("Removed Lessons page changed during deletion: " + pageId);
            }
        }

        int deletedCount = 0;
        for (Long pageId : pageIds) {
            deletePage(siteId, pageId);
            deletedCount++;
            if (deletedCount % 10 == 0) {
                simplePageToolDao.flush();
                simplePageToolDao.clear();
            }
        }

        return new DeleteResult(OperationStatus.SUCCESS, pageIds.size());
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public DeleteResult deleteAllRemovedPages(String siteId) {
        Set<Long> removedPageIds = pageIndexService.getRemovedPageIds(siteId);
        if (removedPageIds.isEmpty()) {
            return new DeleteResult(OperationStatus.SUCCESS, 0);
        }
        return deleteRemovedPages(siteId, removedPageIds);
    }

    private void deletePage(String siteId, Long pageId) {
        SimplePage page = simplePageToolDao.getPage(pageId);
        List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
        if (items != null) {
            for (SimplePageItem item : items) {
                if (item.isPrerequisite()) {
                    removeAccessControl(item);
                }
                removeGradebookEntries(siteId, item);
                deletePersistedObject(item, "item " + item.getId());
            }
        }

        if (page.getGradebookPoints() != null && page.getGradebookPoints() != 0.0
                && !gradebookIfc.removeExternalAssessment(siteId, "lesson-builder:" + pageId)) {
            throw new RemovedPageOperationException("Unable to remove gradebook entry for Lessons page " + pageId);
        }

        SimplePageItem topLevelItem = simplePageToolDao.findTopLevelPageItemBySakaiId(pageId.toString());
        if (topLevelItem != null) {
            deletePersistedObject(topLevelItem, "top-level item for page " + pageId);
        }
        deletePersistedObject(page, "page " + pageId);
    }

    private void removeGradebookEntries(String siteId, SimplePageItem item) {
        if (item.getGradebookId() != null
                && !gradebookIfc.removeExternalAssessment(siteId, item.getGradebookId())) {
            throw new RemovedPageOperationException("Unable to remove gradebook entry " + item.getGradebookId());
        }
        if (item.getAltGradebook() != null
                && !gradebookIfc.removeExternalAssessment(siteId, item.getAltGradebook())) {
            throw new RemovedPageOperationException("Unable to remove gradebook entry " + item.getAltGradebook());
        }
    }

    private void removeAccessControl(SimplePageItem item) {
        if (item.getType() == SimplePageItem.RESOURCE) {
            restoreResourceVisibility(item);
            return;
        }
        if (item.getType() != SimplePageItem.ASSESSMENT
                && item.getType() != SimplePageItem.ASSIGNMENT
                && item.getType() != SimplePageItem.FORUM) {
            return;
        }

        String sakaiId = item.getSakaiId();
        if (sakaiId == null || SimplePageItem.DUMMY.equals(sakaiId) || sakaiId.startsWith("/sam_core/")) {
            return;
        }

        SimplePageGroup group = simplePageToolDao.findGroup(sakaiId);
        LessonEntity entity = getLessonEntity(item);
        if (group != null && entity != null) {
            String groups = group.getGroups();
            entity.setGroups(groups == null || groups.isBlank() ? null : List.of(groups.split(",")));
            deletePersistedObject(group, "access-control group for item " + item.getId());
        }
    }

    private LessonEntity getLessonEntity(SimplePageItem item) {
        return switch (item.getType()) {
            case SimplePageItem.ASSIGNMENT -> assignmentEntity.getEntity(item.getSakaiId());
            case SimplePageItem.ASSESSMENT -> quizEntity.getEntity(item.getSakaiId());
            case SimplePageItem.FORUM -> forumEntity.getEntity(item.getSakaiId());
            default -> null;
        };
    }

    private void restoreResourceVisibility(SimplePageItem item) {
        String resourceId = item.getSakaiId();
        if (resourceId == null) {
            return;
        }
        try {
            ContentResource resource = contentHostingService.getResource(resourceId);
            if (resource.isHidden()) {
                ContentResourceEdit edit = contentHostingService.editResource(resourceId);
                edit.setAvailability(false, edit.getReleaseDate(), edit.getRetractDate());
                contentHostingService.commitResource(edit, NotificationService.NOTI_NONE);
            }
        } catch (Exception e) {
            log.warn("Unable to restore visibility for resource {} while deleting Lessons item {}",
                    resourceId, item.getId(), e);
        }
    }

    private void deletePersistedObject(Object object, String description) {
        if (!simplePageToolDao.deleteItem(object)) {
            throw new RemovedPageOperationException("Unable to delete Lessons " + description);
        }
    }

    public enum OperationStatus {
        SUCCESS,
        INVALID
    }

    public record RestoredPage(long pageId, String pageTitle) {
    }

    public record DeleteResult(OperationStatus status, int deletedCount) {
    }

    public static class RemovedPageOperationException extends RuntimeException {
        public RemovedPageOperationException(String message) {
            super(message);
        }
    }
}
