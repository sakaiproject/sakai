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

package org.sakaiproject.dash.app;

import java.util.Collection;
import java.util.List;
import java.util.Observer;

import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.user.api.User;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 * Note to developers: This interface should not need to reference any classes or interfaces
 * in any part of org.sakaiproject.dash.*. 
 */
public interface SakaiProxy {

	public final static String EVENT_ASSIGNMENT_NEW = AssignmentConstants.EVENT_ADD_ASSIGNMENT;
	public final static String EVENT_ASSIGNMENT_REMOVE = AssignmentConstants.EVENT_REMOVE_ASSIGNMENT;
	public final static String EVENT_UPDATE_ASSIGNMENT_TITLE = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_TITLE;
	public static final String EVENT_UPDATE_ASSIGNMENT_OPENDATE = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_OPENDATE;
	public static final String EVENT_UPDATE_ASSIGNMENT_DUEDATE = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_DUEDATE;
	public static final String EVENT_UPDATE_ASSIGNMENT_CLOSEDATE = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_CLOSEDATE;
	public static final String EVENT_UPDATE_ASSIGNMENT_ACCESS = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS;
	public static final String EVENT_UPDATE_ASSIGNMENT = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT;
	
	public static final String EVENT_CONTENT_UPD_ACCESS = ContentHostingService.EVENT_RESOURCE_UPD_ACCESS;
	public static final String EVENT_CONTENT_UPD_TITLE = ContentHostingService.EVENT_RESOURCE_UPD_TITLE;
	public static final String EVENT_CONTENT_UPD_VISIBILITY = ContentHostingService.EVENT_RESOURCE_UPD_VISIBILITY;
	public static final String EVENT_CONTENT_AVAILABLE = ContentHostingService.EVENT_RESOURCE_AVAILABLE;
	public final static String EVENT_CONTENT_NEW = ContentHostingService.EVENT_RESOURCE_ADD;
	public final static String EVENT_CONTENT_REMOVE = ContentHostingService.EVENT_RESOURCE_REMOVE;
	public final static String EVENT_CONTENT_REVISE = ContentHostingService.EVENT_RESOURCE_WRITE;
	
	public final static String EVENT_ANNOUNCEMENT_ROOT =  AnnouncementService.SECURE_ANNC_ROOT;
	public final static String EVENT_ANNOUNCEMENT_NEW =  AnnouncementService.SECURE_ANNC_ADD;
	public final static String EVENT_ANNOUNCEMENT_REMOVE_OWN = AnnouncementService.SECURE_ANNC_REMOVE_OWN;
	public final static String EVENT_ANNOUNCEMENT_REMOVE_ANY = AnnouncementService.SECURE_ANNC_REMOVE_ANY;
	public static final String EVENT_ANNC_UPDATE_TITLE = AnnouncementService.EVENT_ANNC_UPDATE_TITLE;
    public static final String EVENT_ANNC_UPDATE_ACCESS = AnnouncementService.EVENT_ANNC_UPDATE_ACCESS;
    public static final String EVENT_ANNC_UPDATE_AVAILABILITY = AnnouncementService.EVENT_ANNC_UPDATE_AVAILABILITY;
    public static final String EVENT_ANNC_UPDATE_ANY = AnnouncementService.SECURE_ANNC_UPDATE_ANY;
    public static final String EVENT_ANNC_UPDATE_OWN = AnnouncementService.SECURE_ANNC_UPDATE_OWN;
	
	public final static String EVENT_SCHEDULE_NEW_EVENT = CalendarService.EVENT_ADD_CALENDAR;
	public final static String EVENT_REMOVE_CALENDAR_EVENT = CalendarService.EVENT_REMOVE_CALENDAR_EVENT;
	public final static String EVENT_SCHEDULE_REVISE_EVENT = CalendarService.EVENT_MODIFY_CALENDAR;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TITLE = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TITLE;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TIME = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TIME;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TYPE = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TYPE;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_ACCESS = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_ACCESS;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_EXCLUDED = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_EXCLUDED;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_EXCLUSIONS = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_EXCLUSIONS;
	
