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

package org.sakaiproject.dash.tool.util;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.util.DateUtil;
import org.sakaiproject.util.FormattedText;

/**
 * 
 *
 */
public class JsonHelper {
	
	private static Logger logger = LoggerFactory.getLogger(JsonHelper.class);
	
	protected DashboardCommonLogic dashboardCommonLogic;
	protected DashboardConfig dashboardConfig;
	
	public JsonHelper(DashboardCommonLogic dashboardCommonLogic, DashboardConfig dashboardConfig) {
		this.dashboardCommonLogic = dashboardCommonLogic;
		this.dashboardConfig = dashboardConfig;
	}
	
	
	public JSONObject getJsonObjectFromMap(Map<String, Object> map) {
		JSONObject json = new JSONObject();
		if(map != null) {
			for(Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				if(value instanceof String) {
					json.element(key, value);
				} else if(value instanceof Boolean) {
					json.element(key, value);
				} else if(value instanceof Number) {
					json.element(key, value);
				} else if(value instanceof Map) {
					json.element(key, getJsonObjectFromMap((Map<String, Object>) value));
				} else if(value instanceof List) {
					json.element(key, getJsonArrayFromList((List) value));
				}
			}
				
		}
		return json;
	}

	public JSONArray getJsonArrayFromList(List list) {
		JSONArray json = new JSONArray();
		if(list != null) {
			for(Object value : list) {
				if(value instanceof String) {
					json.element(value);
				} else if(value instanceof Boolean) {
					json.element(value);
				} else if(value instanceof Number) {
					json.element(value);
				} else if(value instanceof Map) {
					json.element(getJsonObjectFromMap((Map<String, Object>) value));
				} else if(value instanceof List) {
					json.element(getJsonArrayFromList((List) value));
				} else if(value instanceof NewsItem) {
					json.element(getJsonObjectFromNewsItem((NewsItem) value));
				} else if(value instanceof NewsLink) {
					json.element(getJsonObjectFromNewsLink((NewsLink) value));
				}
				
			}
		}
		return json;
	}

	private JSONObject getJsonObjectFromNewsLink(NewsLink newsLink) {
		JSONObject json = getJsonObjectFromNewsItem(newsLink.getNewsItem());
		json.element("sticky", newsLink.isSticky());
		json.element("hidden", newsLink.isHidden());
		json.element("newsLinkId", newsLink.getId());
		if(newsLink.isHidden()) {
			json.element("hidingActionIcon", dashboardConfig.getActionIcon(DashboardConfig.ACTION_SHOW));
		} else {
			json.element("hidingActionIcon", dashboardConfig.getActionIcon(DashboardConfig.ACTION_HIDE));
		}
		if(newsLink.isSticky()) {
			json.element("starringActionIcon", dashboardConfig.getActionIcon(DashboardConfig.ACTION_UNSTAR));
		} else {
			json.element("starringActionIcon", dashboardConfig.getActionIcon(DashboardConfig.ACTION_STAR));
		}
		return json;
	}

	public JSONObject getJsonObjectFromNewsItem(NewsItem newsItem) {
		JSONObject json = new JSONObject();
		json.element("entityReference", newsItem.getEntityReference());
		json.element("newsItemId", newsItem.getId());
		json.element("newsTime", newsItem.getNewsTime());
		json.element("newsTimeShortString", DateUtil.getNewsTimeString(newsItem.getNewsTime()));
		json.element("newsTimeFullString", DateUtil.getFullDateString(newsItem.getNewsTime()));
		json.element("label", dashboardCommonLogic.getString(newsItem.getNewsTimeLabelKey(), "", newsItem.getSourceType().getIdentifier()));
		json.element("entityType", newsItem.getSourceType().getIdentifier());
		json.element("subtype", newsItem.getSubtype());
		StringBuilder errorMessages = new StringBuilder();
		String title = FormattedText.processFormattedText(newsItem.getTitle(), errorMessages , true, true);
		if(errorMessages != null && errorMessages.length() > 0) {
			logger.warn("Error(s) encountered while cleaning calendarItem title:\n" + errorMessages);
		}
		json.element("title", title);
		json.element("iconUrl", dashboardCommonLogic.getEntityIconUrl(newsItem.getSourceType().getIdentifier(), newsItem.getSubtype()));
		return json;
	}

	public JSONObject getJsonObjectFromCalendarItem(CalendarItem calendarItem) {
		JSONObject json = new JSONObject();
		json.element("entityReference", calendarItem.getEntityReference());
		json.element("calendarItemId", calendarItem.getId());
		json.element("calendarTime", calendarItem.getCalendarTime());
		json.element("calendarTimeShortString", DateUtil.getCalendarTimeString(calendarItem.getCalendarTime()));
		json.element("calendarTimeFullString", DateUtil.getFullDateString(calendarItem.getCalendarTime()));
		json.element("label", dashboardCommonLogic.getString(calendarItem.getCalendarTimeLabelKey(), "", calendarItem.getSourceType().getIdentifier()));
		json.element("entityType", calendarItem.getSourceType().getIdentifier());
		json.element("subtype", calendarItem.getSubtype());
		StringBuilder errorMessages = new StringBuilder();
		String title = FormattedText.processFormattedText(calendarItem.getTitle(), errorMessages , true, true);
		if(errorMessages != null && errorMessages.length() > 0) {
			logger.warn("Error(s) encountered while cleaning calendarItem title:\n" + errorMessages);
		}
		json.element("title", title);
		json.element("iconUrl", dashboardCommonLogic.getEntityIconUrl(calendarItem.getSourceType().getIdentifier(), calendarItem.getSubtype()));
		return json;
	}

}
