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
	@Setter private SiteStatsSubmissionsAnalytics submissionsAnalytics;
	@Setter private SiteStatsGradesAnalytics gradesAnalytics;

	public SiteStatsWidgetDefinitionSupport create() {
		filterCatalog.setContext(context);

		reportFactory.setContext(context);
		reportFactory.setFilterCatalog(filterCatalog);

		metricSupport.setContext(context);
		metricSupport.setReportFactory(reportFactory);

		if (submissionsAnalytics != null) {
			submissionsAnalytics.setContext(context);
			submissionsAnalytics.setFilterCatalog(filterCatalog);
			submissionsAnalytics.setMetricSupport(metricSupport);
			filterCatalog.setSubmissionsAnalytics(submissionsAnalytics);
		}

		if (gradesAnalytics != null) {
			gradesAnalytics.setContext(context);
			gradesAnalytics.setFilterCatalog(filterCatalog);
			gradesAnalytics.setMetricSupport(metricSupport);
			filterCatalog.setGradesAnalytics(gradesAnalytics);
		}

		SiteStatsWidgetDefinitionSupport support = new SiteStatsWidgetDefinitionSupport();
		support.setContext(context);
		support.setFilterCatalog(filterCatalog);
		support.setReportFactory(reportFactory);
		support.setMetricSupport(metricSupport);
		support.setSubmissionsAnalytics(submissionsAnalytics);
		support.setGradesAnalytics(gradesAnalytics);
		return support;
	}
}
