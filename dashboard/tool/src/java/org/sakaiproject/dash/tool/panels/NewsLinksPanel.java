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
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.NewsLink;
import org.sakaiproject.dash.tool.util.JsonHelper;
import org.sakaiproject.dash.util.DateUtil;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

/**
 * 
 *
 */
public class NewsLinksPanel extends Panel {

	public static final String TAB_ID_CURRENT = "current";
	public static final String TAB_ID_STARRED = "starred";
	public static final String TAB_ID_HIDDEN = "hidden";

	private static final Logger logger = LoggerFactory.getLogger(NewsLinksPanel.class);
	
	protected static final String DATE_FORMAT = "dd-MMM-yyyy";
	protected static final String TIME_FORMAT = "HH:mm";
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	
	@SpringBean(name="org.sakaiproject.dash.app.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.dash.app.DashboardCommonLogic")
	protected DashboardCommonLogic dashboardCommonLogic;

	@SpringBean(name="org.sakaiproject.dash.app.DashboardConfig")
	protected DashboardConfig dashboardConfig;

	protected NewsLinksDataProvider newsLinksProvider = null;
	protected String selectedNewsTab;
	protected String newsLinksDivId = null;
	protected String newsLinksCountId = null;
	//protected int pageSize = 5;
	protected int pageSize = dashboardConfig.getConfigValue(DashboardConfig.PROP_DEFAULT_ITEMS_IN_PANEL, 5);

	public NewsLinksPanel(String id) {
		super(id);
		
		initPanel();
	}
	
	public NewsLinksPanel(String id, String selectedTab) {
		super(id);
		
		this.selectedNewsTab = selectedTab;
		
		initPanel();
	}
		

	protected void initPanel() {
		
		if(this.selectedNewsTab == null) {
			this.selectedNewsTab = TAB_ID_CURRENT;
		}
		
		if(this.newsLinksDivId != null) {
			this.remove(newsLinksDivId );
		}
		
		ResourceLoader rl = new ResourceLoader("dash_entity");
		
		//get list of items from db, wrapped in a dataprovider
		newsLinksProvider = new NewsLinksDataProvider(this.selectedNewsTab);
		
        final WebMarkupContainer newsLinksDiv = new WebMarkupContainer("newsLinksDiv");
        newsLinksDiv.setOutputMarkupId(true);
        add(newsLinksDiv);
        this.newsLinksDivId = newsLinksDiv.getId();
        
        newsLinksDiv.add(new Label("newsTitle", rl.getString("dash.news.title")));
        
        AjaxLink<IModel<List<NewsLink>>> currentNewsLink = new AjaxLink<IModel<List<NewsLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if(logger.isDebugEnabled()) {
					logger.debug("currentNewsLink onClick called");
				}
				// set currentNewsTab to "current"
				selectedNewsTab = TAB_ID_CURRENT;
				// reset news dataview to show current stuff
		        if(newsLinksProvider == null) {
		        	newsLinksProvider = new NewsLinksDataProvider(selectedNewsTab);
		        } else {
		        	newsLinksProvider.setNewsTab(selectedNewsTab);
					dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/news/" + selectedNewsTab);
		        }
				initPanel();
				
				// refresh newsLinksDiv
				target.addComponent(NewsLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#newsPanel').focus();");
			}
        	
        };
        
