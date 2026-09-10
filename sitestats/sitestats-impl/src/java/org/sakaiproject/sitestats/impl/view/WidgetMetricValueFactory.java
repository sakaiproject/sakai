/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

@FunctionalInterface
interface WidgetMetricValueFactory {

	WidgetMetricValue getValue(String siteId, String userId, SiteStatsReportRequest request);
}
