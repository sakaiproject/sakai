/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_STUDENT_VISITS;
import static org.sakaiproject.sitestats.api.view.SiteStatsWidgetIds.WIDGET_VISITS;

import java.util.ArrayList;

import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.sitestats.api.StatsAuthz;
import org.sakaiproject.sitestats.tool.facade.Locator;

public class VisitsWidget extends Panel {
	private static final long serialVersionUID = 1L;

	public VisitsWidget(String id, final String siteId, final String currentUserId) {
		super(id);
		setRenderBodyOnly(true);

		StatsAuthz statsAuthz = Locator.getFacade().getStatsAuthz();
		boolean siteStatsAll = statsAuthz.isUserAbleToViewSiteStatsAll(siteId);
		boolean siteStatsOwn = statsAuthz.isUserAbleToViewSiteStatsOwn(siteId);

		if (siteStatsAll) {
			add(new CatalogWidgetPanel("widget", siteId, WIDGET_VISITS));
		} else if (siteStatsOwn) {
			add(new StudentVisitsWidget("widget", WidgetMiniStats.forWidget(siteId, WIDGET_STUDENT_VISITS)));
		} else {
			add(new StudentVisitsWidget("widget", new ArrayList<WidgetMiniStat>()));
		}
	}
}
