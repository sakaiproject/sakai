/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class SiteStatsReportRequests {

	static SiteStatsReportRequest orDefault(SiteStatsReportRequest request) {
		return request == null ? new SiteStatsReportRequest() : new SiteStatsReportRequest(request);
	}
}
