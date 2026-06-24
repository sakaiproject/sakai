/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsTestConfiguration.class})
public class SiteStatsToolEventsServiceTest {

	private static final String SITE_ID = "site-a";
	private static final String ASSIGNMENTS_TOOL = "sakai.assignment";
	private static final String ASSIGNMENT_SUBMIT = "assignment.submit";

	@Autowired private SiteStatsToolEventsService service;

	@Test
	public void getToolIdsReturnsSelectedSupportedTools() {
		PrefsData prefsData = prefsData(
				tool(ASSIGNMENTS_TOOL, true, event(ASSIGNMENT_SUBMIT, true)),
				tool("sakai.resources", false, event("content.read", true)));

		List<String> toolIds = service.getToolIds(SITE_ID, prefsData);

		assertEquals(Arrays.asList(ASSIGNMENTS_TOOL), toolIds);
	}

	@Test
	public void getEventsForAllToolsCanExcludeContentReadAndIncludePresenceForUserTracking() {
		PrefsData prefsData = prefsData(
				tool("sakai.resources", true, event("content.read", true), event("content.new", true)),
				tool(ASSIGNMENTS_TOOL, true, event(ASSIGNMENT_SUBMIT, true)));

		List<String> events = service.getEventsForToolFilter(ReportManager.WHAT_EVENTS_ALLTOOLS_EXCLUDE_CONTENT_READ, SITE_ID, prefsData, true);

		assertEquals(Arrays.asList("content.new", ASSIGNMENT_SUBMIT, StatsManager.SITEVISIT_EVENTID), events);
	}

	@Test
	public void getEventsForPresenceToolReturnsSiteVisitForUserTracking() {
		List<String> events = service.getEventsForToolFilter(StatsManager.PRESENCE_TOOLID, SITE_ID, new PrefsData(), true);

		assertEquals(Arrays.asList(StatsManager.SITEVISIT_EVENTID), events);
	}

	@Test
	public void getEventsForToolFilterUsesSelectedEventsFromSupportedTool() {
		PrefsData prefsData = prefsData(
				tool(ASSIGNMENTS_TOOL, true, event(ASSIGNMENT_SUBMIT, true), event("assignment.draft", false)),
				tool("sakai.resources", true, event("content.read", true)));

		List<String> events = service.getEventsForToolFilter(ASSIGNMENTS_TOOL, SITE_ID, prefsData, false);

		assertEquals(Arrays.asList(ASSIGNMENT_SUBMIT), events);
	}

	private PrefsData prefsData(ToolInfo... tools) {
		PrefsData prefsData = new PrefsData();
		prefsData.setToolEventsDef(Arrays.asList(tools));
		return prefsData;
	}

	private ToolInfo tool(String toolId, boolean selected, EventInfo... events) {
		ToolInfo toolInfo = new ToolInfo(toolId);
		toolInfo.setSelected(selected);
		toolInfo.setEvents(Arrays.asList(events));
		return toolInfo;
	}

	private EventInfo event(String eventId, boolean selected) {
		EventInfo eventInfo = new EventInfo(eventId);
		eventInfo.setSelected(selected);
		return eventInfo;
	}
}
