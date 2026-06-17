/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

final class SiteStatsReportRequests {

	private static final int MAX_PAGE_SIZE = 500;

	private SiteStatsReportRequests() {
	}

	static SiteStatsReportRequest orDefault(SiteStatsReportRequest request) {
		SiteStatsReportRequest safeRequest = request == null ? new SiteStatsReportRequest() : new SiteStatsReportRequest(request);
		if (safeRequest.getPageSize() > MAX_PAGE_SIZE) {
			safeRequest.setPageSize(MAX_PAGE_SIZE);
		}
		return safeRequest;
	}
}
