/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;

class WidgetMetricMiniStat extends WidgetMiniStat {

	private static final long serialVersionUID = 1L;

	private final SiteStatsWidgetMetric metric;

	WidgetMetricMiniStat(SiteStatsWidgetMetric metric) {
		this.metric = metric;
	}

	@Override
	public String getMetricId() {
		return metric.getId();
	}

	@Override
	public String getValue() {
		return WidgetMetricDisplay.primary(metric);
	}

	@Override
	public String getSecondValue() {
		return WidgetMetricDisplay.secondary(metric);
	}

	@Override
	public String getTooltip() {
		return WidgetMetricDisplay.tooltip(metric);
	}

	@Override
	public boolean isWiderText() {
		return WidgetMetricDisplay.usesDetailLayout(metric);
	}

	@Override
	public String getLabel() {
		return metric.getLabel();
	}

	@Override
	public String getReportMetricId() {
		return metric.isReportable() ? metric.getId() : null;
	}
}
