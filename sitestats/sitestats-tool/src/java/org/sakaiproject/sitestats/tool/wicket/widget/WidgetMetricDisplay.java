/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_AVERAGE_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_PRESENCE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_VISITS_TOTAL;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetricSnapshot;

final class WidgetMetricDisplay {

	private static final int MAX_TEXT_LENGTH = 16;

	private WidgetMetricDisplay() {
	}

	static String primary(SiteStatsWidgetMetric metric) {
		SiteStatsWidgetMetricSnapshot snapshot = metric.getSnapshot();
		if (snapshot == null || snapshot.getPrimary() == null) {
			return null;
		}
		if (shouldTruncate(snapshot)) {
			return snapshot.getPrimary().substring(0, MAX_TEXT_LENGTH) + "...";
		}
		return snapshot.getPrimary();
	}

	static String secondary(SiteStatsWidgetMetric metric) {
		SiteStatsWidgetMetricSnapshot snapshot = metric.getSnapshot();
		if (snapshot == null || snapshot.getPercentage() == null) {
			return null;
		}
		return snapshot.getPercentage() + "%";
	}

	static String tooltip(SiteStatsWidgetMetric metric) {
		SiteStatsWidgetMetricSnapshot snapshot = metric.getSnapshot();
		if (snapshot == null || StringUtils.isBlank(snapshot.getDetail())) {
			return null;
		}
		return snapshot.getDetail();
	}

	static boolean usesDetailLayout(SiteStatsWidgetMetric metric) {
		return shouldTruncate(metric.getSnapshot());
	}

	static String studentIconClass(String metricId) {
		if (METRIC_STUDENT_VISITS_TOTAL.equals(metricId)) {
			return "fa-eye";
		}
		if (METRIC_STUDENT_VISITS_AVERAGE_PRESENCE.equals(metricId)) {
			return "fa-hourglass-end";
		}
		if (METRIC_STUDENT_VISITS_PRESENCE.equals(metricId)) {
			return "fa-clock-o";
		}
		return null;
	}

	private static boolean shouldTruncate(SiteStatsWidgetMetricSnapshot snapshot) {
		return snapshot != null
				&& StringUtils.isNotBlank(snapshot.getDetail())
				&& snapshot.getPrimary() != null
				&& snapshot.getPrimary().length() > MAX_TEXT_LENGTH;
	}
}
