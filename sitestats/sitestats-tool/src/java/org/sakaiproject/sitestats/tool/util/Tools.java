/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.tool.facade.Locator;

/**
 * Utility class for working with tool info
 * @author plukasew, bjones86
 */
public class Tools
{
	/**
	 * Returns the tool ids for the Sakai tools supported by SiteStats, filtered by user preferences
	 * @param siteId the site id
	 * @param pd the user preferences for filtering the tool list
	 * @return list of filtered tools
	 */
	public static List<String> getToolIds(String siteId, PrefsData pd)
	{
		List<String> toolIds = new ArrayList<>();
		for (ToolInfo ti : pd.getToolEventsDef())
		{
			if (ti.isSelected() && isToolSupported(siteId, ti, pd))
			{
				toolIds.add(ti.getToolId());
			}
		}

		return toolIds;
	}

	/**
	 * Returns the events associated with the given tool, filtered by user preferences
	 * @param toolFilter the tool
	 * @param siteId the site id
	 * @param pd the user preferences
	 * @param isForUserTracking whether the listing will be used for User Activity
	 * @return the matching events
	 */
	public static List<String> getEventsForToolFilter(String toolFilter, String siteId, PrefsData pd, boolean isForUserTracking)
	{
		if (ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolFilter) || ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ.equals(toolFilter))
		{
			List<String> eventIDs = pd.getToolEventsStringList();
			if (isForUserTracking)
			{
				eventIDs.add(StatsManager.SITEVISIT_EVENTID);
			}
			if (ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ.equals(toolFilter))
			{
				eventIDs.remove("content.read");
			}
			return eventIDs;
		}
		else if (isForUserTracking && StatsManager.PRESENCE_TOOLID.equals(toolFilter))
		{
			return Arrays.asList(StatsManager.SITEVISIT_EVENTID);
		}

		List<String> eventIds = new ArrayList<>();
		for (ToolInfo ti : pd.getToolEventsDef())
		{
			if (ti.isSelected() && ti.getToolId().equals(toolFilter) && isToolSupported(siteId, ti, pd))
			{
				for (EventInfo ei : ti.getEvents())
				{
					if (ei.isSelected())
					{
						eventIds.add(ei.getEventId());
					}
				}
			}
		}

		return eventIds;
	}

	/**
	 * Returns true if the tool is supported by SiteStats, may be restricted to tools available in the given site based
	 * on user preferences
	 * @param siteId the site id
	 * @param toolInfo the tool
	 * @param pd user preferences
	 * @return true if the tool is supported
	 */
	public static boolean isToolSupported(String siteId, ToolInfo toolInfo, PrefsData pd)
	{
		if (Locator.getFacade().getStatsManager().isEventContextSupported())
		{
			return true;
		}
		else
		{
			List<ToolInfo> siteTools = Locator.getFacade().getEventRegistryService().getEventRegistry(siteId, pd.isListToolEventsOnlyAvailableInSite());
			for (ToolInfo t : siteTools)
			{
				if (t.getToolId().equals(toolInfo.getToolId()))
				{
					return t.getEventParserTips().stream().anyMatch(tip -> StatsManager.PARSERTIP_FOR_CONTEXTID.equals(tip.getFor()));
				}
			}
		}

		return false;
	}
}
