/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Setter;

import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;

public class SiteStatsToolEventsServiceImpl implements SiteStatsToolEventsService {

	@Setter private StatsManager statsManager;
	@Setter private EventRegistryService eventRegistryService;

	@Override
	public List<String> getToolIds(String siteId, PrefsData prefsData) {
		List<String> toolIds = new ArrayList<String>();
		for (ToolInfo toolInfo : prefsData.getToolEventsDef()) {
			if (toolInfo.isSelected() && isToolSupported(siteId, toolInfo, prefsData)) {
				toolIds.add(toolInfo.getToolId());
			}
		}
		return toolIds;
	}

	@Override
	public List<String> getEventsForToolFilter(String toolFilter, String siteId, PrefsData prefsData, boolean isForUserTracking) {
		if (ReportManager.WHAT_EVENTS_ALLTOOLS.equals(toolFilter) || ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ.equals(toolFilter)) {
			List<String> eventIds = new ArrayList<String>(prefsData.getToolEventsStringList());
			if (isForUserTracking) {
				eventIds.add(StatsManager.SITEVISIT_EVENTID);
			}
			if (ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ.equals(toolFilter)) {
				eventIds.remove("content.read");
			}
			return eventIds;
		} else if (isForUserTracking && StatsManager.PRESENCE_TOOLID.equals(toolFilter)) {
			return Arrays.asList(StatsManager.SITEVISIT_EVENTID);
		}

		List<String> eventIds = new ArrayList<String>();
		for (ToolInfo toolInfo : prefsData.getToolEventsDef()) {
			if (toolInfo.isSelected() && toolInfo.getToolId().equals(toolFilter) && isToolSupported(siteId, toolInfo, prefsData)) {
				for (EventInfo eventInfo : toolInfo.getEvents()) {
					if (eventInfo.isSelected()) {
						eventIds.add(eventInfo.getEventId());
					}
				}
			}
		}
		return eventIds;
	}

	@Override
	public boolean isToolSupported(String siteId, ToolInfo toolInfo, PrefsData prefsData) {
		if (statsManager.isEventContextSupported()) {
			return true;
		}
		List<ToolInfo> siteTools = eventRegistryService.getEventRegistry(siteId, prefsData.isListToolEventsOnlyAvailableInSite());
		for (ToolInfo siteTool : siteTools) {
			if (siteTool.getToolId().equals(toolInfo.getToolId())) {
				return siteTool.getEventParserTips().stream().anyMatch(tip -> StatsManager.PARSERTIP_FOR_CONTEXTID.equals(tip.getFor()));
			}
		}
		return false;
	}
}
