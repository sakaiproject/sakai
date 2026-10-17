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

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_OWN;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.HIGHLIGHT_COMMUNICATION_SHARE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_COMMUNICATION_AUTHORED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.METRIC_STUDENT_COMMUNICATION_REPLIED;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.TAB_BY_TOOL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_COMMUNICATION;

import org.sakaiproject.commons.api.CommonsConstants;
import org.sakaiproject.conversations.api.ConversationsService;
import org.sakaiproject.sitestats.api.view.SiteStatsChart;
import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;

public class StudentCommunicationWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_STUDENT_COMMUNICATION, "overview_title_communication", "sakai-sitestats",
				AUDIENCE_OWN, () -> true,
				tabs(viewTabSpec(WIDGET_STUDENT_COMMUNICATION, TAB_BY_TOOL, "overview_tab_bytool", this::byTool,
						FILTER_DATE)),
				metrics(
						viewMetricSpec(WIDGET_STUDENT_COMMUNICATION, METRIC_STUDENT_COMMUNICATION_AUTHORED,
								"overview_title_communication_authored_own", AUDIENCE_OWN, this::byTool,
								this::authoredValue)
								.withHelp("overview_help_communication_authored_own"),
						viewMetricSpec(WIDGET_STUDENT_COMMUNICATION, METRIC_STUDENT_COMMUNICATION_REPLIED,
								"overview_title_communication_replied_own", AUDIENCE_OWN, this::byTool,
								this::repliedValue)
								.withHelp("overview_help_communication_replied_own")),
				highlights(highlightSpec(HIGHLIGHT_COMMUNICATION_SHARE, "overview_title_communication_share_own",
						this::shareChart)),
				toolFilterIds(SiteStatsCommunicationAnalytics.FORUMS_TOOL_ID,
						SiteStatsCommunicationAnalytics.MESSAGES_TOOL_ID, ConversationsService.TOOL_ID,
						CommonsConstants.TOOL_ID));
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

	private SiteStatsChart shareChart(String siteId, String userId, SiteStatsReportRequest request) {
		return communicationAnalytics().shareChart(siteId, userId, request);
	}
}
