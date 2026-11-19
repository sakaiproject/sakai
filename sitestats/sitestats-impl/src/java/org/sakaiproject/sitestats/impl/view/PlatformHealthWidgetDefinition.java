/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ADMIN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_PLATFORM_LOGIN_TREND;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_LOGINS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_NEW_USERS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_SITES_CREATED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_SITES_DELETED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_TOP_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_PLATFORM_UNIQUE_LOGINS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_PLATFORM_HEALTH;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class PlatformHealthWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_PLATFORM_HEALTH, "overview_title_platform_health", "sakai-sitestats", AUDIENCE_ADMIN,
				() -> true,
				tabs(
						viewTabSpec(WIDGET_PLATFORM_HEALTH, TAB_BY_DATE, "overview_tab_bydate", this::loginsByDateReport),
						viewTabSpec(WIDGET_PLATFORM_HEALTH, TAB_BY_TOOL, "overview_tab_bytool", this::toolInstallsReport)),
				metrics(
						metricSpec(WIDGET_PLATFORM_HEALTH, METRIC_PLATFORM_LOGINS, "overview_title_platform_logins",
								AUDIENCE_ADMIN, null, this::loginsValue)
								.withHelp("overview_help_platform_logins"),
						metricSpec(WIDGET_PLATFORM_HEALTH, METRIC_PLATFORM_UNIQUE_LOGINS, "overview_title_platform_unique_logins",
								AUDIENCE_ADMIN, null, this::uniqueLoginsValue)
								.withHelp("overview_help_platform_unique_logins"),
						metricSpec(WIDGET_PLATFORM_HEALTH, METRIC_PLATFORM_SITES_CREATED, "overview_title_platform_sites_created",
								AUDIENCE_ADMIN, null, this::sitesCreatedValue)
								.withHelp("overview_help_platform_sites_created"),
						metricSpec(WIDGET_PLATFORM_HEALTH, METRIC_PLATFORM_SITES_DELETED, "overview_title_platform_sites_deleted",
								AUDIENCE_ADMIN, null, this::sitesDeletedValue)
								.withHelp("overview_help_platform_sites_deleted"),
						metricSpec(WIDGET_PLATFORM_HEALTH, METRIC_PLATFORM_NEW_USERS, "overview_title_platform_new_users",
								AUDIENCE_ADMIN, null, this::newUsersValue)
								.withHelp("overview_help_platform_new_users"),
						metricSpec(WIDGET_PLATFORM_HEALTH, METRIC_PLATFORM_TOP_TOOL, "overview_title_platform_top_tool",
								AUDIENCE_ADMIN, null, this::topToolValue)
								.withHelp("overview_help_platform_top_tool")),
				highlights(highlightSpec(HIGHLIGHT_PLATFORM_LOGIN_TREND, "overview_title_platform_login_trend",
						this::loginTrend)));
	}

	private SiteStatsReportView loginsByDateReport(String siteId, SiteStatsReportRequest request, String userId) {
		return platformHealthAnalytics().loginsByDateReport(siteId, request, userId);
	}

	private SiteStatsReportView toolInstallsReport(String siteId, SiteStatsReportRequest request, String userId) {
		return platformHealthAnalytics().toolInstallsReport(siteId, request, userId);
	}

	private WidgetMetricValue loginsValue(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().loginsValue(siteId, userId, request);
	}

	private WidgetMetricValue uniqueLoginsValue(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().uniqueLoginsValue(siteId, userId, request);
	}

	private WidgetMetricValue sitesCreatedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().sitesCreatedValue(siteId, userId, request);
	}

	private WidgetMetricValue sitesDeletedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().sitesDeletedValue(siteId, userId, request);
	}

	private WidgetMetricValue newUsersValue(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().newUsersValue(siteId, userId, request);
	}

	private WidgetMetricValue topToolValue(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().topToolValue(siteId, userId, request);
	}

	private SiteStatsChart loginTrend(String siteId, String userId, SiteStatsReportRequest request) {
		return platformHealthAnalytics().loginTrendHighlight(siteId, userId, request);
	}

	private SiteStatsPlatformHealthAnalytics platformHealthAnalytics() {
		return support.getPlatformHealthAnalytics();
	}
}
