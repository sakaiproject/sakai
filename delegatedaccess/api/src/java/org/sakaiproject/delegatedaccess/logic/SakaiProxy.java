package org.sakaiproject.delegatedaccess.logic;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
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
	 * Returns a list of realm/role options for the shopping period role
	 * 
	 * first checks the sakai.property: delegatedaccess.realmoptions.shopping
	 * if not found, defaults to: all site realm templates that start with "!site."
	 * 
	 * @return
	 */
	public Map<String, List<String>> getShoppingRealmOptions();
	
	/**
	 * Returns a list of realm/role options for the shopping period role
	 * 
	 * first checks the sakai.property: delegatedaccess.realmoptions.delegatedaccess
	 * if not found, defaults to: all site realm templates that start with "!site."
	 * 
	 * @return
	 */
	public Map<String, List<String>> getDelegatedAccessRealmOptions();

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


	/**
	 * Adds a security advisor to allow site.update
	 * 
	 * @return
	 */
	public SecurityAdvisor addSiteUpdateSecurityAdvisor();

	/**
	 * pops the site.update security advisor
	 * 
	 * @param advisor
	 */
	public void popSecurityAdvisor(SecurityAdvisor advisor);

	/**
	 * Returns a list of sites based on the attributes sent in
	 * @param type
	 * @param search
	 * @param propsMap
	 * @return
	 */
	public List<Site> getSites(SelectionType type, String search, Map<String, String> propsMap);
	
	/**
	 * returns true if this is the shopping period tool
	 * @return
	 */
	public boolean isShoppingTool();
	
	/**
	 * returns the systems term field based on a sakai.property (delegatedaccess.termfield)
	 * 
	 * default is term_eid
	 * 
	 * @return
	 */
	public String getTermField();
	
	/**
	 * returns sakai.properties setting of home tool ids
	 * 
	 * delegatedaccess.hometools
	 * 
	 * @return
	 */
	public String[] getHomeTools();
	
	/**
	 * default is true, otherwise looks up sakai.property
	 * 
	 * delegatedaccess.showtermcolumn.shopping
	 * 
	 * @return
	 */
	public boolean isShowTermColumnShopping();
	
	/**
	 * default is true, otherwise looks up sakai.property
	 * 
	 * delegatedaccess.showtermcolumn.access
	 * @return
	 */
	public boolean isShowTermColumnAccess();

	/**
	 * default is true
	 * 
	 * if this is true, then the term options will be loaded from the CourseManagement API,
	 * otherwise, a distinct query will be ran against site properties based on the 
	 * term field
	 * 
	 * delegatedaccess.term.useCourseManagmentApi
	 * 
	 * @return
	 */
	public boolean useCourseManagementApiForTerms();
	
	/**
	 * Returns a map (id, title) of terms either by the course management api or by the unique set of site propeties if you unset useCourseManagementApiForTerms
	 * 
	 * @return
	 */
	public List<String[]> getTerms();
}
