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

import org.sakaiproject.sitestats.api.view.SiteStatsReportRequest;
import org.sakaiproject.sitestats.api.view.SiteStatsReportView;
import org.sakaiproject.sitestats.api.view.SiteStatsTable;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.AUDIENCE_ALL;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_DATE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_ITEM_TYPE;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.FILTER_THRESHOLD;

public class ViewFactoryFixtureWidgetDefinition extends AbstractSiteStatsWidgetDefinition {

	public static final String WIDGET_ID = "view-factory-fixture";
	public static final String TAB_ID = "byitem";
	public static final String METRIC_ID = "view-factory-metric";

	@Override
	public WidgetSpec getSpec() {
		return widgetSpec(WIDGET_ID, "view_factory_fixture", "sakai-sitestats", AUDIENCE_ALL, () -> true,
				tabs(viewTabSpec(WIDGET_ID, TAB_ID, "overview_tab_bydate", this::buildView,
						FILTER_DATE, FILTER_ITEM_TYPE, FILTER_THRESHOLD)),
				metrics(viewMetricSpec(WIDGET_ID, METRIC_ID, "overview_title_events_sum", AUDIENCE_ALL,
						this::buildView, (siteId, userId) -> WidgetMetricValue.of("7"))));
	}

	private SiteStatsReportView buildView(String siteId, SiteStatsReportRequest request, String userId) {
		SiteStatsReportView view = new SiteStatsReportView();
		view.setTitle("View factory fixture");
		view.setPresentationMode("how-presentation-table");
		SiteStatsTable table = new SiteStatsTable();
		table.setCaption(filterCatalog().itemTypeFilter(request) + "|" + filterCatalog().thresholdFilter(request)
				+ "|" + request.getWhenFrom() + "|" + request.getWhenTo());
		table.setPage(request.getPage());
		table.setPageSize(request.getPageSize());
		table.setTotalRows(1);
		view.setTable(table);
		return view;
	}
}
