/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.user.api.User;


public interface StatsManager {
	public final static String			SITESTATS_WEBAPP			= "/sitestats-tool";
	public final static String			SILK_ICONS_DIR				= "/library/image/silk/";
	
	public final static int				PREFS_OVERVIEW_PAGE			= 0;
	public final static int				PREFS_EVENTS_PAGE			= 1;
	public final static int				PREFS_RESOURCES_PAGE		= 2;
	public final static String			SEPARATOR					= "/";
	public final static String			SITEVISIT_EVENTID			= "pres.begin";
	public final static String			SITEVISITEND_EVENTID		= "pres.end";
	public final static String			LOGIN_EVENTID				= "user.login";
	public final static String			CONTAINER_LOGIN_EVENTID		= "user.login.container";
	public final static String			LOGOUT_EVENTID				= "user.logout";
	public final static String			RESOURCE_EVENTID_PREFIX		= "content.";
	public final static String			LESSONS_EVENTID_PREFIX		= "lessonbuilder.";
	public final static String			SITESTATS_TOOLID			= "sakai.sitestats";
	public final static String			SITESTATS_ADMIN_TOOLID		= "sakai.sitestats.admin";
	public final static String			LOG_APP						= "sitestats";
	public final static String			LOG_OBJ_REPORTDEF			= "report";
	public final static String			LOG_OBJ_PREFSDATA			= "prefs";
	public final static String			LOG_ACTION_NEW				= "new";
	public final static String			LOG_ACTION_EDIT				= "edit";
	public final static String			LOG_ACTION_VIEW				= "view";
	public final static String			LOG_ACTION_DELETE			= "delete";
	public final static String			RESOURCES_TOOLID			= "sakai.resources";
	public final static String			DROPBOX_TOOLID				= "sakai.dropbox";
	public final static String			PARSERTIP_FOR_CONTEXTID		= "contextId";
	public static final String			VIEW_WEEK					= "week";
	public static final String			VIEW_MONTH					= "month";
	public static final String			VIEW_YEAR					= "year";
	public static final String			MONTHLY_LOGIN_REPORT		= "monthlyLogin";
	public static final String			WEEKLY_LOGIN_REPORT			= "weeklyLogin";
	public static final String			DAILY_LOGIN_REPORT			= "dailyLogin";
	public static final String			REGULAR_USERS_REPORT		= "regularUsers";
	public static final String			HOURLY_USAGE_REPORT			= "hourlyUsage";
	public static final String			TOP_ACTIVITIES_REPORT		= "topActivities";
	public static final String			TOOL_REPORT					= "toolReport";
	public static final String			CHARTTYPE_LINE				= "line";
	public static final String			CHARTTYPE_BAR				= "bar";
	public static final String			CHARTTYPE_PIE				= "pie";
	public static final String			CHARTTYPE_TIMESERIES		= "timeseries";
	public static final String			CHARTTYPE_TIMESERIESBAR		= "timeseriesbar";
	public static final String			CHARTTIMESERIES_DAY			= "byday";
	public static final String			CHARTTIMESERIES_WEEKDAY		= "byweekday";
	public static final String			CHARTTIMESERIES_MONTH		= "bymonth";
	public static final String			CHARTTIMESERIES_YEAR		= "byyear";
	public static final String			RESOURCES_DIR				= "/group/";
	public static final String			DROPBOX_DIR					= "/group-user/";
	public static final String			ATTACHMENTS_DIR				= "/attachment/";
	public static final int				Q_TYPE_EVENT				= 0;
	public static final int				Q_TYPE_RESOURCE				= 1;
	public static final int				Q_TYPE_VISITSTOTALS			= 2;
	public static final int				Q_TYPE_ACTIVITYTOTALS		= 3;
	public static final int				Q_TYPE_PRESENCE				= 4;
	public static final int				Q_TYPE_LESSON				= 5;
	public static final String			T_NONE						= "none";
	public static final String			T_SITE						= "site";
	public static final String			T_USER						= "user";
	public static final String			T_EVENT						= "event";
	public static final String			T_TOOL						= "tool";
	public static final String			T_RESOURCE					= "resource";
	public static final String			T_RESOURCE_ACTION			= "resource-action";
	public static final String			T_PAGE                      = "page";
	public static final String			T_PAGE_ACTION               = "page-action";
	public static final String			T_DATE						= "date";
	public static final String			T_DATEMONTH					= "month";
	public static final String			T_DATEYEAR					= "year";
	public static final String			T_LASTDATE					= "last-date";
	public static final String			T_TOTAL						= "total";
	public static final String			T_VISITS					= "visits";
	public static final String			T_UNIQUEVISITS				= "unique-visits";
	public static final String			T_DURATION					= "duration";
	public static final List<String>	TOTALSBY_EVENT_DEFAULT		= Arrays.asList(T_USER, T_EVENT, T_DATE);
	public static final List<String>	TOTALSBY_RESOURCE_DEFAULT	= Arrays.asList(T_USER, T_RESOURCE, T_RESOURCE_ACTION, T_DATE);
	public static final List<String>	TOTALSBY_VISITSTOTALS_DEFAULT	= Arrays.asList(T_DATE);
	public static final List<String>	TOTALSBY_ACTIVITYTOTALS_DEFAULT	= Arrays.asList(T_DATE);
	public static final List<String>	TOTALSBY_PRESENCE_DEFAULT	= Arrays.asList(T_DATE);
	public static final List<String>	TOTALSBY_LESSONS_DEFAULT	= Arrays.asList(T_USER, T_PAGE, T_PAGE_ACTION, T_DATE);
	
