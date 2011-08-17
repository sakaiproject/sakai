package org.sakaiproject.dash.tool.pages;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.NewsItem;

/**
 * An example page. This interacts with a list of items from the database
 * 
 * @author Steve Swinsburg (steve.swinsburg@anu.edu.au)
 *
 */
public class DashboardPage extends BasePage {
	
	private static final Logger logger = Logger.getLogger(DashboardPage.class); 
	
	private static final String DATE_FORMAT = "dd-MMM-yyyy";
	private static final String TIME_FORMAT = "HH:mm";
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";

	NewsItemDataProvider newsItemsProvider;
	CalendarItemDataProvider calendarItemsProvider;
	
	public DashboardPage() {
		disableLink(thirdLink);
		
		//get list of items from db, wrapped in a dataprovider
		newsItemsProvider = new NewsItemDataProvider();
		calendarItemsProvider = new CalendarItemDataProvider();
		
		//present the calendar data in a table
		final DataView<CalendarItem> calendarDataView = new DataView<CalendarItem>("calendarItems", calendarItemsProvider) {

			@Override
			public void populateItem(final Item item) {
				if(item != null && item.getModelObject() != null) {
	                final CalendarItem cItem = (CalendarItem) item.getModelObject();
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                //item.add(new Label("name", thing.getName()));
	                item.add(new Label("calendarDate", new SimpleDateFormat(DATE_FORMAT).format(cItem.getCalendarTime())));
	                item.add(new Label("calendarTime", new SimpleDateFormat(TIME_FORMAT).format(cItem.getCalendarTime())));
	                item.add(new ExternalLink("itemLink", cItem.getEntityUrl(), cItem.getTitle()));
	                item.add(new ExternalLink("siteLink", cItem.getContext().getContextUrl(), cItem.getContext().getContextTitle()));
				}
			}
        };
        calendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        calendarDataView.setItemsPerPage(5);
        add(calendarDataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("calendarNavigator", calendarDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(calendarItemsProvider.size() > 5) {
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
                //item.add(new Label("name", thing.getName()));
                item.add(new ExternalLink("itemLink", nItem.getEntityUrl(), nItem.getTitle()));
                item.add(new ExternalLink("siteLink", nItem.getContext().getContextUrl(), nItem.getContext().getContextTitle()));
                item.add(new Label("newsTime", new SimpleDateFormat(DATETIME_FORMAT).format(nItem.getNewsTime())));
            }
        };
        newsDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        newsDataView.setItemsPerPage(5);
        add(newsDataView);

        //add a pager to our table, only visible if we have more than 5 items
        add(new PagingNavigator("newsNavigator", newsDataView) {
        	
        	@Override
        	public boolean isVisible() {
        		if(newsItemsProvider.size() > 5) {
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
        
	}
		
	/**
	 * DataProvider to manage our list
	 * 
	 */
	private class CalendarItemDataProvider implements IDataProvider<CalendarItem> {
	   
		private List<CalendarItem> calendarItems;
		
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
					calendarItems = dashboardLogic.getCalendarItems(sakaiId);
				} else {
					calendarItems = dashboardLogic.getCalendarItems(sakaiId, siteId);
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
					newsItems = dashboardLogic.getNewsItems(sakaiId);
				} else {
					newsItems = dashboardLogic.getNewsItems(sakaiId, siteId);
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
			
			// get the thing
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
			
			// get the thing
			return dashboardLogic.getNewsItem(id);
		}
	}
}
