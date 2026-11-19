/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.webapi.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = DashboardWidgetService.class)
public class DashboardWidgetServiceTests {

    @Autowired private DashboardWidgetService service;

    @Test
    public void siteWithoutToolsOnlyOffersSiteWideWidgets() {
        Site site = mock(Site.class);
        assertEquals(List.of("sitestats-visits", "sitestats-presence-access", "sitestats-activity", "tasks"),
                service.getAvailableWidgets(site, List.of("announcements", "calendar", "forums", "grades",
                        "sitestats-grades", "sitestats-student-grades", "sitestats-submissions",
                        "sitestats-student-submissions", "sitestats-communication", "sitestats-student-communication",
                        "sitestats-resources", "sitestats-lessons", "sitestats-visits",
                        "sitestats-presence-access", "sitestats-activity", "tasks")));
    }

    @Test
    public void eachSourceToolEnablesItsWidgets() {
        List<List<String>> cases = List.of(
                List.of("sakai.announcements", "announcements"),
                List.of("sakai.schedule", "calendar"),
                List.of("sakai.forums", "forums", "sitestats-communication", "sitestats-student-communication"),
                List.of("sakai.messages", "sitestats-communication", "sitestats-student-communication"),
                List.of("sakai.conversations", "sitestats-communication", "sitestats-student-communication"),
                List.of("sakai.commons", "sitestats-communication", "sitestats-student-communication"),
                List.of("sakai.gradebookng", "grades", "sitestats-grades", "sitestats-student-grades"),
                List.of("sakai.assignment.grades", "sitestats-submissions", "sitestats-student-submissions"),
                List.of("sakai.samigo", "sitestats-submissions", "sitestats-student-submissions"),
                List.of("sakai.resources", "sitestats-resources"),
                List.of("sakai.lessonbuildertool", "sitestats-lessons"));
        for (List<String> entry : cases) {
            Site site = mock(Site.class);
            when(site.getToolForCommonId(entry.get(0))).thenReturn(mock(ToolConfiguration.class));
            List<String> widgets = entry.subList(1, entry.size());
            assertEquals(entry.get(0), widgets, service.getAvailableWidgets(site, widgets));
        }
    }
}
