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

public interface StatsAuthz {

	/** Permissions */
	public static final String	PERMISSION_SITESTATS_VIEW		= "sitestats.view";
	public static final String	PERMISSION_SITESTATS_ADMIN_VIEW	= "sitestats.admin.view";
	public static final String	PERMISSION_SITESTATS_ALL		= "sitestats.all";
	public static final String	PERMISSION_SITESTATS_OWN		= "sitestats.own";
	public static final String	PERMISSION_SITESTATS_USER_TRACKING_CAN_BE_TRACKED = "sitestats.usertracking.be.tracked";
	public static final String	PERMISSION_SITESTATS_USER_TRACKING_CAN_TRACK = "sitestats.usertracking.track";

	/**
	 * Check if current user has permission to access SiteStats tool at all
	 *
	 * @param siteId The site id to check against.
	 */
	public boolean isUserAbleToViewSiteStats(String siteId);

	/**
	 * Check if current user has permission to access stats for all users
	 *
	 * @param siteId The site id to check against.
	 */
	public boolean isUserAbleToViewSiteStatsAll(String siteId);

	/**
	 * Check if current user has permission to access SiteStats Admin tool.
	 * @param siteId The site id to check against.
	 */
	public boolean isUserAbleToViewSiteStatsAdmin(String siteId);

	/**
	 * Check if current user has permission to access own SiteStats tool.
	 * @param siteId The site id to check against.
	 */
	public boolean isUserAbleToViewSiteStatsOwn(String siteId);

	/** Check if current tool is an instance of the SiteStats tool. */
	public boolean isSiteStatsPage();
	
	/** Check if current tool is an instance of the SiteStats Admin tool. */
	public boolean isSiteStatsAdminPage();

	/**
	 * Find the current user id
	 * @return current user id
	 */
	public String getCurrentSessionUserId();

	/**
	 * Check if the given user can be tracked in the current site.
	 * @param siteID the ID of the current site
	 * @param userID the ID of the user
	 * @return true/false if the user has the 'be tracked' permission
	 */
	public boolean canUserBeTracked(String siteID, String userID);

	/**
	 * Check if the current user has permission to track users in the given site. Also checks and requires
	 * that the user has permission to access the sitestats tool.
	 * @param siteID the ID of the current site
	 * @return true/false if the current user has the 'view stats' and 'can track' permissions in the given site, or true if the current user is an admin
	 */
	public boolean canCurrentUserTrackInSite(String siteID);

	/**
	 * General purpose permission check for the current user
	 * @param siteId the site id
	 * @param permission the name of the permission to check
	 * @return true if the user is admin or has the specified permission in the given site
	 */
	public boolean currentUserHasPermission(String siteId, String permission);
}
