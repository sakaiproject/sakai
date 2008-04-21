/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.javax.PagingPosition;


public interface StatsManager {
	public final static String	TOOL_EVENTS_DEF_FILE		= "toolEventsDef.xml";
	public final static int		PREFS_OVERVIEW_PAGE			= 0;
	public final static int		PREFS_EVENTS_PAGE			= 1;
	public final static int		PREFS_RESOURCES_PAGE		= 2;
	public final static String	SEPARATOR					= "/";
	public final static String	SITEVISIT_EVENTID			= "pres.begin";
	public final static String	SITESTATS_TOOLID			= "sakai.sitestats";
	public final static String	RESOURCES_TOOLID			= "sakai.resources";
	public final static String	PARSERTIP_FOR_CONTEXTID		= "contextId";
	public static final String	VIEW_WEEK					= "week";
	public static final String	VIEW_MONTH					= "month";
	public static final String	VIEW_YEAR					= "year";
	public static final String	CHATTYPE_BAR				= "bar";
	public static final String	CHATTYPE_PIE				= "pie";
	public static final String	WHO_CUSTOM					= "who-custom";
	public static final String	WHO_ROLE					= "who-role";
	public static final String	WHO_GROUPS					= "who-groups";
	public static final String	WHO_ALL						= "who-all";
	public static final String	WHO_NONE					= "who-none";
	public static final String	WHEN_CUSTOM					= "when-custom";
	public static final String	WHEN_LAST30DAYS				= "when-last30days";
	public static final String	WHEN_LAST7DAYS				= "when-last7days";
	public static final String	WHEN_ALL					= "when-all";
	public static final String	WHAT_RESOURCES				= "what-resources";
	public static final String	WHAT_RESOURCES_ACTION_NEW	= "new";
	public static final String	WHAT_RESOURCES_ACTION_READ	= "read";
	public static final String	WHAT_RESOURCES_ACTION_REVS	= "revise";
	public static final String	WHAT_RESOURCES_ACTION_DEL	= "delete";
	public static final String	WHAT_EVENTS_BYEVENTS		= "what-events-byevent";
	public static final String	WHAT_EVENTS_BYTOOL			= "what-events-bytool";
	public static final String	WHAT_EVENTS					= "what-events";
	public static final String	WHAT_VISITS					= "what-visits";

	
	// ################################################################
	// Spring bean methods
	// ################################################################
	public String getToolEventsDefinitionFile();
	
	public String getToolEventsAddDefinitionFile();
	
	public String getToolEventsRemoveDefinitionFile();
		
	public boolean isEnableSiteVisits();
	
	public boolean isEnableSiteActivity();
	
	public String getChartBackgroundColor();
	
	public boolean isChartIn3D();
	
	public float getChartTransparency();
	
	public boolean isItemLabelsVisible();
	
	public boolean isLastJobRunDateVisible();
	
	// ################################################################
	// Registered/configured events 
	// ################################################################
	/** Get a list of all tool events definition (org.sakaiproject.sitestats.api.ToolInfo objects)
	 *  configured for this Sakai installation. */
	public List<ToolInfo> getAllToolEventsDefinition();
	
	/** Get a list of all event ids. */
	public List<String> getAllToolEventIds();
	
	/** Get a list of all tool events definition (org.sakaiproject.sitestats.api.ToolInfo objects)
	 *  configured for this Sakai installation. This list is intersected with tools available
	 *  on this Sakai installation and, optionally (if set by parameter), also intersected with the available
	 *  tools in site. */
	public List<ToolInfo> getSiteToolEventsDefinition(String siteId, boolean onlyAvailableInSite);
	
	/** Get the event id for the site visit event. */ 
	public String getSiteVisitEventId();
	
	/** Gets SiteStats preferences for a specific site. This list is intersected with tools available
	 *  on this Sakai installation and, optionally (if set by site), also intersected with the available
	 *  tools in site. */
	public PrefsData getPreferences(String siteId, boolean includeUnselected);
	/** Sets SiteStats preferences for a specific site. */
	public boolean setPreferences(String siteId, PrefsData prefsdata);	
	
	/** Helper method for parsing tool event definition xml */
	public ToolFactory getToolFactory();	
	/** Helper method for parsing tool event definition xml */
	public EventFactory getEventFactory();
	
	
	// ################################################################
	// Maps
	// ################################################################
	/** Get the tool name for a given tool id. */
	public String getToolName(String toolId);
	
	/** Get the event name for a given event id. */
	public String getEventName(String eventId);
	
	/** Get the event tool mapping (event id <-> tool mapping). */
	public Map<String, ToolInfo> getEventIdToolMap();
	
	/** Get the resource name from a reference */
	public String getResourceName(String ref);
	
	/** Get the resource image from a reference */
	public String getResourceImage(String ref);
	
	/** Get the resource url from a reference. Returns null if resource no longer exist. */
	public String getResourceURL(String ref);
	
	// ################################################################
	// Summary/report methods
	// ################################################################
	/** Get summary information about site visits. */
	public SummaryVisitsTotals getSummaryVisitsTotals(String siteId);	
	
