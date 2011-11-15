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

import org.apache.log4j.Logger;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentService;
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
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

public class AssignmentSupport {
	
	private static Logger logger = Logger.getLogger(AssignmentSupport.class);
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	protected AssignmentService assignmentService;
	public void setAssignmentService(AssignmentService assignmentService)
	{
		this.assignmentService = assignmentService;
	}
	
	protected EntityBroker entityBroker;
	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
	}
	
	public static final String IDENTIFIER = "assignment";
	
	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(new AssignmentEntityType());
		this.dashboardLogic.registerEventProcessor(new AssignmentNewEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AssignmentRemoveEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AssignmentUpdateTitleEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AssignmentUpdateAccessEventProcessor());
		this.dashboardLogic.registerEventProcessor(new AssignmentUpdateOpenDateEventProcessor());
	}
	
	/**
	 * Inner class
	 * @author zqian
	 *
	 */
	public class AssignmentEntityType implements EntityType {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return IDENTIFIER;
		}

		public static final String LABEL_METADATA = "assn_metadata-label";
		public static final String LABEL_DATA = "assn_data-label";
		
		// {due-time} {close-time} {grade-type} {grade-max}
		// {user-name} {news-time} {open-time}
		public static final String VALUE_DUE_TIME = "due-time";
		public static final String VALUE_CLOSE_TIME = "close-time";
		public static final String VALUE_GRADE_TYPE = "grade-type";
		public static final String VALUE_MAX_GRADE = "grade-max";
		public static final String VALUE_OPEN_TIME = "open-time";

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getEntityLinkStrategy(java.lang.String)
		 */
		public EntityLinkStrategy getEntityLinkStrategy(String entityReference) {
			
			return EntityLinkStrategy.SHOW_PROPERTIES;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getValues(java.lang.String, java.lang.String)
		 */
		public Map<String, Object> getValues(String entityReference,
				String localeCode) {
			Map<String, Object> values = new HashMap<String, Object>();

			Assignment assn = (Assignment) sakaiProxy.getEntity(entityReference);
			ResourceLoader rl = new ResourceLoader("dash_entity");
			if(assn != null) {
				// {grade-type} {grade-max}
				// {user-name} 
				ResourceProperties props = assn.getProperties();
				// "grade-type": ""
				values.put(VALUE_GRADE_TYPE, assn.getContent().getTypeOfGradeString(assn.getContent().getTypeOfGrade()));

				try {
					DateFormat df = DateFormat.getDateTimeInstance();
					values.put(VALUE_NEWS_TIME, df.format(new Date(props.getTimeProperty(ResourceProperties.PROP_CREATION_DATE).getTime())));
					values.put(VALUE_OPEN_TIME, df.format(new Date(assn.getOpenTime().getTime())));
					values.put(VALUE_DUE_TIME, df.format(new Date(assn.getDueTime().getTime())));
					values.put(VALUE_CLOSE_TIME, df.format(new Date(assn.getCloseTime().getTime())));
					
				} catch (EntityPropertyNotDefinedException e) {
					logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyNotDefinedException: " + e);
				} catch (EntityPropertyTypeException e) {
					logger.warn("getValues(" + entityReference + "," + localeCode + ") EntityPropertyTypeException: " + e);
				}
				// "entity-type": "assignment"
				values.put(EntityType.VALUE_ENTITY_TYPE, IDENTIFIER);
				
				
				// "submission-type": ""
				//values.put(VALUE_SUBMISSION_TYPE, Integer.toString(assn.getContent().getTypeOfSubmission()));
				if(Assignment.SCORE_GRADE_TYPE == assn.getContent().getTypeOfGrade()) {
					values.put(VALUE_MAX_GRADE, assn.getContent().getMaxGradePointDisplay());
				}
				// "calendar-time": 1234567890
				values.put(VALUE_CALENDAR_TIME, assn.getDueTimeString());
				// "description": "Long thing, markup, escaped",
				values.put(VALUE_DESCRIPTION, assn.getContent().getInstructions());
				// "title": "Assignment hoedown"
				values.put(VALUE_TITLE, assn.getTitle());
				// "user-name": "Creator's Name"
				User user = sakaiProxy.getUser(assn.getCreator());
				if(user != null) {
					values.put(VALUE_USER_NAME, user.getDisplayName());
				}
				
				// pass in the assignment reference to get the assignment data we need
				Map<String, Object> assignData = new HashMap<String, Object>();
	            ActionReturn ret = entityBroker.executeCustomAction(assn.getReference(), "deepLink", null, null);
	            if (ret != null && ret.getEntityData() != null) {
	            	Object returnData = ret.getEntityData().getData();
	            	assignData = (Map<String, Object>)returnData;
	            }
	            String assignmentUrl = (String) assignData.get("assignmentUrl");
				
				// "more-info"
				List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();
				Map<String,String> infoItem = new HashMap<String,String>();
				infoItem.put(VALUE_INFO_LINK_URL, assignmentUrl);
				infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("assignment.info.link"));
				infoItem.put(VALUE_INFO_LINK_TARGET, "_top");
				infoList.add(infoItem);
				values.put(VALUE_MORE_INFO, infoList);
				
				// "attachments": [ ... ]
				List<Reference> attachments = assn.getContent().getAttachments();
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
			// TODO: create language bundle here or have SakaiProxy get the language bundle from assn??
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Map<String, String> props = new HashMap<String, String>();
			
			// detect the assignment type, so that maxpoint is only shown when the assignment grading is point-based.
			Assignment assn = (Assignment) sakaiProxy.getEntity(entityReference);
			if(Assignment.SCORE_GRADE_TYPE == assn.getContent().getTypeOfGrade()) {
				props.put(LABEL_DATA, rl.getString("assignment.data.with.maxpoint"));
			}
			else
			{
				props.put(LABEL_DATA, rl.getString("assignment.data"));
			}
			props.put(LABEL_METADATA, rl.getString("assignment.metadata"));
			//props.put(LABEL_DESCRIPTION, rl.getString("assignment.description"));
			//props.put(LABEL_ATTACHMENTS, rl.getString("assignment.attachments"));
			
			return props;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getOrder(java.lang.String, java.lang.String)
		 */
		public List<List<String>> getOrder(String entityReference, String localeCode) {
			List<List<String>> order = new ArrayList<List<String>>();
			
			List<String> section1 = new ArrayList<String>();
			section1.add(VALUE_TITLE);
			order.add(section1);
			
			List<String> section2 = new ArrayList<String>();
			section2.add(LABEL_DATA);
			order.add(section2);
			
			List<String> section3 = new ArrayList<String>();
			section3.add(VALUE_DESCRIPTION);
			order.add(section3);
			
			List<String> section4 = new ArrayList<String>();
			section4.add(VALUE_ATTACHMENTS);
			order.add(section4);
			
			List<String> section5 = new ArrayList<String>();
			section5.add(LABEL_METADATA);
			order.add(section5);
			
			List<String> section6 = new ArrayList<String>();
			section6.add(VALUE_MORE_INFO);
			order.add(section6);

			return order;
		}

		public void init() {
			logger.info("init()");
			dashboardLogic.registerEntityType(this);
		}
		
		public boolean isAvailable(String entityReference) {
			Assignment assn = (Assignment) sakaiProxy.getEntity(entityReference);
			Date openTime = new Date(assn.getOpenTime().getTime());
			
			if (openTime != null && openTime.after(new Date()))
			{
				// assignment is not open yet, don't create links
				return false;
			}
			else
			{
				return true;
			}
		}

		public String getString(String key, String dflt) {
			logger.debug("getString() " + key);
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}
		
		public boolean isUserPermitted(String sakaiUserId, String accessPermission,
				String entityReference, String contextId) {
			// for now just check the permission for submit assignment
			List users = assignmentService.allowAddSubmissionUsers(entityReference);
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
		
		public String getGroupTitle(int numberOfItems, String contextTitle) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Object[] args = new Object[]{ numberOfItems, contextTitle };
			return rl.getFormattedMessage("assignment.grouped.title", args );
	}

		public String getIconUrl(String subtype) {
			// we will use the Deadline icon for now, the same as the one used in schedule tool
			return "/library/image/sakai/deadline.gif";
		}
	}
	
	/**
	 * Inner Class: AssignmentNewEventProcessor
	 */
	public class AssignmentNewEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {

			return SakaiProxy.EVENT_ASSIGNMENT_NEW;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof Assignment) {
			
				Assignment assn = (Assignment) entity;
				Context context = dashboardLogic.getContext(event.getContext());
				if(context == null) {
					context = dashboardLogic.createContext(event.getContext());
				}
	            
				SourceType sourceType = dashboardLogic.getSourceType("assignment");
				if(sourceType == null) {
					sourceType = dashboardLogic.createSourceType("assignment", SakaiProxy.PERMIT_ASSIGNMENT_ACCESS, EntityLinkStrategy.SHOW_PROPERTIES);
				}
				
				String assnReference = assn.getReference();
				
				NewsItem newsItem = dashboardLogic.createNewsItem(assn.getTitle(), event.getEventTime(), "assignment.added", assnReference, context, sourceType, null);
				CalendarItem calendarDueDateItem = dashboardLogic.createCalendarItem(assn.getTitle(), new Date(assn.getDueTime().getTime()), "assignment.due.date", assnReference, context, sourceType, null, null);
				CalendarItem calendarCloseDateItem = assn.getCloseTime().equals(assn.getDueTime())? null : dashboardLogic.createCalendarItem(assn.getTitle(), new Date(assn.getCloseTime().getTime()), "assignment.close.date", assnReference, context, sourceType, null, null);
				
				if(dashboardLogic.isAvailable(assnReference, IDENTIFIER)) {
					// if the assignment is open, add the news links
					dashboardLogic.createNewsLinks(newsItem);
					dashboardLogic.createCalendarLinks(calendarDueDateItem);
					if (calendarCloseDateItem != null)
					{
						// if the close time is different from due time, create a separate close time item
						dashboardLogic.createCalendarLinks(calendarCloseDateItem);
					}
					// currently, we don't retract assignment item once it is available
				}
				else
				{
					// assignment is not open yet, schedule for check later
					dashboardLogic.scheduleAvailabilityCheck(assnReference, IDENTIFIER, new Date(assn.getOpenTime().getTime()));
				}
				
			} else {
				// for now, let's log the error
				logger.warn("Error trying to process " + this.getEventIdentifer() + " event for entityReference " + event.getResource());
			}
		}
	}
	
	/**
	 * Inner Class: AssignmentRemoveEventProcessor
	 */
	public class AssignmentRemoveEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_ASSIGNMENT_REMOVE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			
			String ref = event.getResource();
			dashboardLogic.removeCalendarItems(ref);
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			dashboardLogic.removeNewsItem(ref);
			
			// also remove all availability checks
			dashboardLogic.removeAllScheduledAvailabilityChecks(ref);
		}

	}
	
	/**
	 * Inner Class: AssignmentUpdateTitleEventProcessor
	 */
	public class AssignmentUpdateTitleEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_UPDATE_ASSIGNMENT_TITLE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof Assignment) {
				// get the assignment entity and its current title
				Assignment assn = (Assignment) entity;
				
				// update news item title
				dashboardLogic.reviseNewsItemTitle(assn.getReference(), assn.getTitle(), null, null);
				
				// update news item title
				dashboardLogic.reviseCalendarItemsTitle(assn.getReference(), assn.getTitle());
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
	
	/**
	 * Inner Class: AssignmentUpdateAccessEventProcessor
	 */
	public class AssignmentUpdateAccessEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_UPDATE_ASSIGNMENT_ACCESS;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof Assignment) {
				// get the assignment entity and its current title
				Assignment assn = (Assignment) entity;
				String assnReference = assn.getReference();
				
				if(dashboardLogic.isAvailable(assnReference, IDENTIFIER)) {
					// update the calendar and news links
					dashboardLogic.updateCalendarLinks(assn.getReference());
					dashboardLogic.updateNewsLinks(assnReference);
				}
				else
				{
					// assignment is not open yet, schedule for check later
					dashboardLogic.scheduleAvailabilityCheck(assnReference, IDENTIFIER, new Date(assn.getOpenTime().getTime()));
				}
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
	
	/**
	 * Inner Class: AssignmentUpdateOpenDateEventProcessor
	 */
	public class AssignmentUpdateOpenDateEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_UPDATE_ASSIGNMENT_OPENDATE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			Entity entity = sakaiProxy.getEntity(event.getResource());
			
			if(entity != null && entity instanceof Assignment) {
				// get the assignment entity and its current title
				Assignment assn = (Assignment) entity;
				String assnReference = assn.getReference();
				
				//1. remove all scheduled availability checks for this assignment reference
				dashboardLogic.removeAllScheduledAvailabilityChecks(assnReference);
				
				// 2. check the availability based on updated open time
				if(dashboardLogic.isAvailable(assnReference, IDENTIFIER)) {
					// TODO: DASH-82 for updatet News item event
				}
				else
				{
					// if the assignment if not open now, remove any previous assignment calendar items and links
					dashboardLogic.removeCalendarItems(assnReference);
					dashboardLogic.removeNewsItem(assnReference);
					
					// assignment is not open yet, schedule for check later
					dashboardLogic.scheduleAvailabilityCheck(assnReference, IDENTIFIER, new Date(assn.getOpenTime().getTime()));
				}
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
}
