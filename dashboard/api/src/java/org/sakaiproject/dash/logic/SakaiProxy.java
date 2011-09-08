package org.sakaiproject.dash.logic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.content.api.ContentHostingService;
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
	
	public final static String EVENT_CONTENT_NEW = ContentHostingService.EVENT_RESOURCE_ADD;
	public final static String EVENT_CONTENT_REMOVE = ContentHostingService.EVENT_RESOURCE_REMOVE;
	
	public final static String EVENT_ANNOUNCEMENT_ROOT =  AnnouncementService.SECURE_ANNC_ROOT;
	public final static String EVENT_ANNOUNCEMENT_NEW =  AnnouncementService.SECURE_ANNC_ADD;
	public final static String EVENT_ANNOUNCEMENT_REMOVE_OWN = AnnouncementService.SECURE_ANNC_REMOVE_OWN;
	public final static String EVENT_ANNOUNCEMENT_REMOVE_ANY = AnnouncementService.SECURE_ANNC_REMOVE_ANY;

	public static final String PERMIT_ANNOUNCEMENT_ACCESS = AnnouncementService.SECURE_ANNC_READ;
	public static final String PERMIT_ASSIGNMENT_ACCESS = AssignmentService.SECURE_ACCESS_ASSIGNMENT;
	public static final String PERMIT_RESOURCE_ACCESS = ContentHostingService.AUTH_RESOURCE_READ;
	
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

}
