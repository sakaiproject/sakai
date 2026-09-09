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
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
public class PagePromotionService {

    public enum PagePromotionResult {
        SUCCESS,
        INVALID_PAGE_ID,
        PAGE_NOT_FOUND,
        STUDENT_PAGE,
        ALREADY_TOP_LEVEL,
        NAVIGATION_CREATION_FAILED,
        PAGE_UPDATE_FAILED,
        TOP_LEVEL_ITEM_SAVE_FAILED,
        SITE_SAVE_FAILED,
        PERSISTENCE_FAILED
    }

    private final SimplePageToolDao simplePageToolDao;
    private final SiteService siteService;
    private final TransactionTemplate transactionTemplate;

    private static final class PromotionState {
        private boolean siteSaved;
    }

    public PagePromotionService(SimplePageToolDao simplePageToolDao, SiteService siteService, TransactionTemplate transactionTemplate) {
        this.simplePageToolDao = simplePageToolDao;
        this.siteService = siteService;
        this.transactionTemplate = transactionTemplate;
    }

    public PagePromotionResult promote(String submittedPageId, String siteId) {
        Long pageId = parsePageId(submittedPageId);
        if (pageId == null) {
            log.warn("Unable to add an existing page as top level: invalid page ID");
            return PagePromotionResult.INVALID_PAGE_ID;
        }

        SimplePage target = simplePageToolDao.getPage(pageId);
        if (target == null || !siteId.equals(target.getSiteId())) {
            log.warn("Unable to add page {} as top level in site {}: page was not found in the site", pageId, siteId);
            return PagePromotionResult.PAGE_NOT_FOUND;
        }
        if (isStudentPage(target)) {
            log.warn("Unable to add page {} as top level in site {}: student pages cannot be promoted", pageId, siteId);
            return PagePromotionResult.STUDENT_PAGE;
        }

        String canonicalPageId = pageId.toString();
        if (simplePageToolDao.findTopLevelPageItemBySakaiId(canonicalPageId) != null) {
            log.warn("Unable to add page {} as top level in site {}: page is already top level", pageId, siteId);
            return PagePromotionResult.ALREADY_TOP_LEVEL;
        }

        Site site;
        try {
            site = siteService.getSite(siteId);
        } catch (Exception e) {
            log.error("Unable to add page {} as top level in site {}: site lookup failed", pageId, siteId, e);
            return PagePromotionResult.NAVIGATION_CREATION_FAILED;
        }

        String oldToolId = target.getToolId();
        Long oldParent = target.getParent();
        Long oldTopParent = target.getTopParent();
        SitePage sitePage = null;

        try {
            sitePage = site.addPage();
            ToolConfiguration tool = sitePage.addTool(LessonBuilderConstants.TOOL_ID);
            target.setToolId(tool.getPageId());
            target.setParent(null);
            target.setTopParent(null);
            tool.setTitle(target.getTitle());
            sitePage.setTitle(target.getTitle());
            sitePage.setTitleCustom(true);
        } catch (RuntimeException e) {
            restore(target, oldToolId, oldParent, oldTopParent, site, sitePage, false);
            log.error("Unable to create site navigation while promoting page {} in site {}", pageId, siteId, e);
            return PagePromotionResult.NAVIGATION_CREATION_FAILED;
        }

        PromotionState promotionState = new PromotionState();
        try {
            PagePromotionResult result = transactionTemplate.execute(status -> promote(target, canonicalPageId, site, status, promotionState));
            if (result == null) {
                result = PagePromotionResult.PERSISTENCE_FAILED;
            }
            if (result != PagePromotionResult.SUCCESS) {
                restore(target, oldToolId, oldParent, oldTopParent, site, sitePage, promotionState.siteSaved);
                if (result == PagePromotionResult.PERSISTENCE_FAILED) {
                    log.error("Unable to add page {} as top level in site {}: {}", pageId, siteId, result);
                }
            }
            return result;
        } catch (RuntimeException e) {
            restore(target, oldToolId, oldParent, oldTopParent, site, sitePage, promotionState.siteSaved);
            log.error("Unable to add page {} as top level in site {}", pageId, siteId, e);
            return PagePromotionResult.PERSISTENCE_FAILED;
        }
    }

    private boolean isStudentPage(SimplePage page) {
        return page != null && page.getOwner() != null && !page.isOwned();
    }

    private PagePromotionResult promote(SimplePage target, String canonicalPageId, Site site, TransactionStatus status,
            PromotionState promotionState) {
        List<String> updateErrors = new ArrayList<>();
        if (!simplePageToolDao.update(target, updateErrors, "Unable to update page", true)) {
            status.setRollbackOnly();
            log.warn("Unable to update page {} while promoting it in site {}: {}", canonicalPageId, site.getId(), updateErrors);
            return PagePromotionResult.PAGE_UPDATE_FAILED;
        }

        SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, canonicalPageId, target.getTitle());
        List<String> itemErrors = new ArrayList<>();
        if (!simplePageToolDao.saveItem(item, itemErrors, "Unable to save page", true)) {
            status.setRollbackOnly();
            log.warn("Unable to save a top-level item for page {} in site {}: {}", canonicalPageId, site.getId(), itemErrors);
            return PagePromotionResult.TOP_LEVEL_ITEM_SAVE_FAILED;
        }

        try {
            simplePageToolDao.flush();
            siteService.save(site);
            promotionState.siteSaved = true;
            return PagePromotionResult.SUCCESS;
        } catch (Exception e) {
            status.setRollbackOnly();
            log.error("Unable to save site navigation while promoting page {} in site {}", canonicalPageId, site.getId(), e);
            return PagePromotionResult.SITE_SAVE_FAILED;
        }
    }

    private Long parsePageId(String submittedPageId) {
        if (!StringUtils.isNumeric(submittedPageId)) {
            return null;
        }

        try {
            long parsedPageId = Long.parseLong(submittedPageId);
            return parsedPageId > 0 ? parsedPageId : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void restore(SimplePage target, String toolId, Long parent, Long topParent, Site site, SitePage sitePage,
            boolean persistSiteRestore) {
        target.setToolId(toolId);
        target.setParent(parent);
        target.setTopParent(topParent);
        if (sitePage != null) {
            site.removePage(sitePage);
            if (persistSiteRestore) {
                try {
                    siteService.save(site);
                } catch (Exception e) {
                    log.error("Unable to remove site page {} while restoring site {}", sitePage.getId(), site.getId(), e);
                }
            }
        }
    }
}
