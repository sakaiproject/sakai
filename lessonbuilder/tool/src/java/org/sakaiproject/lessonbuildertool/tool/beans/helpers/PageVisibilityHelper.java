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
package org.sakaiproject.lessonbuildertool.tool.beans.helpers;

import java.util.ArrayList;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.service.PageIndexService.PageNode;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.ToolManager;

/** Applies the existing Lessons visibility rules to a page index. */
public final class PageVisibilityHelper {

    private PageVisibilityHelper() {
    }

    public static VisibilityResult getVisiblePages(
            List<PageNode> pages, boolean canEditPage, SimplePageBean simplePageBean) {
        if (canEditPage) {
            return new VisibilityResult(List.copyOf(pages), false);
        }

        List<PageNode> visiblePages = new ArrayList<>();
        Integer hiddenSubtreeLevel = null;
        boolean hasPrerequisites = false;
        for (PageNode node : pages) {
            if (hiddenSubtreeLevel != null && node.level() > hiddenSubtreeLevel) {
                continue;
            }
            hiddenSubtreeLevel = null;

            Visibility visibility = getVisibility(node, simplePageBean);
            hasPrerequisites |= visibility.hasPrerequisites();
            if (!visibility.visible()) {
                hiddenSubtreeLevel = node.level();
                continue;
            }
            visiblePages.add(node);
        }
        return new VisibilityResult(List.copyOf(visiblePages), hasPrerequisites);
    }

    private static Visibility getVisibility(PageNode node, SimplePageBean simplePageBean) {
        boolean hasPrerequisites;
        try {
            hasPrerequisites = node.pageItem().isPrerequisite()
                    || simplePageBean.getItemGroups(node.pageItem(), null, false) != null;
        } catch (IdUnusedException e) {
            return new Visibility(false, false);
        }

        SimplePage page = node.page();
        if (page.isHidden() || page.getReleaseDate() != null
                && page.getReleaseDate().toInstant().isAfter(Instant.now())) {
            return new Visibility(false, hasPrerequisites);
        }
        if (!node.topLevel() || page.getToolId() == null) {
            return new Visibility(true, hasPrerequisites);
        }

        SitePage sitePage = simplePageBean.getCurrentSite().getPage(page.getToolId());
        if (sitePage == null) {
            return new Visibility(false, hasPrerequisites);
        }
        for (ToolConfiguration placement : sitePage.getTools()) {
            Properties roleConfig = placement.getPlacementConfig();
            String roleList = roleConfig.getProperty("functions.require");
            String visibility = roleConfig.getProperty(ToolManager.PORTAL_VISIBLE);
            if (!"false".equals(visibility)
                    && (roleList == null || !roleList.contains(SiteService.SECURE_UPDATE_SITE))) {
                return new Visibility(true, hasPrerequisites);
            }
        }
        return new Visibility(false, hasPrerequisites);
    }

    private record Visibility(boolean visible, boolean hasPrerequisites) {
    }

    public record VisibilityResult(List<PageNode> pages, boolean hasPrerequisites) {
    }
}
