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
