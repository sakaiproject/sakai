/**
 * 
 */
package org.sakaiproject.dash.tool.util;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.util.DateUtil;

/**
 * 
 *
 */
public class JsonHelper {
	
	protected DashboardLogic dashboardLogic;
	
	public JsonHelper(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
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
				}
				
			}
		}
		return json;
	}

	public JSONObject getJsonObjectFromNewsItem(NewsItem newsItem) {
		JSONObject json = new JSONObject();
		json.element("entityReference", newsItem.getEntityReference());
		json.element("id", newsItem.getId());
		json.element("newsTime", newsItem.getNewsTime());
		json.element("newsTimeShortString", DateUtil.getNewsTimeString(newsItem.getNewsTime()));
		json.element("newsTimeFullString", DateUtil.getFullDateString(newsItem.getNewsTime()));
		json.element("label", dashboardLogic.getString(newsItem.getNewsTimeLabelKey(), "", newsItem.getSourceType().getIdentifier()));
		json.element("entityType", newsItem.getSourceType().getIdentifier());
		json.element("subtype", newsItem.getSubtype());
		json.element("title", newsItem.getTitle());
		json.element("iconUrl", dashboardLogic.getEntityIconUrl(newsItem.getSourceType().getIdentifier(), newsItem.getSubtype()));
		return json;
	}

	public JSONObject getJsonObjectFromCalendarItem(CalendarItem calendarItem) {
		JSONObject json = new JSONObject();
		json.element("entityReference", calendarItem.getEntityReference());
		json.element("id", calendarItem.getId());
		json.element("calendarTime", calendarItem.getCalendarTime());
		json.element("calendarTimeShortString", DateUtil.getCalendarTimeString(calendarItem.getCalendarTime()));
		json.element("calendarTimeFullString", DateUtil.getFullDateString(calendarItem.getCalendarTime()));
		json.element("label", dashboardLogic.getString(calendarItem.getCalendarTimeLabelKey(), "", calendarItem.getSourceType().getIdentifier()));
		json.element("entityType", calendarItem.getSourceType().getIdentifier());
		json.element("subtype", calendarItem.getSubtype());
		json.element("title", calendarItem.getTitle());
		json.element("iconUrl", dashboardLogic.getEntityIconUrl(calendarItem.getSourceType().getIdentifier(), calendarItem.getSubtype()));
		return json;
	}

}
