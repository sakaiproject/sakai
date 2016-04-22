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

package org.sakaiproject.dash.tool.panels;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.dash.app.DashboardCommonLogic;
import org.sakaiproject.dash.app.DashboardConfig;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
import org.sakaiproject.dash.tool.pages.DashboardPage;
import org.sakaiproject.dash.tool.util.JsonHelper;
import org.sakaiproject.dash.util.DateUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
public class CalendarLinksPanel extends Panel {

	public static final String TAB_ID_UPCOMING = "upcoming";
	public static final String TAB_ID_PAST = "past";
	public static final String TAB_ID_STARRED = "starred";
	public static final String TAB_ID_HIDDEN = "hidden";
	public static final String TAB_ID_PAST_HIDDEN = "past-hidden";

	private static final Logger logger = LoggerFactory.getLogger(CalendarLinksPanel.class);
	
	protected static final String DATE_FORMAT = "dd-MMM-yyyy";
	protected static final String TIME_FORMAT = "HH:mm";
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	
	@SpringBean(name="org.sakaiproject.dash.app.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.dash.app.DashboardCommonLogic")
	protected DashboardCommonLogic dashboardCommonLogic;
		
	@SpringBean(name="org.sakaiproject.dash.app.DashboardConfig")
	protected DashboardConfig dashboardConfig;
	
	protected CalendarLinksDataProvider calendarLinksProvider = null;
	
	protected DashboardPage dashboardPage;
		 
	protected String selectedCalendarTab = null;
	protected String calendarLinksDivId = null;
	protected String calendarLinksCountId = null;
	protected int pageSize = dashboardConfig.getConfigValue(DashboardConfig.PROP_DEFAULT_ITEMS_IN_PANEL, 5);
	
	public CalendarLinksPanel(String id) {
		super(id);
		
		initPanel();
        
	}
	public CalendarLinksPanel(String id, String selectedTab) {
		super(id);
		this.selectedCalendarTab = selectedTab;
		
		initPanel();
        
	}

	/**
	 * 
	 */
	protected void initPanel() {
		
		if(selectedCalendarTab == null) {
			selectedCalendarTab = TAB_ID_UPCOMING;
		}

		if(this.calendarLinksDivId != null) {
			this.remove(calendarLinksDivId);
		}

        ResourceLoader rl = new ResourceLoader("dash_entity");

		final WebMarkupContainer calendarLinksDiv = new WebMarkupContainer("calendarLinksDiv");
		calendarLinksDiv.setOutputMarkupId(true);
		add(calendarLinksDiv);
		this.calendarLinksDivId = calendarLinksDiv.getId();
		
		calendarLinksDiv.add(new Label("calendarTitle",rl.getString("dash.calendar.title")));

        AjaxLink<IModel<List<CalendarLink>>> upcomingCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("upcomingCalendarLink onClick called");
				// set currentCalendarTab to "upcoming"
				selectedCalendarTab = TAB_ID_UPCOMING;
				// reset calendar dataview to show upcoming stuff
		        if(calendarLinksProvider == null) {
		        	calendarLinksProvider = new CalendarLinksDataProvider(selectedCalendarTab);
		        } else {
		        	calendarLinksProvider.setCalendarTab(selectedCalendarTab);
		        }
				
				// refresh calendarItemsDiv
		        initPanel();
				target.addComponent(CalendarLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#schedPanel').focus();");
				dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/calendar/upcoming");
			}
        	
        };
        
