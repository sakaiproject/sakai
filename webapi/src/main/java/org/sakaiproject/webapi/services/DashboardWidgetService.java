/*
 * Copyright (c) 2026 The Apereo Foundation
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.webapi.services;

import java.util.List;

import org.sakaiproject.site.api.Site;
import org.springframework.stereotype.Service;

@Service
public class DashboardWidgetService {

    /** Filters course widgets by their source tools, preserving the configured order. */
    public List<String> getAvailableWidgets(Site site, List<String> widgets) {
        return widgets.stream().filter(widget -> {
            List<String> tools = sourceTools(widget);
            return tools.isEmpty() || tools.stream().anyMatch(tool -> site.getToolForCommonId(tool) != null);
        }).toList();
    }

    private List<String> sourceTools(String widget) {
        return switch (widget) {
            case "announcements" -> List.of("sakai.announcements");
            case "calendar" -> List.of("sakai.schedule");
            case "forums" -> List.of("sakai.forums");
            case "grades", "sitestats-grades", "sitestats-student-grades" -> List.of("sakai.gradebookng");
            case "sitestats-submissions", "sitestats-student-submissions" -> List.of("sakai.assignment.grades", "sakai.samigo");
            case "sitestats-communication", "sitestats-student-communication" ->
                    List.of("sakai.forums", "sakai.messages", "sakai.conversations", "sakai.commons");
            case "sitestats-resources" -> List.of("sakai.resources");
            case "sitestats-lessons" -> List.of("sakai.lessonbuildertool");
            default -> List.of(); // Site-wide activity and tasks do not require a particular tool.
        };
    }
}
