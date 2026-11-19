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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_PUBLISHED_ONLY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_SITE_PREFIX;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_SITE_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_INST_PRESENCE_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_ACTIVE_30D;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_ACTIVE_SITES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_LOW_ACTIVITY;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_PRESENCE_NEVER_VISITED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_SITE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_INSTITUTIONAL_PRESENCE;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class InstitutionalPresenceWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_INSTITUTIONAL_PRESENCE, "overview_title_institutional_presence", "sakai-sitestats",
				AUDIENCE_ADMIN, () -> true,
				tabs(
						viewTabSpec(WIDGET_INSTITUTIONAL_PRESENCE, TAB_BY_SITE, "overview_tab_bysite", this::bySite,
								FILTER_DATE, FILTER_SITE_TYPE, FILTER_SITE_PREFIX, FILTER_PUBLISHED_ONLY)),
				metrics(
						metricSpec(WIDGET_INSTITUTIONAL_PRESENCE, METRIC_INST_PRESENCE_ACTIVE_SITES,
								"overview_title_inst_presence_active", AUDIENCE_ADMIN, null, this::activeSites)
								.withHelp("overview_help_inst_presence_active"),
						metricSpec(WIDGET_INSTITUTIONAL_PRESENCE, METRIC_INST_PRESENCE_ACTIVE_30D,
								"overview_title_inst_presence_active_30d", AUDIENCE_ADMIN, null, this::active30d)
								.withHelp("overview_help_inst_presence_active_30d"),
						metricSpec(WIDGET_INSTITUTIONAL_PRESENCE, METRIC_INST_PRESENCE_LOW_ACTIVITY,
								"overview_title_inst_presence_low_activity", AUDIENCE_ADMIN, null, this::lowActivity)
								.withHelp("overview_help_inst_presence_low_activity"),
						metricSpec(WIDGET_INSTITUTIONAL_PRESENCE, METRIC_INST_PRESENCE_NEVER_VISITED,
								"overview_title_inst_presence_never_visited", AUDIENCE_ADMIN, null, this::neverVisited)
								.withHelp("overview_help_inst_presence_never_visited")),
				highlights(highlightSpec(HIGHLIGHT_INST_PRESENCE_SHARE, "overview_title_inst_presence_share",
						this::presenceShare)));
	}

	private SiteStatsChart presenceShare(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().presenceShareHighlight(siteId, userId, request);
	}

	private SiteStatsReportView bySite(String siteId, SiteStatsReportRequest request, String userId) {
		return institutionalAnalytics().presenceBySiteReport(siteId, request, userId);
	}

	private WidgetMetricValue activeSites(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().presenceActiveSitesValue(siteId, userId, request);
	}

	private WidgetMetricValue active30d(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().presenceActive30dValue(siteId, userId, request);
	}

	private WidgetMetricValue lowActivity(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().presenceLowActivityValue(siteId, userId, request);
	}

	private WidgetMetricValue neverVisited(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().presenceNeverVisitedValue(siteId, userId, request);
	}

	private SiteStatsInstitutionalAnalytics institutionalAnalytics() {
		return support.getInstitutionalAnalytics();
	}
}
