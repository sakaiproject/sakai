/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

final class SiteStatsReportRequests {

	private SiteStatsReportRequests() {
	}

	static SiteStatsReportRequest orDefault(SiteStatsReportRequest request) {
		return request == null ? new SiteStatsReportRequest() : new SiteStatsReportRequest(request);
	}
}