    /* Here are the samigo events we care about
       sam.assessment.publish | siteId=f08b61c2-2b74-4355-8978-ae26718a00b7, assessmentId=9, publishedAssessmentId=13 
       sam.pubsetting.edit    | siteId=f08b61c2-2b74-4355-8978-ae26718a00b7, publishedAssessmentId=13 
       sam.pubassessment.confirm_edit | siteId=f08b61c2-2b74-4355-8978-ae26718a00b7, publishedAssessmentId=13 
       sam.pubassessment.republish    | siteId=f08b61c2-2b74-4355-8978-ae26718a00b7, publishedAssessmentId=13  
       sam.pubAssessment.remove       | siteId=f08b61c2-2b74-4355-8978-ae26718a00b7, publisedAssessmentId=13  
    */

	public static final String EVENT_PUBASSESSMENT_PUBLISH = "sam.assessment.publish";
	public static final String EVENT_PUBASSESSMENT_SETTINGS = "sam.pubsetting.edit";
	public static final String EVENT_PUBASSESSMENT_UNPUBLISH = "sam.pubassessment.confirm_edit";
	public static final String EVENT_PUBASSESSMENT_REPUBLISH = "sam.pubassessment.republish";
	public static final String EVENT_PUBASSESSMENT_REMOVE = "sam.pubAssessment.remove";

	public static final String PERMIT_ANNOUNCEMENT_ACCESS = AnnouncementService.SECURE_ANNC_READ;
    public static final String PERMIT_ANNOUNCEMENT_ACCESS_DRAFT = AnnouncementService.SECURE_ANNC_READ_DRAFT;
	public static final String PERMIT_SAMIGO_ACCESS = "assessment.takeAssessment";
	public static final String PERMIT_ASSIGNMENT_ACCESS = AssignmentService.SECURE_ACCESS_ASSIGNMENT;
	public static final String PERMIT_ASSIGNMENT_SHARE_DRAFTS = AssignmentService.SECURE_SHARE_DRAFTS;
	public static final String PERMIT_RESOURCE_ACCESS = ContentHostingService.AUTH_RESOURCE_READ;
	public static final String PERMIT_RESOURCE_MAINTAIN_1 = ContentHostingService.AUTH_RESOURCE_HIDDEN;
	public static final String PERMIT_RESOURCE_MAINTAIN_2 = ContentHostingService.AUTH_RESOURCE_WRITE_ANY;
	public static final String PERMIT_DROPBOX_ACCESS = ContentHostingService.AUTH_DROPBOX_OWN;
	public static final String PERMIT_DROPBOX_MAINTAIN = ContentHostingService.AUTH_DROPBOX_MAINTAIN;
	public static final String PERMIT_SCHEDULE_ACCESS = CalendarService.AUTH_READ_CALENDAR;
	// the prefix for permissions
	public static final String[] PERMIT_PREFIX = new String[]{"calendar.", "asn.", "content.", "annc."};
	
	public static final String ANNOUNCEMENT_RELEASE_DATE = AnnouncementService.RELEASE_DATE;
	public static final String ANNOUNCEMENT_RETRACT_DATE = AnnouncementService.RETRACT_DATE;
	
	
	public static final String CONFIG_DISABLE_DASHBOARD_EVENTPROCESSING = "disable.dashboard.eventprocessing";
	
	/**
	 * 
	 * @param observer
	 */
	public abstract void addLocalEventListener(Observer observer);
	
	/**
	 * Get a configuration parameter as a boolean
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public boolean getConfigParam(String param, boolean dflt);
	
	/**
	 * Get a configuration parameter as a String
	 * 
	 * @param	dflt the default value if the param is not set
	 * @return
	 */
	public String getConfigParam(String param, String dflt);
	
	/**
	 * Retrieve the URL for an icon that represents items of a particular content-type (or mimetype).
	 * @param contenttype
	 * @return
	 */
	public String getContentTypeImageUrl(String contenttype);

	/**
	 * 
	 * @return
	 */
	public String getCurrentSessionId();

	/**
	 * Get current siteid
	 * @return
	 */
	public String getCurrentSiteId();
	
	/**
	 * Get current user display name
	 * @return
	 */
	public String getCurrentUserDisplayName();
	
	/**
	 * Get current user id
	 * @return
	 */
	public String getCurrentUserId();
	
