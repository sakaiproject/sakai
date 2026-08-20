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
package org.sakaiproject.sitestats.tool.mvc;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportAccessService;
import org.sakaiproject.tool.api.ToolManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SiteStatsToolAuthorizationService {

    private final SiteStatsReportAccessService reportAccessService;
    private final StatsAuthz statsAuthz;
    private final StatsManager statsManager;
    private final ToolManager toolManager;

    public String currentSiteId() {
        return toolManager.getCurrentPlacement().getContext();
    }

    public String currentUserId() {
        return reportAccessService.currentUserId();
    }

    public boolean isAdminTool() {
        return StatsManager.SITESTATS_ADMIN_TOOLID.equals(toolManager.getCurrentTool().getId());
    }

    public String viewSite(String requestedSiteId) {
        return authorizedSite(requestedSiteId, AccessLevel.VIEW);
    }

    public String reportSite(String requestedSiteId) {
        return authorizedSite(requestedSiteId, AccessLevel.ALL);
    }

    public String adminSite(String requestedSiteId) {
        return authorizedSite(requestedSiteId, AccessLevel.ADMIN);
    }

    public boolean canViewUserActivity(String siteId) {
        return statsManager.isDisplayDetailedEvents() && statsAuthz.canCurrentUserTrackInSite(siteId);
    }

    public boolean canViewAllSiteStats(String siteId) {
        return statsAuthz.isUserAbleToViewSiteStatsAll(siteId);
    }

    private String authorizedSite(String requestedSiteId, AccessLevel accessLevel) {
        String siteId = StringUtils.defaultIfBlank(requestedSiteId, currentSiteId());
        String currentSiteId = currentSiteId();
        if (!siteId.equals(currentSiteId)) {
            reportAccessService.assertCanViewAdmin(currentSiteId);
        }
        if (AccessLevel.ADMIN == accessLevel) {
            reportAccessService.assertCanViewAdmin(siteId);
        } else if (AccessLevel.ALL == accessLevel) {
            reportAccessService.assertCanViewAll(siteId);
        } else {
            reportAccessService.assertCanView(siteId);
        }
        return siteId;
    }

    private enum AccessLevel {
        VIEW,
        ALL,
        ADMIN
    }
}
