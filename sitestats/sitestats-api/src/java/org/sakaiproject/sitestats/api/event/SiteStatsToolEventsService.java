/**
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.api.event;

import java.util.List;

import org.sakaiproject.sitestats.api.PrefsData;

/**
 * Centralizes SiteStats tool/event filtering rules used by reports and UI filters.
 */
public interface SiteStatsToolEventsService {

	List<String> getToolIds(String siteId, PrefsData prefsData);

	List<String> getEventsForToolFilter(String toolFilter, String siteId, PrefsData prefsData, boolean isForUserTracking);

	boolean isToolSupported(String siteId, ToolInfo toolInfo, PrefsData prefsData);
}
