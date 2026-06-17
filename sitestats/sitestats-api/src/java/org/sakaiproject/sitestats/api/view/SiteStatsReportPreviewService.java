/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.view;

import org.sakaiproject.sitestats.api.report.ReportDef;

/**
 * Stores short-lived report definitions created by Wicket flows before they are
 * persisted, so client-rendered report panels can fetch them through the JSON API.
 */
public interface SiteStatsReportPreviewService {

	public String register(String siteId, String userId, ReportDef reportDef);

	public ReportDef get(String siteId, String userId, String previewId);
}
