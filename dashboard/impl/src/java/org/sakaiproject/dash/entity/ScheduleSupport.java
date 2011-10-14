/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
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
	
	protected TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	protected Map<String,String> scheduleEventTypeMap;
	
	protected RepeatingEventGenerator scheduleEntityType;

	public static final String IDENTIFIER = "schedule";
	
	public void init() {
		logger.info("init()");
		this.scheduleEntityType = new ScheduleEntityType();
		this.dashboardLogic.registerEntityType(scheduleEntityType);
		this.dashboardLogic.registerEventProcessor(new ScheduleNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleRemoveEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateTitleEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleReviseEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateAccessEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateFrequencyEventProcessor());
		
		scheduleEventTypeMap = new HashMap<String,String>();
		
		scheduleEventTypeMap.put("Academic Calendar", "schedule.key1");
		scheduleEventTypeMap.put("Activity", "schedule.key2");
		scheduleEventTypeMap.put("Cancellation", "schedule.key3");
		scheduleEventTypeMap.put("Class section - Discussion", "schedule.key4");
		scheduleEventTypeMap.put("Class section - Lab", "schedule.key5");
		scheduleEventTypeMap.put("Class section - Lecture", "schedule.key6");
		scheduleEventTypeMap.put("Class section - Small Group", "schedule.key7");
		scheduleEventTypeMap.put("Class session", "schedule.key8");
		scheduleEventTypeMap.put("Computer Session", "schedule.key9");
		scheduleEventTypeMap.put("Deadline", "schedule.key10");
		scheduleEventTypeMap.put("Exam", "schedule.key11");
		scheduleEventTypeMap.put("Meeting", "schedule.key12");
		scheduleEventTypeMap.put("Multidisciplinary Conference", "schedule.key13");
		scheduleEventTypeMap.put("Quiz", "schedule.key14");
		scheduleEventTypeMap.put("Special event", "schedule.key15");
		scheduleEventTypeMap.put("Web Assignment", "schedule.key16");

	}
	
	/**
	 * Inner class: ScheduleEntityType
	 * @author zqian
	 *
	 */
	public class ScheduleEntityType implements RepeatingEventGenerator {
		
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
				infoItem.put(VALUE_INFO_LINK_URL, sakaiProxy.getScheduleEventUrl(cEvent.getReference()));
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
		
		public boolean isUserPermitted(String sakaiUserId, String accessPermission,
				String entityReference, String contextId) {
			// use message read permission
			List users = sakaiProxy.unlockUsers(accessPermission, sakaiProxy.getSiteReference(contextId));
			for (Object user : users)
			{
				if (sakaiUserId.equals(((User) user).getId()))
				{
					// user can submit
					return true;
				}
			}
			return false;
		}
		
		public String getString(String key, String dflt) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.RepeatingEventGenerator#generateRepeatingEventDates(java.lang.String, java.util.Date, java.util.Date)
		 */
		public Map<Integer,Date> generateRepeatingEventDates(String entityReference, Date beginDate, Date endDate) {
			logger.info("generateRepeatingEventDates(" + entityReference + ", " + endDate + ") ");
			Map<Integer, Date> dateMap = new HashMap<Integer, Date>();
			CalendarEvent cEvent = (CalendarEvent) sakaiProxy.getEntity(entityReference);
			
			if(cEvent != null) {
				Date now = new Date();
				TimeRange range2 = timeService.newTimeRange(now.getTime(), endDate.getTime() - now.getTime());
				TimeZone timezone = timeService.getLocalTimeZone();
				Integer first = null;
				Integer last = null;

				List objects = cEvent.getRecurrenceRule().generateInstances(cEvent.getRange(), range2, timezone);
				cEvent.getRecurrenceRule().excludeInstances(objects);
				
				for(Object obj : objects) {
					try {
						// the following use of reflection would not be necessary if 
						// sakai.schedule exposed these methods in the API ....
						TimeRange range = (TimeRange) obj.getClass().getMethod("getRange", null).invoke(obj, null);
						Integer sequence = (Integer) obj.getClass().getMethod("getSequence", null).invoke(obj, null);
						dateMap.put(sequence, new Date(range.firstTime().getTime()));
						if(first == null || first.intValue() > sequence.intValue()) {
							first = sequence;
						} 
						// actually, this is not necessary.
						// the dates come to us in order (but the API doesn't guarantee that).
						if(last == null || last.intValue() < sequence.intValue()) {
							last = sequence;
						}
						if(logger.isDebugEnabled()) {
							logger.debug("   " + sequence + " --> " + dateMap.get(sequence));
						}
					} catch(NoSuchMethodException e) {
						logger.warn("NoSuchMethodException while generating a list of dates for a recurring event: " + e);
					} catch (IllegalArgumentException e) {
						logger.warn("IllegalArgumentException while generating a list of dates for a recurring event: " + e);
					} catch (SecurityException e) {
						logger.warn("SecurityException while generating a list of dates for a recurring event: " + e);
					} catch (IllegalAccessException e) {
						logger.warn("IllegalAccessException while generating a list of dates for a recurring event: " + e);
					} catch (InvocationTargetException e) {
						logger.warn("InvocationTargetException while generating a list of dates for a recurring event: " + e);
					}
				}
				
			}
			
			return dateMap;
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
			
			if(logger.isInfoEnabled()) {
				logger.info("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("removing links and item for " + event.getResource());
			}
			String eventId = event.getEvent();
			
			String eventContextString = event.getContext();
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			// handle add events
			if(entity != null && entity instanceof CalendarEvent) {
			
				CalendarEvent cEvent = (CalendarEvent) entity;

				String cEventReference = cEvent.getReference();
				
				Context context = dashboardLogic.getContext(eventContextString);
				if(context == null) {
					context = dashboardLogic.createContext(eventContextString);
				}
				SourceType sourceType = dashboardLogic.getSourceType(IDENTIFIER);
				if(sourceType == null) {
					sourceType = dashboardLogic.createSourceType(IDENTIFIER, SakaiProxy.PERMIT_SCHEDULE_ACCESS, EntityLinkStrategy.SHOW_PROPERTIES);
				}
				
				// Third parameter in dashboardLogic.createCalendarItem() below should be a key for a label such as "Due Date: " or "Accept Until: " 
				// from dash_entity properties bundle for use in the dashboard list
				String type = cEvent.getType();
				// Based on the event-type, we may be able to select a key for a label? 
				String key = null;
				if(type == null) {
					key = "";
				} else {
					key = scheduleEventTypeMap.get(type);
					if(key == null) {
						key = "";
					}
				}
				// is this a repeating event?
				RecurrenceRule recurrenceRule = cEvent.getRecurrenceRule();
				if(recurrenceRule == null) {
					// not a repeating event so create one calendar event
					CalendarItem calendarItem = dashboardLogic.createCalendarItem(cEvent.getDisplayName(), new Date(cEvent.getRange().firstTime().getTime()), key, cEventReference, context, sourceType, null, null);
					dashboardLogic.createCalendarLinks(calendarItem);
				} else {
					// this is a repeating event -- create a repeating calendar item
					String frequency = recurrenceRule.getFrequency();
					int maxCount = recurrenceRule.getCount();
					int interval = recurrenceRule.getInterval();
					
					Date lastDate = null;
					if(recurrenceRule.getUntil() != null) {
						lastDate = new Date(recurrenceRule.getUntil().getTime());
					}
					
					RepeatingCalendarItem repeatingCalendarItem = dashboardLogic.createRepeatingCalendarItem(cEvent.getDisplayName(), new Date(cEvent.getRange().firstTime().getTime()), 
							lastDate, key, cEventReference, context, sourceType, frequency, maxCount);
						
					logger.info(repeatingCalendarItem);
				} 
				
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
			
			if(logger.isInfoEnabled()) {
				logger.info("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
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
			
			if(logger.isInfoEnabled()) {
				logger.info("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("updating title of calendar item for " + event.getResource());
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
	
	public class ScheduleReviseEventProcessor implements EventProcessor {

		public String getEventIdentifer() {
			return SakaiProxy.EVENT_SCHEDULE_REVISE_EVENT;
		}

		public void processEvent(Event event) {
			if(logger.isInfoEnabled()) {
				logger.info("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("revising calendar item for " + event.getResource());
			}

		}
		
	}
	
	public class ScheduleUpdateAccessEventProcessor implements EventProcessor {

		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_ACCESS;
		}

		public void processEvent(Event event) {
			if(logger.isInfoEnabled()) {
				logger.info("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("revising calendar item for " + event.getResource());
			}

		}
	
	}
	
	public class ScheduleUpdateFrequencyEventProcessor implements EventProcessor {

		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY;
		}

		public void processEvent(Event event) {
			if(logger.isInfoEnabled()) {
				logger.info("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("revising calendar item for " + event.getResource());
			}

		}
	
	}
	
	
}
