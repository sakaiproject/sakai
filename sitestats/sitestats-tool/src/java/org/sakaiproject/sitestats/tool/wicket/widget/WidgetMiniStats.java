/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.sitestats.api.view.SiteStatsWidgetMetric;
import org.sakaiproject.sitestats.tool.facade.Locator;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
final class WidgetMiniStats {

	static List<WidgetMiniStat> forWidget(String siteId, String widgetId) {
		try {
			List<WidgetMiniStat> miniStats = new ArrayList<WidgetMiniStat>();
			for (SiteStatsWidgetMetric metric : Locator.getFacade().getSiteStatsViewService().getWidgetMetrics(siteId, widgetId)) {
				miniStats.add(new WidgetMetricMiniStat(metric));
			}
			return miniStats;
		} catch (RuntimeException e) {
			log.warn("Unable to load SiteStats widget metrics for {}/{}: {}", siteId, widgetId, e.getMessage());
			return Collections.emptyList();
		}
	}
}
