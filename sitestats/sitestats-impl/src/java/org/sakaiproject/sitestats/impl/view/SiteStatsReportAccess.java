/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import javax.persistence.EntityNotFoundException;

import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportPreviewService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

@Setter
public class SiteStatsReportAccess {

	private StatsAuthz statsAuthz;
	private ReportManager reportManager;
	private SiteStatsReportPreviewService siteStatsReportPreviewService;
	private SessionManager sessionManager;
	private SiteStatsWidgetCatalog siteStatsWidgetCatalog;

	ReportDef persistedReportDefinition(long reportId) {
		if (reportId <= 0) {
			throw new IllegalArgumentException("Unknown report id: " + reportId);
		}

		try {
			ReportDef reportDef = reportManager.getReportDefinition(reportId);
			if (reportDef != null) {
				return reportDef;
			}
		} catch (EntityNotFoundException e) {
			throw new IllegalArgumentException("Unknown report id: " + reportId, e);
		}
		throw new IllegalArgumentException("Unknown report id: " + reportId);
	}

	ReportDef previewReportDefinition(String siteId, String previewId) {
		ReportDef reportDef = siteStatsReportPreviewService == null ? null : siteStatsReportPreviewService.get(siteId, currentUserId(), previewId);
		if (reportDef == null) {
			throw new IllegalArgumentException("Unknown report preview id: " + previewId);
		}
		return reportDef;
	}

	String currentUserId() {
		if (sessionManager == null) {
			throw new SecurityException("Current Sakai session is required to access SiteStats reports");
		}
		Session session = sessionManager.getCurrentSession();
		if (session == null || StringUtils.isBlank(session.getUserId())) {
			throw new SecurityException("Current Sakai user is required to access SiteStats reports");
		}
		return session.getUserId();
	}

	void assertCanView(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStats(siteId)) {
			throw new SecurityException("Current user cannot view SiteStats for site " + siteId);
		}
	}

	boolean isViewAllAllowed(String siteId) {
		return statsAuthz.isUserAbleToViewSiteStatsAll(siteId);
	}

	boolean isViewOwnAllowed(String siteId) {
		return statsAuthz.isUserAbleToViewSiteStatsOwn(siteId);
	}

	boolean isViewAdminAllowed(String siteId) {
		return statsAuthz.isUserAbleToViewSiteStatsAdmin(siteId);
	}

	void assertCanViewAdmin(String siteId) {
		assertCanView(siteId);
		if (!statsAuthz.isUserAbleToViewSiteStatsAdmin(siteId)) {
			throw new SecurityException("Current user cannot view admin SiteStats data for site " + siteId);
		}
	}

	void assertCanViewAll(String siteId) {
		assertCanView(siteId);
		assertCanViewAllStats(siteId);
	}

	void assertCanViewWidget(String siteId, String widgetId) {
		assertCanView(siteId);
		if (siteStatsWidgetCatalog.isOwnOnlyWidget(widgetId)) {
			assertCanViewOwn(siteId);
		} else {
			assertCanViewAllStats(siteId);
		}
	}

	void assertCanViewMetric(String siteId, String widgetId, String metricId) {
		assertCanView(siteId);
		if (siteStatsWidgetCatalog.isOwnOnlyMetric(widgetId, metricId)) {
			assertCanViewOwn(siteId);
		} else {
			assertCanViewAllStats(siteId);
		}
	}

	private void assertCanViewAllStats(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStatsAll(siteId)) {
			throw new SecurityException("Current user cannot view all SiteStats data for site " + siteId);
		}
	}

	private void assertCanViewOwn(String siteId) {
		if (!statsAuthz.isUserAbleToViewSiteStatsOwn(siteId)) {
			throw new SecurityException("Current user cannot view own SiteStats data for site " + siteId);
		}
	}
}
