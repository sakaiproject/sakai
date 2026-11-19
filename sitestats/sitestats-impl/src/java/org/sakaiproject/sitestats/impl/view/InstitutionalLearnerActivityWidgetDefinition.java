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
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_INST_LEARNER_EVENT_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_LEARNER_EVENTS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_LEARNER_SITES;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_INST_LEARNER_TOP_EVENT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_EVENT;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY;

import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class InstitutionalLearnerActivityWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY, "overview_title_institutional_learner_activity",
				"sakai-sitestats", AUDIENCE_ADMIN, () -> true,
				tabs(viewTabSpec(WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY, TAB_BY_EVENT, "overview_tab_byevent",
						this::byEvent, FILTER_DATE, FILTER_SITE_TYPE, FILTER_SITE_PREFIX, FILTER_PUBLISHED_ONLY)),
				metrics(
						metricSpec(WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY, METRIC_INST_LEARNER_EVENTS,
								"overview_title_inst_learner_events", AUDIENCE_ADMIN, null, this::events)
								.withHelp("overview_help_inst_learner_events"),
						metricSpec(WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY, METRIC_INST_LEARNER_SITES,
								"overview_title_inst_learner_sites", AUDIENCE_ADMIN, null, this::sites)
								.withHelp("overview_help_inst_learner_sites"),
						metricSpec(WIDGET_INSTITUTIONAL_LEARNER_ACTIVITY, METRIC_INST_LEARNER_TOP_EVENT,
								"overview_title_inst_learner_top_event", AUDIENCE_ADMIN, null, this::topEvent)
								.withHelp("overview_help_inst_learner_top_event")),
				highlights(highlightSpec(HIGHLIGHT_INST_LEARNER_EVENT_SHARE,
						"overview_title_inst_learner_event_share", this::eventShare)));
	}

	private SiteStatsReportView byEvent(String siteId, SiteStatsReportRequest request, String userId) {
		return institutionalAnalytics().learnerByEventReport(siteId, request, userId);
	}

	private WidgetMetricValue events(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().learnerEventsValue(siteId, userId, request);
	}

	private WidgetMetricValue sites(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().learnerSitesValue(siteId, userId, request);
	}

	private WidgetMetricValue topEvent(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().learnerTopEventValue(siteId, userId, request);
	}

	private SiteStatsChart eventShare(String siteId, String userId, SiteStatsReportRequest request) {
		return institutionalAnalytics().learnerEventShareHighlight(siteId, userId, request);
	}

	private SiteStatsInstitutionalAnalytics institutionalAnalytics() {
		return support.getInstitutionalAnalytics();
	}
}
