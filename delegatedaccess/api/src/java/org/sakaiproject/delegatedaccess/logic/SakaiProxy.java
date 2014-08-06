/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.logic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.tool.api.Placement;
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
	 * returns all sites in Sakai, but uses a paging mechanism
	 * @param propsMap can be null or send over props to filter sites on
	 * @param page Page to start from (1 based)
	 * @param pageMax maximum number of sites per page
	 * @param orderByModifiedDate
	 * @return
	 */
	public List<Site> getAllSitesByPages(Map<String, String> propsMap, int page, int pageMax, boolean orderByModifiedDate);

	/**
	 * user's Sakai userDirectoryService to search for users
	 * 
	 * @param search
	 * @param first
	 * @param last
	 * @return
	 */
	public List<User> searchUsers(String search);

	/**
	 * Returns the site for the site ref
	 * 
	 * @param siteRef
	 * @return
	 */
	public Site getSiteByRef(String siteRef);
	
	/**
	 * Returns the site for that Id
	 * 
	 * @param siteRef
	 * @return
	 */
	public Site getSiteById(String siteRef);


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
	
	/**
	 * Sends an email
	 * 
	 * @param subject
	 * @param body
	 */
	public void sendEmail(String subject, String body);
	
	/**
	 * returns the setting for
	 * delegatedaccess.disable.user.tree.view
	 * 
	 * @return
	 */
	
	public boolean getDisableUserTreeView();
	
	/**
	 * returns a sakai.property for
	 * delegatedaccess.disable.shopping.tree.view
	 * @return
	 */
	public boolean getDisableShoppingTreeView();
	
	/**
	 * returns a user for passed in userId
	 * @param id
	 * @return
	 */
	public User getUser(String id);
	
	/**
	 * returns true only if the user is a member of the site and has site.upd permission
	 * 
	 * @param userId
	 * @param siteId
	 * @return
	 */
	public boolean isUserInstructor(String userId, String siteId);
	
	/**
	 * returns true if delegatedaccess.shopping.instructorEditable is set to true, otherwise, false
	 * @return
	 */
	public boolean isShoppingPeriodInstructorEditable();
	
	/**
	 * returns true if the user is a member of the site
	 * 
	 * @param userId
	 * @param siteRef
	 * @return
	 */
	public boolean isUserMember(String userId, String siteRef);
	
	/**
	 * returns a list of users who have site.upd permission for this site
	 * @param siteId
	 * @return
	 */
	public List<User> getInstructorsForSite(String siteId);
	
	/**
	 * returns a sakai.property value for the setting:  delegatedaccess.sync.myworkspacetool
	 * true by default
	 * @return
	 */
	public boolean getSyncMyworkspaceTool();

	/**
	 * returns the tool for the given tool id
	 * @param toolId
	 * @return
	 */
	public Tool getTool(String toolId);
	
	/**
	 * returns a list of hidden roles that an instructor shouldn't know have access to their site
	 * 
	 * delegatedaccess.siteaccess.instructorViewable.hiddenRoles
	 * 
	 * @return
	 */
	public String[] getHideRolesForInstructorViewAccess();
	
	/**
	 * returns a map of site ref -> role.  If role is null, then they are not a member
	 * @param userId
	 * @param siteRefs
	 * @return
	 */
	public Map<String,String> isUserMember(String userId, Collection<String> siteRefs);
	
	/**
	 * returns a list of "realm:role;realm:role;" from highest to lowest level of access.  
	 * For instance, if you wanted to order the importance of roles
	 * of sakai's default permissions, it would look like:
	 * 1-> "!site.template.course :Instructor;!site.template:maintain;"
	 * 2-> "!site.template.course :TA;"
	 * 3-> "!site.template.course :Student;!site.template:access;"
	 *  
	 *  This will only allow subadmin to assign permissions at their level and below.  
	 *  Any realm/role that isn't in that list will be considered the last level on the bottom.
	 * @return
	 */
	public String[] getSubAdminOrderedRealmRoles();
	


	/**
	 * This requires an external quartz job that determines whether a site is active or not.  If ths site is
	 * active, this job will set "activesite" permission to the site node
	 * 
	 * 
	 * DAC-40 Highlight Inactive Courses in site search
	 * requires the job "InactiveCoursesJob" attached in the jira
	 *
	 * @return
	 */
	public boolean isActiveSiteFlagEnabled();
	
	/**
	 * Sets the current session userId
	 * @param userId
	 * @return
	 */
	public void setSessionUserId(String userId);
	
	/**
	 * Returns a sakai.property: delegatedaccess.allow.accessadmin.set.allowBecomeUser (default true)
	 * This property determines whether an Access Admin is able to set the advanced option permission
	 * "allowBecomeUser".
	 * 
	 * @return
	 */
	public boolean allowAccessAdminsSetBecomeUserPerm();
	
	/**
	 * returns user by eid
	 * @param eid
	 * @return
	 */
	public User getUserByEid(String eid);
	
	/**
	 * get site reference from site id
	 * @param context
	 * @return
	 */
	public String siteReference(String context);
	
	/**
	 * get current placement
	 * @return
	 */
	public Placement getCurrentPlacement();
	
	/**
	 * if this is set to true, then a "provider id lookup" column will be added to DA search results
	 * delegatedaccess.enableProviderIdLookup
	 * @return
	 */
	public boolean isProviderIdLookupEnabled();
	
	/**
	 * returns the provider id for a realm
	 * @param siteRef
	 * @return
	 */
	public String getProviderId(String siteRef);
	
	/**
	 * returns a label for a hierarchy if it exists:
	 * delegatedaccess.search.hierarchyLabel.{hierarchyLevel}
	 * @param hierarchyLevel
	 * @return
	 */
	public String getHierarchySearchLabel(String hierarchyLevel);
	
	/**
	 * If this is set to true, then the term dropdown option in the search pages will be hidden
	 * delegatedaccess.search.hideTerm
	 * @return
	 */
	public boolean isSearchHideTerm();
}
