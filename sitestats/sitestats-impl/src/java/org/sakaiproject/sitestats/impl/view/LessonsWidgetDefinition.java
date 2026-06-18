/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_MOST_READ_PAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_READ_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_LESSONS_USER_READ_MORE_PAGES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_PAGE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_LESSON_ACTION;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_LESSONS;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;

public class LessonsWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_LESSONS, "overview_title_lessonpages", "sakai-lessonbuildertool", AUDIENCE_ALL,
				() -> statsManager().isEnableLessonsStats(),
				tabs(
						tabSpec(WIDGET_LESSONS, TAB_BY_DATE, "overview_tab_bydate", this::lessonsByDateDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_LESSON_ACTION),
						tabSpec(WIDGET_LESSONS, TAB_BY_USER, "overview_tab_byuser", this::lessonsByUserDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_LESSON_ACTION),
						tabSpec(WIDGET_LESSONS, TAB_BY_PAGE, "overview_tab_bypage", this::lessonsByPageDefinition,
								FILTER_DATE, FILTER_ROLE,
								FILTER_LESSON_ACTION)),
				metrics(
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_PAGES, "overview_title_pages_sum", AUDIENCE_ALL,
								null, this::lessonsPagesValue),
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_READ_PAGES, "overview_title_readpages_sum", AUDIENCE_ALL,
								null, this::lessonsReadPagesValue),
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_MOST_READ_PAGE, "overview_title_mostreadpage_sum", AUDIENCE_ALL,
								null, this::lessonsMostReadPageValue),
						metricSpec(WIDGET_LESSONS, METRIC_LESSONS_USER_READ_MORE_PAGES, "overview_title_userreadmorepage_sum", AUDIENCE_ALL,
								null, this::lessonsUserReadMorePagesValue)));
	}

	private WidgetReportDefinition lessonsByDateDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return reportFactory().resourceLikeByDateDefinition(siteId, request, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", filterCatalog().lessonActionFilter(request));
	}

	private WidgetReportDefinition lessonsByUserDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return reportFactory().resourceLikeByUserDefinition(siteId, request, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", filterCatalog().lessonActionFilter(request), StatsManager.T_PAGE);
	}

	private WidgetReportDefinition lessonsByPageDefinition(String siteId, SiteStatsReportRequest request, String userId) {
		return reportFactory().resourceLikeByItemDefinition(siteId, request, message("overview_title_lessonpages"),
				ReportManager.WHAT_LESSONPAGES, "/page/", filterCatalog().lessonActionFilter(request), StatsManager.T_PAGE);
	}

	private WidgetMetricValue lessonsPagesValue(String siteId, String userId) {
		return WidgetMetricValue.of(Integer.toString(statsManager().getTotalLessonPages(siteId)));
	}

	private WidgetMetricValue lessonsReadPagesValue(String siteId, String userId) {
		int readPages = statsManager().getTotalReadLessonPages(siteId);
		int totalPages = statsManager().getTotalLessonPages(siteId);
		return WidgetMetricValue.withPercentage(Integer.toString(readPages), (int) metricSupport().percent(readPages, totalPages));
	}

	private WidgetMetricValue lessonsMostReadPageValue(String siteId, String userId) {
		String mostReadPage = statsManager().getMostReadLessonPage(siteId);
		return WidgetMetricValue.withDetail(mostReadPage == null ? "-" : mostReadPage, mostReadPage);
	}

	private WidgetMetricValue lessonsUserReadMorePagesValue(String siteId, String userId) {
		String activeUserId = statsManager().getMostActiveLessonPageReader(siteId);
		String displayId = activeUserId == null ? "-" : metricSupport().userDisplayId(activeUserId);
		return WidgetMetricValue.withDetail(displayId, metricSupport().userTooltip(activeUserId));
	}
}
