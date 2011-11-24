package org.sakaiproject.dash.tool.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.StringRequestTarget;
import org.sakaiproject.dash.logic.DashboardConfig;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.tool.panels.CalendarLinksPanel;
import org.sakaiproject.dash.tool.panels.MOTDPanel;
import org.sakaiproject.dash.tool.panels.NewsLinksPanel;
import org.sakaiproject.dash.tool.util.JsonHelper;
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
	
	protected String selectedCalendarTab;
	protected String selectedNewsTab;
	
	public DashboardPage() {
		
		dashboardLogic.recordDashboardActivity(DashboardLogic.EVENT_DASH_VISIT, "/dashboard/page/" + sakaiProxy.getCurrentSiteId());
		
		final WebMarkupContainer dashboardPage = new WebMarkupContainer("dashboard-page");
		dashboardPage.setOutputMarkupId(true);
		add(dashboardPage);
		
		MOTDPanel motdPanel = new MOTDPanel("motdPanel");
		motdPanel.setOutputMarkupId(true);
		dashboardPage.add(motdPanel);
		
		CalendarLinksPanel calendarPanel = new CalendarLinksPanel("calendarPanel");
		calendarPanel.setOutputMarkupId(true);
		dashboardPage.add(calendarPanel);
		
		NewsLinksPanel newsPanel = new NewsLinksPanel("newsPanel");
		newsPanel.setOutputMarkupId(true);
		dashboardPage.add(newsPanel);
		
        AbstractAjaxBehavior entityDetailRequest = new AbstractAjaxBehavior() {

			public void onRequest() {
				
				logger.info("entityDetailRequest.onClick() ");
				
				//get parameters
                final RequestCycle requestCycle = RequestCycle.get();

                WebRequest wr=(WebRequest)requestCycle.getRequest();

                HttpServletRequest hsr = wr.getHttpServletRequest();
                
                String entityReference = null;
                String entityType = null;
                int itemCount = 0;
                String dashEvent = null;
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
                       dashEvent = jsonObject.optString("dashEvent", "");

                   }
                   

                } catch (IOException ex) {
                    logger.error(ex);
                }

                Locale locale = hsr.getLocale();
 				if(entityReference != null && ! entityReference.trim().equals("") && entityType != null && ! entityType.trim().equals("")) {
 					if(itemCount > 1) {
 						int pageSize = dashboardConfig.getConfigValue(DashboardConfig.PROP_DEFAULT_ITEMS_IN_DISCLOSURE, 20);
 						int pageNumber = 0;
 						String sakaiUserId = sakaiProxy.getCurrentUserId();
						List<NewsItem> items = dashboardLogic.getNewsItemsByGroupId(sakaiUserId, entityReference, pageSize, pageNumber);
						JsonHelper jsonHelper = new JsonHelper(dashboardLogic);
						String jsonString = jsonHelper.getJsonArrayFromList(items).toString();
		                logger.debug("Returning JSON:\n" + jsonString);
		                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
		                getRequestCycle().setRequestTarget(t);
						dashboardLogic.recordDashboardActivity(DashboardLogic.EVENT_DASH_VIEW_GROUP, "/dashboard/news/current/" + entityReference);
 					} else if(itemCount == 1) {
 		                Map<String,Object> entityMap = dashboardLogic.getEntityMapping(entityType, entityReference, locale);
		                
		                String jsonString = getJsonStringFromMap(entityMap);
		                logger.debug("Returning JSON:\n" + jsonString);
		                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
		                getRequestCycle().setRequestTarget(t);
						dashboardLogic.recordDashboardActivity(DashboardLogic.EVENT_DASH_ITEM_DETAILS, "/dashboard/?/?/" + entityReference);
					} else if(dashEvent != null && ! dashEvent.trim().equals("")) {
 						// report the event
 						dashboardLogic.recordDashboardActivity(dashEvent, entityReference);
	 				}
	 			}
			}
        };
        dashboardPage.add(entityDetailRequest);
        dashboardPage.add(new Label("callbackUrl", entityDetailRequest.getCallbackUrl().toString()));
        
        AbstractAjaxBehavior starHandler = new AbstractAjaxBehavior() {

			public void onRequest() {
				logger.info("starHandler.onRequest() ");

				String message = null;
				boolean success = true;
		        ResourceLoader rl = new ResourceLoader("dash_entity");
				//get parameters
				final RequestCycle requestCycle = RequestCycle.get();

				WebRequest wr=(WebRequest)requestCycle.getRequest();

				HttpServletRequest hsr = wr.getHttpServletRequest();

				long itemId = -1L;
				String action = null;
				try {
					BufferedReader br = hsr.getReader();

					String  jsonString = br.readLine();
					if((jsonString == null) || jsonString.isEmpty()){
						logger.error(" no json found ");
						message = rl.getString("dash.ajax.failed");
						success = false;
					}
					else {
						if(logger.isDebugEnabled()) {
							logger.info(" json  is :"+ jsonString);
						}
						JSONObject jsonObject = JSONObject.fromObject(jsonString);

						itemId = jsonObject.optLong("itemId", Long.MIN_VALUE);
						action = jsonObject.optString("dashAction");
					}


				} catch (IOException ex) {
					logger.error(ex);
					message = rl.getString("dash.ajax.failed");
					success = false;
				}

				if(itemId < 1 || action == null || action.trim().equals("")) {
					logger.error("invalid values found " + action + " " + itemId);
					message = rl.getString("");
				} else if(success) {
					if("star".equalsIgnoreCase(action)) {
						if(dashboardLogic.keepNewsItem(sakaiProxy.getCurrentUserId(), itemId)) {
							message = rl.getString("dash.ajax.star.success");
						} else {
							message = rl.getString("dash.ajax.star.failed");
							success = false;
						}
					} else if("unstar".equalsIgnoreCase(action)) {
						if(dashboardLogic.unkeepNewsItem(sakaiProxy.getCurrentUserId(), itemId)) {
							message = rl.getString("dash.ajax.unstar.success");
						} else {
							message = rl.getString("dash.ajax.unstar.failed");
							success = false;
						}
					} else if("hide".equalsIgnoreCase(action)) {
						if(dashboardLogic.hideNewsItem(sakaiProxy.getCurrentUserId(), itemId)) {
							message = rl.getString("dash.ajax.hide.success");
						} else {
							message = rl.getString("dash.ajax.hide.failed");
							success = false;
						}
					} else if("show".equalsIgnoreCase(action)) {
						if(dashboardLogic.unhideNewsItem(sakaiProxy.getCurrentUserId(), itemId)) {
							message = rl.getString("dash.ajax.show.success");
						} else {
							message = rl.getString("dash.ajax.show.failed");
							success = false;
						}
					}
				}
                Map<String,Object> results = new HashMap<String,Object>();
                results.put("message", message);
                results.put("success", Boolean.valueOf(success));
                String jsonString = getJsonStringFromMap(results);
                logger.debug("Returning JSON:\n" + jsonString);
                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
                getRequestCycle().setRequestTarget(t);				
				
			}
		};
		dashboardPage.add(starHandler);
		dashboardPage.add(new Label("dashActionHandler", starHandler.getCallbackUrl().toString()));
			
	}
		
	// should this be in JsonHelper ??
	protected String getJsonStringFromMap(Map<String, Object> map) {
		JsonHelper jsonHelper = new JsonHelper(dashboardLogic);
		JSONObject json = jsonHelper.getJsonObjectFromMap(map);
		//logger.info("Returning json: " + json.toString(3));
		return json.toString();
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
