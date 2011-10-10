package org.sakaiproject.dash.tool.pages;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.ComponentModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.StringRequestTarget;

import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.NewsItem;

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

	NewsItemDataProvider newsItemsProvider;
	NewsItemDataProvider savedNewsItemsProvider;
	NewsItemDataProvider hiddenNewsItemsProvider;
	CalendarItemDataProvider calendarItemsProvider;
	CalendarItemDataProvider expiredCalendarItemsProvider;
	CalendarItemDataProvider savedCalendarItemsProvider;
	CalendarItemDataProvider hiddenCalendarItemsProvider;
	
	public DashboardPage() {
		
		//get list of items from db, wrapped in a dataprovider
		newsItemsProvider = new NewsItemDataProvider(false, false);
		savedNewsItemsProvider = new NewsItemDataProvider(true, false);
		hiddenNewsItemsProvider = new NewsItemDataProvider(false, true);
		
		calendarItemsProvider = new CalendarItemDataProvider(true, false, false, false);
		expiredCalendarItemsProvider = new CalendarItemDataProvider(false, true, false, false);
		savedCalendarItemsProvider = new CalendarItemDataProvider(false, false, true, false);
		hiddenCalendarItemsProvider = new CalendarItemDataProvider(false, false, false, true);
		
		//present the calendar data in a table
		final DataView<CalendarItem> calendarDataView = new DataView<CalendarItem>("calendarItems", calendarItemsProvider) {

			@Override
			public void populateItem(final Item item) {
				if(item != null && item.getModelObject() != null) {
	                final CalendarItem cItem = (CalendarItem) item.getModelObject();
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                String itemType = cItem.getSourceType().getIdentifier();
	                item.add(new Label("itemType", itemType));
	                item.add(new Label("entityReference", cItem.getEntityReference()));
	                String calendarTimeLabel = dashboardLogic.getString(cItem.getCalendarTimeLabelKey(), "", itemType);
	                if(calendarTimeLabel == null) {
	                	calendarTimeLabel = "";
	                }
					item.add(new Label("calendarTimeLabel", calendarTimeLabel ));
	                item.add(new Label("calendarDate", new SimpleDateFormat(DATE_FORMAT).format(cItem.getCalendarTime())));
	                item.add(new Label("calendarTime", new SimpleDateFormat(TIME_FORMAT).format(cItem.getCalendarTime())));
	                
	                item.add(new ExternalLink("itemLink", "#", cItem.getTitle()));
	                item.add(new ExternalLink("siteLink", cItem.getContext().getContextUrl(), cItem.getContext().getContextTitle()));
	      
	                MarkupContainer actionPanel = new WebMarkupContainer("actionPanel");
	                item.add(actionPanel);
	                
	                MarkupContainer actionKeepThis = new WebMarkupContainer("actionKeepThis");
	                actionPanel.add(actionKeepThis);
	                AjaxLink<CalendarItem> actionKeepThisLink = new AjaxLink<CalendarItem>("actionKeepThisLink") {
	                	protected long calendarItemId = cItem.getId();
	                	
						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.info(target.toString());
							// need to keep one item
							logger.info(calendarItemId);
							//logger.info(this.getModelObject());
							
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean sticky = dashboardLogic.keepCalendarItem(sakaiUserId, calendarItemId);
							
							// if sticky adjust UI, else report failure?
						}
	                	
	                };
	                actionKeepThisLink.setDefaultModel(item.getModel());
	                //actionKeepThisLink.setModelObject(cItem);
	                
	                actionKeepThis.add(actionKeepThisLink);
	                actionKeepThisLink.add(new Label("actionKeepThisLabel", "Make me stay here"));
	                
	                MarkupContainer actionHideThis = new WebMarkupContainer("actionHideThis");
	                actionPanel.add(actionHideThis);
	                AjaxLink<CalendarItem> actionHideThisLink = new AjaxLink<CalendarItem>("actionHideThisLink") {
	                	protected long calendarItemId = cItem.getId();

						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.info(target.toString());
							// need to trash one item
							logger.info(calendarItemId);
							//logger.info(this.getModelObject());
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean hidden = dashboardLogic.hideCalendarItem(sakaiUserId, calendarItemId);
							
							// if hidden adjust UI, else report failure?
						}
	                	
	                };
	                actionHideThisLink.setDefaultModel(item.getModel());
	                //actionHideThisLink.setModelObject(cItem);
	                actionHideThis.add(actionHideThisLink);
	                actionHideThisLink.add(new Label("actionHideThisLabel", "Dump me in the TrAsH"));
	                
	                MarkupContainer actionHideType = new WebMarkupContainer("actionHideType");
	                actionPanel.add(actionHideType);
	                AjaxLink<CalendarItem> actionHideTypeLink = new AjaxLink<CalendarItem>("actionHideTypeLink") {
	                	long type_id = cItem.getSourceType().getId();
	                	String type_name = cItem.getSourceType().getIdentifier();
	                	
						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.info(target.toString());
							// need to trash one kind of item
							logger.info(type_id + " " + type_name);
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean completed = dashboardLogic.hideCalendarItemsBySourceType(sakaiUserId, type_id);
						}
	                	
	                };
	                actionHideTypeLink.setDefaultModel(item.getModel());
	                //actionHideTypeLink.setModelObject(cItem);
	                actionHideType.add(actionHideTypeLink);
	                actionHideTypeLink.add(new Label("actionHideTypeLabel", "Dump all " + itemType + "s in the tRaSh"));
	                
	                String siteTitle = cItem.getContext().getContextTitle();
	                MarkupContainer actionHideContext = new WebMarkupContainer("actionHideContext");
	                actionPanel.add(actionHideContext);
	                AjaxLink<CalendarItem> actionHideContextLink = new AjaxLink<CalendarItem>("actionHideContextLink") {
	                	long context_id = cItem.getContext().getId();
	                	String contextId = cItem.getContext().getContextId();
	                	
						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.info(target.toString());
							// need to trash items from one site
							logger.info(context_id + " " + contextId);
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean completed = dashboardLogic.hideCalendarItemsByContext(sakaiUserId, context_id);
						}
	                	
	                };
	                actionHideContextLink.setDefaultModel(item.getDefaultModel());
	                //actionHideContextLink.setModelObject(cItem);
	                actionHideContext.add(actionHideContextLink);
	                actionHideContextLink.add(new Label("actionHideContextLabel", "Dump everything from " + siteTitle + " in the TrAsH"));
	                
	                MarkupContainer actionHideTypeInContext = new WebMarkupContainer("actionHideTypeInContext");
	                actionPanel.add(actionHideTypeInContext);
	                AjaxLink<CalendarItem> actionHideTypeInContextLink = new AjaxLink<CalendarItem>("actionHideTypeInContextLink") {
	                	long type_id = cItem.getSourceType().getId();
	                	String type_name = cItem.getSourceType().getIdentifier();
	                	long context_id = cItem.getContext().getId();
	                	String contextId = cItem.getContext().getContextId();

						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.info(target.toString());
							// need to trash one kind of item
							logger.info(type_id + " " + type_name);
							logger.info(context_id + " " + contextId);
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean completed = dashboardLogic.hideCalendarItemsByContextSourceType(sakaiUserId, context_id, type_id);
						}
	                	
	                };
	                actionHideTypeInContextLink.setDefaultModel(item.getDefaultModel());
	                //actionHideTypeInContextLink.setModelObject(cItem);
	                actionHideTypeInContext.add(actionHideTypeInContextLink);
	                actionHideTypeInContextLink.add(new Label("actionHideTypeInContextLabel", "Pulverize all " + itemType + "s from " + siteTitle));
				}
			}
        };
        calendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        calendarDataView.setItemsPerPage(pageSize);
        add(calendarDataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("calendarNavigator", calendarDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(calendarItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
        });
        
		//present the calendar data in a table
		final DataView<CalendarItem> expiredCalendarDataView = new DataView<CalendarItem>("expiredCalendarItems", expiredCalendarItemsProvider) {

			@Override
			protected void populateItem(Item<CalendarItem> item) {
				if(item != null && item.getModelObject() != null) {
	                final CalendarItem cItem = (CalendarItem) item.getModelObject();
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                String itemType = cItem.getSourceType().getIdentifier();
	                item.add(new Label("expiredCalendarItemType", itemType));
	                item.add(new Label("expiredCalendarEntityReference", cItem.getEntityReference()));
	                String calendarTimeLabel = dashboardLogic.getString(cItem.getCalendarTimeLabelKey(), "", itemType);
	                if(calendarTimeLabel == null) {
	                	calendarTimeLabel = "";
	                }
					item.add(new Label("expiredCalendarTimeLabel", calendarTimeLabel ));
	                item.add(new Label("expiredCalendarDate", new SimpleDateFormat(DATE_FORMAT).format(cItem.getCalendarTime())));
	                item.add(new Label("expiredCalendarTime", new SimpleDateFormat(TIME_FORMAT).format(cItem.getCalendarTime())));
	                
	                item.add(new ExternalLink("expiredCalendarItemLink", "#", cItem.getTitle()));
	                item.add(new ExternalLink("expiredCalendarSiteLink", cItem.getContext().getContextUrl(), cItem.getContext().getContextTitle()));
				}
	      
			}
		};
		
		expiredCalendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
		expiredCalendarDataView.setItemsPerPage(pageSize);
		
		add(expiredCalendarDataView);
		add(new PagingNavigator("expiredCalendarNavigator", expiredCalendarDataView){
        	@Override
        	public boolean isVisible() {
        		if(expiredCalendarItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
 		});
        
		//present the calendar data in a table
		final DataView<CalendarItem> savedCalendarDataView = new DataView<CalendarItem>("savedCalendarItems", savedCalendarItemsProvider) {

			@Override
			protected void populateItem(Item<CalendarItem> item) {
				if(item != null && item.getModelObject() != null) {
	                final CalendarItem cItem = (CalendarItem) item.getModelObject();
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                String itemType = cItem.getSourceType().getIdentifier();
	                item.add(new Label("savedCalendarItemType", itemType));
	                item.add(new Label("savedCalendarEntityReference", cItem.getEntityReference()));
	                String calendarTimeLabel = dashboardLogic.getString(cItem.getCalendarTimeLabelKey(), "", itemType);
	                if(calendarTimeLabel == null) {
	                	calendarTimeLabel = "";
	                }
					item.add(new Label("savedCalendarTimeLabel", calendarTimeLabel ));
	                item.add(new Label("savedCalendarDate", new SimpleDateFormat(DATE_FORMAT).format(cItem.getCalendarTime())));
	                item.add(new Label("savedCalendarTime", new SimpleDateFormat(TIME_FORMAT).format(cItem.getCalendarTime())));
	                
	                item.add(new ExternalLink("savedCalendarItemLink", "#", cItem.getTitle()));
	                item.add(new ExternalLink("savedCalendarSiteLink", cItem.getContext().getContextUrl(), cItem.getContext().getContextTitle()));
				}
	      
			}
		};
		
		savedCalendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
		savedCalendarDataView.setItemsPerPage(pageSize);
		
		add(savedCalendarDataView);
		add(new PagingNavigator("savedCalendarNavigator", savedCalendarDataView){
        	@Override
        	public boolean isVisible() {
        		if(savedCalendarItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
 		});
        
		//present the calendar data in a table
		final DataView<CalendarItem> hiddenCalendarDataView = new DataView<CalendarItem>("hiddenCalendarItems", hiddenCalendarItemsProvider) {

			@Override
			protected void populateItem(Item<CalendarItem> item) {
				if(item != null && item.getModelObject() != null) {
	                final CalendarItem cItem = (CalendarItem) item.getModelObject();
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                String itemType = cItem.getSourceType().getIdentifier();
	                item.add(new Label("hiddenCalendarItemType", itemType));
	                item.add(new Label("hiddenCalendarEntityReference", cItem.getEntityReference()));
	                String calendarTimeLabel = dashboardLogic.getString(cItem.getCalendarTimeLabelKey(), "", itemType);
	                if(calendarTimeLabel == null) {
	                	calendarTimeLabel = "";
	                }
					item.add(new Label("hiddenCalendarTimeLabel", calendarTimeLabel ));
	                item.add(new Label("hiddenCalendarDate", new SimpleDateFormat(DATE_FORMAT).format(cItem.getCalendarTime())));
	                item.add(new Label("hiddenCalendarTime", new SimpleDateFormat(TIME_FORMAT).format(cItem.getCalendarTime())));
	                
	                item.add(new ExternalLink("hiddenCalendarItemLink", "#", cItem.getTitle()));
	                item.add(new ExternalLink("hiddenCalendarSiteLink", cItem.getContext().getContextUrl(), cItem.getContext().getContextTitle()));
				}
	      
			}
		};
		
		hiddenCalendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
		hiddenCalendarDataView.setItemsPerPage(pageSize);
		
		add(hiddenCalendarDataView);
		add(new PagingNavigator("hiddenCalendarNavigator", hiddenCalendarDataView){
        	@Override
        	public boolean isVisible() {
        		if(hiddenCalendarItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
 		});
        
		//present the news data in a table
		final DataView<NewsItem> newsDataView = new DataView<NewsItem>("newsItems", newsItemsProvider) {

			@Override
			public void populateItem(final Item item) {
                final NewsItem nItem = (NewsItem) item.getModelObject();
                if(logger.isDebugEnabled()) {
                	logger.debug(this + "populateItem()  item: " + item);
                }
                
                String itemType = nItem.getSourceType().getIdentifier();
                item.add(new Label("itemType", itemType));
                item.add(new Label("entityReference", nItem.getEntityReference()));

                String siteTitle = nItem.getContext().getContextTitle();
                item.add(new ExternalLink("itemLink", "#", nItem.getTitle()));
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), siteTitle));
                item.add(new Label("newsTime", new SimpleDateFormat(DATETIME_FORMAT).format(nItem.getNewsTime())));
                
                MarkupContainer actionPanel = new WebMarkupContainer("actionPanel");
                item.add(actionPanel);
                
                MarkupContainer actionKeepThis = new WebMarkupContainer("actionKeepThis");
                actionPanel.add(actionKeepThis);
                AjaxLink<NewsItem> actionKeepThisLink = new AjaxLink<NewsItem>("actionKeepThisLink") {
                	protected long newsItemId = nItem.getId();
                	
					@Override
					public void onClick(AjaxRequestTarget target) {
						logger.info(target.toString());
						// need to keep one item
						
						String sakaiUserId = sakaiProxy.getCurrentUserId();
						boolean sticky = dashboardLogic.keepNewsItem(sakaiUserId, newsItemId);
						
						// if sticky adjust UI, else report failure?
					}
                	
                };
                actionKeepThisLink.setDefaultModel(item.getModel());
                //actionKeepThisLink.setModelObject(nItem);
                
                actionKeepThis.add(actionKeepThisLink);
                actionKeepThisLink.add(new Label("actionKeepThisLabel", "Make me stay here"));
                
                MarkupContainer actionHideThis = new WebMarkupContainer("actionHideThis");
                actionPanel.add(actionHideThis);
                AjaxLink<NewsItem> actionHideThisLink = new AjaxLink<NewsItem>("actionHideThisLink") {
                	protected long newsItemId = nItem.getId();

					@Override
					public void onClick(AjaxRequestTarget target) {
						logger.info(target.toString());
						// need to trash one item
						
						String sakaiUserId = sakaiProxy.getCurrentUserId();
						boolean hidden = dashboardLogic.hideNewsItem(sakaiUserId, newsItemId);
						
						// if hidden adjust UI, else report failure?
						
					}
                	
                };
                actionHideThisLink.setDefaultModel(item.getModel());
                //actionHideThisLink.setModelObject(nItem);
                actionHideThis.add(actionHideThisLink);
                actionHideThisLink.add(new Label("actionHideThisLabel", "Dump me in the TrAsH"));
                
                MarkupContainer actionHideType = new WebMarkupContainer("actionHideType");
                actionPanel.add(actionHideType);
                AjaxLink<NewsItem> actionHideTypeLink = new AjaxLink<NewsItem>("actionHideTypeLink") {
                	long sourceTypeId = nItem.getSourceType().getId();
                	String sourceTypeName = nItem.getSourceType().getIdentifier();

					@Override
					public void onClick(AjaxRequestTarget target) {
						logger.info(target.toString());
						// need to trash one kind of item
						
						String sakaiUserId = sakaiProxy.getCurrentUserId();
						boolean hidden = dashboardLogic.hideNewsItemsBySourceType(sakaiUserId, sourceTypeId);
					}
                	
                };
                actionHideTypeLink.setDefaultModel(item.getModel());
                //actionHideTypeLink.setModelObject(nItem);
                actionHideType.add(actionHideTypeLink);
                actionHideTypeLink.add(new Label("actionHideTypeLabel", "Dump all " + itemType + "s in the tRaSh"));
                
                MarkupContainer actionHideContext = new WebMarkupContainer("actionHideContext");
                actionPanel.add(actionHideContext);
                AjaxLink<NewsItem> actionHideContextLink = new AjaxLink<NewsItem>("actionHideContextLink") {
                	long context_id = nItem.getContext().getId();
                	String contextId = nItem.getContext().getContextId();
                	
					@Override
					public void onClick(AjaxRequestTarget target) {
						logger.info(target.toString());
						// need to trash items from one site
						String sakaiUserId = sakaiProxy.getCurrentUserId();
						boolean completed = dashboardLogic.hideNewsItemsByContext(sakaiUserId, context_id);
					}
                	
                };
                actionHideContextLink.setDefaultModel(item.getModel());
                //actionHideContextLink.setDefaultModel(new ComponentModel<Item>());
                //actionHideContextLink.setModelObject(nItem);
                actionHideContext.add(actionHideContextLink);
                actionHideContextLink.add(new Label("actionHideContextLabel", "Dump everything from " + siteTitle + " in the TrAsH"));
                
                MarkupContainer actionHideTypeInContext = new WebMarkupContainer("actionHideTypeInContext");
                actionPanel.add(actionHideTypeInContext);
                AjaxLink<NewsItem> actionHideTypeInContextLink = new AjaxLink<NewsItem>("actionHideTypeInContextLink") {
                	long type_id = nItem.getSourceType().getId();
                	String type_name = nItem.getSourceType().getIdentifier();
                	long context_id = nItem.getContext().getId();
                	String contextId = nItem.getContext().getContextId();

					@Override
					public void onClick(AjaxRequestTarget target) {
						logger.info(target.toString());
						// need to trash one kind of item in one site
						String sakaiUserId = sakaiProxy.getCurrentUserId();
						boolean completed = dashboardLogic.hideNewsItemsByContextSourceType(sakaiUserId, context_id, type_id);
					}
                	
                };
                actionHideTypeInContextLink.setDefaultModel(item.getModel());
                //actionHideTypeInContextLink.setDefaultModel(new ComponentModel<Item>());
                //actionHideTypeInContextLink.setModelObject(nItem);
                actionHideTypeInContext.add(actionHideTypeInContextLink);
                actionHideTypeInContextLink.add(new Label("actionHideTypeInContextLabel", "Pulverize all " + itemType + "s from " + siteTitle));

            }
        };
        
        
        
        newsDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        newsDataView.setItemsPerPage(pageSize);
        add(newsDataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("newsNavigator", newsDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(newsItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
        });
        
		final DataView<NewsItem> savedNewsDataView = new DataView<NewsItem>("savedNewsItems", savedNewsItemsProvider) {

			@Override
			public void populateItem(final Item item) {
                final NewsItem nItem = (NewsItem) item.getModelObject();
                if(logger.isDebugEnabled()) {
                	logger.debug(this + "populateItem()  item: " + item);
                }
                
                String itemType = nItem.getSourceType().getIdentifier();
                item.add(new Label("itemType", itemType));
                item.add(new Label("entityReference", nItem.getEntityReference()));

                String siteTitle = nItem.getContext().getContextTitle();
                item.add(new ExternalLink("itemLink", "#", nItem.getTitle()));
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), siteTitle));
                item.add(new Label("newsTime", new SimpleDateFormat(DATETIME_FORMAT).format(nItem.getNewsTime())));
                
			}
		};

        savedNewsDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        savedNewsDataView.setItemsPerPage(pageSize);
        add(savedNewsDataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("savedNewsNavigator", savedNewsDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(savedNewsItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
        });
        
		final DataView<NewsItem> hiddenNewsDataView = new DataView<NewsItem>("hiddenNewsItems", hiddenNewsItemsProvider) {

			@Override
			public void populateItem(final Item item) {
                final NewsItem nItem = (NewsItem) item.getModelObject();
                if(logger.isDebugEnabled()) {
                	logger.debug(this + "populateItem()  item: " + item);
                }
                
                String itemType = nItem.getSourceType().getIdentifier();
                item.add(new Label("itemType", itemType));
                item.add(new Label("entityReference", nItem.getEntityReference()));

                String siteTitle = nItem.getContext().getContextTitle();
                item.add(new ExternalLink("itemLink", "#", nItem.getTitle()));
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), siteTitle));
                item.add(new Label("newsTime", new SimpleDateFormat(DATETIME_FORMAT).format(nItem.getNewsTime())));
                
			}
		};

        hiddenNewsDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        hiddenNewsDataView.setItemsPerPage(pageSize);
        add(hiddenNewsDataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("hiddenNewsNavigator", hiddenNewsDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(hiddenNewsItemsProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		super.onBeforeRender();
        		
        		//clear the feedback panel messages
        		clearFeedback(feedbackPanel);
        	}
        });

        AbstractAjaxBehavior entityDetailRequest = new AbstractAjaxBehavior() {

			public void onRequest() {
				//get parameters
                final RequestCycle requestCycle = RequestCycle.get();

                WebRequest wr=(WebRequest)requestCycle.getRequest();

                HttpServletRequest hsr = wr.getHttpServletRequest();
                
                String entityReference = null;
                String entityType = null;
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

                   }
                   

                } catch (IOException ex) {
                    logger.error(ex);
                }

                Locale locale = hsr.getLocale();
 				if(entityReference != null && ! entityReference.trim().equals("") && entityType != null && ! entityType.trim().equals("")) {
	                Map<String,Object> entityMap = dashboardLogic.getEntityMapping(entityType, entityReference, locale);
	                
	                String jsonString = getJsonStringFromMap(entityMap);
	                logger.debug("Returning JSON:\n" + jsonString);
	                IRequestTarget t = new StringRequestTarget("application/json", "UTF-8", jsonString);
	                getRequestCycle().setRequestTarget(t);
	 			}
			}
        };
        add(entityDetailRequest);
        add(new Label("callbackUrl", entityDetailRequest.getCallbackUrl().toString()));
	}
		
	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class CalendarItemDataProvider implements IDataProvider<CalendarItem> {
	   
		private List<CalendarItem> calendarItems;
		private boolean showFuture = true;
		private boolean showPast = false;
		private boolean saved = false;
		private boolean hidden = false;
		
		public CalendarItemDataProvider() {
			super();
		}
		
		public CalendarItemDataProvider(boolean showFuture, boolean showPast, boolean saved, boolean hidden) {
			super();
			this.showFuture = showFuture;
			this.showPast = showPast;
			this.saved = saved;
			this.hidden = hidden;
		}
		
		private List<CalendarItem> getData() {
			if(calendarItems == null) {
				String siteId = sakaiProxy.getCurrentSiteId();
				String sakaiId = sakaiProxy.getCurrentUserId();
				if(siteId == null || sakaiId == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("CalendarItemDataProvider.getData() siteId:" + siteId + "  sakaiId:" + sakaiId);
					}
					return new ArrayList<CalendarItem>();
				}
				if(sakaiProxy.isWorksite(siteId)) {
					calendarItems = dashboardLogic.getCalendarItems(sakaiId, showFuture, showPast, saved, hidden);
				} else {
					calendarItems = dashboardLogic.getCalendarItems(sakaiId, siteId, showFuture, showPast, saved, hidden);
				}
			}
			if(calendarItems == null) {
				logger.warn("Error getting calendarItems");
				return new ArrayList<CalendarItem>();
			}
			return calendarItems;
		}
		
		public Iterator<CalendarItem> iterator(int first, int count){
			return getData().subList(first, first + count).iterator();
		}
		
		public int size(){
			return getData().size();
		}
		
		public IModel<CalendarItem> model(CalendarItem object){
			return new DetachableCalendarItemModel(object);
		}

		public void detach(){
			calendarItems = null;
		}
	}

	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class NewsItemDataProvider implements IDataProvider<NewsItem> {
	   
		private List<NewsItem> newsItems;
		private boolean saved = false;
		private boolean hidden = false;
		
		public NewsItemDataProvider() {
			super();
		}
		
		public NewsItemDataProvider(boolean saved, boolean hidden) {
			super();
			this.saved = saved;
			this.hidden = hidden;
		}
		
		private List<NewsItem> getData() {
			if(newsItems == null) {
				String siteId = sakaiProxy.getCurrentSiteId();
				String sakaiId = sakaiProxy.getCurrentUserId();
				if(siteId == null || sakaiId == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("NewsItemDataProvider.getData() siteId:" + siteId + "  sakaiId:" + sakaiId);
					}
					return new ArrayList<NewsItem>();
				}
				if(sakaiProxy.isWorksite(siteId)) {
					newsItems = dashboardLogic.getNewsItems(sakaiId, saved, hidden);
				} else {
					newsItems = dashboardLogic.getNewsItems(sakaiId, siteId, saved, hidden);
				}
			}
			if(newsItems == null) {
				logger.warn("Error getting news items");
				return new ArrayList<NewsItem>();
			}
			return newsItems;
		}
		
		public Iterator<NewsItem> iterator(int first, int count){
			return getData().subList(first, first + count).iterator();
		}
		
		public int size(){
			return getData().size();
		}
		
		public IModel<NewsItem> model(NewsItem object){
			return new DetachableNewsItemModel(object);
		}

		public void detach(){
			newsItems = null;
		}
	}

	/**
	 * Detachable model to wrap a CalendarItem
	 * 
	 */
	private class DetachableCalendarItemModel extends LoadableDetachableModel<CalendarItem>{

		private Long id = null;
		
		/**
		 * @param m
		 */
		public DetachableCalendarItemModel(CalendarItem t){
			this.id = t.getId();
		}
		
		/**
		 * @param id
		 */
		public DetachableCalendarItemModel(long id){
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
			else if (obj instanceof DetachableCalendarItemModel) {
				DetachableCalendarItemModel other = (DetachableCalendarItemModel)obj;
				return other.id == id;
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected CalendarItem load(){
			
			// get the calendar item
			return dashboardLogic.getCalendarItem(id);
		}
	}

	/**
	 * Detachable model to wrap a NewsItem
	 * 
	 */
	private class DetachableNewsItemModel extends LoadableDetachableModel<NewsItem>{

		private Long id = null;
		
		/**
		 * @param m
		 */
		public DetachableNewsItemModel(NewsItem t){
			this.id = t.getId();
		}
		
		/**
		 * @param id
		 */
		public DetachableNewsItemModel(long id){
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
			else if (obj instanceof DetachableNewsItemModel) {
				DetachableNewsItemModel other = (DetachableNewsItemModel)obj;
				return other.id == id;
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected NewsItem load(){
			
			// get the news item
			return dashboardLogic.getNewsItem(id);
		}
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
				}
				
			}
		}
		return json;
	}


}
