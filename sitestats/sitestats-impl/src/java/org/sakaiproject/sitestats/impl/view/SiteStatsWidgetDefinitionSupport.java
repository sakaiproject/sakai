/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Setter;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.report.ReportManager;

public class SiteStatsWidgetDefinitionSupport {

	@Setter private SiteStatsWidgetContext context;
	@Setter private WidgetFilterCatalog filterCatalog;
	@Setter private WidgetReportDefFactory reportFactory;
	@Setter private WidgetMetricSupport metricSupport;

	public StatsManager getStatsManager() {
		return context.getStatsManager();
	}

	public ReportManager getReportManager() {
		return context.getReportManager();
	}

	public SiteStatsToolEventsService getSiteStatsToolEventsService() {
		return context.getSiteStatsToolEventsService();
	}

	public EventRegistryService getEventRegistryService() {
		return context.getEventRegistryService();
	}

	public WidgetFilterCatalog getFilterCatalog() {
		return filterCatalog;
	}

	public WidgetReportDefFactory getReportFactory() {
		return reportFactory;
	}

	public WidgetMetricSupport getMetricSupport() {
		return metricSupport;
	}

	public String message(String key) {
		return context.message(key);
	}

	public String message(String key, String defaultValue) {
		return context.message(key, defaultValue);
	}
}
