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

import org.sakaiproject.javax.PagingPosition;


public interface StatsManager {
	public final static int		PREFS_OVERVIEW_PAGE		= 0;
	public final static int		PREFS_EVENTS_PAGE		= 1;
	public final static int		PREFS_RESOURCES_PAGE	= 2;
	public final static String	SEPARATOR				= "/";
	public final static String	SITEVISIT_EVENTID		= "pres.begin";
	public final static String	RESOURCE_EVENTID_PREFIX	= "content.";
	public final static String	SITESTATS_TOOLID		= "sakai.sitestats";
	public final static String	RESOURCES_TOOLID		= "sakai.resources";
	public final static String	PARSERTIP_FOR_CONTEXTID	= "contextId";
	public static final String	VIEW_WEEK				= "week";
	public static final String	VIEW_MONTH				= "month";
	public static final String	VIEW_YEAR				= "year";
	public static final String	MONTHLY_LOGIN_REPORT	= "monthlyLogin";
	public static final String	WEEKLY_LOGIN_REPORT		= "weeklyLogin";
	public static final String	DAILY_LOGIN_REPORT		= "dailyLogin";
	public static final String	REGULAR_USERS_REPORT	= "regularUsers";
	public static final String	HOURLY_USAGE_REPORT		= "hourlyUsage";
	public static final String	TOP_ACTIVITIES_REPORT	= "topActivities";
	public static final String	TOOL_REPORT				= "toolReport";
	public static final String	CHARTTYPE_LINE			= "line";
	public static final String	CHARTTYPE_BAR			= "bar";
	public static final String	CHARTTYPE_PIE			= "pie";
	
	// ################################################################
	// Spring bean methods
	// ################################################################		
	public boolean isEnableSiteVisits();
	
	public boolean isEnableSiteActivity();
	
	public boolean isVisitsInfoAvailable();
	
	public String getChartBackgroundColor();
	
	public boolean isChartIn3D();
	
	public float getChartTransparency();
	
	public boolean isItemLabelsVisible();
	
	public boolean isLastJobRunDateVisible();

	public boolean isServerWideStatsEnabled();
	
	// ################################################################
	// Preferences
	// ################################################################
	/** Gets SiteStats preferences for a specific site. This list is intersected with tools available
	 *  on this Sakai installation and, optionally (if set by site), also intersected with the available
	 *  tools in site. */
	public PrefsData getPreferences(String siteId, boolean includeUnselected);
	/** Sets SiteStats preferences for a specific site. */
	public boolean setPreferences(String siteId, PrefsData prefsdata);
	
	
	// ################################################################
	// Maps
	// ################################################################
	/** Get the resource name from a reference */
	public String getResourceName(String ref);
	
	/** Get the resource image from a reference */
	public String getResourceImage(String ref);
	
	/** Get the resource image relative path (to /library) from a reference */
	public String getResourceImageLibraryRelativePath(String ref);
	
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
	 * Get event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @param userIds The list of user Ids
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return
	 * @param sortBy Columns to sort by
	 * @param sortAscending Sort ascending?
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	public List<CommonStatGrpByDate> getEventStats(
			final String siteId,
			final List<String> events, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final String groupBy,
			final String sortBy, 
			final boolean sortAscending);
	
	/**
	 * Get row count for event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @param userIds The list of user Ids
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return
	 * @param sortBy Columns to sort by
	 * @param sortAscending Sort ascending?
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	public int getEventStatsRowCount(
			final String siteId,
			final List<String> events, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final String groupBy,
			final String sortBy, 
			final boolean sortAscending);
	
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
	 * Get event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param resourceAction A specific resource action to limit to.
	 * @param resourceIds A list of specific resources to limit to.
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @param userIds The list of user Ids
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return
	 * @param sortBy Columns to sort by
	 * @param sortAscending Sort ascending?
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	public List<CommonStatGrpByDate> getResourceStats(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final String groupBy,
			final String sortBy, 
			final boolean sortAscending);
	
	/**
	 * Get row count for event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param resourceAction A specific resource action to limit to.
	 * @param resourceIds A list of specific resources to limit to.
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @param userIds The list of user Ids
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return
	 * @param sortBy Columns to sort by
	 * @param sortAscending Sort ascending?
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	public int getResourceStatsRowCount(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final String groupBy,
			final String sortBy, 
			final boolean sortAscending);


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
	
	/** Checks whether Event.getContext is implemented in Event (from Event API) */
	public boolean isEventContextSupported();
}
