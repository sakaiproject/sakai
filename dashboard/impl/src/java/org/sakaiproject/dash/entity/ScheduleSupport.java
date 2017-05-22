/********************************************************************************** 
 * $URL: $ 
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

package org.sakaiproject.dash.entity;

import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.calendar.api.Calendar;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.calendar.api.RecurrenceRule;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.RepeatingCalendarItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * THIS WILL BE MOVED TO THE calendar PROJECT IN SAKAI CORE ONCE THE INTERFACE IS MOVED TO KERNEL.
 * 
 * When this moves to calendar project, eliminate references to SakaiProxy and use sakai services instead. 
 */
public class ScheduleSupport{
	
	private Logger logger = LoggerFactory.getLogger(ScheduleSupport.class);
	
	ResourceLoader rl = new ResourceLoader("dash_entity");

	// TODO: add member variable and setter for defaultImageForEvent that will allow DEFAULT_IMAGE_FOR_EVENT
	// TODO: to be overridden by spring injection.  Then use DEFAULT_IMAGE_FOR_EVENT only if defaultImageForEvent
	// TODO: is not set.
	public static final String DEFAULT_IMAGE_FOR_EVENT = "/library/image/sakai/activity.gif";

	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
	
	protected TimeService timeService;
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}
	
	protected CalendarService calendarService;
	public void setCalendarService(CalendarService calendarService) {
		this.calendarService = calendarService;
	}
	
	protected SiteService siteService;
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	protected Map<String,String> scheduleEventTypeMap;
	protected Map<String,String> eventTypeImageUrlMap;
	
	protected RepeatingEventGenerator scheduleEntityType;

	public static final String IDENTIFIER = "schedule";
	
	public void init() {
		logger.info("init()");
		this.scheduleEntityType = new ScheduleEntityType();
		this.dashboardLogic.registerEntityType(scheduleEntityType);
		this.dashboardLogic.registerEventProcessor(new ScheduleNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleRemoveEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateTimeEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateTitleEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateTypeEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleReviseEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateAccessEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateFrequencyEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateExcludedEventProcessor());
		this.dashboardLogic.registerEventProcessor(new ScheduleUpdateExclusionsEventProcessor());
		
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
		
		eventTypeImageUrlMap = new HashMap<String,String>();
		
		eventTypeImageUrlMap.put("Academic Calendar", "/library/image/sakai/academic_calendar.gif");
		eventTypeImageUrlMap.put("Activity", "/library/image/sakai/activity.gif");
		eventTypeImageUrlMap.put("Cancellation", "/library/image/sakai/cancelled.gif");
		eventTypeImageUrlMap.put("Class section - Discussion", "/library/image/sakai/class_dis.gif");
		eventTypeImageUrlMap.put("Class section - Lab", "/library/image/sakai/class_lab.gif");
		eventTypeImageUrlMap.put("Class section - Lecture", "/library/image/sakai/class_lec.gif");
		eventTypeImageUrlMap.put("Class section - Small Group", "/library/image/sakai/class_sma.gif");
		eventTypeImageUrlMap.put("Class session", "/library/image/sakai/class_session.gif");
		eventTypeImageUrlMap.put("Computer Session", "/library/image/sakai/computersession.gif");
		eventTypeImageUrlMap.put("Deadline", "/library/image/sakai/deadline.gif");
		eventTypeImageUrlMap.put("Exam", "/library/image/silk/accept.png");
		eventTypeImageUrlMap.put("Meeting", "/library/image/sakai/meeting.gif");
		eventTypeImageUrlMap.put("Multidisciplinary Conference", "/library/image/sakai/multi-conference.gif");
		eventTypeImageUrlMap.put("Quiz", "/library/image/silk/star.png");
		eventTypeImageUrlMap.put("Special event", "/library/image/sakai/special_event.gif");
		eventTypeImageUrlMap.put("Web Assignment", "/library/image/sakai/webassignment.gif");

	}
	
	/**
	 * Inner class: ScheduleEntityType
	 * @author zqian
	 *
	 */
	public class ScheduleEntityType implements RepeatingEventGenerator {
		
		public static final String LABEL_METADATA = "calendar_metadata-label";
		
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
			
			if(cEvent != null) {

				DateFormat df = DateFormat.getDateTimeInstance();
				ResourceProperties props = cEvent.getProperties();
				// "entity-type": "assignment"
				values.put(DashboardEntityInfo.VALUE_ENTITY_TYPE, IDENTIFIER);
				values.put(VALUE_TITLE, cEvent.getDisplayName());
				values.put(VALUE_CALENDAR_TIME, df.format(new Date(cEvent.getRange().firstTime().getTime())));
				try {
					values.put(VALUE_NEWS_TIME, df.format(new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime())));
				} catch (EntityPropertyNotDefinedException e) {
					logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyNotDefinedException: " + e);
				} catch (EntityPropertyTypeException e) {
					logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyTypeException: " + e);
				}
				
				values.put(DashboardEntityInfo.VALUE_ENTITY_TYPE, IDENTIFIER);
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
				infoItem.put(VALUE_INFO_LINK_TARGET, "_top");
				infoList.add(infoItem);
				values.put(VALUE_MORE_INFO, infoList);
				
				// "attachments": [ ... ]
				List<Reference> attachments = cEvent.getAttachments();
				if(attachments != null && ! attachments.isEmpty()) {
					List<Map<String,String>> attList = new ArrayList<Map<String,String>>();
					for(Reference ref : attachments) {
						ContentResource resource = (ContentResource) ref.getEntity();
						Map<String, String> attInfo = new HashMap<String, String>();
						attInfo.put(VALUE_ATTACHMENT_TITLE, resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
						attInfo.put(VALUE_ATTACHMENT_URL, resource.getUrl());
						attInfo.put(VALUE_ATTACHMENT_MIMETYPE, resource.getContentType());
						attInfo.put(VALUE_ATTACHMENT_SIZE, Long.toString(resource.getContentLength()));
						attInfo.put(VALUE_ATTACHMENT_TARGET, sakaiProxy.getTargetForMimetype(resource.getContentType()));
						attList.add(attInfo );
					}
					values.put(VALUE_ATTACHMENTS, attList);
				}
				
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
			props.put(LABEL_METADATA, rl.getString("schedule.metadata"));
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
			section1.add(LABEL_METADATA);
			order.add(section1);
			
			List<String> section2 = new ArrayList<String>();
			section2.add(VALUE_DESCRIPTION);
			order.add(section2);
			
			List<String> section3 = new ArrayList<String>();
			section3.add(VALUE_ATTACHMENTS);
			order.add(section3);
			
			List<String> section4 = new ArrayList<String>();
			section4.add(VALUE_MORE_INFO);
			order.add(section4);
			
			return order;
		}

		public void init() {
			logger.info("init()");
		}

		public boolean isAvailable(String entityReference) {
			boolean rv = false;
			if(entityReference == null) {
				logger.warn(this + "isAvailable() invoked with null entity reference");
			} else {
				CalendarEvent cEvent = (CalendarEvent) sakaiProxy.getEntity(entityReference);
				
				if(cEvent != null) {
					String calendarRef = cEvent.getCalendarReference();
					try
					{
						Calendar calendar = calendarService.getCalendar(calendarRef);
						String siteId = calendar.getContext();
						try
						{
							Site s = siteService.getSite(siteId);
							if (s.isPublished())
							{
								rv = true;
							}
						}
						catch (IdUnusedException exception)
						{
							logger.warn(this + " isAvailable: cannot find site " + siteId + " " + exception.getMessage());
						}
					}
					catch (IdUnusedException exception)
					{
						logger.warn(this + " isAvailable: cannot find calendar " + calendarRef + " " + exception.getMessage());
					}
					catch (PermissionException exception)
					{
						logger.warn(this + " isAvailable: don't have permission to get calendar " + calendarRef + " " + exception.getMessage());
					}
				}
			}
			return rv;
		}
		
		public boolean isUserPermitted(String sakaiUserId, String entityReference,
				String contextId) {
			// use message read permission
			
			String accessPermission = SakaiProxy.PERMIT_SCHEDULE_ACCESS;
			List users = sakaiProxy.unlockUsers(accessPermission , sakaiProxy.getSiteReference(contextId));
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
		
		/**
		 * {@inheritDoc}
		 */
		public String getEventDisplayString(String key, String dflt) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}

		/*
		 * (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.RepeatingEventGenerator#generateRepeatingEventDates(java.lang.String, java.util.Date, java.util.Date)
		 */
		public Map<Integer,Date> generateRepeatingEventDates(String entityReference, Date beginDate, Date endDate) {
			if(logger.isDebugEnabled()) {
				logger.debug("generateRepeatingEventDates(" + entityReference + ", " + endDate + ") ");
			}
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
		
		public String getGroupTitle(int numberOfItems, String contextTitle, String labelKey) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Object[] args = new Object[]{ numberOfItems, contextTitle };
			return rl.getFormattedMessage("announcement.grouped.title", args );
	}

		public String getIconUrl(String subtype) {
			
			String url = eventTypeImageUrlMap.get(subtype);
			if(url == null) {
				// default url to activity.gif
				url = DEFAULT_IMAGE_FOR_EVENT;
			}
			return url ;
		}

		public List<String> getUsersWithAccess(String entityReference) {
			Collection<String> users = null;
			users = sakaiProxy.getAuthorizedUsers(SakaiProxy.PERMIT_SCHEDULE_ACCESS, entityReference);
			List<String> rv = new ArrayList<String>();
			if(users != null && ! users.isEmpty()) {
				rv.addAll(users);
			}
			return rv;
		}
	}
	
	/**
	 * Inner class: ScheduleNewEventProcessor
	 * @author zqian
	 *
	 */
	public class ScheduleNewEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleNewEventProcessor.class);

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
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("removing links and item for {}", event.getResource());

			String eventId = event.getEvent();
			String eventContextString = event.getContext();

			Entity entity = sakaiProxy.getEntity(event.getResource());
			// handle add events
			if(entity != null && entity instanceof CalendarEvent) {
			
				CalendarEvent cEvent = (CalendarEvent) entity;

				String cEventReference = cEvent.getReference();
				
				Context context = dashboardLogic.getContext(eventContextString);
				if (context != null)
				{
					boolean sitePublished = false;
					try
					{
						Site s = siteService.getSite(context.getContextId());
						if (s.isPublished())
						{
							sitePublished = true;
						}
					}
					catch (IdUnusedException exception)
					{
						logger.warn(this + " ScheduleNewEventProcessor.processEvent(): cannot find site " + context.getContextId() + " " + exception.getMessage());
					}
					
					SourceType sourceType = dashboardLogic.getSourceType(IDENTIFIER);
					
					// Third parameter in dashboardLogic.createCalendarItem() below should be a key for a label such as "Due Date: " or "Accept Until: " 
					// from dash_entity properties bundle for use in the dashboard list
					String type = cEvent.getType();
					// Based on the event-type, we may be able to select a key for a label? 
					String key = null;
					if(type == null) {
						key = "schedule.key2";
					} else {
						key = scheduleEventTypeMap.get(type);
						if(key == null) {
							key = "schedule.key2";
						}
					}
					// is this a repeating event?
					RecurrenceRule recurrenceRule = cEvent.getRecurrenceRule();
					if(recurrenceRule == null) {
						// not a repeating event so create one calendar event
						CalendarItem calendarItem = dashboardLogic.createCalendarItem(cEvent.getDisplayName(), new Date(cEvent.getRange().firstTime().getTime()), key, cEventReference, context, sourceType, type, null, null);
						if (sitePublished)
						{
							dashboardLogic.createCalendarLinks(calendarItem);
						}
					} else {
						// this is a repeating event -- create a repeating calendar item
						String frequency = recurrenceRule.getFrequency();
						int maxCount = recurrenceRule.getCount();
						
						Date lastDate = null;
						if(recurrenceRule.getUntil() != null) {
							lastDate = new Date(recurrenceRule.getUntil().getTime());
						}
						
						if (lastDate == null || lastDate.after(new Date()))
						{
							// there is no need to track past repeating schedule events
							// most likely those were brought in when old sites were duplicated or imported
							RepeatingCalendarItem repeatingCalendarItem = dashboardLogic.createRepeatingCalendarItem(cEvent.getDisplayName(), new Date(cEvent.getRange().firstTime().getTime()), 
									lastDate, key, cEventReference, context, sourceType, frequency, maxCount);
								
							logger.debug(repeatingCalendarItem.toString());
						}
					}
				}
				
			} else {
				// for now, let's log the error. 
				// this event is posted for creation of a calendar as well as for creation of calendar events, so this is not necessarily an error.
				logger.debug("{} is not processed for entityReference {}", eventId, event.getResource());
			}
		}
		
	}
	
	/**
	 * Inner class: ScheduleRemoveEventProcessor
	 */
	public class ScheduleRemoveEventProcessor implements EventProcessor {
		
		private Logger logger = LoggerFactory.getLogger(ScheduleRemoveEventProcessor.class);
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_REMOVE_CALENDAR_EVENT;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("removing news links and news item for {}", event.getResource());
			logger.debug("removing calendar links and news item for {}", event.getResource());
			// remove all links and CalendarItem itself
			dashboardLogic.removeCalendarItems(event.getResource());
		}

	}
	
	/**
	 * Inner Class: ScheduleUpdateTitleEventProcessor
	 */
	public class ScheduleUpdateTitleEventProcessor implements EventProcessor {
		
		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateTitleEventProcessor.class);

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
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("updating title of calendar item for {}", event.getResource());
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof CalendarEvent) {
				// get the assignment entity and its current title
				CalendarEvent cEvent = (CalendarEvent) entity;
				
				String title = cEvent.getDisplayName();
				
				
				// update news item title
				//dashboardLogic.reviseNewsItemTitle(cEvent.getReference(), title, null, null);
				
				String type = cEvent.getType();
				// Based on the event-type, we may be able to select a key for a label? 
				String key = null;
				if(type == null) {
					key = "schedule.key2";
				} else {
					key = scheduleEventTypeMap.get(type);
					if(key == null) {
						key = "schedule.key2";
					}
				}
				
				RecurrenceRule rule = cEvent.getRecurrenceRule();
				if(rule == null) {
					// update calendar item title
					dashboardLogic.reviseCalendarItemsTitle(cEvent.getReference(), title);
				} else {
					// update repeating calendar item
					dashboardLogic.reviseRepeatingCalendarItemTitle(cEvent.getReference(), title);
					// update all instances of repeating calendar item
					dashboardLogic.reviseCalendarItemsTitle(cEvent.getReference(), title);
				}
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
	
	/**
	 * Inner Class: ScheduleUpdateTimeEventProcessor
	 */
	public class ScheduleUpdateTimeEventProcessor implements EventProcessor {
		
		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateTimeEventProcessor.class);
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_TIME;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("updating time of calendar item for {}", event.getResource());

			String entityReference = event.getResource();
			Entity entity = sakaiProxy.getEntity(entityReference );
			
			if(entity != null && entity instanceof CalendarEvent) {
				// get the calendar-event entity and its new time
				CalendarEvent cEvent = (CalendarEvent) entity;
				TimeRange range = cEvent.getRange();
				String calendarTimeLabelKey = scheduleEventTypeMap.get(cEvent.getType());
				Date newStartTime = new Date(range.firstTime().getTime());
				//Date newEndTime = new Date(range.lastTime().getTime());
				
				RecurrenceRule rule = cEvent.getRecurrenceRule();
				if(rule != null) {
					// change times for the repeating calendar item and all instances 
					// remove all instances and add new instances
					ResourceProperties props = cEvent.getProperties();
					RepeatingCalendarItem item = dashboardLogic.getRepeatingCalendarItem(entityReference, calendarTimeLabelKey);
					 
					
					// update the time of the repating item and each instance
					if(rule.getUntil() != null) {
						Date lastTime = new Date(rule.getUntil().getTime());
					}
					dashboardLogic.reviseRepeatingCalendarItemTime(entityReference, newStartTime, null);
					
					// need to get each item in sequence and update its time
					Map<Integer, Date> dates = scheduleEntityType.generateRepeatingEventDates(entityReference, newStartTime, dashboardLogic.getRepeatingEventHorizon());
					for(Map.Entry<Integer, Date> entry : dates.entrySet()) {
						if(logger.isDebugEnabled()) {
							String msg = entry.getKey().toString() + " ==> " + entry.getValue().toString();
							CalendarItem oneItem = dashboardLogic.getCalendarItem(entityReference, calendarTimeLabelKey, entry.getKey());
							if(oneItem != null) {
								msg += " -----> " + oneItem.getCalendarTime().toString();
							}
							logger.debug(msg);
						}
						dashboardLogic.reviseCalendarItemTime(entityReference, calendarTimeLabelKey, entry.getKey(), entry.getValue());
					}
				} else {
					// update calendar item title
					dashboardLogic.reviseCalendarItemsTime(entityReference, newStartTime);	
				}	
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for {}", event.getResource());
			}
		}

	}

	/**
	 * Inner Class: ScheduleUpdateTypeEventProcessor
	 */
	public class ScheduleUpdateTypeEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateTypeEventProcessor.class);

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_TYPE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("updating type of calendar item for {}", event.getResource());

			String[] parts = event.getResource().split("::");
			if(parts.length > 2) {
				String entityReference = parts[0];
				String oldType = parts[1];
				String newType = parts[2];
				
				String oldLabelKey = scheduleEventTypeMap.get(oldType);
				String newLabelKey = scheduleEventTypeMap.get(newType);
				
				Entity entity = sakaiProxy.getEntity(entityReference);
				
				if(entity != null && entity instanceof CalendarEvent) {
					// get the assignment entity and its new time
					CalendarEvent cEvent = (CalendarEvent) entity;
					
					logger.debug("removing news links and news item for {}", entityReference);
					if(cEvent.getRecurrenceRule() != null) {
						// update the label key for the repeating calendar item 
						dashboardLogic.reviseRepeatingCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
						// update the label key for all instances
						dashboardLogic.reviseCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
						
					} else {
						// update calendar item title
						dashboardLogic.reviseCalendarItemsLabelKey(entityReference, oldLabelKey, newLabelKey);
						
					}
					
				}
			}

		}

	}

	public class ScheduleReviseEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleReviseEventProcessor.class);

		public String getEventIdentifer() {
			return SakaiProxy.EVENT_SCHEDULE_REVISE_EVENT;
		}

		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("revising calendar item for {}", event.getResource());
		}
	}
	
	public class ScheduleUpdateAccessEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateAccessEventProcessor.class);

		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_ACCESS;
		}

		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("revising calendar item for {}", event.getResource());

			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof AnnouncementMessage) {
				// get the calendar event entity
				CalendarEvent cEvent = (CalendarEvent) entity;
				String cReference = cEvent.getReference();
				
				// update the calendar/news item links according to current announcement
				dashboardLogic.updateNewsLinks(cReference);
				dashboardLogic.updateCalendarLinks(cReference);
			}
			
			logger.debug("removing news links and news item for {}", event.getResource());
		}
	}
	
	public class ScheduleUpdateFrequencyEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateFrequencyEventProcessor.class);

		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY;
		}

		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());
			logger.debug("revising calendar item for {}", event.getResource());

			String entityReference = event.getResource();
			Entity entity = sakaiProxy.getEntity(entityReference );
			
			if(entity != null && entity instanceof CalendarEvent) {
				// get the calendar-event entity and its new time
				CalendarEvent cEvent = (CalendarEvent) entity;
				TimeRange range = cEvent.getRange();
				String calendarTimeLabelKey = scheduleEventTypeMap.get(cEvent.getType());
				Date newStartTime = new Date(range.firstTime().getTime());
				//Date newEndTime = new Date(range.lastTime().getTime());
				
				RecurrenceRule rule = cEvent.getRecurrenceRule();
				if(rule == null) {
					// remove repeating-event object and all instances?
					// add single calendar item?
				} else {
					// update the repeating-event object
					RepeatingCalendarItem repeater = dashboardLogic.getRepeatingCalendarItem(entityReference, calendarTimeLabelKey);
					if(repeater == null) {
						// create repeating calendar item?
						
					} else {
						String frequency = rule.getFrequency();
						if(frequency == null) {
							// what to do?
							logger.warn("Error trying to revise frequency of repeating event: event.getRecurrenceRule().getFrequency() is null");
						} else if(! frequency.equalsIgnoreCase(repeater.getFrequency())) {
							dashboardLogic.reviseRepeatingCalendarItemFrequency(entityReference, frequency);
						}
					}	
					
					// need to get each item in sequence and update its time
					Map<Integer, Date> dates = scheduleEntityType.generateRepeatingEventDates(entityReference, newStartTime, dashboardLogic.getRepeatingEventHorizon());
					Integer firstSequenceNumber = findSmallest(dates.keySet());
					
					SortedSet<Integer> futureSequenceNumbers = dashboardLogic.getFutureSequnceNumbers(entityReference, calendarTimeLabelKey, firstSequenceNumber);
					
					for(Map.Entry<Integer, Date> entry : dates.entrySet()) {
						if(futureSequenceNumbers.contains(entry.getKey())) {
							// update each existing calendar-item
							dashboardLogic.reviseCalendarItemTime(entityReference, calendarTimeLabelKey, entry.getKey(), entry.getValue());
							futureSequenceNumbers.remove(entry.getKey());
						} else {
							// add new calendar-items as needed
							CalendarItem calendarItem = dashboardLogic.createCalendarItem(repeater.getTitle(), entry.getValue(), calendarTimeLabelKey, entityReference, repeater.getContext(), repeater.getSourceType(), repeater.getSubtype(), repeater, entry.getKey());
							dashboardLogic.createCalendarLinks(calendarItem);
						}
						
						if(logger.isDebugEnabled()) {
							String msg = entry.getKey().toString() + " ==> " + entry.getValue().toString();
							CalendarItem oneItem = dashboardLogic.getCalendarItem(entityReference, calendarTimeLabelKey, entry.getKey());
							if(oneItem != null) {
								msg += " -----> " + oneItem.getCalendarTime().toString();
							}
							logger.debug(msg);
						}
					}
					
					// futureSequenceNumbers now contains only the id's of existing calendar-items that need to be removed
					for(Integer seqNum : futureSequenceNumbers) {
						CalendarItem item = dashboardLogic.getCalendarItem(entityReference, calendarTimeLabelKey, seqNum);
						dashboardLogic.removeCalendarLinks(entityReference, calendarTimeLabelKey, seqNum.intValue());
						dashboardLogic.removeCalendarItem(entityReference, calendarTimeLabelKey, seqNum);
					}
				}
				
			}

		}
	
	}
	
	public class ScheduleUpdateExcludedEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateExcludedEventProcessor.class);

		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_EXCLUDED;
		}

		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());

			// This is a case of a revision to one instance of a repeating event. If effect, 
			// a new calendar-event entity has been created to represent the instance that 
			// was excluded from the recurring event.  
			String eventId = event.getEvent();
			
			String eventContextString = event.getContext();
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			// handle add events
			if(entity != null && entity instanceof CalendarEvent) {
			
				CalendarEvent cEvent = (CalendarEvent) entity;

				String cEventReference = cEvent.getReference();
				
				Context context = dashboardLogic.getContext(eventContextString);
				
				SourceType sourceType = dashboardLogic.getSourceType(IDENTIFIER);
				
				// Third parameter in dashboardLogic.createCalendarItem() below should be a key for a label such as "Due Date: " or "Accept Until: " 
				// from dash_entity properties bundle for use in the dashboard list
				String type = cEvent.getType();
				// Based on the event-type, we may be able to select a key for a label? 
				String key = null;
				if(type == null) {
					key = "schedule.key2";
				} else {
					key = scheduleEventTypeMap.get(type);
					if(key == null) {
						key = "schedule.key2";
					}
				}
				
				// The schedule tool and/or service does not save a recurrence rule for the newly  
				// separated calendar event, though the UI elements are presented to the user,
				// so we will assume this to be a non-repeating event.
				CalendarItem calendarItem = dashboardLogic.createCalendarItem(cEvent.getDisplayName(), new Date(cEvent.getRange().firstTime().getTime()), key, cEventReference, context, sourceType, type, null, null);
				dashboardLogic.createCalendarLinks(calendarItem);
			} else {
				// for now, let's log the error
				logger.info("{} is not processed for entityReference {}",eventId, event.getResource());
			}
		}
	
	}
	
	public class ScheduleUpdateExclusionsEventProcessor implements EventProcessor {

		private Logger logger = LoggerFactory.getLogger(ScheduleUpdateExclusionsEventProcessor.class);
		
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_MODIFY_CALENDAR_EVENT_EXCLUSIONS;
		}

		public void processEvent(Event event) {
			logger.debug("\n\n\n============================================================\n{}\n=============================================================\n\n\n",
					event.toString());

			// This is a case of a revision to one instance of a repeating event, which is 
			// then no longer an instance of the repeating event. This event processor handles
			// the change to the repeating event: exclusion of one instance from the sequence.
			
			// TODO: remove the calendar item if it has already been created.
			
			// sample entityReference /calendar/event/f971f216-625c-4e5e-9609-05313f836ae2/main/!20111125131500000]20111125141500000!49!c4c1f3f5-c331-4c5e-8afe-07168ff0cc72
			String entityReference = null;
			int sequenceNumber = -1;
			if(event != null && event.getResource() != null) {
				String[] parts = event.getResource().split("/");
				if(parts.length > 5) {
					StringBuilder buf = new StringBuilder();
					buf.append("/");
					// 'calendar'
					buf.append(parts[1]);
					buf.append("/");
					// 'event'
					buf.append(parts[2]);
					buf.append("/");
					// site-id
					buf.append(parts[3]);
					buf.append("/");
					// 'main'
					buf.append(parts[4]);
					buf.append("/");
					String[] subparts = parts[5].split("!");
					if(subparts.length > 3) {
						// event-id
						buf.append(subparts[3]);
						sequenceNumber = Integer.parseInt(subparts[2]);
					}
					entityReference = buf.toString();
				}
				logger.info("processEvent() {} {}", entityReference, sequenceNumber);

				if(entityReference != null && sequenceNumber >= -1) {
					String calendarTimeLabelKey = null;
					CalendarItem calendarItem = dashboardLogic.getCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
					dashboardLogic.removeCalendarItem(entityReference, calendarTimeLabelKey, sequenceNumber);
				}
				
			}
		}
	
	}

	public Integer findSmallest(Set<Integer> numbers) {
		SortedSet<Integer> set = new TreeSet<Integer>(numbers);
		return set.first();
	}

	public Integer findLargest(Set<Integer> numbers) {
		SortedSet<Integer> set = new TreeSet<Integer>(numbers);
		return set.last();
	}
	
}