	// ################################################################
	// Spring bean methods
	// ################################################################	
	/** Are site visits statistics enabled? */
	public boolean isEnableSiteVisits();
	
	/** Are site activity statistics enabled? */
	public boolean isEnableSiteActivity();
	
	/** Are site visits info available (displayable) in SiteStats tool? */
	public boolean isVisitsInfoAvailable();
	
	/** Are Resource statistics enabled for Overview page? */
	public boolean isEnableResourceStats();	
	
	/** Are site presence statistics enabled? */
	public boolean isEnableSitePresences();
	
	/** Get chart background color used to draw charts on SiteStats tool. */
	public String getChartBackgroundColor();
	
	/** Check if default is to draw charts in 3D on SiteStats tool. */
	public boolean isChartIn3D();
	
	/** Get default chart transparency (alpha value 0.0 - 1-0) on SiteStats tool. */
	public float getChartTransparency();
	
	/** Check if default is to draw item labels on charts on SiteStats tool. */
	public boolean isItemLabelsVisible();
	
	/** Is last quartz job run date displayable on SiteStats tool? */
	public boolean isLastJobRunDateVisible();

	/** Are server wide statistics enabled on SiteStats tool (admin version)? */
	public boolean isServerWideStatsEnabled();
	
	/** Are events triggered by anomymous access aggregated and displayable on SiteStats tool. */
	public boolean isShowAnonymousAccessEvents();
	
	/** Are exporting reports enabled? */
	public boolean isEnableReportExport();
	
	/** Is user name sorted using User.getSortName()? Otherwise, User.getDisplayName() should be used. */
	public boolean isSortUsersByDisplayName();
	
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
	// Resources related
	// ################################################################
	/** Get the resource name from a reference */
	public String getResourceName(String ref);
	
	/** Get the resource name from a reference */
	public String getResourceName(String ref, boolean includeLocationPrefix);
	
	/** Get the resource image from a reference */
	public String getResourceImage(String ref);
	
	/** Get the resource image relative path (to /library) from a reference */
	public String getResourceImageLibraryRelativePath(String ref);
	
	/** Get the resource url from a reference. Returns null if resource no longer exist. */
	public String getResourceURL(String ref);
	
	/** Get total number of resources (eventually, files only) in specified site, based on resources events (faster than consulting CHS). */
	public int getTotalResources(String siteId, boolean excludeFolders);

	/** Get the lesson page title from a page id */
	public String getLessonPageTitle(long pageId);

	/** Get total number of lesson pages in the specified site. */
	public int getTotalLessonPages(String siteId);

	/** Get total number of read lesson pages in the specified site. */
	public int getTotalReadLessonPages(String siteId);

	public String getMostReadLessonPage(final String siteId);