        currentNewsLink.add(new Label("label", rl.getString("dash.news.current")));
		WebMarkupContainer currentNewsTab = new WebMarkupContainer("currentNewsTab");
		if(selectedNewsTab == null || TAB_ID_CURRENT.equalsIgnoreCase(selectedNewsTab)) {
			currentNewsTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		currentNewsTab.add(currentNewsLink);
		newsLinksDiv.add(currentNewsTab);

        AjaxLink<IModel<List<NewsLink>>> starredNewsLink = new AjaxLink<IModel<List<NewsLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if(logger.isDebugEnabled()) {
					logger.debug("starredNewsLink onClick called");
				}
				// set currentNewsTab to "starred"
				selectedNewsTab = TAB_ID_STARRED;
				// reset news dataview to show starred stuff
		        if(newsLinksProvider == null) {
		        	newsLinksProvider = new NewsLinksDataProvider(selectedNewsTab);
		        } else {
		        	newsLinksProvider.setNewsTab(selectedNewsTab);
					dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/news/" + selectedNewsTab);
		        }
				initPanel();
				
				// refresh newsLinksDiv
				target.addComponent(NewsLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#newsPanel').focus();");
			}
        	
        };
        starredNewsLink.add(new Label("label", rl.getString("dash.news.starred")));
		WebMarkupContainer starredNewsTab = new WebMarkupContainer("starredNewsTab");
		if(selectedNewsTab != null && TAB_ID_STARRED.equalsIgnoreCase(selectedNewsTab)) {
			starredNewsTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		starredNewsTab.add(starredNewsLink);
		newsLinksDiv.add(starredNewsTab);

        AjaxLink<IModel<List<NewsLink>>> hiddenNewsLink = new AjaxLink<IModel<List<NewsLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				if(logger.isDebugEnabled()) {
					logger.debug("hiddenNewsLink onClick called");
				}
				// set currentNewsTab to "hidden"
				selectedNewsTab = TAB_ID_HIDDEN;
				// reset news dataview to show hidden stuff
		        if(newsLinksProvider == null) {
		        	newsLinksProvider = new NewsLinksDataProvider(selectedNewsTab);
		        } else {
		        	newsLinksProvider.setNewsTab(selectedNewsTab);
					dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_TABBING, "/dashboard/news/" + selectedNewsTab);
		        }
				initPanel();
				
