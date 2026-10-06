/**********************************************************************************
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.sitestats.impl.view;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_GROUP;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ROLE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_COMMUNICATION_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_AUTHORED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_MOST_ACTIVE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_REPLIED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_COMMUNICATION_UNANSWERED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_USER;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_COMMUNICATION;

import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class CommunicationWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_COMMUNICATION, "overview_title_communication", "sakai-sitestats", AUDIENCE_ALL,
				() -> true,
				tabs(
						viewTabSpec(WIDGET_COMMUNICATION, TAB_BY_USER, "overview_tab_byuser", this::byUser,
								FILTER_DATE, FILTER_ROLE, FILTER_GROUP),
						viewTabSpec(WIDGET_COMMUNICATION, TAB_BY_TOOL, "overview_tab_bytool", this::byTool,
								FILTER_DATE, FILTER_ROLE, FILTER_GROUP)),
				metrics(
						viewMetricSpec(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_AUTHORED,
								"overview_title_communication_authored", AUDIENCE_ALL, this::byUser, this::authoredValue)
								.withHelp("overview_help_communication_authored"),
						viewMetricSpec(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_REPLIED,
								"overview_title_communication_replied", AUDIENCE_ALL, this::byUser, this::repliedValue)
								.withHelp("overview_help_communication_replied"),
						viewMetricSpec(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_UNANSWERED,
								"overview_title_communication_unanswered", AUDIENCE_ALL, this::byTool,
								this::unansweredValue)
								.withHelp("overview_help_communication_unanswered"),
						viewMetricSpec(WIDGET_COMMUNICATION, METRIC_COMMUNICATION_MOST_ACTIVE,
								"overview_title_communication_most_active", AUDIENCE_ALL, this::byUser,
								this::mostActiveValue)
								.withHelp("overview_help_communication_most_active")),
				highlights(highlightSpec(HIGHLIGHT_COMMUNICATION_SHARE, "overview_title_communication_share",
						this::shareChart)),
				toolFilterIds(SiteStatsCommunicationAnalytics.FORUMS_TOOL_ID,
						SiteStatsCommunicationAnalytics.MESSAGES_TOOL_ID, ConversationsService.TOOL_ID,
						CommonsConstants.TOOL_ID));
	}

	private SiteStatsReportView byUser(String siteId, SiteStatsReportRequest request, String userId) {
		return communicationAnalytics().byUserReport(siteId, request, userId);
	}

	private SiteStatsReportView byTool(String siteId, SiteStatsReportRequest request, String userId) {
		return communicationAnalytics().byToolReport(siteId, request, userId);
	}

	private WidgetMetricValue authoredValue(String siteId, String userId, SiteStatsReportRequest request) {
		return communicationAnalytics().authoredValue(siteId, userId, request);
	}

	private WidgetMetricValue repliedValue(String siteId, String userId, SiteStatsReportRequest request) {
		return communicationAnalytics().repliedValue(siteId, userId, request);
	}

	private WidgetMetricValue unansweredValue(String siteId, String userId, SiteStatsReportRequest request) {
		return communicationAnalytics().unansweredValue(siteId, userId, request);
	}

	private WidgetMetricValue mostActiveValue(String siteId, String userId, SiteStatsReportRequest request) {
		return communicationAnalytics().mostActiveValue(siteId, userId, request);
	}

	private SiteStatsChart shareChart(String siteId, String userId, SiteStatsReportRequest request) {
		return communicationAnalytics().shareChart(siteId, null, request);
	}
}
