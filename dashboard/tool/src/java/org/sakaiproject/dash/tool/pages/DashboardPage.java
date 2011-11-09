package org.sakaiproject.dash.tool.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.StringRequestTarget;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.tool.panels.CalendarLinksPanel;
import org.sakaiproject.dash.tool.panels.NewsLinksPanel;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 * 
 * 
 *
 */
public class DashboardPage extends BasePage {
	
	private static final Logger logger = Logger.getLogger(DashboardPage.class); 
	
	private static final String DATE_FORMAT = "dd-MMM-yyyy";
	private static final String TIME_FORMAT = "HH:mm";
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	
	protected int pageSize = 5;

	protected String selectedCalendarTab;
	protected String selectedNewsTab;
	
	
	public DashboardPage() {
		
		ResourceLoader rl = new ResourceLoader("dash_entity");
				
		final WebMarkupContainer dashboardPage = new WebMarkupContainer("dashboard-page");
		dashboardPage.setOutputMarkupId(true);
		add(dashboardPage);
		
		CalendarLinksPanel calendarPanel = new CalendarLinksPanel("calendarPanel");
		calendarPanel.setOutputMarkupId(true);
		dashboardPage.add(calendarPanel);
		
		NewsLinksPanel newsPanel = new NewsLinksPanel("newsPanel");
		newsPanel.setOutputMarkupId(true);
		dashboardPage.add(newsPanel);
		
        AbstractAjaxBehavior entityDetailRequest = new AbstractAjaxBehavior() {

			public void onRequest() {
				//get parameters
                final RequestCycle requestCycle = RequestCycle.get();

                WebRequest wr=(WebRequest)requestCycle.getRequest();

                HttpServletRequest hsr = wr.getHttpServletRequest();
                
                String entityReference = null;
                String entityType = null;
                int itemCount = 0;
                try {
                   BufferedReader br = hsr.getReader();

                   String  jsonString = br.readLine();
                   if((jsonString == null) || jsonString.isEmpty()){
                       logger.error(" no json found for entityReference: " + entityReference);
                   }
                   else {
                	   if(logger.isDebugEnabled()) {
                		   logger.info(" json  is :"+ jsonString);
                	   }
                       JSONObject jsonObject = JSONObject.fromObject(jsonString);
                       
                       entityReference = jsonObject.optString("entityReference", "");
                       entityType = jsonObject.optString("entityType", "");
                       itemCount = jsonObject.optInt("itemCount", 1);

                   }
                   

                } catch (IOException ex) {
                    logger.error(ex);
                }

                Locale locale = hsr.getLocale();
 				if(entityReference != null && ! entityReference.trim().equals("") && entityType != null && ! entityType.trim().equals("")) {
 					if(itemCount > 1) {
 						int pageSize = 20;
 						int pageNumber = 0;
 						String sakaiUserId = sakaiProxy.getCurrentUserId();
						List<NewsItem> items = dashboardLogic.getNewsItemsByGroupId(sakaiUserId, entityReference, pageSize, pageNumber);
						String jsonString = getJsonArrayFromList(items).toString();
		                logger.debug("Returning JSON:\n" + jsonString);
		                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
		                getRequestCycle().setRequestTarget(t);
 					} else {
 					
		                Map<String,Object> entityMap = dashboardLogic.getEntityMapping(entityType, entityReference, locale);
		                
		                String jsonString = getJsonStringFromMap(entityMap);
		                logger.debug("Returning JSON:\n" + jsonString);
		                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
		                getRequestCycle().setRequestTarget(t);
	 				}
	 			}
			}
        };
        dashboardPage.add(entityDetailRequest);
        dashboardPage.add(new Label("callbackUrl", entityDetailRequest.getCallbackUrl().toString()));
	}
		
	
	protected String getJsonStringFromMap(Map<String, Object> map) {
		JSONObject json = getJsonObjectFromMap(map);
		logger.info("Returning json: " + json.toString(3));
		return json.toString();
	}

	private JSONObject getJsonObjectFromMap(Map<String, Object> map) {
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

	private JSONArray getJsonArrayFromList(List list) {
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

	private JSONObject getJsonObjectFromNewsItem(NewsItem newsItem) {
		JSONObject json = new JSONObject();
		json.element("entityReference", newsItem.getEntityReference());
		json.element("id", newsItem.getId());
		json.element("newsTime", newsItem.getNewsTime());
		json.element("label", newsItem.getNewsTimeLabelKey());
		json.element("entityType", newsItem.getSourceType().getIdentifier());
		json.element("subtype", newsItem.getSubtype());
		json.element("title", newsItem.getTitle());
		json.element("iconUrl", dashboardLogic.getEntityIconUrl(newsItem.getSourceType().getIdentifier(), newsItem.getSubtype()));
		return json;
	}

	/**
	 * @return the selectedCalendarTab
	 */
	public String getSelectedCalendarTab() {
		return selectedCalendarTab;
	}

	/**
	 * @return the selectedNewsTab
	 */
	public String getSelectedNewsTab() {
		return selectedNewsTab;
	}

	/**
	 * @param selectedCalendarTab the selectedCalendarTab to set
	 */
	public void setSelectedCalendarTab(String selectedCalendarTab) {
		this.selectedCalendarTab = selectedCalendarTab;
	}

	/**
	 * @param selectedNewsTab the selectedNewsTab to set
	 */
	public void setSelectedNewsTab(String selectedNewsTab) {
		this.selectedNewsTab = selectedNewsTab;
	}

}
