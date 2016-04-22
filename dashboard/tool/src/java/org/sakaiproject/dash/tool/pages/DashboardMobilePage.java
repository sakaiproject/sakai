/********************************************************************************** 
 * $URL: https://source.sakaiproject.org/contrib/dashboard/trunk/tool/src/java/org/sakaiproject/dash/tool/pages/DashboardPage.java $ 
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

package org.sakaiproject.dash.tool.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.StringRequestTarget;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.model.NewsLink;
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
public class DashboardMobilePage extends BasePage {
	
	private static final Logger logger = LoggerFactory.getLogger(DashboardMobilePage.class); 
	
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	
	protected String selectedCalendarTab;
	protected String selectedNewsTab;
	
	public DashboardMobilePage() {
		
		dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_VISIT, "/dashboard/page/" + sakaiProxy.getCurrentSiteId());
		
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
				
				logger.debug("entityDetailRequest.onClick() ");
				
				//get parameters
                final RequestCycle requestCycle = RequestCycle.get();

                WebRequest wr=(WebRequest)requestCycle.getRequest();

                HttpServletRequest hsr = wr.getHttpServletRequest();
                
                String entityReference = null;
                String entityType = null;
                int itemCount = 0;
                int offset = -1;
                String dashEvent = null;
                try {
                   BufferedReader br = hsr.getReader();

                   String  jsonString = br.readLine();
                   if((jsonString == null) || jsonString.isEmpty()){
                       logger.error(" no json found for entityReference: " + entityReference);
                   }
                   else {
                	   if(logger.isDebugEnabled()) {
                		   logger.debug(" json  is :"+ jsonString);
                	   }
                       JSONObject jsonObject = JSONObject.fromObject(jsonString);
                       
                       entityReference = jsonObject.optString("entityReference", "");
                       entityType = jsonObject.optString("entityType", "");
                       itemCount = jsonObject.optInt("itemCount", 1);
                       dashEvent = jsonObject.optString("dashEvent", "");
                       offset = jsonObject.optInt("offset", -1);
                   }
                   

                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }

                Locale locale = hsr.getLocale();
 				if(entityReference != null && ! entityReference.trim().equals("") && entityType != null && ! entityType.trim().equals("")) {
 					if(itemCount > 1) {
 						ResourceLoader rl = new ResourceLoader("dash_entity");
 						int limit = dashboardConfig.getConfigValue(DashboardConfig.PROP_DEFAULT_ITEMS_IN_DISCLOSURE, 20);
 						if(offset < 0) {
 							offset = 0;
 						}
 						String sakaiUserId = sakaiProxy.getCurrentUserId();
						int totalItems = dashboardCommonLogic.countNewsLinksByGroupId(sakaiUserId, entityReference);
						
						Map<String,Object> results = new HashMap<String,Object>();
						results.put("totalCount", totalItems);
						if(offset > totalItems) {
							results.put("items", new ArrayList<NewsLink>());
							results.put("count", 0);
						} else {
							List<NewsLink> items = dashboardCommonLogic.getNewsLinksByGroupId(sakaiUserId, entityReference, limit, offset);
							results.put("items", items);
							results.put("count", items.size());
						}
						results.put("details", rl.getString("dash.details"));
						results.put("offset", offset);
						
						results.put("more-link", rl.getString("dash.grouped.more.link", "[[ Show more ... ]]"));
						results.put("more-status", rl.getString("dash.grouped.more.status", "[[ Showing {0} of {1} items ]]"));
						results.put("more-status-last", rl.getString("dash.news.linksCount2", "[[ Showing item {0} of {1} items ]]"));
						results.put("more-status-range", rl.getString("dash.news.linksCount3", "[[ Showing {0} to {1} of {2} items ]]"));
						
						JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
						String jsonString = jsonHelper.getJsonObjectFromMap(results).toString();
						if(logger.isDebugEnabled()) {
							logger.debug("Returning JSON:\n" + jsonString);
						}
		                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
		                getRequestCycle().setRequestTarget(t);
						dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_VIEW_GROUP, "/dashboard/news/current/" + entityReference);
 					} else if(itemCount == 1) {
 		                Map<String,Object> entityMap = dashboardCommonLogic.getEntityMapping(entityType, entityReference, locale);
		                
		                String jsonString = getJsonStringFromMap(entityMap);
		                if(logger.isDebugEnabled()) {
		                	logger.debug("Returning JSON:\n" + jsonString);
		                }
		                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
		                getRequestCycle().setRequestTarget(t);
						dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_ITEM_DETAILS, "/dashboard/?/?/" + entityReference);
					} else if(dashEvent != null && ! dashEvent.trim().equals("")) {
 						// report the event
 						dashboardCommonLogic.recordDashboardActivity(dashEvent, entityReference);
	 				}
	 			}
			}
        };
        dashboardPage.add(entityDetailRequest);
        dashboardPage.add(new Label("callbackUrl", entityDetailRequest.getCallbackUrl().toString()));
        
        AbstractAjaxBehavior starHandler = new AbstractAjaxBehavior() {

			public void onRequest() {
				logger.debug("starHandler.onRequest() ");

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
						logger.debug(" json  is :"+ jsonString);
						JSONObject jsonObject = JSONObject.fromObject(jsonString);

						itemId = jsonObject.optLong("itemId", Long.MIN_VALUE);
						action = jsonObject.optString("dashAction");
					}


				} catch (IOException ex) {
					logger.error(ex.getMessage(), ex);
					message = rl.getString("dash.ajax.failed");
					success = false;
				}
				
				String newIcon = null;

				if(itemId < 1 || action == null || action.trim().equals("")) {
					logger.error("invalid values found " + action + " " + itemId);
					message = rl.getString("");
				} else if(success) {
					if("star".equalsIgnoreCase(action)) {
						try {
							success = dashboardCommonLogic.keepNewsItem(sakaiProxy.getCurrentUserId(), itemId);
						} catch(Exception e) {
							logger.warn("Error trying to star news-link for user " + sakaiProxy.getCurrentUserId() + ", newsLinkId == " + itemId);
							success = false;
						}
						if(success) {
							message = rl.getString("dash.ajax.star.success");
							newIcon = dashboardConfig.getActionIcon(DashboardConfig.ACTION_UNSTAR);
						} else {
							message = rl.getString("dash.ajax.star.failed");
						}
					} else if("unstar".equalsIgnoreCase(action)) {
						try {
							success = dashboardCommonLogic.unkeepNewsItem(sakaiProxy.getCurrentUserId(), itemId);
						} catch(Exception e) {
							logger.warn("Error trying to unstar news-link for user " + sakaiProxy.getCurrentUserId() + ", newsLinkId == " + itemId);
							success = false;
						}
						if(success) {
							message = rl.getString("dash.ajax.unstar.success");
							newIcon = dashboardConfig.getActionIcon(DashboardConfig.ACTION_STAR);
						} else {
							message = rl.getString("dash.ajax.unstar.failed");
						}
					} else if("hide".equalsIgnoreCase(action)) {
						try {
							success = dashboardCommonLogic.hideNewsItem(sakaiProxy.getCurrentUserId(), itemId);
						} catch(Exception e) {
							logger.warn("Error trying to hide news-link for user " + sakaiProxy.getCurrentUserId() + ", newsLinkId == " + itemId);
							success = false;
						}
						if(success) {
							message = rl.getString("dash.ajax.hide.success");
						} else {
							message = rl.getString("dash.ajax.hide.failed");
							success = false;
						}
					} else if("show".equalsIgnoreCase(action)) {
						try {
							success = dashboardCommonLogic.unhideNewsItem(sakaiProxy.getCurrentUserId(), itemId);
						} catch(Exception e) {
							logger.warn("Error trying to show news-link for user " + sakaiProxy.getCurrentUserId() + ", newsLinkId == " + itemId);
							success = false;
						}
						if(success) {
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
                if(newIcon != null) {
                	results.put("newIcon", newIcon);
                }
                String jsonString = getJsonStringFromMap(results);
                if(logger.isDebugEnabled()) {
                	logger.debug("Returning JSON:\n" + jsonString);
                }
                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
                getRequestCycle().setRequestTarget(t);				
				
			}
		};
		dashboardPage.add(starHandler);
		dashboardPage.add(new Label("dashActionHandler", starHandler.getCallbackUrl().toString()));
		
		ResourceLoader rl = new ResourceLoader("dash_entity");
		dashboardPage.add(new Label("genericErrorMessage", rl.getString("dash.generic.error")));
			
	}
		
	// should this be in JsonHelper ??
	protected String getJsonStringFromMap(Map<String, Object> map) {
		JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
		JSONObject json = jsonHelper.getJsonObjectFromMap(map);
		//logger.debug("Returning json: " + json.toString(3));
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
