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
	public final static int	PREFS_OVERVIEW_PAGE		= 0;
	public final static int	PREFS_EVENTS_PAGE		= 1;
	public final static int	PREFS_RESOURCES_PAGE	= 2;
	public final static String	SEPARATOR			= "/";

	
	// ################################################################
	// Spring bean methods
	// ################################################################
	public boolean isCollectAdminEvents();
	
	// ################################################################
	// Tool access 
	// ################################################################
	/** Check if the given user has access to the specified tool */
	//public boolean isUserAllowed(String userId, Site site, Tool tool);
	
	// ################################################################
	// Registered/configured events 
	// ################################################################
	/** Get all registered event ids. Event list is registered on tool xml file. */
	public List getRegisteredEventIds();
	
	/** Get default event ids for counting activity. Default event list is the registered less site visits. */
	public List getDefaultEventIdsForActivity();
	
	/** Configure site event ids for specified page. This is a sublist from registered events configured per site. */
	public void setSiteConfiguredEventIds(String siteId, List eventIds, int page);
	
	/** Get all site configured event ids for the specified page. This is a sublist from registered events configured per site. */
	public List getSiteConfiguredEventIds(String siteId, int page);
	
	// ################################################################
	// Maps
	// ################################################################	
	/** Get the event name for a given event id. */
	public String getEventName(String eventId);
	
	/** Get the event name mapping (id <-> name mapping). */
	public Map getEventNameMap();
	
	/** Get the resource name from a reference */
	public String getResourceName(String ref);
	
	/** Get the resource image from a reference */
	public String getResourceImage(String ref);
	
	/** Get the resource url from a reference. Returns null if resource no longer exist. */
	public String getResourceURL(String ref);
	
	// ################################################################
	// Event stats
	// ################################################################	
	/**
	 * Get events grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @return a list of EventStat objects (date member contains last date for the given event)
	 */
	public List getEventStats(String siteId, List events);
	
	/**
	 * Get event statistics grouped by user, site and event
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @return a list of EventStat objects
	 */
	public List getEventStats(String siteId, List events, String searchKey, Date iDate, Date fDate);

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
	public List getEventStatsGrpByDate(String siteId, List events, String searchKey, Date iDate, Date fDate, PagingPosition page);

	/**
	 * Count event statistics grouped by user, site, event and date
	 * @param siteId The site ID
	 * @param events List of events to get statistics for
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given event)
	 */
	public int countEventStatsGrpByDate(String siteId, List events, String searchKey, Date iDate, Date fDate);

	
	// ################################################################
	// Resource stats
	// ################################################################
	/**
	 * Get resource statistics grouped by user, site, resource and date
	 * @param siteId The site ID
	 * @return a list of ResourceStat objects (date member contains last date for the given resource access)
	 */
	public List getResourceStats(String siteId);
	
	/**
	 * Get resource statistics grouped by user, site and resource
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date 
	 * @return a list of ResourceStat objects
	 */
	public List getResourceStats(String siteId, String searchKey, Date iDate, Date fDate);

	/**
	 * Get resource statistics grouped by user, site, resource and date
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date
	 * @param page The PagePosition subset of items to return
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given resource access)
	 */
	public List getResourceStatsGrpByDateAndAction(String siteId, String searchKey, Date iDate, Date fDate, PagingPosition page);
	
	/**
	 * Count resource statistics grouped by user, site, resource and date
	 * @param siteId The site ID
	 * @param searchKey An user ID, first or last name
	 * @param iDate The initial date
	 * @param fDate The final date
	 * @return a list of CommonStatGrpByDate objects (date member contains last date for the given resource access)
	 */
	public int countResourceStatsGrpByDateAndAction(String siteId, String searchKey, Date iDate, Date fDate);


	// ################################################################
	// Site stats
	// ################################################################
	/**
	 * Get site statistics grouped by day.
	 * @param siteId Site identifier
	 * @return a list of SiteVisits objects
	 */
	public List getSiteVisits(String siteId);
	
	/**
	 * Get site statistics grouped by day on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteVisits objects
	 */
	public List getSiteVisits(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get site statistics grouped by month on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteVisits objects
	 */
	public List getSiteVisitsByMonth(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by day.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @return a list of SiteActivity objects
	 */
	public List getSiteActivity(String siteId, List events);
	
	/**
	 * Get site activity statistics grouped by day on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List getSiteActivity(String siteId, List events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by day on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List getSiteActivityByDay(String siteId, List events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by month on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List getSiteActivityByMonth(String siteId, List events, Date iDate, Date fDate);
	
	/**
	 * Get site activity statistics grouped by day on a specific date interval (sum of event activity).
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return a list of SiteActivity objects
	 */
	public List getSiteActivityGrpByDate(String siteId, List events, Date iDate, Date fDate);
	
	/**
	 * Get total site visits on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public long getTotalSiteVisits(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get total site users (active and inactive).
	 * @param siteId Site identifier
	 * @return Total users
	 */
	public int getTotalSiteUsers(String siteId);
	
	/**
	 * Get total site visits.
	 * @param siteId Site identifier
	 * @return Total visits.
	 */
	public long getTotalSiteVisits(String siteId);

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
	 * Get total site activity on a specific date interval.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public long getTotalSiteActivity(String siteId, List events, Date iDate, Date fDate);
	
	/**
	 * Get total site activity.
	 * @param siteId Site identifier
	 * @param events List of events to get statistics for
	 * @return Total visits.
	 */
	public long getTotalSiteActivity(String siteId, List events);
	

	// ################################################################
	// Utility methods
	// ################################################################
	/** Get site initial activity date. */
	public Date getInitialActivityDate(String siteId);
}
