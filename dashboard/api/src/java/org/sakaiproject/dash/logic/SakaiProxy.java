package org.sakaiproject.dash.logic;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.announcement.api.AnnouncementService;
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
	public static final String EVENT_UPDATE_ASSIGNMENT_ACCESS = AssignmentConstants.EVENT_UPDATE_ASSIGNMENT_ACCESS;
	
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
   
	
	public final static String EVENT_SCHEDULE_NEW_EVENT = CalendarService.EVENT_ADD_CALENDAR;
	public final static String EVENT_SCHEDULE_REMOVE_EVENT = CalendarService.EVENT_REMOVE_CALENDAR;
	public final static String EVENT_SCHEDULE_REVISE_EVENT = CalendarService.EVENT_MODIFY_CALENDAR;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TITLE = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TITLE;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_TIME = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_TIME;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_ACCESS = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_ACCESS;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_FREQUENCY;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_EXCLUDED = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_EXCLUDED;
	public static final String EVENT_MODIFY_CALENDAR_EVENT_EXCLUSIONS = CalendarService.EVENT_MODIFY_CALENDAR_EVENT_EXCLUSIONS;

	public static final String PERMIT_ANNOUNCEMENT_ACCESS = AnnouncementService.SECURE_ANNC_READ;
	public static final String PERMIT_ASSIGNMENT_ACCESS = AssignmentService.SECURE_ACCESS_ASSIGNMENT;
	public static final String PERMIT_RESOURCE_ACCESS = ContentHostingService.AUTH_RESOURCE_READ;
	public static final String PERMIT_SCHEDULE_ACCESS = CalendarService.AUTH_READ_CALENDAR;
	
	public static final String ANNOUNCEMENT_RELEASE_DATE = AnnouncementService.RELEASE_DATE;
	public static final String ANNOUNCEMENT_RETRACT_DATE = AnnouncementService.RETRACT_DATE;
	
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
	 * 
	 * @param realmId
	 * @param accessPermission
	 * @return
	 */
	public List<String> getUsersWithReadAccess(String entityReference, String accessPermission);
	
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

	public void startAdminSession();

	public List<ContentResource> getAllContentResources(String contentCollectionId);
	
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
}