	/** Get summary information about site activity. */
	public SummaryActivityTotals getSummaryActivityTotals(String siteId);
	/** Get summary information about site activity. */
	public SummaryActivityTotals getSummaryActivityTotals(String siteId, PrefsData prefsdata);
	
	/** Get summary information for chart draw about site visits. */
	public SummaryVisitsChartData getSummaryVisitsChartData(String siteId, String viewType);
	/** Get summary information for chart draw about site activity. */
	public SummaryActivityChartData getSummaryActivityChartData(String siteId, String viewType, String chartType);
	
	/** Produce a report based on the parameters passed. */
	public Report getReport(String siteId, PrefsData prefsdata, ReportParams params);
	
	
	// ################################################################
	// EventInfo related methods
	// ################################################################	
	/**
	 * Get events grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @return a list of EventStat objects (date member contains last date for the given event)
	 */
	public List<EventStat> getEventStats(String siteId, List<String> events);
	
	/**
	 * Get event statistics grouped by user, site and event
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @return a list of EventStat objects
	 */
	public List<EventStat> getEventStats(String siteId, List<String> events, String searchKey, Date iDate, Date fDate);

	/**
	 * Get event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @param page The PagePosition subset of items to return
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	public List<CommonStatGrpByDate> getEventStatsGrpByDate(String siteId, List<String> events, Date iDate, Date fDate, List<String> userIds, boolean inverseUserSelection, PagingPosition page);

	/**
	 * Count event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	//public int countEventStatsGrpByDate(String siteId, List<String> events, String searchKey, Date iDate, Date fDate);

	
	// ################################################################
	// Resource related methods
	// ################################################################
	/**
	 * Get resource statistics grouped by user, site, resource and date
	 * @param siteId The site ID
	 * @return a list of ResourceStat objects (date member contains last date for the given resource access)
	 */
	public List<ResourceStat> getResourceStats(String siteId);
	
	/**
	 * Get resource statistics grouped by user, site and resource
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @return a list of ResourceStat objects
	 */
	public List<ResourceStat> getResourceStats(String siteId, String searchKey, Date iDate, Date fDate);

	/**
	 * Get resource statistics grouped by user, site, resource and date
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date
	 * @param page The PagePosition subset of items to return
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given resource access)
	 */
	public List<CommonStatGrpByDate> getResourceStatsGrpByDateAndAction(String siteId, String resourceAction, List<String> resourceIds, Date iDate, final Date fDate, List<String> userIds, boolean inverseUserSelection, PagingPosition page);
	
	/**
	 * Count resource statistics grouped by user, site, resource and date
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given resource access)
	 */
	//public int countResourceStatsGrpByDateAndAction(String siteId, String searchKey, Date iDate, Date fDate);


	// ################################################################
	// Site visits related methods
	// ################################################################
	/**
	 * Get site statistics grouped by day.
	 * @param siteId Site identifier
	 * @return a list of SiteVisits objects
	 */
	public List<SiteVisits> getSiteVisits(String siteId);
	
	/**
	 * Get site statistics grouped by day on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteVisits objects
	 */
	public List<SiteVisits> getSiteVisits(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get site statistics grouped by month on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteVisits objects
	 */
	public List<SiteVisits> getSiteVisitsByMonth(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get total site visits.
	 * @param siteId Site identifier
	 * @return Total visits.
	 */
	public long getTotalSiteVisits(String siteId);
	
	/**
	 * Get total site visits on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public long getTotalSiteVisits(String siteId, Date iDate, Date fDate);

	/**
	 * Get total site unique visits on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public long getTotalSiteUniqueVisits(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get total site unique visits.
	 * @param siteId Site identifier
	 * @return Total visits.
	 */
	public long getTotalSiteUniqueVisits(String siteId);
	
	/**
	 * Get total site users (active and inactive).
	 * @param siteId Site identifier
	 * @return Total users
	 */
	public int getTotalSiteUsers(String siteId);


	// ################################################################
	// Site activity related methods
	// ################################################################	
	/**
	 * Get site activity statistics grouped by day.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @return a list of SiteActivity objects
	 */
	public List<SiteActivity> getSiteActivity(String siteId, List<String> events);
	
	/**
	 * Get site activity statistics grouped by day on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List<SiteActivity> getSiteActivity(String siteId, List<String> events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by day on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List<SiteActivity> getSiteActivityByDay(String siteId, List<String> events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by month on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List<SiteActivity> getSiteActivityByMonth(String siteId, List<String> events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by tool on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivityByTool objects
	 */
	public List<SiteActivityByTool> getSiteActivityByTool(String siteId, List<String> events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by day on a specific date interval (sum of event activity).
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List<SiteActivity> getSiteActivityGrpByDate(String siteId, List<String> events, Date iDate, Date fDate);

	/**
	 * Get total site activity on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public long getTotalSiteActivity(String siteId, List<String> events, Date iDate, Date fDate);
	
	/**
	 * Get total site activity.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @return Total visits.
	 */
	public long getTotalSiteActivity(String siteId, List<String> events);
	

	// ################################################################
	// Utility methods
	// ################################################################
	/** Get site initial activity date. */
	public Date getInitialActivityDate(String siteId);
}
