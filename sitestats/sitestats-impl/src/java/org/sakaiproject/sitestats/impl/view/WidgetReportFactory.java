/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

interface WidgetReportFactory {

	WidgetReportDefinition build(String siteId, SiteStatsReportRequest request, String userId);
}
