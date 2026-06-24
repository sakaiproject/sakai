/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.SiteStatsToolEventsService;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

@Getter
public class SiteStatsWidgetContext {

	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private SiteStatsToolEventsService siteStatsToolEventsService;
	@Setter private EventRegistryService eventRegistryService;
	@Setter private SiteService siteService;
	@Setter private ContentHostingService contentHostingService;
	@Setter private UserDirectoryService userDirectoryService;

	@Setter private ResourceLoader messages = new ResourceLoader("Messages");

	public String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	public String message(String key, String defaultValue) {
		String value = message(key);
		return key.equals(value) || StringUtils.startsWith(value, "[missing key") ? defaultValue : value;
	}
}
