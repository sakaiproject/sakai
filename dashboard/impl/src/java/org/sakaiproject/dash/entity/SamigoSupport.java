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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedEvaluationModel;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.dash.listener.EventProcessor;
import org.sakaiproject.dash.logic.DashboardLogic;
import org.sakaiproject.dash.app.SakaiProxy;
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
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.util.ResourceLoader;

/**
 * Samigo support for Dashboard
 *
 * Processes only add, remove and change properties. If an instructor edits a published
 *   assessment, it unpublishes while he's editing and republishes. I don't think we
 *   want to show those events. If he changes due date or name during the republish
 *   we'll get a settings event, which we will process.
 * There's no way for an instructor to change the group to which a test is released
 *  WARNING: Lessons uses groups for access control. I don't currently take that
 *    into account. I believe the correct behavior is to show the group membership
 *    set by the instructor, even if Lessons hasn't released it to the user yet.
 *    That will require use of a Lessons API. I haven't done that yet. It should
 *    also be done for Assignments.
 * Samigo seems to create calendar entries. I'm not putting calendar events here,
 *   because that would result in two calendar entries for each test.
 *  WARNING: When a student is added to a class, they don't get a copy of any 
 *    existing calendar notifications. If we did calendar events here, they would.
 *    But that would result in duplicate events for people already in the site.
 *    There's no way to win.
 * This code accesses published assignments directly for access checks, where
 *   Assignments uses the entity broker. It appears that Samigo's entity broker
 *   code doesn't take group membership into account.
 */
public class SamigoSupport {
	
	private static Logger logger = LoggerFactory.getLogger(SamigoSupport.class);
	private static final String CAN_TAKE = "assessment.takeAssessment";
	private static final String CAN_PUBLISH = "assessment.publishAssessment.any";

	ResourceLoader rl = new ResourceLoader("dash_entity");
	
	protected DashboardLogic dashboardLogic;
	public void setDashboardLogic(DashboardLogic dashboardLogic) {
		this.dashboardLogic = dashboardLogic;
	}
	
	protected SakaiProxy sakaiProxy;
	public void setSakaiProxy(SakaiProxy proxy) {
		this.sakaiProxy = proxy;
	}
	
	PublishedAssessmentService publishedAssessmentService = new PublishedAssessmentService();
        AuthzGroupService authzGroupService = (AuthzGroupService) ComponentManager.get(AuthzGroupService.class);
	
