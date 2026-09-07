/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetricSnapshot;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;

public class SiteStatsWidgetCatalog {

	@Setter private SiteStatsWidgetDefinitionSupport support;
	@Setter private List<SiteStatsWidgetDefinition> widgetDefinitions = new ArrayList<SiteStatsWidgetDefinition>();

	private Map<String, WidgetSpec> widgetSpecs;
	private Map<String, WidgetTabSpec> tabSpecs;
	private Map<String, WidgetMetricSpec> metricSpecs;

	public SiteStatsOverview getOverview(String siteId, boolean allAllowed, boolean ownAllowed, boolean adminAllowed, String userId) {
		SiteStatsOverview overview = new SiteStatsOverview();
		overview.setSiteId(siteId);
		overview.setViewAllowed(true);
		overview.setAllAllowed(allAllowed);
		overview.setOwnAllowed(ownAllowed);
		overview.setAdminAllowed(adminAllowed);

		for (WidgetSpec spec : widgetSpecs.values()) {
			if (!spec.isAvailable() || !isAudienceAllowed(spec.getAudience(), allAllowed, ownAllowed)) {
				continue;
			}
			overview.getWidgets().add(toWidget(siteId, spec, userId));
		}
		return overview;
	}

	public SiteStatsWidgetTab getWidgetTab(String siteId, String widgetId, String tabId) {
		WidgetTabSpec spec = tabSpecs.get(key(widgetId, tabId));
		if (spec == null || !widgetAvailable(widgetId)) {
			throw new IllegalArgumentException("Unknown SiteStats widget tab: " + widgetId + "/" + tabId);
		}
		return toTab(siteId, spec);
	}

	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId, String userId) {
		return getWidgetMetrics(siteId, widgetId, userId, null);
	}

	public List<SiteStatsWidgetMetric> getWidgetMetrics(String siteId, String widgetId, String userId,
			SiteStatsReportRequest request) {
		WidgetSpec spec = widgetSpecs.get(widgetId);
		if (spec == null || !spec.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget: " + widgetId);
		}
		return toMetrics(siteId, spec, userId, true, request);
	}

	public List<SiteStatsChart> getWidgetHighlights(String siteId, String widgetId, String userId,
			SiteStatsReportRequest request) {
		WidgetSpec spec = widgetSpecs.get(widgetId);
		if (spec == null || !spec.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget: " + widgetId);
		}
		return toHighlights(siteId, spec, userId, request);
	}

	public SiteStatsWidgetMetric getWidgetMetric(String siteId, String widgetId, String metricId, String userId) {
		WidgetSpec spec = widgetSpecs.get(widgetId);
		WidgetMetricSpec metric = metricSpecs.get(key(widgetId, metricId));
		if (spec == null || metric == null || !spec.isAvailable() || !metric.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget metric: " + widgetId + "/" + metricId);
		}
		return toMetric(siteId, spec, metric, userId, true);
	}

	public boolean isIncompleteCustomRange(SiteStatsReportRequest request) {
		return support.getFilterCatalog().isIncompleteCustomRange(request);
	}

	public boolean isOwnOnlyWidget(String widgetId) {
		WidgetSpec spec = widgetSpecs.get(widgetId);
		return spec != null && AUDIENCE_OWN.equals(spec.getAudience());
	}

	public boolean isOwnOnlyMetric(String widgetId, String metricId) {
		WidgetMetricSpec spec = metricSpecs.get(key(widgetId, metricId));
		return spec != null && AUDIENCE_OWN.equals(spec.getAudience());
	}

	public WidgetReportDefinition getWidgetReportDefinition(String siteId, String widgetId, String tabId,
			SiteStatsReportRequest request, String userId) {
		WidgetTabSpec spec = tabSpecs.get(key(widgetId, tabId));
		if (spec == null || spec.getReportFactory() == null || !widgetAvailable(widgetId)) {
			throw new IllegalArgumentException("Unknown SiteStats widget report: " + widgetId + "/" + tabId);
		}
		return spec.getReportFactory().build(siteId, SiteStatsReportRequest.normalized(request), userId);
	}

	public SiteStatsReportView getWidgetReportView(String siteId, String widgetId, String tabId,
			SiteStatsReportRequest request, String userId) {
		WidgetTabSpec spec = tabSpecs.get(key(widgetId, tabId));
		if (spec == null || !widgetAvailable(widgetId)) {
			throw new IllegalArgumentException("Unknown SiteStats widget report: " + widgetId + "/" + tabId);
		}
		if (spec.getViewFactory() == null) {
			return null;
		}
		return spec.getViewFactory().build(siteId, SiteStatsReportRequest.normalized(request), userId);
	}

	public void init() {
		buildRegistry();
	}

	public WidgetReportDefinition getWidgetMetricReportDefinition(String siteId, String widgetId, String metricId, String userId) {
		WidgetMetricSpec spec = metricSpecs.get(key(widgetId, metricId));
		if (spec == null || spec.getReportFactory() == null || !widgetAvailable(widgetId) || !spec.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget metric report: " + widgetId + "/" + metricId);
		}
		return spec.getReportFactory().build(siteId, new SiteStatsReportRequest(), userId);
	}

	public SiteStatsReportView getWidgetMetricReportView(String siteId, String widgetId, String metricId, String userId) {
		WidgetMetricSpec spec = metricSpecs.get(key(widgetId, metricId));
		if (spec == null || !widgetAvailable(widgetId) || !spec.isAvailable()) {
			throw new IllegalArgumentException("Unknown SiteStats widget metric report: " + widgetId + "/" + metricId);
		}
		if (spec.getViewFactory() == null) {
			return null;
		}
		return spec.getViewFactory().build(siteId, new SiteStatsReportRequest(), userId);
	}

	private void buildRegistry() {
		if (widgetSpecs != null) {
			return;
		}

		Map<String, WidgetSpec> widgets = new LinkedHashMap<String, WidgetSpec>();
		for (SiteStatsWidgetDefinition definition : widgetDefinitions) {
			WidgetSpec spec = definition.getSpec();
			widgets.put(spec.getId(), spec);
		}
		widgetSpecs = Collections.unmodifiableMap(widgets);

		Map<String, WidgetTabSpec> tabs = new LinkedHashMap<String, WidgetTabSpec>();
		Map<String, WidgetMetricSpec> metrics = new LinkedHashMap<String, WidgetMetricSpec>();
		for (WidgetSpec widget : widgetSpecs.values()) {
			for (WidgetTabSpec tab : widget.getTabs()) {
				tabs.put(key(tab.getWidgetId(), tab.getId()), tab);
			}
			for (WidgetMetricSpec metric : widget.getMetrics()) {
				metrics.put(key(metric.getWidgetId(), metric.getId()), metric);
			}
		}
		tabSpecs = Collections.unmodifiableMap(tabs);
		metricSpecs = Collections.unmodifiableMap(metrics);
	}

	private boolean isAudienceAllowed(String audience, boolean allAllowed, boolean ownAllowed) {
		return (AUDIENCE_ALL.equals(audience) && allAllowed)
				|| (AUDIENCE_OWN.equals(audience) && ownAllowed && !allAllowed);
	}

	private boolean widgetAvailable(String widgetId) {
		WidgetSpec spec = widgetSpecs.get(widgetId);
		return spec != null && spec.isAvailable();
	}

	private SiteStatsWidget toWidget(String siteId, WidgetSpec spec, String userId) {
		SiteStatsWidget widget = new SiteStatsWidget();
		widget.setId(spec.getId());
		widget.setTitle(support.message(spec.getTitleKey()));
		widget.setIcon(spec.getIcon());
		widget.setAudience(spec.getAudience());
		List<SiteStatsWidgetTab> tabs = new ArrayList<SiteStatsWidgetTab>();
		for (WidgetTabSpec tab : spec.getTabs()) {
			tabs.add(toTab(siteId, tab));
		}
		widget.setTabs(tabs);
		widget.setMetrics(toMetrics(siteId, spec, userId, false, null));
		widget.setHighlights(toHighlights(siteId, spec, userId, null));
		widget.setToolFilters(support.getFilterCatalog().toolFilters(siteId, spec.getId(), spec.getToolFilterIds()));
		widget.setHighlightsConfigured(spec.getHighlights() != null && !spec.getHighlights().isEmpty());
		return widget;
	}

	private SiteStatsWidgetTab toTab(String siteId, WidgetTabSpec spec) {
		SiteStatsWidgetTab tab = new SiteStatsWidgetTab();
		tab.setId(spec.getId());
		tab.setTitle(support.message(spec.getTitleKey()));
		WidgetSpec widget = widgetSpecs.get(spec.getWidgetId());
		if (widget != null) {
			tab.setWidgetTitle(support.message(widget.getTitleKey()));
		}
		tab.setFilters(support.getFilterCatalog().filters(siteId, spec.getWidgetId(), spec.getFilterIds()));
		return tab;
	}

	private List<SiteStatsWidgetMetric> toMetrics(String siteId, WidgetSpec spec, String userId, boolean includeValues,
			SiteStatsReportRequest request) {
		List<SiteStatsWidgetMetric> metrics = new ArrayList<SiteStatsWidgetMetric>();
		for (WidgetMetricSpec metric : spec.getMetrics()) {
			if (metric.isAvailable()) {
				metrics.add(toMetric(siteId, spec, metric, userId, includeValues, request));
			}
		}
		return metrics;
	}

	private List<SiteStatsChart> toHighlights(String siteId, WidgetSpec spec, String userId, SiteStatsReportRequest request) {
		List<SiteStatsChart> charts = new ArrayList<SiteStatsChart>();
		if (spec.getHighlights() == null) {
			return charts;
		}
		for (WidgetHighlightSpec highlight : spec.getHighlights()) {
			if (!highlight.isAvailable() || highlight.getFactory() == null) {
				continue;
			}
			SiteStatsChart chart = highlight.getFactory().build(siteId, userId, SiteStatsReportRequest.normalized(request));
			if (chart != null) {
				charts.add(chart);
			}
		}
		return charts;
	}

	private SiteStatsWidgetMetric toMetric(String siteId, WidgetSpec spec, WidgetMetricSpec metric, String userId,
			boolean includeValues) {
		return toMetric(siteId, spec, metric, userId, includeValues, null);
	}

	private SiteStatsWidgetMetric toMetric(String siteId, WidgetSpec spec, WidgetMetricSpec metric, String userId,
			boolean includeValues, SiteStatsReportRequest request) {
		SiteStatsWidgetMetric viewMetric = new SiteStatsWidgetMetric(metric.getId(), support.message(metric.getLabelKey()),
				metric.getAudience(), metric.isReportable());
		viewMetric.setWidgetTitle(support.message(spec.getTitleKey()));
		if (includeValues && metric.getValueFactory() != null) {
			WidgetMetricValue value = metric.getValueFactory().getValue(siteId, userId,
					SiteStatsReportRequest.normalized(request));
			SiteStatsWidgetMetricSnapshot snapshot = new SiteStatsWidgetMetricSnapshot();
			snapshot.setPrimary(value.getPrimary());
			snapshot.setPercentage(value.getPercentage());
			snapshot.setDetail(value.getDetail());
			viewMetric.setSnapshot(snapshot);
		}
		return viewMetric;
	}

	private String key(String widgetId, String id) {
		return widgetId + "/" + id;
	}
}
