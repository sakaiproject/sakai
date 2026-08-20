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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Creates or repairs the persisted top-level page for each Lessons placement. */
@Setter
public class PlacementPageService {

    private SimplePageToolDao simplePageToolDao;
    private SiteService siteService;
    private SecurityService securityService;

    /**
     * Ensures every Lessons placement in the site has one persisted page and top-level item.
     * Repeated calls preserve existing records. Serializable isolation prevents concurrent
     * requests from creating duplicate placement pages.
     *
     * @return the number of pages or top-level items created
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.SERIALIZABLE)
    public int ensurePlacementPagesExist(String siteId) {
        if (!securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteService.siteReference(siteId))) {
            throw new SecurityException("User cannot update Lessons in site " + siteId);
        }

        List<SimplePage> sitePages = simplePageToolDao.getSitePages(siteId);
        if (sitePages == null) {
            sitePages = Collections.emptyList();
        }

        Map<String, SimplePage> placementPages = new LinkedHashMap<>();
        for (SimplePage page : sitePages) {
            if (page.getParent() == null) {
                placementPages.putIfAbsent(page.getToolId(), page);
            }
        }

        int createdCount = 0;
        List<ToolConfiguration> siteTools = simplePageToolDao.getSiteTools(siteId);
        if (siteTools == null) {
            return createdCount;
        }

        for (ToolConfiguration tool : siteTools) {
            SimplePage page = placementPages.get(tool.getPageId());
            if (page == null) {
                page = simplePageToolDao.makePage(tool.getPageId(), siteId, tool.getTitle(), null, null);
                saveOrFail(page, "page", tool.getPageId());
                placementPages.put(tool.getPageId(), page);
                createdCount++;
            }

            if (simplePageToolDao.findTopLevelPageItemBySakaiId(Long.toString(page.getPageId())) == null) {
                SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE,
                        Long.toString(page.getPageId()), page.getTitle());
                saveOrFail(item, "top-level page item", tool.getPageId());
                createdCount++;
            }
        }
        return createdCount;
    }

    private void saveOrFail(Object value, String valueType, String toolId) {
        if (!simplePageToolDao.saveItem(value, new ArrayList<>(), "ignored", false)) {
            throw new IllegalStateException("Failed to save Lessons " + valueType + " for tool " + toolId);
        }
    }
}