	protected EntityBroker entityBroker;
	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
	}
	
	public static final String IDENTIFIER = "samigo";
	
	public static String makeRef(String ref) {

	    if (ref.startsWith("/sam_pub/"))
		return ref;

	    int i = ref.indexOf("publishedAssessmentId=");
	    if (i >= 0) {
		i = i + "publishedAssessmentId=".length();
	    } else {
		i = ref.indexOf("publisedAssessmentId=");
		if (i >= 0)
		    i = i + "publisedAssessmentId=".length();
	    }
	    if (i < 0)
		return null;
	    
	    int j = i;
	    int len = ref.length();
	    while (j < len && Character.isDigit(ref.charAt(j)))
		j++;
	    
	    Long pubId = new Long(ref.substring(i, j));
	    
	    return "/sam_pub/" + pubId.toString();
	    
	}

	// allows either event.getResource or an actual ID
	private PublishedAssessmentFacade getPublishedAssessment(String entityReference) {
	    if (entityReference == null)
		return null;
	    
	    if (!entityReference.startsWith("/sam_pub"))
		entityReference = makeRef(entityReference);
	    
	    if (entityReference == null)
		return null;
	    
	    String idString = entityReference.substring("/sam_pub/".length());
	    
	    try {
		return publishedAssessmentService.getPublishedAssessment(idString);
	    } catch (Exception e) {
		return null;
	    }

	}

	public void init() {
		logger.info("init()");
		
		this.dashboardLogic.registerEntityType(new SamigoEntityType());
		this.dashboardLogic.registerEventProcessor(new SamigoPublishEventProcessor());
		this.dashboardLogic.registerEventProcessor(new SamigoPubSettingEventProcessor());
		//		this.dashboardLogic.registerEventProcessor(new SamigoPubUnpublishEventProcessor());
		//		this.dashboardLogic.registerEventProcessor(new SamigoPubRepublishEventProcessor());
		this.dashboardLogic.registerEventProcessor(new SamigoPubRemoveEventProcessor());

	}
	
	/**
	 * Inner class
	 * @author zqian
	 *
	 */
	public class SamigoEntityType implements DashboardEntityInfo {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getIdentifier()
		 */
		public String getIdentifier() {
			return IDENTIFIER;
		}

		public static final String LABEL_METADATA = "samigo_metadata-label";
		public static final String LABEL_DATA = "samigo_data-label";
		
		// {due-time} {close-time} {grade-type} {grade-max}
		// {user-name} {news-time} {open-time}
		public static final String VALUE_DUE_TIME = "due-time";
		public static final String VALUE_CLOSE_TIME = "close-time";
		public static final String VALUE_GRADE_TYPE = "grade-type";
		public static final String VALUE_MAX_GRADE = "grade-max";
		public static final String VALUE_OPEN_TIME = "open-time";

	    // Events for samigo don't use an actual reference for their resource.
	    // Within dashboard we use a reosurce like /sam_pub/NNNN
	    // This code makes the source ID



		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.entity.EntityType#getValues(java.lang.String, java.lang.String)
		 */
		public Map<String, Object> getValues(String entityReference,
				String localeCode) {
			Map<String, Object> values = new HashMap<String, Object>();

			ResourceLoader rl = new ResourceLoader("dash_entity");
			PublishedAssessmentFacade pub = getPublishedAssessment(entityReference);
			if(pub != null) {
				AssessmentAccessControlIfc accessControl = null;
				accessControl = pub.getAssessmentAccessControl();

				// {grade-type} {grade-max}
				// {user-name} 
				// "grade-type": ""

				DateFormat df = DateFormat.getDateTimeInstance();

				values.put(VALUE_NEWS_TIME, pub.getCreatedDate() == null ? rl.getString("dash.notset") : df.format(pub.getCreatedDate()));
				values.put(VALUE_OPEN_TIME, accessControl.getStartDate() == null ? rl.getString("dash.notset") : df.format(accessControl.getStartDate()));
				values.put(VALUE_DUE_TIME, accessControl.getDueDate() == null ? rl.getString("dash.notset") : df.format(accessControl.getDueDate()));
				values.put(VALUE_CLOSE_TIME, accessControl.getRetractDate() == null ? rl.getString("dash.notset") : df.format(accessControl.getRetractDate()));
				
				// "entity-type": "assignment"
				values.put(DashboardEntityInfo.VALUE_ENTITY_TYPE, IDENTIFIER);

				values.put(VALUE_MAX_GRADE, pub.getTotalScore().toString());

				// "calendar-time": 1234567890
				values.put(VALUE_CALENDAR_TIME, accessControl.getDueDate() == null ? rl.getString("dash.notset") : df.format(accessControl.getDueDate()));
				// "description": "Long thing, markup, escaped",
				values.put(VALUE_DESCRIPTION, pub.getDescription());
				// "title": "Assignment hoedown"
				values.put(VALUE_TITLE, pub.getTitle());
				// "user-name": "Creator's Name"
				values.put(VALUE_USER_NAME, pub.getCreatedBy());
				
				// pass in the assignment reference to get the assignment data we need
				Map<String, Object> assignData = new HashMap<String, Object>();
				
				// "more-info"
				List<Map<String,String>> infoList = new ArrayList<Map<String,String>>();

				String alias = pub.getAssessmentMetaDataByLabel("ALIAS");

				Map<String,String> infoItem = new HashMap<String,String>();
				infoItem.put(VALUE_INFO_LINK_URL, "/samigo-app/servlet/Login?id=" + alias);
				infoItem.put(VALUE_INFO_LINK_TITLE, rl.getString("samigo.info.link"));
				infoItem.put(VALUE_INFO_LINK_TARGET, "_top");
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
			// TODO: create language bundle here or have SakaiProxy get the language bundle from assn??
			ResourceLoader rl = new ResourceLoader("dash_entity");
			Map<String, String> props = new HashMap<String, String>();
			
			// detect the assignment type, so that maxpoint is only shown when the assignment grading is point-based.

			PublishedAssessmentFacade pub = getPublishedAssessment(entityReference);
			if (pub != null && pub.getTotalScore() > 0.0)
			    props.put(LABEL_DATA, rl.getString("samigo.data.with.maxpoint"));
			else
			    props.put(LABEL_DATA, rl.getString("samigo.data"));

			props.put(LABEL_METADATA, rl.getString("samigo.metadata"));
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
		    PublishedAssessmentFacade pub = getPublishedAssessment(entityReference);

		    if (pub == null) return false;

		    String siteId = publishedAssessmentService.getPublishedAssessmentOwner(pub.getPublishedAssessmentId());
		    if (!sakaiProxy.isSitePublished(siteId)) {
			// return false if site is unpublished
			return false;
		    }

		    return isAvailable(pub);
		}

		public boolean isAvailable(PublishedAssessmentFacade pub) {
		    if (pub == null)
			return false;

		    if (pub.getStatus().equals(PublishedAssessmentFacade.DEAD_STATUS))
			return false;

		    AssessmentAccessControlIfc accessControl = pub.getAssessmentAccessControl();
		    Date open = accessControl.getStartDate();
		    
		    // not open yet?
		    if (open != null && open.after(new Date()))
			return false;
		    return true;

		}

		/**
		 * {@inheritDoc}
		 */
		public String getEventDisplayString(String key, String dflt) {
			logger.debug("getString() " + key);
			ResourceLoader rl = new ResourceLoader("dash_entity");
			return rl.getString(key, dflt);
		}
		
		public boolean isUserPermitted(String sakaiUserId, String entityReference,
				String contextId) {
		    if (!isAvailable(entityReference))
			return false;
		    List<String> ret = new ArrayList<String>();

		    try {

			entityReference = makeRef(entityReference);
			String pubId = entityReference.substring("/sam_pub/".length());
			PublishedAssessmentIfc pub = publishedAssessmentService.getPublishedAssessment(pubId);
			AssessmentAccessControlIfc control = null;
			control = pub.getAssessmentAccessControl();
			String releaseTo = control.getReleaseTo();
			Long pubIdNumber = new Long(pubId);
			String siteId = publishedAssessmentService.getPublishedAssessmentOwner(pubIdNumber);
			AuthzGroup realm = authzGroupService.getAuthzGroup("/site/" + siteId);
			Site site = SiteService.getSite(siteId);

			if (control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
			    Set<String> groupIds = new HashSet<String>(PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
				getReleaseToGroupIdsForPublishedAssessment(pubId));
			    Collection<Group> groups = site.getGroupsWithMember(sakaiUserId);
			    for (Group group:groups)
				if (groupIds.contains(group.getId()))
				    return true;
			    return false;
			} else {
			    // anonymous or site
			    return (realm.getMember(sakaiUserId) != null);
			}
		    } catch (Exception e) {
			return false;
		    }

		}
		
		public String getGroupTitle(int numberOfItems, String contextTitle, String labelKey) {
			ResourceLoader rl = new ResourceLoader("dash_entity");
			String titleKey = "samigo.grouped.created";
			if(labelKey != null && "dash.updated".equals(labelKey)) {
				titleKey = "samigo.grouped.updated";
			}
			Object[] args = new Object[]{ numberOfItems, contextTitle };
			return rl.getFormattedMessage(titleKey, args );
	}

		public String getIconUrl(String subtype) {
			// we will use the Assignment tool icon for now
			return "/library/image/silk/pencil.png";
		}

		public List<String> getUsersWithAccess(String entityReference) {
		    if (!isAvailable(entityReference))
			return new ArrayList<String>();
		    List<String> ret = new ArrayList<String>();

		    try {

			entityReference = makeRef(entityReference);
			String pubId = entityReference.substring("/sam_pub/".length());
			PublishedAssessmentIfc pub = publishedAssessmentService.getPublishedAssessment(pubId);
			AssessmentAccessControlIfc control = null;
			control = pub.getAssessmentAccessControl();
			String releaseTo = control.getReleaseTo();
			Long pubIdNumber = new Long(pubId);
			String siteId = publishedAssessmentService.getPublishedAssessmentOwner(pubIdNumber);
			AuthzGroup realm = authzGroupService.getAuthzGroup("/site/" + siteId);
			Site site = SiteService.getSite(siteId);

			if (control.getReleaseTo().equals(AssessmentAccessControlIfc.RELEASE_TO_SELECTED_GROUPS)) {
			    List<String> groupIds = PersistenceService.getInstance().getPublishedAssessmentFacadeQueries().
				getReleaseToGroupIdsForPublishedAssessment(pubId);
			    Collection<String> users = site.getMembersInGroups(new HashSet<String>(groupIds));
			    ret.addAll(users);
			} else {
			    // anonymous or site
			    Set<Member> siteMembers = realm.getMembers();
			    for (Member m:siteMembers)
				ret.add(m.getUserId());
			}
		    } catch (Exception e) {
			ret = new ArrayList<String>();
		    }

		    return ret;

		}

		
	}
	
    /* NOTE: currently Samigo issues calendar events itself. Thus we don't create
       or update calendar entries in this code.
    */

	/**
	 * Inner Class: AssignmentNewEventProcessor
	 */
	public class SamigoPublishEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
		    return SakaiProxy.EVENT_PUBASSESSMENT_PUBLISH;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			PublishedAssessmentFacade pub = getPublishedAssessment(event.getResource());
			
			if(pub != null) {
			
				AssessmentAccessControlIfc accessControl = null;
				accessControl = pub.getAssessmentAccessControl();

				Context context = dashboardLogic.getContext(event.getContext());
	            
				SourceType sourceType = dashboardLogic.getSourceType(IDENTIFIER);;
				
				String ref = event.getResource();
				String samigoReference = makeRef(ref);
				
				NewsItem newsItem = dashboardLogic.createNewsItem(pub.getTitle(), event.getEventTime(), "samigo.added", makeRef(event.getResource()), context, sourceType, null);
				//				CalendarItem calendarDueDateItem = dashboardLogic.createCalendarItem(pub.getTitle(), accessControl.getDueDate(), "samigo.due.date", samigoReference, context, sourceType, (String) null, (RepeatingCalendarItem) null, (Integer) null);

				if(dashboardLogic.isAvailable(samigoReference, IDENTIFIER)) {

					// add the news links as appropriate
					dashboardLogic.createNewsLinks(newsItem);
					
					//if (calendarDueDateItem != null)
					//{
						// create links for due date Calendar item
					    //						dashboardLogic.createCalendarLinks(calendarDueDateItem);
					    //	System.out.println("calendar");
					//}
					// currently, we don't retract assignment item once it is available
				}
				else
				{
					// assignment is not open yet, schedule for check later
					dashboardLogic.scheduleAvailabilityCheck(samigoReference, IDENTIFIER, accessControl.getStartDate());
				}
				
			} else {
				// for now, let's log the error
				logger.warn("Error trying to process event for entityReference " + event.getResource());
			}
			//			return null;
			//		    }});
		       
		}
	}

	/**
	 * Inner Class: AssignmentRemoveEventProcessor
	 */
	public class SamigoPubRemoveEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_PUBASSESSMENT_REMOVE;
		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("removing calendar links and calendar item for " + event.getResource());
			}
			
			String ref = makeRef(event.getResource());
			//dashboardLogic.removeCalendarItems(ref);
			
			if(logger.isDebugEnabled()) {
				logger.debug("removing news links and news item for " + event.getResource());
			}
			dashboardLogic.removeNewsItem(ref);
			
			// also remove all availability checks
			dashboardLogic.removeAllScheduledAvailabilityChecks(ref);
		}

	}

	// We just have one kind of event for changing things. 
	// handle change in title and open date. Close and due are
	// are handled by properties and calendar, but don't need
	// to be in the event itself


	/**
	 * Inner Class: SamigoPubSettingEventProcessor
	 */
	public class SamigoPubSettingEventProcessor implements EventProcessor {
		
		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#getEventIdentifer()
		 */
		public String getEventIdentifer() {
			
			return SakaiProxy.EVENT_PUBASSESSMENT_SETTINGS;

		}

		/* (non-Javadoc)
		 * @see org.sakaiproject.dash.listener.EventProcessor#processEvent(org.sakaiproject.event.api.Event)
		 */
		public void processEvent(Event event) {
			
			if(logger.isDebugEnabled()) {
				logger.debug("\n\n\n=============================================================\n" + event  
						+ "\n=============================================================\n\n\n");
			}
			if(logger.isDebugEnabled()) {
				logger.debug("changing settings for " + event.getResource());
			}
			
			String ref = makeRef(event.getResource());
			PublishedAssessmentFacade pub = getPublishedAssessment(ref);
			AssessmentAccessControlIfc accessControl = pub.getAssessmentAccessControl();
			Date open = accessControl.getStartDate();
			
			NewsItem item = dashboardLogic.getNewsItem(ref);
			if (item == null) {
			    (new SamigoPublishEventProcessor()).processEvent(event);
			    return;
			}

			boolean needUpdate = false;
			if (!item.getTitle().equals(pub.getTitle())) {
			    item.setNewsTime(event.getEventTime());
			    item.setNewsTimeLabelKey("dash.updated");
			    dashboardLogic.reviseNewsItemTitle(ref, pub.getTitle(), item.getNewsTime(), item.getNewsTimeLabelKey(), item.getGroupingIdentifier());
			}

			if (item.getNewsTime().compareTo(open) != 0) {
			    item.setNewsTime(event.getEventTime());
			    item.setNewsTimeLabelKey("dash.updated");
			    dashboardLogic.reviseNewsItemTime(ref, item.getNewsTime(), item.getGroupingIdentifier());

			    // if date changed, may need to redo whether item is visible
			    if(! dashboardLogic.isAvailable(ref, IDENTIFIER)) {
				// remove all news and calendar links
				dashboardLogic.removeNewsLinks(ref);
				// assignment is not open yet, schedule for check later
				dashboardLogic.scheduleAvailabilityCheck(ref, IDENTIFIER, open);
			    } else {
				dashboardLogic.createNewsLinks(item);
			    }
			}

		}

	}

}