        upcomingCalendarLink.add(new Label("label", rl.getString("dash.calendar.upcoming")));
		WebMarkupContainer upcomingCalendarTab = new WebMarkupContainer("upcomingCalendarTab");
		if(selectedCalendarTab == null || TAB_ID_UPCOMING.equalsIgnoreCase(selectedCalendarTab)) {
			upcomingCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		upcomingCalendarTab.add(upcomingCalendarLink);
        calendarLinksDiv.add(upcomingCalendarTab);

		AjaxLink<IModel<List<CalendarLink>>> pastCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("pastCalendarLink onClick called");
				// set currentCalendarTab to "past"
				selectedCalendarTab = TAB_ID_PAST;
				
				// reset calendar dataview to show past stuff
		        if(calendarLinksProvider == null) {
		        	calendarLinksProvider = new CalendarLinksDataProvider(selectedCalendarTab);
		        } else {
		        	calendarLinksProvider.setCalendarTab(selectedCalendarTab);
		        }
								
				// refresh calendarItemsDiv
		        initPanel();
				target.addComponent(CalendarLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#schedPanel').focus();");
				dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/calendar/past");
			}
        	
        };
        pastCalendarLink.add(new Label("label", rl.getString("dash.calendar.past")));
		WebMarkupContainer pastCalendarTab = new WebMarkupContainer("pastCalendarTab");
		if(selectedCalendarTab != null && TAB_ID_PAST.equalsIgnoreCase(selectedCalendarTab)) {
			pastCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		pastCalendarTab.add(pastCalendarLink);
        calendarLinksDiv.add(pastCalendarTab);

		AjaxLink<IModel<List<CalendarLink>>> starredCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("starredCalendarLink onClick called");
				// set currentCalendarTab to "starred"
				selectedCalendarTab = TAB_ID_STARRED;
				
				// reset calendar dataview to show starred stuff
		        if(calendarLinksProvider == null) {
		        	calendarLinksProvider = new CalendarLinksDataProvider(selectedCalendarTab);
		        } else {
		        	calendarLinksProvider.setCalendarTab(selectedCalendarTab);
		        }
								
				// refresh calendarItemsDiv
		        initPanel();
				target.addComponent(CalendarLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#schedPanel').focus();");
				dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/calendar/starred");
			}
        	
        };
        starredCalendarLink.add(new Label("label", rl.getString("dash.calendar.starred")));
		WebMarkupContainer starredCalendarTab = new WebMarkupContainer("starredCalendarTab");
		if(selectedCalendarTab != null && TAB_ID_STARRED.equalsIgnoreCase(selectedCalendarTab)) {
			starredCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		starredCalendarTab.add(starredCalendarLink);
        calendarLinksDiv.add(starredCalendarTab);

		AjaxLink<IModel<List<CalendarLink>>> hiddenCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.debug("hiddenCalendarLink onClick called");
				// set currentCalendarTab to "hidden"
				selectedCalendarTab = TAB_ID_HIDDEN;
				
				// reset calendar dataview to show hidden stuff
		        if(calendarLinksProvider == null) {
		        	calendarLinksProvider = new CalendarLinksDataProvider(selectedCalendarTab);
		        } else {
		        	calendarLinksProvider.setCalendarTab(selectedCalendarTab);
		        }
				
				// refresh calendarItemsDiv
		        initPanel();
				target.addComponent(CalendarLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#schedPanel').focus();");
				dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/calendar/hidden");
			}
        	
        };
        hiddenCalendarLink.add(new Label("label", rl.getString("dash.calendar.hidden")));
		WebMarkupContainer hiddenCalendarTab = new WebMarkupContainer("hiddenCalendarTab");
		if(selectedCalendarTab != null && TAB_ID_HIDDEN.equalsIgnoreCase(selectedCalendarTab)) {
			hiddenCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		hiddenCalendarTab.add(hiddenCalendarLink);
        calendarLinksDiv.add(hiddenCalendarTab);

        if(calendarLinksProvider == null) {
        	calendarLinksProvider = new CalendarLinksDataProvider(selectedCalendarTab);
        } else {
        	calendarLinksProvider.setCalendarTab(selectedCalendarTab);
        }
        
        WebMarkupContainer haveLinks = new WebMarkupContainer("haveLinks");
        calendarLinksDiv.add(haveLinks);

		//present the calendar data in a table
		final DataView<CalendarLink> calendarDataView = new DataView<CalendarLink>("calendarItems", calendarLinksProvider) {

			@Override
			public void populateItem(final Item item) {
				if(item != null && item.getModelObject() != null) {
					item.setOutputMarkupId(true);
					ResourceLoader rl = new ResourceLoader("dash_entity");
					
	                final CalendarLink cLink = (CalendarLink) item.getModelObject();
	                final CalendarItem cItem = cLink.getCalendarItem();
	                
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                
	                String itemType = cItem.getSourceType().getIdentifier();
	                item.add(new Label("itemType", itemType));
	                item.add(new Label("itemCount", "1"));
	                item.add(new Label("entityReference", cItem.getEntityReference()));
	                Component timeLabel = new Label("calendarDate", DateUtil.getCalendarTimeString(cItem.getCalendarTime()));
	                timeLabel.add(new AttributeModifier("title", true, new AbstractReadOnlyModel(){

						@Override
						public Object getObject() {
							// TODO Auto-generated method stub
							return DateUtil.getFullDateString(cItem.getCalendarTime());
						}
	                	
	                }));
					item.add(timeLabel );
	                //item.add(new Label("calendarTime", new SimpleDateFormat(TIME_FORMAT).format(cItem.getCalendarTime())));
	                
	                Image icon = new Image("icon");
	                icon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

						@Override
						public Object getObject() {
							// TODO Auto-generated method stub
							return dashboardCommonLogic.getEntityIconUrl(cItem.getSourceType().getIdentifier(), cItem.getSubtype());
						}
	                	
	                }));
	                item.add(icon);
	                StringBuilder errorMessages = new StringBuilder();
					String title = FormattedText.processFormattedText(cItem.getTitle(), errorMessages , true, true);
					if(errorMessages != null && errorMessages.length() > 0) {
						logger.warn("Error(s) encountered while cleaning calendarItem title:\n" + errorMessages);
					}
					ExternalLink itemLink = new ExternalLink("itemLink", "#itemEvent");
					itemLink.add(new Label("itemTitle",title));
					itemLink.add(new Label("itemClick",rl.getString("dash.details")));
					item.add(itemLink);
					
	                String calendarItemLabel = dashboardCommonLogic.getString(cItem.getCalendarTimeLabelKey(), "", itemType);
	                if(calendarItemLabel == null) {
	                	calendarItemLabel = "";
	                }
					item.add(new Label("itemLabel", calendarItemLabel ));
	                item.add(new ExternalLink("siteLink", cItem.getContext().getContextUrl(), cItem.getContext().getContextTitle()));
	      
	                if(cLink.isSticky()) {
		                AjaxLink<CalendarLink> starringAction = new AjaxLink<CalendarLink>("starringAction") {
		                	protected long calendarItemId = cItem.getId();
		                	protected Component thisRow = item;
		                	
							@Override
							public void onClick(AjaxRequestTarget target) {
								logger.debug("starringAction onClick() called -- unstar ");
								// need to keep one item
								logger.debug(Long.toString(calendarItemId));
								//logger.debug(this.getModelObject());
								
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardCommonLogic.unkeepCalendarItem(sakaiUserId, calendarItemId);
								dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_UNSTAR, "/dashboard/calendar/" + selectedCalendarTab + "/" + calendarItemId);
								
								// if success adjust UI, else report failure?
								if(success) {
									target.addComponent(CalendarLinksPanel.this);
									if(TAB_ID_STARRED.equals(selectedCalendarTab)) {
										ResourceLoader rl = new ResourceLoader("dash_entity");

										CalendarItem changedItem = dashboardCommonLogic.getCalendarItem(calendarItemId);
										JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
										String jsonStr = jsonHelper.getJsonObjectFromCalendarItem(changedItem).toString();
										String javascript = "reportSuccess('" + rl.getString("dash.ajax.unstar.success") + "'," + jsonStr + ",'" + "not-sure-about-url-yet" + "');";
										target.appendJavascript(javascript);
										target.appendJavascript("resizeFrame('grow');");
										target.appendJavascript("$('#schedPanel').focus();");
									}
								}
							}
		                	
		                };
		                
		                starringAction.setDefaultModel(item.getModel());
		                item.add(starringAction);
		                
						Image starringActionIcon = new Image("starringActionIcon");
						starringActionIcon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								return dashboardConfig.getActionIcon(dashboardConfig.ACTION_UNSTAR);
							}
							
						}));
						starringActionIcon.add(new AttributeModifier("alt", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								ResourceLoader rl = new ResourceLoader("dash_entity");
								return rl.getString("dash.unstar");
							}
							
						}));
						starringAction.add(starringActionIcon);
		                //starringAction.add(new Label("starringActionLabel", "Unstar"));
		                
		                if(cLink.isHidden()) {
		                	// this shouldn't happen, but just in case ...
		                	starringAction.setVisible(false);
		                	starringAction.setVisibilityAllowed(false);
		                }
	                } else {
		                AjaxLink<CalendarLink> starringAction = new AjaxLink<CalendarLink>("starringAction") {
		                	protected long calendarItemId = cItem.getId();
		                	protected Component thisRow = item;
		                	
							@Override
							public void onClick(AjaxRequestTarget target) {
								logger.debug("starringAction onClick() called -- star ");
								// need to keep one item
								logger.debug(Long.toString(calendarItemId));
								//logger.debug(this.getModelObject());
								
								ResourceLoader rl = new ResourceLoader("dash_entity");

								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardCommonLogic.keepCalendarItem(sakaiUserId, calendarItemId);
								dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_STAR, "/dashboard/calendar/" + selectedCalendarTab + "/" + calendarItemId);
								
								// if success adjust UI, else report failure?
								if(success) {
									target.addComponent(CalendarLinksPanel.this);
									//String javascript = "alert('success. (" + thisRow.getMarkupId() + ")');";
									//target.appendJavascript(javascript );
									target.appendJavascript("resizeFrame('grow');");
									target.appendJavascript("$('#schedPanel').focus();");
								}
							}
							
		                };
						Image starringActionIcon = new Image("starringActionIcon");
						starringActionIcon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								return dashboardConfig.getActionIcon(dashboardConfig.ACTION_STAR);
							}
							
						}));
						starringActionIcon.add(new AttributeModifier("alt", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								ResourceLoader rl = new ResourceLoader("dash_entity");
								return rl.getString("dash.star");
							}
							
						}));
						starringAction.add(starringActionIcon);

		                //starringAction.add(new Label("starringActionLabel", "Star"));
		                item.add(starringAction);

		                if(cLink.isHidden()) {
		                	starringAction.setVisible(false);
		                	starringAction.setVisibilityAllowed(false);
		                }
	                }
	                
	                if(cLink.isHidden()) {
		                AjaxLink<CalendarLink> hidingAction = new AjaxLink<CalendarLink>("hidingAction") {
		                	protected long calendarItemId = cItem.getId();
		                	protected Component thisRow = item;

							@Override
							public void onClick(AjaxRequestTarget target) {
								logger.debug("hidingAction onClick() called -- show");
								// need to trash one item
								logger.debug(Long.toString(calendarItemId));
								//logger.debug(this.getModelObject());
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardCommonLogic.unhideCalendarItem(sakaiUserId, calendarItemId);
								dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_SHOW, "/dashboard/calendar/" + selectedCalendarTab + "/" + calendarItemId);
								
								// if success adjust UI, else report failure?
								if(success) {
									ResourceLoader rl = new ResourceLoader("dash_entity");

									target.addComponent(CalendarLinksPanel.this);
									CalendarItem changedItem = dashboardCommonLogic.getCalendarItem(calendarItemId);
									JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
									String jsonStr = jsonHelper.getJsonObjectFromCalendarItem(changedItem).toString();
									String javascript = "reportSuccess('" + rl.getString("dash.ajax.show.success") + "'," + jsonStr + ",'" + "not-sure-about-url-yet" + "');";
									target.appendJavascript(javascript );
									target.appendJavascript("resizeFrame('grow');");
									target.appendJavascript("$('#schedPanel').focus();");
								}
							}
		                	
		                };
		                hidingAction.setDefaultModel(item.getModel());
		                //actionHideThisLink.setModelObject(cItem);
		                item.add(hidingAction);
						Image hidingActionIcon = new Image("hidingActionIcon");
						hidingActionIcon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								return dashboardConfig.getActionIcon(dashboardConfig.ACTION_SHOW);
							}
							
						}));
						hidingActionIcon.add(new AttributeModifier("alt", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								ResourceLoader rl = new ResourceLoader("dash_entity");
								return rl.getString("dash.show");
							}
							
						}));
						hidingAction.add(hidingActionIcon);
		                //hidingAction.add(new Label("hidingActionLabel", "Show"));
		                
		                if(cLink.isSticky() || TAB_ID_PAST.equals(selectedCalendarTab)) {
		                	// this shouldn't happen, but just in case ...
		                	hidingAction.setVisible(false);
		                	hidingAction.setVisibilityAllowed(false);
		                } 
	                	
	                } else {
		                AjaxLink<CalendarLink> hidingAction = new AjaxLink<CalendarLink>("hidingAction") {
		                	protected long calendarItemId = cItem.getId();
		                	protected Component thisRow = item;

							@Override
							public void onClick(AjaxRequestTarget target) {
								logger.debug("hidingAction onClick() called -- hide");
								// need to trash one item
								logger.debug(Long.toString(calendarItemId));
								//logger.debug(this.getModelObject());
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardCommonLogic.hideCalendarItem(sakaiUserId, calendarItemId);
								dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_HIDE, "/dashboard/calendar/" + selectedCalendarTab + "/" + calendarItemId);
								
								// if success adjust UI, else report failure?
								if(success) {
									ResourceLoader rl = new ResourceLoader("dash_entity");
									//renderItemCounter(calendarLinksDiv, calendarDataView); 
									target.addComponent(CalendarLinksPanel.this);
									
									CalendarItem changedItem = dashboardCommonLogic.getCalendarItem(calendarItemId);
									JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
									String jsonStr = jsonHelper.getJsonObjectFromCalendarItem(changedItem).toString();
									String javascript = "reportSuccess('" + rl.getString("dash.ajax.hide.success") + "'," + jsonStr + ",'" + "not-sure-about-url-yet" + "');";
									target.appendJavascript(javascript );
									target.appendJavascript("resizeFrame('grow');");
									target.appendJavascript("$('#schedPanel').focus();");
								}
							}
		                	
		                };
		                hidingAction.setDefaultModel(item.getModel());
		                
		                //actionHideThisLink.setModelObject(cItem);
		                item.add(hidingAction);
						Image hidingActionIcon = new Image("hidingActionIcon");
						hidingActionIcon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								return dashboardConfig.getActionIcon(dashboardConfig.ACTION_HIDE);
							}
							
						}));
						hidingActionIcon.add(new AttributeModifier("alt", true, new AbstractReadOnlyModel(){

							@Override
							public Object getObject() {
								ResourceLoader rl = new ResourceLoader("dash_entity");
								return rl.getString("dash.hide");
							}
							
						}));
						hidingAction.add(hidingActionIcon);
		                //hidingAction.add(new Label("hidingActionLabel", "Hide"));
		                
		                if(cLink.isSticky() || TAB_ID_PAST.equals(selectedCalendarTab)) {
			                hidingAction.setVisible(false);
		                	hidingAction.setVisibilityAllowed(false);
		                } 
	                	
	                }
	                
	                
				}
			}
        };
        calendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        calendarDataView.setItemsPerPage(pageSize);
        haveLinks.add(calendarDataView);

        IPagingLabelProvider pagingLabelProvider = new IPagingLabelProvider() {

			public String getPageLabel(int page) {
		        ResourceLoader rl = new ResourceLoader("dash_entity");
				
				int itemCount = 0;
				String pagerStatus = "";
				if(calendarDataView != null) {
				    int first = 0;
				    int last = 0;
					itemCount = calendarDataView.getItemCount();
				    int pageSize = calendarDataView.getItemsPerPage();
					if(itemCount > pageSize) {
						//int page = calendarDataView.getCurrentPage();
						first = page * pageSize + 1;
						last = Math.min(itemCount, (page + 1) * pageSize);
						if(first == last) {
				    		pagerStatus = Integer.toString(first);
						} else {
				    		pagerStatus = rl.getFormattedMessage("dash.pager.range", new Object[]{new Integer(first), new Integer(last)});
						}
					} else if(itemCount > 1) {
						pagerStatus = rl.getFormattedMessage("dash.pager.range", new Object[]{new Integer(1), new Integer(itemCount)});
					} else if(itemCount > 0) {
						pagerStatus = "1";
					} else {
						pagerStatus = "0";
					}
				}
				if(logger.isDebugEnabled()) {
					logger.debug("getPageLabel() " + pagerStatus);
				}
				return pagerStatus;
			}
        };
		//add a pager to our table, only visible if we have more than 5 items
        calendarLinksDiv.add(new PagingNavigator("calendarNavigator", calendarDataView, pagingLabelProvider ) {
        	
        	protected int currentPage = 1;
        	
        	@Override
        	public boolean isVisible() {
        		if(calendarLinksProvider != null && calendarLinksProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		if(this.getPageable().getCurrentPage() != currentPage) {
    				dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_PAGING, "/dashboard/calendar/" + selectedCalendarTab);
    				currentPage = this.getPageable().getCurrentPage();
        		}

        		//renderItemCounter(calendarLinksDiv, (DataView<CalendarLink>) getPageable()); 
       		
        		//clear the feedback panel messages
        		//clearFeedback(feedbackPanel);
        	}
        	
        });

        WebMarkupContainer haveNoLinks = new WebMarkupContainer("haveNoLinks");
        calendarLinksDiv.add(haveNoLinks);
        
        String noCalendarLinksLabel = null;
		if(TAB_ID_UPCOMING.equals(selectedCalendarTab)) {
			noCalendarLinksLabel = rl.getString("dash.calendar.noupcoming");
		} else if(TAB_ID_PAST.equals(selectedCalendarTab)) {
			noCalendarLinksLabel = rl.getString("dash.calendar.nopast");
		} else if(TAB_ID_STARRED.equals(selectedCalendarTab)) {
			noCalendarLinksLabel = rl.getString("dash.calendar.nostarred");
		} else if(TAB_ID_HIDDEN.equals(selectedCalendarTab)) {
			noCalendarLinksLabel = rl.getString("dash.calendar.nohidden");
		}
		haveNoLinks.add(new Label("message", noCalendarLinksLabel));
   
		//renderItemCounter(calendarLinksDiv, calendarDataView); 

        int itemCount = 0;
        if(calendarDataView != null) {
        	itemCount = calendarDataView.getItemCount();
        } 
                
        if(itemCount > 0) {
        	// show the haveLinks
        	haveLinks.setVisible(true);
        	// hide the haveNoLinks
        	haveNoLinks.setVisible(false);
        } else {
        	// show the haveNoLinks
        	haveNoLinks.setVisible(true);
        	// hide the haveLinks
        	haveLinks.setVisible(false);
        }
	}
	
	/**
	 * @param rl
	 * @param calendarLinksDiv
	 * @param calendarDataView
	 */
	protected void renderItemCounter(WebMarkupContainer calendarLinksDiv,
			DataView<CalendarLink> calendarDataView) {
		
		ResourceLoader rl = new ResourceLoader("dash_entity");
		
		if(calendarLinksCountId != null) {
			Iterator itx = calendarLinksDiv.iterator();
			while(itx.hasNext()) {
				Component child = (Component) itx.next();
				if(calendarLinksCountId.equals(child.getId())) {
					calendarLinksDiv.remove(child);
					break;
				}
			}
		}

		int itemCount = 0;
		String pagerStatus = "";
		if(calendarDataView != null) {
		    int first = 0;
		    int last = 0;
			itemCount = calendarDataView.getItemCount();
		    int pageSize = calendarDataView.getItemsPerPage();
			if(itemCount > pageSize) {
				int page = calendarDataView.getCurrentPage();
				first = page * pageSize + 1;
				last = Math.min(itemCount, (page + 1) * pageSize);
				if(first == last) {
		    		pagerStatus = rl.getFormattedMessage("dash.calendar.linksCount2", new Object[]{new Integer(first), new Integer(itemCount)});
				} else {
		    		pagerStatus = rl.getFormattedMessage("dash.calendar.linksCount3", new Object[]{new Integer(first), new Integer(last), new Integer(itemCount)});
				}
			} else if(itemCount > 1) {
				pagerStatus = rl.getFormattedMessage("dash.calendar.linksCount3", new Object[]{new Integer(1), new Integer(itemCount), new Integer(itemCount)});
			} else if(itemCount > 0) {
				pagerStatus = rl.getString("dash.calendar.linksCount1");
			} else {
				pagerStatus = rl.getString("dash.calendar.linksCount0");
			}
		}
		Label calendarLinksCount = new Label("calendarLinksCount", pagerStatus);
    	// add the count to the calendarLinksDiv
        calendarLinksDiv.add(calendarLinksCount);
        calendarLinksCountId = calendarLinksCount.getId();
	}

	private class CalendarLinkListDataProvider implements IDataProvider<List<CalendarLink>> {

		public void detach() {
			// TODO Auto-generated method stub
			
		}

		public Iterator<? extends List<CalendarLink>> iterator(int first, int count) {
			// TODO Auto-generated method stub
			return null;
		}

		public int size() {
			
			return 0;
		}

		public IModel<List<CalendarLink>> model(List<CalendarLink> object) {
			// TODO Auto-generated method stub
			return null;
		}
	
	}
	

	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class CalendarLinksDataProvider implements IDataProvider<CalendarLink> {
	   
		private List<CalendarLink> calendarLinks;
		private String calendarTabId;
		
		public CalendarLinksDataProvider() {
			super();
		}
		
		public CalendarLinksDataProvider(String calendarTabId) {
			super();
			this.calendarTabId = calendarTabId;
			if(this.calendarTabId == null) {
				this.calendarTabId = TAB_ID_UPCOMING;
			}
		}
				
		public void setCalendarTab(String calendarTabId) {
			if(this.calendarTabId == null || ! this.calendarTabId.equals(calendarTabId)) {
				// force refresh of dataProvider
				this.calendarLinks = null;
			}
			this.calendarTabId = calendarTabId;
			
		}

		private List<CalendarLink> getData() {
			
			if(calendarLinks == null) {
				String siteId = sakaiProxy.getCurrentSiteId();
				String sakaiId = sakaiProxy.getCurrentUserId();
				if(siteId == null || sakaiId == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("CalendarLinkDataProvider.getData() siteId:" + siteId + "  sakaiId:" + sakaiId);
					}
					return new ArrayList<CalendarLink>();
				}
				if(TAB_ID_UPCOMING.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardCommonLogic.getFutureCalendarLinks(sakaiId, null, false);
				} else if(TAB_ID_UPCOMING.equals(this.calendarTabId)) {
					calendarLinks = dashboardCommonLogic.getFutureCalendarLinks(sakaiId, siteId, false);
				} else if(TAB_ID_STARRED.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardCommonLogic.getStarredCalendarLinks(sakaiId, null);
				} else if(TAB_ID_STARRED.equals(this.calendarTabId)) {
					calendarLinks = dashboardCommonLogic.getStarredCalendarLinks(sakaiId, siteId);
				} else if(TAB_ID_PAST.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardCommonLogic.getPastCalendarLinks(sakaiId, null, false);
				} else if(TAB_ID_PAST.equals(this.calendarTabId) ) {
					calendarLinks = dashboardCommonLogic.getPastCalendarLinks(sakaiId, siteId, false);
				} else if(TAB_ID_HIDDEN.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardCommonLogic.getFutureCalendarLinks(sakaiId, null, true);
				} else if(TAB_ID_HIDDEN.equals(this.calendarTabId)) {
					calendarLinks = dashboardCommonLogic.getFutureCalendarLinks(sakaiId, siteId, true);
				} else if(TAB_ID_PAST_HIDDEN.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardCommonLogic.getPastCalendarLinks(sakaiId, null, false);
				} else {
					calendarLinks = dashboardCommonLogic.getPastCalendarLinks(sakaiId, siteId, false);
				} 
			}
			if(calendarLinks == null) {
				logger.warn("Error getting calendarLinks");
				return new ArrayList<CalendarLink>();
			}
			return calendarLinks;
		}
		
		public Iterator<CalendarLink> iterator(int first, int count){
			return getData().subList(first, first + count).iterator();
		}
		
		public int size(){
			return getData().size();
		}
		
		public IModel<CalendarLink> model(CalendarLink object){
			return new DetachableCalendarLinkModel(object);
		}

		public void detach(){
			calendarLinks = null;
		}
	}

	/**
	 * Detachable model to wrap a CalendarLink
	 * 
	 */
	private class DetachableCalendarLinkModel extends LoadableDetachableModel<CalendarLink>{

		private Long id = null;
		
		/**
		 * @param m
		 */
		public DetachableCalendarLinkModel(CalendarLink t){
			this.id = t.getId();
		}
		
		/**
		 * @param id
		 */
		public DetachableCalendarLinkModel(long id){
			this.id = id;
		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			return Long.valueOf(id).hashCode();
		}
		
		/**
		 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
		 * 
		 * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(final Object obj){
			if (obj == this){
				return true;
			}
			else if (obj == null){
				return false;
			}
			else if (obj instanceof DetachableCalendarLinkModel) {
				DetachableCalendarLinkModel other = (DetachableCalendarLinkModel)obj;
				return other.id == id;
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected CalendarLink load(){
			
			// get the calendar item
			return dashboardCommonLogic.getCalendarLink(id);
		}
	}
	
}