	/**
	 * Retrieve a Sakai Entity based on its string entity reference.
	 * 
	 * @param entityReference
	 * @return The entity, or null if the entity cannot be retrieved 
	 * (e.g., because it doesn't exist).
	 */
	public Entity getEntity(String entityReference);
	
	/**
	 * 
	 * @param entityReference
	 * @param contextId
	 * @return
	 */
	public Collection<String> getRealmId(String entityReference, String contextId);
	
	/**
	 * Retrieve a Sakai Site object.
	 *   
	 * @param siteId
	 * @return the site, or null if the siteId does not identify a site that can be returned.
	 */
	public Site getSite(String siteId);
	
	/**
	 * Returns whether a site is published or not
	 *   
	 * @param siteId
	 * @return true if the site is published; false otherwise
	 */
	public boolean isSitePublished(String siteId);

	/**
	 * Wrapper for ServerConfigurationService.getString("skin.repo")
	 * @return
	 */
	public String getSkinRepoProperty();
	
	/**
	 * Gets the tool skin CSS first by checking the tool, otherwise by using the default property.
	 * @param	the location of the skin repo
	 * @return
	 */
	public String getToolSkinCSS(String skinRepo);
	
	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * @return
	 */
	public boolean isSuperUser();

	/**
	 * Post an event to Sakai
	 * 
	 * @param event			name of event
	 * @param reference		reference
	 * @param modify		true if something changed, false if just access
	 * 
	 */
	public void postEvent(String event,String reference,boolean modify);

	public void pushSecurityAdvisor(SecurityAdvisor securityAdvisor);

	public void popSecurityAdvisor(SecurityAdvisor securityAdvisor);

	public User getUser(String sakaiId);
	
	public boolean isWorksite(String siteId);

	/**
	 * Determine whether a user is permitted to access a sakai entity in a 
	 * particular way.  
	 * @param sakaiUserId
	 * @param accessPermission The identifier of the permission being checked.
	 * @param entityReference The reference for the sakai entity in which the 
	 * permission is being checked.  This may identify a specific entity or the 
	 * site or group in which the entity exists.
	 * @return
	 */
	public boolean isUserPermitted(String sakaiUserId,
			String accessPermission, String entityReference);

	public String getTargetForMimetype(String mimetype);
	
	/**
	 * Whether the resource is an attachment
	 * @param resourceId
	 * @return
	 */
	public boolean isAttachmentResource(String resourceId);
	
	/**
	 * Whether the resource is in a dropbox
	 * @param resourceId
	 * @return
	 */
	public boolean isDropboxResource(String resourceId);

	public void startAdminSession();

	/**
	 * Remove from the thread-local cache all items bound to the current thread.
	 */
	public void clearThreadLocalCache();

	public List<ContentResource> getAllContentResources(String contentCollectionId);
	
	/**
	 * Access a collection of sakai-ids for users who have the specified permission 
	 * with respect to the entity identified by the entityReference. 
	 * @param permission
	 * @param entityReference
	 * @return
	 */
	public Collection<String> getAuthorizedUsers(String permission, String entityReference);

	/**
	 * get the deep link of schedule event
	 * @param eventRef
	 * @return
	 */
	public String getScheduleEventUrl(String eventRef);

	
	/**
	 * returns the site reference string based on the site id
	 * @param siteId
	 * @return
	 */
	public String getSiteReference(String siteId);
	
	/**
	 * returns a list of users with permission to given reference
	 * @param lock
	 * @param reference
	 * @return
	 */
	public List<User> unlockUsers(String lock, String reference);
	
	/**
	 * check with the server configuration whether the Dashboard event process thread should be disabled or not
	 * @return
	 */
	public boolean isEventProcessingThreadDisabled();
	
	/**
	 * returns the server url
	 * @return
	 */
	public String getServerUrl();

	/**
	 * Access the unique id of this server within a cluster.
	 * @return
	 */
	public String getServerId();

	public void registerFunction(String functionName);
	
	/**
	 * returns true if the permission is known to Dashboard tool. Currently cover Announcement, Content, Assignment and Calendar permission)
	 * @param permissionString
	 * @return
	 */
	public boolean isOfDashboardRelatedPermissions(String permissionString);

}
