/**
 * 
 */
package org.sakaiproject.dash.tool.panels;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.DefaultItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.CalendarLink;
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

	private static final Logger logger = Logger.getLogger(CalendarLinksPanel.class);
	
	protected static final String DATE_FORMAT = "dd-MMM-yyyy";
	protected static final String TIME_FORMAT = "HH:mm";
	protected static final String DATETIME_FORMAT = "dd-MMM-yyyy HH:mm";
	
	@SpringBean(name="org.sakaiproject.dash.logic.SakaiProxy")
	protected SakaiProxy sakaiProxy;
	
	@SpringBean(name="org.sakaiproject.dash.logic.DashboardLogic")
	protected DashboardLogic dashboardLogic;
	
	protected CalendarLinksDataProvider calendarLinksProvider = null;
		 
	protected String selectedCalendarTab = null;
	protected String calendarItemsDivId = null;
	protected int pageSize = 5;
	
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
		
		ResourceLoader rl = new ResourceLoader("dash_entity");

		if(selectedCalendarTab == null) {
			selectedCalendarTab = TAB_ID_UPCOMING;
		}

		if(this.calendarItemsDivId != null) {
			this.remove(calendarItemsDivId);
		}

		final WebMarkupContainer calendarItemsDiv = new WebMarkupContainer("calendarItemsDiv");
		calendarItemsDiv.setOutputMarkupId(true);
		add(calendarItemsDiv);
		this.calendarItemsDivId = calendarItemsDiv.getId();

        AjaxLink<IModel<List<CalendarLink>>> upcomingCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("upcomingCalendarLink onClick called");
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
			}
        	
        };
        
        upcomingCalendarLink.add(new Label("label", rl.getString("dash.calendar.upcoming")));
		WebMarkupContainer upcomingCalendarTab = new WebMarkupContainer("upcomingCalendarTab");
		if(selectedCalendarTab == null || TAB_ID_UPCOMING.equalsIgnoreCase(selectedCalendarTab)) {
			upcomingCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		upcomingCalendarTab.add(upcomingCalendarLink);
        calendarItemsDiv.add(upcomingCalendarTab);

		AjaxLink<IModel<List<CalendarLink>>> pastCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("pastCalendarLink onClick called");
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
			}
        	
        };
        pastCalendarLink.add(new Label("label", rl.getString("dash.calendar.past")));
		WebMarkupContainer pastCalendarTab = new WebMarkupContainer("pastCalendarTab");
		if(selectedCalendarTab != null && TAB_ID_PAST.equalsIgnoreCase(selectedCalendarTab)) {
			pastCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		pastCalendarTab.add(pastCalendarLink);
        calendarItemsDiv.add(pastCalendarTab);

		AjaxLink<IModel<List<CalendarLink>>> starredCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("starredCalendarLink onClick called");
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
			}
        	
        };
        starredCalendarLink.add(new Label("label", rl.getString("dash.calendar.starred")));
		WebMarkupContainer starredCalendarTab = new WebMarkupContainer("starredCalendarTab");
		if(selectedCalendarTab != null && TAB_ID_STARRED.equalsIgnoreCase(selectedCalendarTab)) {
			starredCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		starredCalendarTab.add(starredCalendarLink);
        calendarItemsDiv.add(starredCalendarTab);

		AjaxLink<IModel<List<CalendarLink>>> hiddenCalendarLink = new AjaxLink<IModel<List<CalendarLink>>>("link") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				logger.info("hiddenCalendarLink onClick called");
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
			}
        	
        };
        hiddenCalendarLink.add(new Label("label", rl.getString("dash.calendar.hidden")));
		WebMarkupContainer hiddenCalendarTab = new WebMarkupContainer("hiddenCalendarTab");
		if(selectedCalendarTab != null && TAB_ID_HIDDEN.equalsIgnoreCase(selectedCalendarTab)) {
			hiddenCalendarTab.add(new SimpleAttributeModifier("class", "activeTab"));
		}
		hiddenCalendarTab.add(hiddenCalendarLink);
        calendarItemsDiv.add(hiddenCalendarTab);

        if(calendarLinksProvider == null) {
        	calendarLinksProvider = new CalendarLinksDataProvider(selectedCalendarTab);
        } else {
        	calendarLinksProvider.setCalendarTab(selectedCalendarTab);
        }
		
		//present the calendar data in a table
		final DataView<CalendarLink> calendarDataView = new DataView<CalendarLink>("calendarItems", calendarLinksProvider) {

			@Override
			public void populateItem(final Item item) {
				if(item != null && item.getModelObject() != null) {
					item.setOutputMarkupId(true);
	                final CalendarLink cLink = (CalendarLink) item.getModelObject();
	                final CalendarItem cItem = cLink.getCalendarItem();
	                
	                if(logger.isDebugEnabled()) {
	                	logger.debug(this + "populateItem()  item: " + item);
	                }
	                
	                String itemType = cItem.getSourceType().getIdentifier();
	                item.add(new Label("itemType", itemType));
	                item.add(new Label("itemCount", "1"));
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
	      
	                WebMarkupContainer actionPanel = new WebMarkupContainer("actionPanel");
	                item.add(actionPanel);
	                
	                if(cLink.isSticky()) {
		                AjaxLink<CalendarLink> starringAction = new AjaxLink<CalendarLink>("starringAction") {
		                	protected long calendarItemId = cItem.getId();
		                	protected Component thisRow = item;
		                	
							@Override
							public void onClick(AjaxRequestTarget target) {
								logger.info("starringAction onClick() called -- unstar ");
								// need to keep one item
								logger.info(calendarItemId);
								//logger.info(this.getModelObject());
								
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardLogic.unkeepCalendarItem(sakaiUserId, calendarItemId);
								if(success) {
									String javascript = "alert('this item is no longer starred. (" + thisRow.getMarkupId() + ")');";
									target.appendJavascript(javascript );
								}

								// if sticky adjust UI, else report failure?
								target.addComponent(thisRow);
							}
		                	
		                };
		                
		                starringAction.setDefaultModel(item.getModel());
		                actionPanel.add(starringAction);
		                starringAction.add(new Label("starringActionLabel", "Unstar"));
		                
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
								logger.info("starringAction onClick() called -- star ");
								// need to keep one item
								logger.info(calendarItemId);
								//logger.info(this.getModelObject());
								
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardLogic.keepCalendarItem(sakaiUserId, calendarItemId);
								if(success) {
									String javascript = "alert('this item is now starred. (" + thisRow.getMarkupId() + ")');";
									target.appendJavascript(javascript );
								}
								
								target.addComponent(thisRow);
							}
							
		                };
		                starringAction.add(new Label("starringActionLabel", "Star"));
		                actionPanel.add(starringAction);

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
								logger.info("hidingAction onClick() called -- show");
								// need to trash one item
								logger.info(calendarItemId);
								//logger.info(this.getModelObject());
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardLogic.hideCalendarItem(sakaiUserId, calendarItemId);
								if(success) {
									String javascript = "alert('this item is no longer hidden. (" + thisRow.getMarkupId() + ")');";
									target.appendJavascript(javascript );
								}
								
								// if hidden adjust UI, else report failure?
								target.addComponent(thisRow);
							}
		                	
		                };
		                hidingAction.setDefaultModel(item.getModel());
		                //actionHideThisLink.setModelObject(cItem);
		                actionPanel.add(hidingAction);
		                hidingAction.add(new Label("hidingActionLabel", "Show"));
		                
		                if(cLink.isSticky()) {
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
								logger.info("hidingAction onClick() called -- hide");
								// need to trash one item
								logger.info(calendarItemId);
								//logger.info(this.getModelObject());
								String sakaiUserId = sakaiProxy.getCurrentUserId();
								boolean success = dashboardLogic.hideCalendarItem(sakaiUserId, calendarItemId);
								if(success) {
									String javascript = "alert('this item is now hidden. (" + thisRow.getMarkupId() + ")');";
									target.appendJavascript(javascript );
								}
								
								// if hidden adjust UI, else report failure?
								target.addComponent(thisRow);
							}
		                	
		                };
		                hidingAction.setDefaultModel(item.getModel());
		                
		                //actionHideThisLink.setModelObject(cItem);
		                actionPanel.add(hidingAction);
		                hidingAction.add(new Label("hidingActionLabel", "Hide"));
		                
		                if(cLink.isSticky()) {
			                hidingAction.setVisible(false);
		                	hidingAction.setVisibilityAllowed(false);
		                } 
	                	
	                }
	                
	                
				}
			}
        };
        calendarDataView.setItemReuseStrategy(new DefaultItemReuseStrategy());
        calendarDataView.setItemsPerPage(pageSize);
        calendarItemsDiv.add(calendarDataView);

        //add a pager to our table, only visible if we have more than 5 items
        calendarItemsDiv.add(new PagingNavigator("calendarNavigator", calendarDataView) {
        	
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
        		
        		//clear the feedback panel messages
        		//clearFeedback(feedbackPanel);
        	}
        });
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
					calendarLinks = dashboardLogic.getFutureCalendarLinks(sakaiId, null, false);
				} else if(TAB_ID_UPCOMING.equals(this.calendarTabId)) {
					calendarLinks = dashboardLogic.getFutureCalendarLinks(sakaiId, siteId, false);
				} else if(TAB_ID_STARRED.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardLogic.getStarredCalendarLinks(sakaiId, null);
				} else if(TAB_ID_STARRED.equals(this.calendarTabId)) {
					calendarLinks = dashboardLogic.getStarredCalendarLinks(sakaiId, siteId);
				} else if(TAB_ID_PAST.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardLogic.getPastCalendarLinks(sakaiId, null, false);
				} else if(TAB_ID_PAST.equals(this.calendarTabId) ) {
					calendarLinks = dashboardLogic.getPastCalendarLinks(sakaiId, siteId, false);
				} else if(TAB_ID_HIDDEN.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardLogic.getFutureCalendarLinks(sakaiId, siteId, true);
				} else if(TAB_ID_HIDDEN.equals(this.calendarTabId)) {
					calendarLinks = dashboardLogic.getFutureCalendarLinks(sakaiId, siteId, true);
				} else if(TAB_ID_PAST_HIDDEN.equals(this.calendarTabId) && sakaiProxy.isWorksite(siteId)) {
					calendarLinks = dashboardLogic.getPastCalendarLinks(sakaiId, siteId, false);
				} else {
					calendarLinks = dashboardLogic.getPastCalendarLinks(sakaiId, siteId, false);
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
			return dashboardLogic.getCalendarLink(id);
		}
	}

}