				// refresh newsLinksDiv
				target.addComponent(NewsLinksPanel.this);
				target.appendJavascript("resizeFrame('grow');");
				target.appendJavascript("$('#newsPanel').focus();");				
			}
        	
        };
        hiddenNewsLink.add(new Label("label", rl.getString("dash.news.hidden")));
		WebMarkupContainer hiddenNewsTab = new WebMarkupContainer("hiddenNewsTab");
		if(selectedNewsTab != null && TAB_ID_HIDDEN.equalsIgnoreCase(selectedNewsTab)) {
			hiddenNewsTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		hiddenNewsTab.add(hiddenNewsLink);
		newsLinksDiv.add(hiddenNewsTab);
             
        WebMarkupContainer haveLinks = new WebMarkupContainer("haveLinks");
        newsLinksDiv.add(haveLinks);
        
		//present the news data in a table
		final DataView<NewsLink> newsDataView = new DataView<NewsLink>("newsLinks", newsLinksProvider) {

			@Override
			public void populateItem(final Item item) {
				item.setOutputMarkupId(true);
				ResourceLoader rl = new ResourceLoader("dash_entity");
				
				final NewsLink nLink = (NewsLink) item.getModelObject();
                final NewsItem nItem = nLink.getNewsItem();
                
                if(logger.isDebugEnabled()) {
                	logger.debug(this + "populateItem()  item: " + item);
                }
                
                boolean hideActionLinks = nItem.getItemCount() > 1;
                
                String itemType = nItem.getSourceType().getIdentifier();
                item.add(new Label("itemType", itemType));
                item.add(new Label("itemCount", Integer.toString(nItem.getItemCount())));
                item.add(new Label("entityReference", nItem.getEntityReference()));

                String siteTitle = nItem.getContext().getContextTitle();
				StringBuilder errorMessages = new StringBuilder();
				String title = FormattedText.processFormattedText(nItem.getTitle(), errorMessages  , true, true);
				if(errorMessages != null && errorMessages.length() > 0) {
					logger.warn("Error(s) encountered while processing newsItem title:\n" + errorMessages);
				}
				
				ExternalLink itemLink = new ExternalLink("itemLink", "#itemEvent");
				itemLink.add(new Label("itemTitle",title));
				itemLink.add(new Label("itemClick",rl.getString("dash.details")));
				item.add(itemLink);
                
                Image icon = new Image("icon");
                icon.add(new AttributeModifier("src", true, new AbstractReadOnlyModel(){

					@Override
					public Object getObject() {
						// TODO Auto-generated method stub
						return dashboardCommonLogic.getEntityIconUrl(nItem.getSourceType().getIdentifier(), nItem.getSubtype());
					}
                	
                }));
                item.add(icon);
                String newsItemLabel = dashboardCommonLogic.getString(nItem.getNewsTimeLabelKey(), "", itemType);
                if(newsItemLabel == null || hideActionLinks) {
                	newsItemLabel = "";
                }
                Label itemLabel = new Label("itemLabel", newsItemLabel);
                if(! "".equals(newsItemLabel)) {
                	itemLabel.add(new SimpleAttributeModifier("class", "itemLabel"));
                }
				item.add(itemLabel);
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), siteTitle));
                Component timeLabel = new Label("newsTime", DateUtil.getNewsTimeString(nItem.getNewsTime()));
                timeLabel.add(new AttributeModifier("title", true, new AbstractReadOnlyModel(){

					@Override
					public Object getObject() {
						// TODO Auto-generated method stub
						return DateUtil.getFullDateString(nItem.getNewsTime());
					}
                	
                }));
				item.add(timeLabel );
                
                if(nLink.isSticky()) {
	                AjaxLink<NewsLink> starringAction = new AjaxLink<NewsLink>("starringAction") {
	                	protected long newsItemId = nItem.getId();
	                	protected Component thisRow = item;
	                	
						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.debug("starringAction onClick() called -- unstar ");
							// need to keep one item
							logger.debug(Long.toString(newsItemId));
							//logger.debug(this.getModelObject());
							
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean success = dashboardCommonLogic.unkeepNewsItem(sakaiUserId, newsItemId);
							dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_UNSTAR, "/dashboard/news/" + selectedNewsTab + "/" + newsItemId);
							
							// if success adjust UI, else report failure?
							if(success) {
								target.addComponent(NewsLinksPanel.this);
								if(TAB_ID_STARRED.equals(selectedNewsTab)) {
									ResourceLoader rl = new ResourceLoader("dash_entity");
									NewsItem changedItem = dashboardCommonLogic.getNewsItem(newsItemId);
									JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
									String jsonStr = jsonHelper.getJsonObjectFromNewsItem(changedItem).toString();
									String javascript = "reportSuccess('" + rl.getString("dash.ajax.unstar.success") + "'," + jsonStr + ",'" + "not-sure-about-url-yet" + "');";
									target.appendJavascript(javascript );
								}
								target.appendJavascript("resizeFrame('grow');");
								target.appendJavascript("$('#newsPanel').focus();");
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
	                
	                if(nLink.isHidden() || hideActionLinks) {
	                	starringAction.setVisible(false);
	                	starringAction.setVisibilityAllowed(false);
	                }
                } else {
	                AjaxLink<NewsLink> starringAction = new AjaxLink<NewsLink>("starringAction") {
	                	protected long newsItemId = nItem.getId();
	                	protected Component thisRow = item;
	                	
						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.debug("starringAction onClick() called -- star ");
							// need to keep one item
							logger.debug(Long.toString(newsItemId));
							//logger.debug(this.getModelObject());
							
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean success = dashboardCommonLogic.keepNewsItem(sakaiUserId, newsItemId);
							dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_STAR, "/dashboard/news/" + selectedNewsTab + "/" + newsItemId);
							
							// if success adjust UI, else report failure?
							if(success) {
								target.addComponent(NewsLinksPanel.this);
								//String javascript = "alert('success. (" + thisRow.getMarkupId() + ")');";
								//target.appendJavascript(javascript );
							}
							target.appendJavascript("resizeFrame('grow');");
							target.appendJavascript("$('#newsPanel').focus();");
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
	                // starringAction.add(new Label("starringActionLabel", "Star"));
	                item.add(starringAction);

	                if(nLink.isHidden() || hideActionLinks) {
	                	starringAction.setVisible(false);
	                	starringAction.setVisibilityAllowed(false);
	                }
                }
                
                if(nLink.isHidden()) {
	                AjaxLink<NewsLink> hidingAction = new AjaxLink<NewsLink>("hidingAction") {
	                	protected long newsItemId = nItem.getId();
	                	protected Component thisRow = item;

						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.debug("hidingAction onClick() called -- show");
							// need to trash one item
							logger.debug(Long.toString(newsItemId));
							//logger.debug(this.getModelObject());
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean success = dashboardCommonLogic.unhideNewsItem(sakaiUserId, newsItemId);
							dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_SHOW, "/dashboard/news/" + selectedNewsTab + "/" + newsItemId);
							
							// if success adjust UI, else report failure?
							if(success) {
								ResourceLoader rl = new ResourceLoader("dash_entity");
								target.addComponent(NewsLinksPanel.this);
								NewsItem changedItem = dashboardCommonLogic.getNewsItem(newsItemId);
								JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
								String jsonStr = jsonHelper.getJsonObjectFromNewsItem(changedItem).toString();
								String javascript = "reportSuccess('" + rl.getString("dash.ajax.show.success") + "'," + jsonStr + ",'" + "not-sure-about-url-yet" + "');";
								target.appendJavascript(javascript );
							}
							target.appendJavascript("resizeFrame('grow');");
							target.appendJavascript("$('#newsPanel').focus();");
						}
	                	
	                };
	                hidingAction.setDefaultModel(item.getModel());
	                //actionHideThisLink.setModelObject(nItem);
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
	                
	                if(nLink.isSticky() || hideActionLinks) {
	                	hidingAction.setVisible(false);
	                	hidingAction.setVisibilityAllowed(false);
	                } 
                	
                } else {
	                AjaxLink<NewsLink> hidingAction = new AjaxLink<NewsLink>("hidingAction") {
	                	protected long newsItemId = nItem.getId();
	                	protected Component thisRow = item;

						@Override
						public void onClick(AjaxRequestTarget target) {
							logger.debug("hidingAction onClick() called -- hide");
							// need to trash one item
							logger.debug(Long.toString(newsItemId));
							//logger.debug(this.getModelObject());
							String sakaiUserId = sakaiProxy.getCurrentUserId();
							boolean success = dashboardCommonLogic.hideNewsItem(sakaiUserId, newsItemId);
							dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_HIDE, "/dashboard/news/" + selectedNewsTab + "/" + newsItemId);
							
							// if success adjust UI, else report failure?
							if(success) {
								ResourceLoader rl = new ResourceLoader("dash_entity");
								target.addComponent(NewsLinksPanel.this);
								NewsItem changedItem = dashboardCommonLogic.getNewsItem(newsItemId);
								JsonHelper jsonHelper = new JsonHelper(dashboardCommonLogic, dashboardConfig);
								String jsonStr = jsonHelper.getJsonObjectFromNewsItem(changedItem).toString();
								String javascript = "reportSuccess('" + rl.getString("dash.ajax.hide.success") + "'," + jsonStr + ",'" + "not-sure-about-url-yet" + "');";
								target.appendJavascript(javascript );
							}
							target.appendJavascript("resizeFrame('grow');");
							target.appendJavascript("$('#newsPanel').focus();");
						}
	                	
	                };
	                hidingAction.setDefaultModel(item.getModel());
	                
	                //actionHideThisLink.setModelObject(nItem);
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
	                
	                if(nLink.isSticky() || hideActionLinks) {
		                hidingAction.setVisible(false);
	                	hidingAction.setVisibilityAllowed(false);
	                } 
                	
                }
			}
        };
        
        
        
        newsDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        newsDataView.setItemsPerPage(pageSize);
        haveLinks.add(newsDataView);

        IPagingLabelProvider pagingLabelProvider = new IPagingLabelProvider() {

			public String getPageLabel(int page) {
		        ResourceLoader rl = new ResourceLoader("dash_entity");
				
				int itemCount = 0;
				String pagerStatus = "";
				if(newsDataView != null) {
				    int first = 0;
				    int last = 0;
					itemCount = newsDataView.getItemCount();
				    int pageSize = newsDataView.getItemsPerPage();
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
        newsLinksDiv.add(new PagingNavigator("newsNavigator", newsDataView, pagingLabelProvider) {
        	
        	protected int currentPage = 1;
        	
        	@Override
        	public boolean isVisible() {
        		if(newsLinksProvider.size() > pageSize) {
        			return true;
        		}
        		return false;
        	}
        	
        	@Override
        	public void onBeforeRender() {
        		
        		if(this.getPageable().getCurrentPage() != currentPage) {
    				dashboardCommonLogic.recordDashboardActivity(DashboardCommonLogic.EVENT_DASH_PAGING, "/dashboard/news/" + selectedNewsTab);
    				currentPage = this.getPageable().getCurrentPage();
        		}
        		
                //renderItemCounter(newsLinksDiv, newsDataView);

        		//clear the feedback panel messages
        		//clearFeedback(feedbackPanel);
        		super.onBeforeRender();
        	}
        	
        });
        
        WebMarkupContainer haveNoLinks = new WebMarkupContainer("haveNoLinks");
        newsLinksDiv.add(haveNoLinks);
        
        String noNewsLinksLabel = null;
		if(TAB_ID_CURRENT.equals(selectedNewsTab)) {
			noNewsLinksLabel = rl.getString("dash.news.nocurrent");
		} else if(TAB_ID_STARRED.equals(selectedNewsTab)) {
			noNewsLinksLabel = rl.getString("dash.news.nostarred");
		} else if(TAB_ID_HIDDEN.equals(selectedNewsTab)) {
			noNewsLinksLabel = rl.getString("dash.news.nohidden");
		}
        haveNoLinks.add(new Label("message", noNewsLinksLabel));
        
        //renderItemCounter(newsLinksDiv, newsDataView);
        int itemCount = 0;
        if(newsDataView != null) {
        	itemCount = newsDataView.getItemCount();
        }
       
        if(itemCount > 0) {
        	// show the haveLinks
        	haveLinks.setVisible(true);
        	// hide the noNewsLinksDiv
        	haveNoLinks.setVisible(false);
        } else {
        	// show the noNewsLinksDiv
        	haveNoLinks.setVisible(true);
        	// hide the haveLinks
        	haveLinks.setVisible(false);
        }
        
	}
	
	/**
	 * @param rl
	 * @param newsLinksDiv
	 * @param newsDataView
	 */
	protected void renderItemCounter(
			final WebMarkupContainer newsLinksDiv,
			final DataView<NewsLink> newsDataView) {
		
		ResourceLoader rl = new ResourceLoader("dash_entity");
				
		if(newsLinksCountId != null) {
			Iterator itx = newsLinksDiv.iterator();
			while(itx.hasNext()) {
				Component child = (Component) itx.next();
				if(newsLinksCountId.equals(child.getId())) {
					newsLinksDiv.remove(child);
					break;
				}
			}
		}

		int itemCount = 0;
		String pagerStatus = "";
		if(newsDataView != null) {
		    int first = 0;
		    int last = 0;
			itemCount = newsDataView.getItemCount();
		    int pageSize = newsDataView.getItemsPerPage();
			if(itemCount > pageSize) {
				int page = newsDataView.getCurrentPage();
				first = page * pageSize + 1;
				last = Math.min(itemCount, (page + 1) * pageSize);
				if(first == last) {
		    		pagerStatus = rl.getFormattedMessage("dash.news.linksCount2", new Object[]{new Integer(first), new Integer(itemCount)});
				} else {
		    		pagerStatus = rl.getFormattedMessage("dash.news.linksCount3", new Object[]{new Integer(first), new Integer(last), new Integer(itemCount)});
				}
			} else if(itemCount > 1) {
				pagerStatus = rl.getFormattedMessage("dash.news.linksCount3", new Object[]{new Integer(1), new Integer(itemCount), new Integer(itemCount)});
			} else if(itemCount > 0) {
				pagerStatus = rl.getString("dash.news.linksCount1");
			} else {
				pagerStatus = rl.getString("dash.news.linksCount0");
			}
		} 
		Label newsLinksCount = new Label("newsLinksCount", pagerStatus);
		newsLinksCount.setOutputMarkupId(true);
		// add the count to the newsLinksDiv
		newsLinksDiv.add(newsLinksCount);
		
		newsLinksCountId = newsLinksCount.getId();
	}

	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class NewsLinksDataProvider implements IDataProvider<NewsLink> {
	   
		protected List<NewsLink> newsLinks;
		protected String newsTabId = null;
		
		public NewsLinksDataProvider() {
			super();
		}
		
		public NewsLinksDataProvider(String selectedTab) {
			super();
			this.newsTabId = selectedTab;
			if(this.newsTabId == null) {
				this.newsTabId = TAB_ID_CURRENT;
			}
		}
		
		public void setNewsTab(String newsTabId) {
			if(this.newsTabId == null || ! this.newsTabId.equals(newsTabId)) {
				// force refresh of dataProvider
				this.newsLinks = null;
			}
			this.newsTabId = newsTabId;
			
		}

		private List<NewsLink> getData() {
			if(newsLinks == null) {
				String siteId = sakaiProxy.getCurrentSiteId();
				String sakaiId = sakaiProxy.getCurrentUserId();
				if(siteId == null || sakaiId == null) {
					if(logger.isDebugEnabled()) {
						logger.debug("NewsLinksDataProvider.getData() siteId:" + siteId + "  sakaiId:" + sakaiId);
					}
					return new ArrayList<NewsLink>();
				}
				if(sakaiProxy.isWorksite(siteId)) {
					if(TAB_ID_CURRENT.equals(newsTabId)) {
						newsLinks = dashboardCommonLogic.getCurrentNewsLinks(sakaiId, null);
					} else if(TAB_ID_STARRED.equals(newsTabId)) {
						newsLinks = dashboardCommonLogic.getStarredNewsLinks(sakaiId, null);
					} else if(TAB_ID_HIDDEN.equals(newsTabId)) {
						newsLinks = dashboardCommonLogic.getHiddenNewsLinks(sakaiId, null);
					}
				} else {
					if(TAB_ID_CURRENT.equals(newsTabId)) {
						newsLinks = dashboardCommonLogic.getCurrentNewsLinks(sakaiId, siteId);
					} else if(TAB_ID_STARRED.equals(newsTabId)) {
						newsLinks = dashboardCommonLogic.getStarredNewsLinks(sakaiId, siteId);
					} else if(TAB_ID_HIDDEN.equals(newsTabId)) {
						newsLinks = dashboardCommonLogic.getHiddenNewsLinks(sakaiId, siteId);
					}
				}
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("NewsLinksDataProvider selectedNewsTab=" + this.newsTabId + " newsLinks=" + newsLinks);
			}
			if(newsLinks == null) {
				logger.warn("Error getting news items");
				return new ArrayList<NewsLink>();
			}
			return newsLinks;
		}
		
		public Iterator<NewsLink> iterator(int first, int count){
			return getData().subList(first, first + count).iterator();
		}
		
		public int size(){
			return getData().size();
		}
		
		public IModel<NewsLink> model(NewsLink object){
			return new DetachableNewsLinkModel(object);
		}

		public void detach(){
			newsLinks = null;
		}
	}

	/**
	 * Detachable model to wrap a NewsLink
	 * 
	 */
	private class DetachableNewsLinkModel extends LoadableDetachableModel<NewsLink>{

		private NewsLink newsLink = null;
		
		/**
		 * @param m
		 */
		public DetachableNewsLinkModel(NewsLink t){
			this.newsLink  = new NewsLink(t); 
		}
		
		/**
		 * @param id
		 */
//		public DetachableNewsLinkModel(long id){
//			this.id = id;
//		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			if(this.newsLink == null || this.newsLink.getId() == null) {
				return Long.valueOf(0L).hashCode();
			}
			return Long.valueOf(this.newsLink.getId()).hashCode();
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
			else if(this.newsLink == null) {
				return false;
			}
			else if (obj instanceof DetachableNewsLinkModel) {
				DetachableNewsLinkModel other = (DetachableNewsLinkModel)obj;
				if(other.newsLink == null) {
					return false;
				}
				return other.newsLink.getId() == this.newsLink.getId();
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected NewsLink load(){
			
			// get the news item
			return new NewsLink(this.newsLink);
		}
	}

}
