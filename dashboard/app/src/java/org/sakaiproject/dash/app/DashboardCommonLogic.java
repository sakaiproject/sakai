/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.dash.app;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.NewsItem;

/**
 * DashboardCommonLogic
 *
 */
public interface DashboardCommonLogic extends DashboardLogic, DashboardUserLogic {
	
	public static final String EVENT_DASH_VISIT = "dash.visit";
	public static final String EVENT_DASH_FOLLOW_TOOL_LINK = "dash.follow.tool.link";
	public static final String EVENT_DASH_FOLLOW_SITE_LINK = "dash.follow.site.link";
	public static final String EVENT_DASH_ACCESS_URL = "dash.access.url";
	public static final String EVENT_VIEW_ATTACHMENT = "dash.view.attachment";
	
	public static final String EVENT_DASH_TABBING = "dash.tabbing";
	public static final String EVENT_DASH_PAGING = "dash.paging";

	public static final String EVENT_DASH_ITEM_DETAILS = "dash.item.details";
	public static final String EVENT_DASH_VIEW_GROUP = "dash.view.group";
	
	public static final String EVENT_DASH_STAR = "dash.star.item";
	public static final String EVENT_DASH_UNSTAR = "dash.unstar.item";
	public static final String EVENT_DASH_HIDE = "dash.hide.item";
	public static final String EVENT_DASH_SHOW = "dash.show.item";
	public static final String EVENT_DASH_HIDE_MOTD = "dash.hide.motd";
	
	/**
	 * 
	 * @param entityType
	 * @param entityReference
	 * @param locale
	 * @return
	 */
	public Map<String, Object> getEntityMapping(String entityType, String entityReference, Locale locale);

	/**
	 * Retrieve a localized string value specific to a particular type of entity using
	 * the provided key. 
	 * @param key
	 * @param dflt 
	 * @param entityTypeId
	 * @return the value or null if no value is found
	 */
	public String getString(String key, String dflt, String entityTypeId);

	/**
	 * Post or log an event according to the current settings in DashboardConfig. 
	 * May result in an "Event" being posted to sakai's EventTrackingService or in an 
	 * "Event" being persisted locally or both or neither, depending on the settings 
	 * in DashboardConfig for this particular event identifier (the first parameter). 
	 * @param event
	 * @param itemRef
	 */
	public void recordDashboardActivity(String event, String itemRef);

	/**
	 * @param type
	 * @param subtype
	 * @return
	 */
	public String getEntityIconUrl(String type, String subtype);

	/**
	 * @return
	 */
	public List<NewsItem> getMOTD();
	
	/**
	 * Update calendar repeating events 
	 */
	public void updateRepeatingEvents();
	
	/**
	 * Expire and purge dashboard old items and links
	 */
	public void expireAndPurge();
	
	/**
	 * Handle the availability checks of dashboard items
	 */
	public void handleAvailabilityChecks();
	
	/**
	 * Check for admin dashboard configuration changes
	 */
	public void checkForAdminChanges();
	
	/**
	 * Synchronize the dashboard links table users with current site users
	 */
	public void syncDashboardUsersWithSiteUsers();
}
