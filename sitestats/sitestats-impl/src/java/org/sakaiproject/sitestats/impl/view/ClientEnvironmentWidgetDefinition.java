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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_CLIENT_DEVICE_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_DESKTOP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_MOBILE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_TABLET;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_TOP_BROWSER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_CLIENT_TOP_OS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_BROWSER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_DEVICE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_CLIENT_ENVIRONMENT;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class ClientEnvironmentWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_CLIENT_ENVIRONMENT, "overview_title_client_environment", "sakai-sitestats",
				AUDIENCE_ADMIN, () -> clientEnvironmentAnalytics().isEnabled(),
				tabs(
						viewTabSpec(WIDGET_CLIENT_ENVIRONMENT, TAB_BY_DEVICE, "overview_tab_bydevice", this::byDevice,
								FILTER_DATE),
						viewTabSpec(WIDGET_CLIENT_ENVIRONMENT, TAB_BY_BROWSER, "overview_tab_bybrowser", this::byBrowser,
								FILTER_DATE)),
				metrics(
						metricSpec(WIDGET_CLIENT_ENVIRONMENT, METRIC_CLIENT_DESKTOP, "overview_title_client_desktop",
								AUDIENCE_ADMIN, null, this::desktop)
								.withHelp("overview_help_client_desktop"),
						metricSpec(WIDGET_CLIENT_ENVIRONMENT, METRIC_CLIENT_MOBILE, "overview_title_client_mobile",
								AUDIENCE_ADMIN, null, this::mobile)
								.withHelp("overview_help_client_mobile"),
						metricSpec(WIDGET_CLIENT_ENVIRONMENT, METRIC_CLIENT_TABLET, "overview_title_client_tablet",
								AUDIENCE_ADMIN, null, this::tablet)
								.withHelp("overview_help_client_tablet"),
						metricSpec(WIDGET_CLIENT_ENVIRONMENT, METRIC_CLIENT_TOP_BROWSER, "overview_title_client_top_browser",
								AUDIENCE_ADMIN, null, this::topBrowser)
								.withHelp("overview_help_client_top_browser"),
						metricSpec(WIDGET_CLIENT_ENVIRONMENT, METRIC_CLIENT_TOP_OS, "overview_title_client_top_os",
								AUDIENCE_ADMIN, null, this::topOs)
								.withHelp("overview_help_client_top_os")),
				highlights(highlightSpec(HIGHLIGHT_CLIENT_DEVICE_SHARE, "overview_title_client_device_share",
						this::deviceShare)));
	}

	private SiteStatsReportView byDevice(String siteId, SiteStatsReportRequest request, String userId) {
		return clientEnvironmentAnalytics().byDeviceReport(siteId, request, userId);
	}

	private SiteStatsReportView byBrowser(String siteId, SiteStatsReportRequest request, String userId) {
		return clientEnvironmentAnalytics().byBrowserReport(siteId, request, userId);
	}

	private WidgetMetricValue desktop(String siteId, String userId, SiteStatsReportRequest request) {
		return clientEnvironmentAnalytics().desktopValue(siteId, userId, request);
	}

	private WidgetMetricValue mobile(String siteId, String userId, SiteStatsReportRequest request) {
		return clientEnvironmentAnalytics().mobileValue(siteId, userId, request);
	}

	private WidgetMetricValue tablet(String siteId, String userId, SiteStatsReportRequest request) {
		return clientEnvironmentAnalytics().tabletValue(siteId, userId, request);
	}

	private WidgetMetricValue topBrowser(String siteId, String userId, SiteStatsReportRequest request) {
		return clientEnvironmentAnalytics().topBrowserValue(siteId, userId, request);
	}

	private WidgetMetricValue topOs(String siteId, String userId, SiteStatsReportRequest request) {
		return clientEnvironmentAnalytics().topOsValue(siteId, userId, request);
	}

	private SiteStatsChart deviceShare(String siteId, String userId, SiteStatsReportRequest request) {
		return clientEnvironmentAnalytics().deviceShareHighlight(siteId, userId, request);
	}

	private SiteStatsClientEnvironmentAnalytics clientEnvironmentAnalytics() {
		return support.getClientEnvironmentAnalytics();
	}
}
