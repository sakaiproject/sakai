package org.sakaiproject.dash.logic;

import java.util.Observer;

import org.sakaiproject.assignment.api.AssignmentConstants;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.site.api.Site;

/**
 * An interface to abstract all Sakai related API calls in a central method that can be injected into our app.
 * 
 *
 */
public interface SakaiProxy {

	public final static String EVENT_ASSIGNMENT_NEW = AssignmentConstants.EVENT_ADD_ASSIGNMENT;
	
	public final static String EVENT_CONTENT_NEW = ContentHostingService.EVENT_RESOURCE_ADD;
	

	/**
	 * Get current siteid
	 * @return
	 */
	public String getCurrentSiteId();
	
	/**
	 * Get current user id
	 * @return
	 */
	public String getCurrentUserId();
	
	/**
	 * Get current user display name
	 * @return
	 */
	public String getCurrentUserDisplayName();
	
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
	 * 
	 * @param observer
	 */
	public abstract void addLocalEventListener(Observer observer);

	/**
	 * Retrieve a Sakai Entity based on its string entity reference.
	 * 
	 * @param entityReference
	 * @return The entity, or null if the entity cannot be retrieved 
	 * (e.g., because it doesn't exist).
	 */
	public Entity getEntity(String entityReference);

	/**
	 * Retrieve a Sakai Site object.
	 *   
	 * @param siteId
	 * @return the site, or null if the siteId does not identify a site that can be returned.
	 */
	public Site getSite(String siteId);

	/**
	 * 
	 * @param entityReference
	 * @param contextId
	 * @return
	 */
	public String getRealmId(String entityReference, String contextId);

}
