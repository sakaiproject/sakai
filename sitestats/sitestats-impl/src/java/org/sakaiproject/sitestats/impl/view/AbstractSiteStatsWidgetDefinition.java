/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import lombok.Setter;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.report.ReportManager;

abstract class AbstractSiteStatsWidgetDefinition implements SiteStatsWidgetDefinition {

	@Setter protected SiteStatsWidgetDefinitionSupport support;

	protected String message(String key) {
		return support.message(key);
	}

	protected StatsManager statsManager() {
		return support.getStatsManager();
	}

	protected ReportManager reportManager() {
		return support.getReportManager();
	}

	protected EventRegistryService eventRegistryService() {
		return support.getEventRegistryService();
	}

	protected WidgetFilterCatalog filterCatalog() {
		return support.getFilterCatalog();
	}

	protected WidgetReportDefFactory reportFactory() {
		return support.getReportFactory();
	}

	protected WidgetMetricSupport metricSupport() {
		return support.getMetricSupport();
	}

	protected WidgetSpec widgetSpec(String id, String titleKey, String icon, String audience, BooleanSupplier available,
			List<WidgetTabSpec> tabs, List<WidgetMetricSpec> metrics) {
		return new WidgetSpec(id, titleKey, icon, audience, available, tabs, metrics);
	}

	protected List<WidgetTabSpec> tabs(WidgetTabSpec... tabs) {
		return Collections.unmodifiableList(Arrays.asList(tabs));
	}

	protected List<WidgetMetricSpec> metrics(WidgetMetricSpec... metrics) {
		return Collections.unmodifiableList(Arrays.asList(metrics));
	}

	protected WidgetTabSpec tabSpec(String widgetId, String id, String titleKey, WidgetReportFactory reportFactory, String... filterIds) {
		return new WidgetTabSpec(widgetId, id, titleKey, Arrays.asList(filterIds), reportFactory);
	}

	protected WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience,
			WidgetReportFactory reportFactory, WidgetMetricValueFactory valueFactory) {
		return metricSpec(widgetId, id, labelKey, audience, () -> true, reportFactory, valueFactory);
	}

	protected WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience, BooleanSupplier available,
			WidgetReportFactory reportFactory, WidgetMetricValueFactory valueFactory) {
		return new WidgetMetricSpec(widgetId, id, labelKey, audience, available, reportFactory, valueFactory);
	}
}
