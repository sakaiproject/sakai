/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import org.sakaiproject.sitestats.api.report.ReportDef;

/**
 * Central authorization and ownership gate for SiteStats report operations.
 */
public interface SiteStatsReportAccessService {

    public String currentUserId();

    public void assertCanView(String siteId);

    public void assertCanViewAll(String siteId);

    public void assertCanViewAdmin(String siteId);

    public ReportDef persistedReportDefinition(String siteId, long reportId);

    public ReportDef persistedSiteReportDefinition(String siteId, long reportId);

    public ReportDef previewReportDefinition(String siteId, String previewId);
}
