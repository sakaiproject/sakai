/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sakaiproject.sitestats.test.SiteStatsTestFixtures.event;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.SiteStatsToolEventsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SiteStatsToolEventsServiceLegacyContextTest.TestConfiguration.class})
public class SiteStatsToolEventsServiceLegacyContextTest {

	private static final String SITE_ID = "site-a";
	private static final String ASSIGNMENTS_TOOL = "sakai.assignment";
	private static final String ASSIGNMENT_SUBMIT = "assignment.submit";

	@Autowired private EventRegistryService eventRegistryService;
	@Autowired private SiteStatsToolEventsService service;

	@Test
	public void isToolSupportedRequiresContextParserWhenEventContextUnsupported() {
		PrefsData prefsData = new PrefsData();
		ToolInfo prefsTool = selectedTool(ASSIGNMENTS_TOOL);

		ToolInfo supportedSiteTool = new ToolInfo(ASSIGNMENTS_TOOL);
		supportedSiteTool.addEventParserTip(new EventParserTip(StatsManager.PARSERTIP_FOR_CONTEXTID, "/", "0"));
		when(eventRegistryService.getEventRegistry(eq(SITE_ID), eq(true))).thenReturn(Arrays.asList(supportedSiteTool));

		assertTrue(service.isToolSupported(SITE_ID, prefsTool, prefsData));

		ToolInfo unsupportedSiteTool = new ToolInfo(ASSIGNMENTS_TOOL);
		unsupportedSiteTool.addEventParserTip(new EventParserTip("userId", "/", "0"));
		when(eventRegistryService.getEventRegistry(eq(SITE_ID), eq(true))).thenReturn(Arrays.asList(unsupportedSiteTool));

		assertFalse(service.isToolSupported(SITE_ID, prefsTool, prefsData));
	}

	private ToolInfo selectedTool(String toolId) {
		ToolInfo toolInfo = new ToolInfo(toolId);
		toolInfo.setSelected(true);
		toolInfo.setEvents(Arrays.asList(event(ASSIGNMENT_SUBMIT, true)));
		return toolInfo;
	}

	@Configuration
	public static class TestConfiguration {

		@Bean
		public SiteStatsToolEventsService siteStatsToolEventsService(StatsManager statsManager, EventRegistryService eventRegistryService) {
			SiteStatsToolEventsServiceImpl service = new SiteStatsToolEventsServiceImpl();
			service.setStatsManager(statsManager);
			service.setEventRegistryService(eventRegistryService);
			return service;
		}

		@Bean
		public StatsManager statsManager() {
			StatsManager statsManager = mock(StatsManager.class);
			when(statsManager.isEventContextSupported()).thenReturn(false);
			return statsManager;
		}

		@Bean
		public EventRegistryService eventRegistryService() {
			return mock(EventRegistryService.class);
		}
	}
}
