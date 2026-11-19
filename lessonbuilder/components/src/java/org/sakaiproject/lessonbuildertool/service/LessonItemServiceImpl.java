/**
 * Copyright (c) 2026 The Apereo Foundation
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
import java.util.Objects;

import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.springframework.transaction.annotation.Transactional;

@Setter
public class LessonItemServiceImpl implements LessonItemService {
    private SimplePageToolDao dao;
    private SecurityService securityService;
    private SiteService siteService;
    private SessionManager sessionManager;

    @Override
    @Transactional
    public List<SimplePageItem> createItems(String siteId, long lessonId, List<SimplePageItem> items) {
        String userId = sessionManager.getCurrentSessionUserId();
        SimplePageItem lesson = dao.findItem(lessonId);
        if (lesson == null || lesson.getType() != SimplePageItem.PAGE) {
            throw new IllegalArgumentException("Lesson not found");
        }
        SimplePage page = dao.getPage(Long.parseLong(lesson.getSakaiId()));
        if (page == null || !Objects.equals(siteId, page.getSiteId()) || StringUtils.isBlank(userId)
                || !securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteService.siteReference(page.getSiteId()))) {
            throw new SecurityException("Not allowed to edit this lesson");
        }
        if (!dao.findPageItemsByPageId(page.getPageId()).isEmpty()) {
            throw new IllegalArgumentException("Lesson must be empty before bulk creation");
        }
        if (items == null || items.stream().anyMatch(item -> item == null || item.getId() != 0
                || item.getPageId() != page.getPageId())) {
            throw new SecurityException("Items must belong to the authorized lesson page");
        }
        for (SimplePageItem item : items) {
            if (!dao.quickSaveItem(item)) {
                throw new IllegalStateException("Could not create lesson item");
            }
        }
        return dao.findPageItemsByPageId(page.getPageId());
    }
}
