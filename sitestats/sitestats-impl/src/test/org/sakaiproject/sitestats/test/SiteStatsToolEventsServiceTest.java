/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.impl.event.SiteStatsToolEventsServiceImpl;

public class SiteStatsToolEventsServiceTest {

	private static final String SITE_ID = "site-a";
	private static final String ASSIGNMENTS_TOOL = "sakai.assignment";
	private static final String ASSIGNMENT_SUBMIT = "assignment.submit";

	private StatsManager statsManager;
	private EventRegistryService eventRegistryService;
	private SiteStatsToolEventsServiceImpl service;

	@Before
	public void setUp() {
		statsManager = mock(StatsManager.class);
		eventRegistryService = mock(EventRegistryService.class);
		service = new SiteStatsToolEventsServiceImpl();
		service.setStatsManager(statsManager);
		service.setEventRegistryService(eventRegistryService);
	}

	@Test
	public void getToolIdsReturnsSelectedSupportedTools() {
		when(statsManager.isEventContextSupported()).thenReturn(true);
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
		when(statsManager.isEventContextSupported()).thenReturn(true);
		PrefsData prefsData = prefsData(
				tool(ASSIGNMENTS_TOOL, true, event(ASSIGNMENT_SUBMIT, true), event("assignment.draft", false)),
				tool("sakai.resources", true, event("content.read", true)));

		List<String> events = service.getEventsForToolFilter(ASSIGNMENTS_TOOL, SITE_ID, prefsData, false);

		assertEquals(Arrays.asList(ASSIGNMENT_SUBMIT), events);
	}

	@Test
	public void isToolSupportedRequiresContextParserWhenEventContextUnsupported() {
		PrefsData prefsData = new PrefsData();
		ToolInfo prefsTool = tool(ASSIGNMENTS_TOOL, true, event(ASSIGNMENT_SUBMIT, true));
		ToolInfo supportedSiteTool = new ToolInfo(ASSIGNMENTS_TOOL);
		supportedSiteTool.addEventParserTip(new EventParserTip(StatsManager.PARSERTIP_FOR_CONTEXTID, "/", "0"));
		when(eventRegistryService.getEventRegistry(eq(SITE_ID), eq(true))).thenReturn(Arrays.asList(supportedSiteTool));

		assertTrue(service.isToolSupported(SITE_ID, prefsTool, prefsData));

		ToolInfo unsupportedSiteTool = new ToolInfo(ASSIGNMENTS_TOOL);
		unsupportedSiteTool.addEventParserTip(new EventParserTip("userId", "/", "0"));
		when(eventRegistryService.getEventRegistry(eq(SITE_ID), eq(true))).thenReturn(Arrays.asList(unsupportedSiteTool));

		assertFalse(service.isToolSupported(SITE_ID, prefsTool, prefsData));
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
