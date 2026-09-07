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

import org.sakaiproject.site.api.SiteService;
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

	protected SiteService siteService() {
		return support.getSiteService();
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

	protected SiteStatsSubmissionsAnalytics submissionsAnalytics() {
		return support.getSubmissionsAnalytics();
	}

	protected SiteStatsGradesAnalytics gradesAnalytics() {
		return support.getGradesAnalytics();
	}

	protected WidgetSpec widgetSpec(String id, String titleKey, String icon, String audience, BooleanSupplier available,
			List<WidgetTabSpec> tabs, List<WidgetMetricSpec> metrics) {
		return widgetSpec(id, titleKey, icon, audience, available, tabs, metrics, Collections.emptyList());
	}

	protected WidgetSpec widgetSpec(String id, String titleKey, String icon, String audience, BooleanSupplier available,
			List<WidgetTabSpec> tabs, List<WidgetMetricSpec> metrics, List<WidgetHighlightSpec> highlights) {
		return widgetSpec(id, titleKey, icon, audience, available, tabs, metrics, highlights, Collections.emptyList());
	}

	protected WidgetSpec widgetSpec(String id, String titleKey, String icon, String audience, BooleanSupplier available,
			List<WidgetTabSpec> tabs, List<WidgetMetricSpec> metrics, List<WidgetHighlightSpec> highlights,
			List<String> toolFilterIds) {
		List<String> ids = toolFilterIds == null ? Collections.emptyList() : Collections.unmodifiableList(toolFilterIds);
		return new WidgetSpec(id, titleKey, icon, audience, available, tabs, metrics, highlights, ids);
	}

	protected List<String> toolFilterIds(String... ids) {
		return Collections.unmodifiableList(Arrays.asList(ids));
	}

	protected List<WidgetTabSpec> tabs(WidgetTabSpec... tabs) {
		return Collections.unmodifiableList(Arrays.asList(tabs));
	}

	protected List<WidgetMetricSpec> metrics(WidgetMetricSpec... metrics) {
		return Collections.unmodifiableList(Arrays.asList(metrics));
	}

	protected List<WidgetHighlightSpec> highlights(WidgetHighlightSpec... highlights) {
		return Collections.unmodifiableList(Arrays.asList(highlights));
	}

	protected WidgetHighlightSpec highlightSpec(String id, String titleKey, WidgetHighlightFactorySimple factory) {
		return highlightSpec(id, titleKey, () -> true, factory);
	}

	protected WidgetHighlightSpec highlightSpec(String id, String titleKey, WidgetHighlightFactory factory) {
		return highlightSpec(id, titleKey, () -> true, factory);
	}

	protected WidgetHighlightSpec highlightSpec(String id, String titleKey, BooleanSupplier available,
			WidgetHighlightFactorySimple factory) {
		return highlightSpec(id, titleKey, available, adapt(factory));
	}

	protected WidgetHighlightSpec highlightSpec(String id, String titleKey, BooleanSupplier available,
			WidgetHighlightFactory factory) {
		return new WidgetHighlightSpec(id, titleKey, available, factory);
	}

	protected WidgetTabSpec tabSpec(String widgetId, String id, String titleKey, WidgetReportFactory reportFactory, String... filterIds) {
		return new WidgetTabSpec(widgetId, id, titleKey, Arrays.asList(filterIds), reportFactory);
	}

	protected WidgetTabSpec viewTabSpec(String widgetId, String id, String titleKey, WidgetReportViewFactory viewFactory,
			String... filterIds) {
		return new WidgetTabSpec(widgetId, id, titleKey, Arrays.asList(filterIds), null, viewFactory);
	}

	protected WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience,
			WidgetReportFactory reportFactory, WidgetMetricValueFactorySimple valueFactory) {
		return metricSpec(widgetId, id, labelKey, audience, () -> true, reportFactory, valueFactory);
	}

	protected WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience,
			WidgetReportFactory reportFactory, WidgetMetricValueFactory valueFactory) {
		return metricSpec(widgetId, id, labelKey, audience, () -> true, reportFactory, valueFactory);
	}

	protected WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience, BooleanSupplier available,
			WidgetReportFactory reportFactory, WidgetMetricValueFactorySimple valueFactory) {
		return metricSpec(widgetId, id, labelKey, audience, available, reportFactory, adapt(valueFactory));
	}

	protected WidgetMetricSpec metricSpec(String widgetId, String id, String labelKey, String audience, BooleanSupplier available,
			WidgetReportFactory reportFactory, WidgetMetricValueFactory valueFactory) {
		return new WidgetMetricSpec(widgetId, id, labelKey, audience, available, reportFactory, null, valueFactory);
	}

	protected WidgetMetricSpec viewMetricSpec(String widgetId, String id, String labelKey, String audience,
			WidgetReportViewFactory viewFactory, WidgetMetricValueFactorySimple valueFactory) {
		return viewMetricSpec(widgetId, id, labelKey, audience, () -> true, viewFactory, valueFactory);
	}

	protected WidgetMetricSpec viewMetricSpec(String widgetId, String id, String labelKey, String audience,
			WidgetReportViewFactory viewFactory, WidgetMetricValueFactory valueFactory) {
		return viewMetricSpec(widgetId, id, labelKey, audience, () -> true, viewFactory, valueFactory);
	}

	protected WidgetMetricSpec viewMetricSpec(String widgetId, String id, String labelKey, String audience, BooleanSupplier available,
			WidgetReportViewFactory viewFactory, WidgetMetricValueFactorySimple valueFactory) {
		return viewMetricSpec(widgetId, id, labelKey, audience, available, viewFactory, adapt(valueFactory));
	}

	protected WidgetMetricSpec viewMetricSpec(String widgetId, String id, String labelKey, String audience, BooleanSupplier available,
			WidgetReportViewFactory viewFactory, WidgetMetricValueFactory valueFactory) {
		return new WidgetMetricSpec(widgetId, id, labelKey, audience, available, null, viewFactory, valueFactory);
	}

	private WidgetMetricValueFactory adapt(WidgetMetricValueFactorySimple valueFactory) {
		return (siteId, userId, request) -> valueFactory.getValue(siteId, userId);
	}

	private WidgetHighlightFactory adapt(WidgetHighlightFactorySimple factory) {
		return (siteId, userId, request) -> factory.build(siteId, userId);
	}
}
