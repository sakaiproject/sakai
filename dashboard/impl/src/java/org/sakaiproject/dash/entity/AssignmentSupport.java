/**
 * 
 */
package org.sakaiproject.dash.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.logic.SakaiProxy;
import org.sakaiproject.dash.model.CalendarItem;
import org.sakaiproject.dash.model.Context;
import org.sakaiproject.dash.model.NewsItem;
import org.sakaiproject.dash.model.SourceType;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.event.api.Event;

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

		public static final String VALUE_MAX_GRADE = "grade-max";
		public static final String LABEL_MAX_GRADE = "grade-max-label";
		public static final String VALUE_OPEN_TIME = "open-time";
		public static final String LABEL_OPEN_TIME = "open-time-label";
		public static final String VALUE_DUE_TIME = "due-time";
		public static final String LABEL_DUE_TIME = "due-time-label";
		public static final String VALUE_CLOSE_TIME = "close-time";
		public static final String LABEL_CLOSE_TIME = "close-time-label";
		public static final String VALUE_SUBMISSION_STATUS = "submission-status";
		public static final String LABEL_SUBMISSION_STATUS = "submission-status-label";


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
				ResourceProperties props = assn.getProperties();
				// "entity-type": "assignment"
				values.put(EntityType.VALUE_ENTITY_TYPE, IDENTIFIER);
				// "grade-type": ""
				values.put(VALUE_GRADE_TYPE, assn.getContent().getTypeOfGradeString(assn.getContent().getTypeOfGrade()));
				// "submission-type": ""
				//values.put(VALUE_SUBMISSION_TYPE, Integer.toString(assn.getContent().getTypeOfSubmission()));
				if(Assignment.SCORE_GRADE_TYPE == assn.getContent().getTypeOfGrade()) {
					values.put(VALUE_MAX_GRADE, assn.getContent().getMaxGradePointDisplay());
				}
				// "calendar-time": 1234567890
				values.put(VALUE_CALENDAR_TIME, assn.getDueTimeString());
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
			props.put(LABEL_CALENDAR_TIME, rl.getString("assignment.calendar.time"));
			props.put(LABEL_NEWS_TIME, rl.getString("assignment.news.time"));
			props.put(LABEL_USER_NAME, rl.getString("assignment.user.name"));
			props.put(LABEL_GRADE_TYPE, rl.getString("assignment.grade.type"));
			props.put(LABEL_MAX_GRADE, rl.getString("assignment.max.grade"));
			props.put(LABEL_OPEN_TIME, rl.getString("assignment.open.time"));
			props.put(LABEL_DUE_TIME, rl.getString("assignment.due.time"));
			props.put(LABEL_CLOSE_TIME, rl.getString("assignment.close.time"));
			//props.put(LABEL_ATTACHMENTS, rl.getString("assignment.attachments"));
			return props;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getAccessUrlTarget(java.lang.String)
		 */
		public String getAccessUrlTarget(String entityReference) {
			// ignored
			return null;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getOrder(java.lang.String, java.lang.String)
		 */
		public List<List<String>> getOrder(String entityReference, String localeCode) {
			List<List<String>> order = new ArrayList<List<String>>();
			List<String> valueList = new ArrayList<String>(Arrays.asList(VALUE_TITLE, VALUE_USER_NAME, VALUE_NEWS_TIME, VALUE_OPEN_TIME, VALUE_DUE_TIME, VALUE_CLOSE_TIME));
			
			for (String value : valueList)
			{
				List<String> section = new ArrayList<String>();
				section.add(value);
				order.add(section);
			}		
			List<String> section1 = new ArrayList<String>();
			section1.add(VALUE_GRADE_TYPE);
			section1.add(VALUE_MAX_GRADE);
			order.add(section1);
			List<String> section2 = new ArrayList<String>();
			section2.add(VALUE_DESCRIPTION);
			order.add(section2);
			List<String> section3 = new ArrayList<String>();
			section3.add(VALUE_ATTACHMENTS);
			order.add(section3);
			List<String> section5 = new ArrayList<String>();
			section5.add(VALUE_MORE_INFO);
			order.add(section5);

			return order;
		}

		public void init() {
			logger.info("init()");
			dashboardLogic.registerEntityType(this);
		}
		
		public boolean isAvailable(String entityReference) {
			// TODO Auto-generated method stub
			return true;
		}

		public Date getReleaseDate(String entityReference) {
			// TODO Auto-generated method stub
			return null;
		}

		public Date getRetractDate(String entityReference) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getString(String key) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key);
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
				
				/** consult in assignment entity provider to get the deepLink **/
				String assignmentUrl = "";
				Map<String, Object> assignData = new HashMap<String, Object>();
			    Map<String, Object> params = new HashMap<String, Object>();
			    
			    // get the link as student view first
			    params.put("allowReadAssignment", Boolean.TRUE);
			    params.put("allowAddAssignment", Boolean.FALSE);
			    params.put("allowSubmitAssignment", Boolean.TRUE);
	            // pass in the assignment reference to get the assignment data we need
	            //ActionReturn ret = entityBroker.executeCustomAction(assn.getReference(), "deepLinkWithPermissions", params, null);
	            //if (ret != null && ret.getEntityData() != null) {
	            //        Object returnData = ret.getEntityData().getData();
	            //        assignData = (Map<String, Object>)returnData;
	            //    }
	            //assignmentUrl = (String) assignData.get("assignmentUrl");
	            
				SourceType sourceType = dashboardLogic.getSourceType("assignment");
				if(sourceType == null) {
					sourceType = dashboardLogic.createSourceType("assignment", SakaiProxy.PERMIT_ASSIGNMENT_ACCESS, EntityLinkStrategy.SHOW_PROPERTIES);
				}
				
				
				NewsItem newsItem = dashboardLogic.createNewsItem(assn.getTitle(), event.getEventTime(), assn.getReference(), assignmentUrl, context, sourceType);
				dashboardLogic.createNewsLinks(newsItem);
			
				// TODO: Third parameter should be a key for a label such as "Due Date: " or "Accept Until: " 
				// from dash_entity properties bundle for use in the dashboard list
				CalendarItem calendarItem = dashboardLogic.createCalendarItem(assn.getTitle(), new Date(assn.getDueTime().getTime()), "assignment.due.date", assn.getReference(), assignmentUrl, context, sourceType);
				dashboardLogic.createCalendarLinks(calendarItem);
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
			dashboardLogic.removeCalendarItem(ref);
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			dashboardLogic.removeNewsItem(ref);

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
				dashboardLogic.reviseNewsItemTitle(assn.getReference(), assn.getTitle());
				
				// update news item title
				dashboardLogic.reviseCalendarItemTitle(assn.getReference(), assn.getTitle());
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
				
				// TODO
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
				dashboardLogic.reviseCalendarItemTime(assn.getReference(), new Date(assn.getOpenTime().getTime()));
			}
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}

		}

	}
}
