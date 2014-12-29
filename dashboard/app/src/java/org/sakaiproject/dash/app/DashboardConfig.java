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
 * http://www.osedu.org/licenses/ECL-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 **********************************************************************************/ 

package org.sakaiproject.dash.app;

/**
 * 
 *
 */
public interface DashboardConfig {
	
	public static final String PROP_DEFAULT_ITEMS_IN_PANEL = "PROP_DEFAULT_ITEMS_IN_PANEL";
	public static final String PROP_DEFAULT_ITEMS_IN_DISCLOSURE = "PROP_DEFAULT_ITEMS_IN_DISCLOSURE";
	public static final String PROP_DEFAULT_ITEMS_IN_GROUP = "PROP_DEFAULT_ITEMS_IN_GROUP";
	public static final String PROP_LOOP_TIMER_ENABLED = "PROP_LOOP_TIMER_ENABLED";
	public static final String PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS = "PROP_REMOVE_NEWS_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS = "PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_HIDDEN_NEWS_ITEMS_AFTER_WEEKS = "PROP_REMOVE_STARRED_NEWS_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS = "PROP_REMOVE_CALENDAR_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS = "PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_HIDDEN_CALENDAR_ITEMS_AFTER_WEEKS = "PROP_REMOVE_STARRED_CALENDAR_ITEMS_AFTER_WEEKS";
	public static final String PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS = "PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS";
	public static final String PROP_REMOVE_CALENDAR_ITEMS_WITH_NO_LINKS = "PROP_REMOVE_NEWS_ITEMS_WITH_NO_LINKS";
	
	// horizon settings
	public static final String PROP_DAYS_BETWEEN_HORIZ0N_UPDATES = "PROP_DAYS_BETWEEN_HORIZ0N_UPDATES";
	public static final String PROP_WEEKS_TO_HORIZON = "PROP_WEEKS_TO_HORIZON";
	
	/** Modes are TEXT (1), LIST (2) or HIDDEN (0). Default is TEXT.  */
	public static final String PROP_MOTD_MODE = "PROP_MOTD_MODE";
	
	/** Modes are No-logging (0), Store-in-dash-event-table (1), post event to EventTrackingService (2), or both store and post (3) */
	public static final String PROP_LOG_MODE_FOR_NAVIGATION_EVENTS = "PROP_LOG_MODE_FOR_NAVIGATION_EVENTS";
	public static final String PROP_LOG_MODE_FOR_ITEM_DETAIL_EVENTS = "PROP_LOG_MODE_FOR_ITEM_DETAIL_EVENTS";
	public static final String PROP_LOG_MODE_FOR_PREFERENCE_EVENTS = "PROP_LOG_MODE_FOR_PREFERENCE_EVENTS";
	public static final String PROP_LOG_MODE_FOR_DASH_NAV_EVENTS = "PROP_LOG_MODE_FOR_DASH_NAV_EVENTS";
	
	public static final String ACTION_STAR = "star";
	public static final String ACTION_UNSTAR = "unstar";
	public static final String ACTION_HIDE = "hide";
	public static final String ACTION_SHOW = "show";
	
	
	public Integer getConfigValue(String propertyName, Integer propertyValue);
	
	public void setConfigValue(String propertyName, Integer propertyValue);
	
	public String getActionIcon(String actionId);
	
}
