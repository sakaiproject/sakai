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

	NewsItemDataProvider newsItemsProvider;
	
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
		
		//get list of items from db, wrapped in a dataprovider
		newsItemsProvider = new NewsItemDataProvider(false, false);
		
        final WebMarkupContainer newsItemsDiv = new WebMarkupContainer("newsItemsDiv");
        newsItemsDiv.setOutputMarkupId(true);
        
        @SuppressWarnings("rawtypes")
		AjaxLink currentNewsLink = new AjaxLink("link", new PropertyModel<String>(this, "selectedNewsTab")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("currentNewsLink onClick called");
				// set currentNewsTab to "current"
				selectedNewsTab = "current";
				// reset news dataview to show current stuff
				
				// refresh newsItemsDiv
				target.addComponent(newsItemsDiv);
			}
        	
        };
        currentNewsLink.add(new Label("label", rl.getString("dash.news.current")));
		WebMarkupContainer currentNewsTab = new WebMarkupContainer("currentNewsTab");
		if(selectedNewsTab == null || "current".equalsIgnoreCase(selectedNewsTab)) {
			currentNewsTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		currentNewsTab.add(currentNewsLink);
		newsItemsDiv.add(currentNewsTab);

        @SuppressWarnings("rawtypes")
		AjaxLink starredNewsLink = new AjaxLink("link", new PropertyModel<String>(this, "selectedNewsTab")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("starredNewsLink onClick called");
				// set currentNewsTab to "starred"
				selectedNewsTab = "starred";
				// reset news dataview to show starred stuff
				
				// refresh newsItemsDiv
				target.addComponent(newsItemsDiv);
			}
        	
        };
        starredNewsLink.add(new Label("label", rl.getString("dash.news.starred")));
		WebMarkupContainer starredNewsTab = new WebMarkupContainer("starredNewsTab");
		if(selectedNewsTab != null && "starred".equalsIgnoreCase(selectedNewsTab)) {
			starredNewsTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		starredNewsTab.add(starredNewsLink);
		newsItemsDiv.add(starredNewsTab);

        @SuppressWarnings("rawtypes")
		AjaxLink hiddenNewsLink = new AjaxLink("link", new PropertyModel<String>(this, "selectedNewsTab")) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("hiddenNewsLink onClick called");
				// set currentNewsTab to "hidden"
				selectedNewsTab = "hidden";
				// reset news dataview to show hidden stuff
				
				// refresh newsItemsDiv
				target.addComponent(newsItemsDiv);
			}
        	
        };
        hiddenNewsLink.add(new Label("label", rl.getString("dash.news.hidden")));
		WebMarkupContainer hiddenNewsTab = new WebMarkupContainer("hiddenNewsTab");
		if(selectedNewsTab != null && "hidden".equalsIgnoreCase(selectedNewsTab)) {
			hiddenNewsTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		hiddenNewsTab.add(hiddenNewsLink);
		newsItemsDiv.add(hiddenNewsTab);
                
		//present the news data in a table
		final DataView<NewsItem> newsDataView = new DataView<NewsItem>("newsItems", newsItemsProvider) {

			@Override
			public void populateItem(final Item item) {
                final NewsItem nItem = (NewsItem) item.getModelObject();
                if(logger.isDebugEnabled()) {
                	logger.debug(this + "populateItem()  item: " + item);
                }
                
                logger.info("populateItem() " + nItem);
                
                String itemType = nItem.getSourceType().getIdentifier();
                item.add(new Label("itemType", itemType));
                item.add(new Label("itemCount", Integer.toString(nItem.getItemCount())));
                item.add(new Label("entityReference", nItem.getEntityReference()));

                String siteTitle = nItem.getContext().getContextTitle();
                item.add(new ExternalLink("itemLink", "#", nItem.getTitle()));
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), siteTitle));
                item.add(new Label("newsTime", new SimpleDateFormat(DATETIME_FORMAT).format(nItem.getNewsTime())));
                
                WebMarkupContainer actionPanel = new WebMarkupContainer("actionPanel");
                item.add(actionPanel);
                
                WebMarkupContainer actionKeepThis = new WebMarkupContainer("actionKeepThis");
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
                
                WebMarkupContainer actionHideThis = new WebMarkupContainer("actionHideThis");
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
                
                WebMarkupContainer actionHideType = new WebMarkupContainer("actionHideType");
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
                
                WebMarkupContainer actionHideContext = new WebMarkupContainer("actionHideContext");
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
                
                WebMarkupContainer actionHideTypeInContext = new WebMarkupContainer("actionHideTypeInContext");
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
        newsItemsDiv.add(newsDataView);

        //add a pager to our table, only visible if we have more than 5 items
        newsItemsDiv.add(new PagingNavigator("newsNavigator", newsDataView) {
        	
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
        
        dashboardPage.add(newsItemsDiv);
        
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
					if(saved || hidden) {
						newsItems = dashboardLogic.getNewsItems(sakaiId, saved, hidden);
					} else {
						newsItems = dashboardLogic.getNewsItems(sakaiId, null, 2);
					}
				} else {
					if(saved || hidden) {
						newsItems = dashboardLogic.getNewsItems(sakaiId, siteId, saved, hidden);
					} else {
						newsItems = dashboardLogic.getNewsItems(sakaiId, siteId, 2);
					}
				}
			}
			
			logger.info("NewsItemDataProvider saved=" + saved + " hidden=" + hidden + " newsItems=" + newsItems);
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
	 * Detachable model to wrap a NewsItem
	 * 
	 */
	private class DetachableNewsItemModel extends LoadableDetachableModel<NewsItem>{

		private NewsItem newsItem = null;
		
		/**
		 * @param m
		 */
		public DetachableNewsItemModel(NewsItem t){
			this.newsItem  = new NewsItem(t); 
		}
		
		/**
		 * @param id
		 */
//		public DetachableNewsItemModel(long id){
//			this.id = id;
//		}
		
		/**
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode() {
			if(this.newsItem == null || this.newsItem.getId() == null) {
				return Long.valueOf(0L).hashCode();
			}
			return Long.valueOf(this.newsItem.getId()).hashCode();
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
			else if(this.newsItem == null) {
				return false;
			}
			else if (obj instanceof DetachableNewsItemModel) {
				DetachableNewsItemModel other = (DetachableNewsItemModel)obj;
				if(other.newsItem == null) {
					return false;
				}
				return other.newsItem.getId() == this.newsItem.getId();
			}
			return false;
		}
		
		/**
		 * @see org.apache.wicket.model.LoadableDetachableModel#load()
		 */
		protected NewsItem load(){
			
			// get the news item
			return new NewsItem(this.newsItem);
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
