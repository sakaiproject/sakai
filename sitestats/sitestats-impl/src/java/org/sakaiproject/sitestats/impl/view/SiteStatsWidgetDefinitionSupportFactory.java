/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Setter;

public class SiteStatsWidgetDefinitionSupportFactory {

	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetReportDefFactory reportFactory;
	@Setter private WidgetMetricSupport metricSupport;

	public SiteStatsWidgetDefinitionSupport create() {
		filterCatalog.setContext(context);

		reportFactory.setContext(context);
		reportFactory.setFilterCatalog(filterCatalog);

		metricSupport.setContext(context);
		metricSupport.setReportFactory(reportFactory);

		SiteStatsWidgetDefinitionSupport support = new SiteStatsWidgetDefinitionSupport();
		support.setContext(context);
		support.setFilterCatalog(filterCatalog);
		support.setReportFactory(reportFactory);
		support.setMetricSupport(metricSupport);
		return support;
	}
}