	public String getMostActiveLessonPageReader(final String siteId);
	
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
	// Event statistics related methods
	// ################################################################	
	/**
	 * Get events statistics (totals by user/event/date).
	 * @param siteId The site ID
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService})
	 * @return a list of {@link EventStat} objects
	 */
	public List<Stat> getEventStats(String siteId, List<String> events);
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * Use {@link #getEventStats(String, List, Date, Date, List, boolean, PagingPosition, String, String, boolean)} instead.<br/>
	 * Get event statistics grouped by user, site and event.
	 * @param siteId The site ID
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService})
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @return a list of {@link EventStat} objects
	 */
	@Deprecated public List<EventStat> getEventStats(String siteId, List<String> events, String searchKey, Date iDate, Date fDate);

	/**
	/**
	 * Get event statistics (totals by user/event/date).
	 * @param siteId The site ID (can be null)
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService}) (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param userIds The list of user Ids (can be null)
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_EVENT_DEFAULT}, {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortBy Column to sort by (can be null) (see {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortAscending Sort ascending?
	 * @param maxResults Maximum number of results (specify 0 (zero) for no limitation)
	 * @return a list of {@link EventStat} objects
	 */
	public List<Stat> getEventStats(
			final String siteId,
			final List<String> events, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy,
			final String sortBy,
			final boolean sortAscending,
			final int maxResults);
	
	/**
	 * Get row count for event statistics (totals by user/event/date).
	 * @param siteId The site ID (can be null)
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService}) (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param userIds The list of user Ids (can be null)
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_EVENT_DEFAULT}, {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortBy Columns to sort by (can be null)
	 * @param sortAscending Sort ascending?
	 * @return Row count.
	 */
	public int getEventStatsRowCount(
			final String siteId,
			final List<String> events, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final List<String> totalsBy);
	
	/**
	 * Get presence statistics (totals by user/event/date).
	 * @param siteId The site ID (can be null)
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService}) (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param userIds The list of user Ids (can be null)
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_EVENT_DEFAULT}, {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortBy Column to sort by (can be null) (see {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortAscending Sort ascending?
	 * @param maxResults Maximum number of results (specify 0 (zero) for no limitation)
	 * @return a list of {@link EventStat} objects
	 */
	public List<Stat> getPresenceStats(
			final String siteId, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy,
			final String sortBy,
			final boolean sortAscending,
			final int maxResults);
	
	/**
	 * Get row count for presence statistics (totals by user/event/date).
	 * @param siteId The site ID (can be null)
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService}) (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param userIds The list of user Ids (can be null)
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_EVENT_DEFAULT}, {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortBy Columns to sort by (can be null)
	 * @param sortAscending Sort ascending?
	 * @return Row count.
	 */
	public int getPresenceStatsRowCount(
			final String siteId, 
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final List<String> totalsBy);

	public Map<String, SitePresenceTotal> getPresenceTotalsForSite(final String siteId);
	
	// ################################################################
	// Resource statistics related methods
	// ################################################################
	/**
	 * Get resource statistics (totals by user/resource/date).
	 * @param siteId The site ID
	 * @return a list of {@link ResourceStat} objects
	 */
	public List<Stat> getResourceStats(String siteId);
	
	/**
	 * This method is deprecated and will be removed in version 2.1.
	 * Use {@link #getResourceStats(String, String, List, Date, Date, List, boolean, PagingPosition, String, String, boolean)} instead.<br/>
	 * Get resource statistics grouped by user, site and resource.
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @return a list of {@link ResourceStat} objects
	 */
	@Deprecated public List<ResourceStat> getResourceStats(String siteId, String searchKey, Date iDate, Date fDate);

	/**
	 * Get resource statistics (totals by user/event/date).
	 * @param siteId The site ID (can be null)
	 * @param resourceAction A specific resource action to limit to (can be null)
	 * @param resourceIds A list of specific resources to limit to (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param userIds The list of user Ids (can be null)
	 * @param inverseUserSelection match users not in userIds list
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_RESOURCE_DEFAULT}, {@link #T_USER}, {@link #T_RESOURCE}, {@link #T_RESOURCE_ACTION}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortBy Column to sort by (can be null) (see {@link #T_USER}, {@link #T_EVENT}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortAscending Sort ascending?
	 * @param maxResults Maximum number of results (specify 0 (zero) for no limitation)
	 * @return a list of {@link ResourceStat} objects
	 */
	public List<Stat> getResourceStats(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page, 
			final List<String> totalsBy,
			final String sortBy, 
			final boolean sortAscending,
			final int maxResults);

	public List<Stat> getLessonBuilderStats(final String siteId,
			final String resourceAction,
			final List<String> resourceIds,
			final Date iDate,
			final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final PagingPosition page,
			final List<String> totalsBy,
			final String sortBy,
			final boolean sortAscending,
			final int maxResults);
	
	/**
	 * Get row count for resource statistics (totals by user/event/date).
	 * @param siteId The site ID (can be null)
	 * @param resourceAction A specific resource action to limit to (can be null)
	 * @param resourceIds A list of specific resources to limit to (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date  (can be null)
	 * @param userIds The list of user Ids (can be null)
	 * @param inverseUserSelection match users not in userIds list
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_RESOURCE_DEFAULT}, {@link #T_USER}, {@link #T_RESOURCE}, {@link #T_RESOURCE_ACTION}, {@link #T_DATE}, {@link #T_LASTDATE})
	 * @param sortBy Columns to sort by (can be null)
	 * @param sortAscending Sort ascending?
	 * @return Row count
	 */
	public int getResourceStatsRowCount(
			final String siteId,
			final String resourceAction, final List<String> resourceIds,
			final Date iDate, final Date fDate,
			final List<String> userIds,
			final boolean inverseUserSelection,
			final List<String> totalsBy);


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
	 * Get total site users (active).
	 * @param siteId Site identifier
	 * @return Total users
	 */
	public int getTotalSiteUsers(String siteId);
	
	/**
	 * Get site users (active).
	 * @param siteId Site identifier
	 * @return Users id list
	 */
	public Set<String> getSiteUsers(String siteId);
	
	/**
	 * Get user display name (will return User.getSortName() unless "sortUsersByDisplayName@org.sakaiproject.sitestats.api.StatsManager = true" specified in sakai.properties).
	 * @param userId User ID
	 * @return The user display name
	 */
	public String getUserNameForDisplay(String userId);
	
	/**
	 * Get user display name (will return User.getSortName() unless "sortUsersByDisplayName@org.sakaiproject.sitestats.api.StatsManager = true" specified in sakai.properties).
	 * @param userId An User object.
	 * @return The user display name
	 */
	public String getUserNameForDisplay(User user);
	
	/**
	 * Get users with at least one visit in site.
	 * @param siteId Site identifier
	 * @return Users id list
	 */
	public Set<String> getUsersWithVisits(String siteId);

	/**
	 * Get visits/unique visits totals statistics.
	 * @param siteId The site ID (can be null)
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_VISITSTOTALS_DEFAULT}, {@link #T_DATE})
	 * @param sortBy Column to sort by (can be null) (see {@link #T_VISITS}, {@link #T_UNIQUEVISITS}, {@link #T_DATE})
	 * @param sortAscending Sort ascending?
	 * @param maxResults Maximum number of results (specify 0 (zero) for no limitation)
	 * @return a list of {@link SiteVisits} objects
	 */
	public List<Stat> getVisitsTotalsStats(String siteId, Date iDate, Date fDate, PagingPosition page, List<String> totalsBy, String sortBy, boolean sortAscending, int maxResults);


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
	
	/**
	 * Get activity totals statistics.
	 * @param siteId The site ID (can be null)
	 * @param events List of events to get statistics for (see {@link #getPreferences(String, boolean)}, {@link EventRegistryService}) (can be null) 
	 * @param iDate The initial date (can be null)
	 * @param fDate The final date (can be null)
	 * @param page The PagePosition subset of items to return (can be null)
	 * @param totalsBy Columns to sort by (see {@link #TOTALSBY_ACTIVITYTOTALS_DEFAULT}, {@link #T_EVENT}, {@link #T_DATE})
	 * @param sortBy Column to sort by (can be null) (see {@link #T_EVENT}, {@link #T_TOTAL}, {@link #T_DATE})
	 * @param sortAscending Sort ascending?
	 * @param maxResults Maximum number of results (specify 0 (zero) for no limitation)
	 * @return a list of {@link Stat} objects
	 */
	public List<Stat> getActivityTotalsStats(String siteId, List<String> events, Date iDate, Date fDate, PagingPosition page, List<String> totalsBy, String sortBy, boolean sortAscending, int maxResults);

	// ################################################################
	// Utility methods
	// ################################################################
	/** Get site initial activity date. */
	public Date getInitialActivityDate(String siteId);
	
	/** Checks whether Event.getContext is implemented in Event (from Event API) */
	public boolean isEventContextSupported();
	
	/** Logs an event using EventTrackingService. */
	public void logEvent(Object object, String logAction);
	
	/** Logs an event using EventTrackingService. */
	public void logEvent(Object object, String logAction, String siteId, boolean oncePerSession);
	
}
