/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * THIS WILL BE MOVED TO THE assignment PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL
 *
 */
public class ScheduleSupport{
	
	private Log logger = LogFactory.getLog(ScheduleSupport.class);
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}

	public static final String IDENTIFIER = "schedule";
	
	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(new ScheduleEntityType());
		this.dashboardLogic.registerEventProcessor(new ScheduleNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleRemoveEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateTitleEventProcessor());
	}
	/**
	 * Inner class: ScheduleEntityType
	 * @author zqian
	 *
	 */
	public class ScheduleEntityType implements EntityType {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return IDENTIFIER;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getValues(java.lang.String, java.lang.String)
		 */
		public Map<String, Object> getValues(String entityReference,
				String localeCode) {
			Map<String, Object> values = new HashMap<String, Object>();
			CalendarEvent cEvent = (CalendarEvent) sakaiProxy.getEntity(entityReference);
			ResourceLoader rl = new ResourceLoader("dash_entity");
			
			if(cEvent != null) {

				DateFormat df = DateFormat.getDateTimeInstance();
				ResourceProperties props = cEvent.getProperties();
				// "entity-type": "assignment"
				values.put(EntityType.VALUE_ENTITY_TYPE, IDENTIFIER);
				values.put(VALUE_TITLE, cEvent.getDisplayName());
				values.put(VALUE_CALENDAR_TIME, df.format(new Date(cEvent.getRange().firstTime().getTime())));
				
				
				values.put(EntityType.VALUE_ENTITY_TYPE, IDENTIFIER);
				values.put(VALUE_DESCRIPTION, cEvent.getDescription());
				// "user-name": "Creator's Name"
				/*User user = cEvent.getCreator().getFrom();
				if(user != null) {
					values.put(VALUE_USER_NAME, user.getDisplayName());
				}*/
				
				// more info
				List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
				Map<String,String> infoItem = new HashMap<String,String>();
				infoItem.put(VALUE_INFO_LINK_URL, "");
				infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("schedule.info.link"));
				infoList.add(infoItem);
				values.put(VALUE_MORE_INFO, infoList);
				
			}
			
			return values;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getProperties(java.lang.String, java.lang.String)
		 */
		public Map<String, String> getProperties(String entityReference,
				String localeCode) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Map<String, String> props = new HashMap<String, String>();
			props.put(LABEL_NEWS_TIME, rl.getString("schedule.news.time"));
			return props;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getOrder(java.lang.String, java.lang.String)
		 */
		public List<List<String>> getOrder(String entityReference, String localeCode) {
			List<List<String>> order = new ArrayList<List<String>>();
			List<String> section0 = new ArrayList<String>();
			section0.add(VALUE_TITLE);
			order.add(section0);
			List<String> section1 = new ArrayList<String>();
			section1.add(VALUE_NEWS_TIME);
			order.add(section1);
			List<String> section2 = new ArrayList<String>();
			section2.add(VALUE_DESCRIPTION);
			order.add(section2);
			List<String> section3 = new ArrayList<String>();
			section3.add(VALUE_MORE_INFO);
			order.add(section3);
			return order;
		}

		public void init() {
			logger.info("init()");
			dashboardLogic.registerEntityType(this);
		}

		public boolean isAvailable(String entityReference) {
			return true;
		}
		
		public String getString(String key, String dflt) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}
	}
	
	/**
	 * Inner class: ScheduleNewEventProcessor
	 * @author zqian
	 *
	 */
	public class ScheduleNewEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_SCHEDULE_NEW_EVENT;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			String resource =  event.getResource();
			
			String eventId = event.getEvent();
			
			String proxyStringNew = SakaiProxy.EVENT_SCHEDULE_NEW_EVENT;
			
			String proxyStringRemove = SakaiProxy.EVENT_SCHEDULE_REMOVE_EVENT;
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			// handle add events
			if(entity != null && entity instanceof CalendarEvent) {
			
				CalendarEvent cEvent = (CalendarEvent) entity;
				Context context = dashboardLogic.getContext(event.getContext());
				if(context == null) {
					context = dashboardLogic.createContext(event.getContext());
				}
				SourceType sourceType = dashboardLogic.getSourceType(IDENTIFIER);
				if(sourceType == null) {
					sourceType = dashboardLogic.createSourceType(IDENTIFIER, SakaiProxy.PERMIT_SCHEDULE_ACCESS, EntityLinkStrategy.SHOW_PROPERTIES);
				}
				// TODO: Third parameter should be a key for a label such as "Due Date: " or "Accept Until: " 
				// from dash_entity properties bundle for use in the dashboard list
				CalendarItem calendarItem = dashboardLogic.createCalendarItem(cEvent.getDisplayName(), new Date(cEvent.getRange().firstTime().getTime()), "", cEvent.getReference(), "", context, sourceType);
				dashboardLogic.createCalendarLinks(calendarItem);
				
			} else {
				// for now, let's log the error
				logger.info(eventId + " is not processed for entityReference " + event.getResource());
			}
		}
	}
	
	/**
	 * Inner class: ScheduleRemoveEventProcessor
	 */
	public class ScheduleRemoveEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_SCHEDULE_REMOVE_EVENT;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and news item for " + event.getResource());
			}
			// remove all links and CalendarItem itself
			dashboardLogic.removeCalendarItems(event.getResource());
		}

	}
	
	/**
	 * Inner Class: ScheduleUpdateTitleEventProcessor
	 */
	public class ScheduleUpdateTitleEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_TITLE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof CalendarEvent) {
				// get the assignment entity and its current title
				CalendarEvent cEvent = (CalendarEvent) entity;
				
				String title = cEvent.getDisplayName();
				// update news item title
				dashboardLogic.reviseNewsItemTitle(cEvent.getReference(), title);
				
				// update calendar item title
				dashboardLogic.reviseCalendarItemsTitle(cEvent.getReference(), title);
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
}
