package org.sakaiproject.delegatedaccess.logic;

import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;

/**
 * An interface to abstract all Sakai related API calls in a central method that
 * can be injected into our app.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 * 
 */
public interface SakaiProxy {

	/**
	 * Get current user id
	 * 
	 * @return
	 */
	public String getCurrentUserId();

	/**
	 * Is the current user a superUser? (anyone in admin realm)
	 * 
	 * @return
	 */
	public boolean isSuperUser();

	/**
	 * Post an event to Sakai
	 * 
	 * @param event
	 *            name of event
	 * @param reference
	 *            reference
	 * @param modify
	 *            true if something changed, false if just access
	 * 
	 */
	public void postEvent(String event, String reference, boolean modify);

	/**
	 * Wrapper for ServerConfigurationService.getString("skin.repo")
	 * 
	 * @return
	 */
	public String getSkinRepoProperty();

	/**
	 * Gets the tool skin CSS first by checking the tool, otherwise by using the
	 * default property.
	 * 
	 * @param the
	 *            location of the skin repo
	 * @return
	 */
	public String getToolSkinCSS(String skinRepo);

	/**
	 * returns the user's current session
	 * 
	 * @return
	 */
	public Session getCurrentSession();

	/**
	 * returns all site's in Sakai
	 * 
	 * @return
	 */
	public List<Site> getAllSites();

	/**
	 * user's Sakai userDirectoryService to search for users
	 * 
	 * @param search
	 * @param first
	 * @param last
	 * @return
	 */
	public List<User> searchUsers(String search, int first, int last);

	/**
	 * Returns the site for the site ref
	 * 
	 * @param siteRef
	 * @return
	 */
	public Site getSiteByRef(String siteRef);

	/**
	 * Returns the site for the site id
	 * 
	 * @param siteId
	 * @return
	 */
	public Site getSiteById(String siteId);

	/**
	 * Save a Sakai site via the SiteService.
	 * 
	 * @param site
	 */
	public void saveSite(Site site);

	/**
	 * will return Sakai's name for the root of the hierarchy. Looks first for
	 * sakai.property: HIERARCHY_ROOT_TITLE_PROPERTY, then ui.service, then
	 * finally just defaults to the string "Sakai".
	 * 
	 * @return
	 */
	public String getRootName();

	/**
	 * Returns a list of Strings for the sakai.property
	 * 
	 * @param property
	 * @return
	 */
	public String[] getServerConfigurationStrings(String property);

	/**
	 * Returns all site realm templates that start with "!site."
	 * 
	 * @return
	 */
	public List<AuthzGroup> getSiteTemplates();

	/**
	 * call authzGroupService.refreshUser for current user
	 */
	public void refreshCurrentUserAuthz();

	/**
	 * returns all available tools in Sakai Map: ToolId -> Tool Name
	 * 
	 * @return
	 */
	public Set<Tool> getAllTools();

	/**
	 * returns a list of site references that the current user has access to
	 * 
	 * @return
	 */
	public Set<String> getUserMembershipForCurrentUser();

	/**
	 * Remove this role from the authz group and update the group.
	 * 
	 * @param siteRef
	 * @param role
	 * @return
	 */
	public AuthzGroup getAuthzGroup(String siteRef);

	/**
	 * Delete an {@link AuthzGroup} in Sakai
	 * 
	 * @param id
	 */
	public void removeRoleFromAuthzGroup(AuthzGroup group, Role role);

	/**
	 * Copy one role to another in an {@link AuthzGroup}
	 * 
	 * @param siteRef
	 * @param copyRealm
	 * @param copyRole
	 * @param newRole
	 */
	public void copyNewRole(String siteRef, String copyRealm, String copyRole,
			String newRole);

	

}
