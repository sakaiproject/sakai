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

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import org.sakaiproject.delegatedaccess.model.AccessNode;
import org.sakaiproject.delegatedaccess.model.AccessSearchResult;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.model.SiteSearchResult;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.user.api.User;



/**
 * Delegated Access's logic interface
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public interface ProjectLogic {

	
	/**
	 * This returns an array of {realm, role} for which the user has delegated access to.  If nothing is found, then it returns null.
	 * This function also updates the denied tools session map
	 * 
	 * @param siteRef
	 * @return
	 */
	public String[] getCurrentUsersAccessToSite(String siteRef);
	
	/**
	 * Returns a list of SearchResults for the user search
	 * 
	 * @param search
	 * @param first
	 * @param last
	 * @return
	 */
	public List<SearchResult> searchUsers(String search);

	/**
	 * Removes old user permissions and replaces it with the passed in information.
	 * 
	 * @param nodeModel
	 * @param userId
	 */
	public void updateNodePermissionsForUser(DefaultMutableTreeNode node, String userId);
	public void updateNodePermissionsForUser(NodeModel nodeModel, String userId);
	
	/**
	 * updates the user's Session adding all of the user's site and role access to the delegatedaccess.accessmap Session attribute.  This controls the user's 
	 * permissions for that site.  If the nodeId doesn't have an access role specified, it will grant the inherited access role.
	 * 
	 */
	public void initializeDelegatedAccessSession();

	/**
	 * Searches user access sites by siteId and siteTitle and props
	 * 
	 * @param search
	 * @param advancedOptions
	 * @param shoppingPeriod
	 * @param activeShoppingData
	 * @return
	 */
	public List<SiteSearchResult> searchUserSites(String search, Map<String, Object> advancedOptions, boolean shoppingPeriod, boolean activeShoppingData);

	/**
	 * returns the tree model of a user's delegated access.  Each node in the tree has the NodeModel object
	 * completely initialized.
	 * 
	 * @param userId
	 * @param addDirectChildren
	 * @param cascade
	 * @return
	 */

	public TreeModel createAccessTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade);


	/**
	 * This returns a full tree model for a user.  It will reference both their access and shopping period admin permissions.
	 * @param userId
	 * @param addDirectChildren
	 * @param cascade
	 * @return
	 */
	public TreeModel createEntireTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade);

	/**
	 * returns the tree model that looks up the shopping period information for the sites the user has access to
	 * 
	 * @param userId
	 * @return
	 */
	public TreeModel createTreeModelForShoppingPeriod(String userId);

	/**
	 * Adds children node to a node that hasn't had it's children populated.  This is used to increase the efficiency
	 * of the tree so you can create the structure on the fly with ajax
	 * 
	 * @param node
	 * @param userId
	 * @param blankRestrictedTools
	 * @param onlyAccessNodes
	 * @param accessAdminNodes
	 * @param shopping
	 * @param shoppingPeriodTool
	 * @return
	 */
	public boolean addChildrenNodes(Object node, String userId, List<ListOptionSerialized> blankRestrictedTools, boolean onlyAccessNodes, List<String> accessAdminNodes, boolean shopping, boolean shoppingPeriodTool);

	/**
	 * returns a blank (unselected) list of all the tool options for restricting tools
	 * @return
	 */
	public List<ListOptionSerialized> getEntireToolsList();
	
	/**
	 * This will return a fully instantiated NodeModel for that user and id.  It will look up it's parents nodes and instantiate
	 * them as well.
	 * 
	 * @param nodeId
	 * @param userId
	 * @return
	 */
	public NodeModel getNodeModel(String nodeId, String userId);

	/**
	 * returns a HierarchyNodeSerialized node
	 * @param id
	 * @return
	 */
	public HierarchyNodeSerialized getCachedNode(String id);

	/**
	 * returns a map of HierarchyNodes
	 * @param ids
	 * @return
	 */
	public Map<String, HierarchyNodeSerialized> getCachedNodes(String[] ids);

	/**
	 * This returns the entire tree plus any permissions set for a user
	 * 
	 * @param userId
	 * @return
	 */
	public TreeModel getEntireTreePlusUserPerms(String userId);
	
	/**
	 * Returns whether the user has any shopping period admin access
	 * 
	 * @param userId
	 * @return
	 */
	public boolean hasShoppingPeriodAdminNodes(String userId);
	
	/**
	 * returns whether the user has any delegated access
	 * @param userId
	 * @return
	 */
	public boolean hasDelegatedAccessNodes(String userId);
	
	/**
	 * returns whether the user has any "access admin" permission
	 * @param userId
	 * @return
	 */
	public boolean hasAccessAdminNodes(String userId);
	
	/**
	 * returns whether the user has any "allowBecomeUser" permission
	 * @param userId
	 * @return
	 */
	public boolean hasAllowBecomeUserPerm(String userId);
	
	/**
	 * Returns a map of {siteRef, nodeId}
	 * @param siteRef
	 * @param hierarchyId
	 * @return
	 */
	public Map<String, List<String>> getNodesBySiteRef(String siteRef[], String hierarchyId);
	
	/**
	 * Saves the date for the last time the hierarchy job ran successfully
	 * @param runDate
	 * @param nodeId
	 */
	public void saveHierarchyJobLastRunDate(Date runDate, String nodeId);
	
	/**
	 * returns the hierarchyjoblastrundate date for the node Id and hierarchy user
	 * @param nodeId
	 * @return
	 */
	public Date getHierarchyJobLastRunDate(String nodeId);
	
	/**
	 * Removes this node an all permissions and children nodes
	 * @param nodeId
	 */
	public void removeNode(String nodeId);
	
	/**
	 * Removes this node and all permissions and children nodes
	 * @param node
	 */
	public void removeNode(HierarchyNode node);
	
	/**
	 * Deletes empty non sites nodes in a hierarchy (nodes that doesn't start with /site/
	 * and has no children)
	 * 
	 * @param hierarchyId
	 */
	public void deleteEmptyNonSiteNodes(String hierarchyId);
	
	/**
	 * clears DelegatedAccess's own node cache
	 */
	public void clearNodeCache();
	
	/**
	 * returns a map of all role options and their realm/role ids separated by a ':'.  For example:
	 * 
	 * Instructor => !site.template.course:Instructor
	 * 
	 * @param shopping
	 * @return
	 */
	public Map<String, String> getRealmRoleDisplay(boolean shopping);
	
	/**
	 * Returns a set of hierarchy nodes that the user has been assigned accessAdmin privileges for.
	 * @param userId
	 * @return
	 */
	public Set<HierarchyNodeSerialized> getAccessAdminNodesForUser(String userId);
	
	/**
	 * Call this function to determine if the shopping period is available for a set of settings
	 * 
	 * @param startDate
	 * @param endDate
	 * @param nodeAccessRealmRole
	 * @param restrictedAuthTools
	 * @param restrictedPublicTools
	 * @return
	 */
	public boolean isShoppingPeriodOpenForSite(Date startDate, Date endDate, String[] nodeAccessRealmRole, String[] restrictedAuthTools, String[] restrictedPublicTools);
	
	/**
	 * This will ensure the Delegated Access tool is synced with the user's MyWorkspace.  Another words, if the user has no
	 * permissions in DA, then the tool will be removed from the user's My Workspace.  If they are granted access somewhere,
	 * then this will add the tool to thier My Workspace
	 * 
	 * this is turned on/off by a sakai.property delegatedaccess.sync.myworkspacetool
	 * @param userId
	 */
	public void syncMyworkspaceToolForUser(String userId);
	
	/**
	 * Returns a map of UserId -> AccessNode
	 * @param siteRef
	 * @return
	 */
	public Map<String, AccessNode> getUserAccessForSite(String siteRef);
	
	/**
	 * Call this method if you want to update a node and all it's children
	 * it will only update nodes that start with /site/
	 * @param node
	 * @return a map of errors and the stack trace
	 */
	public Map<String, String> updateShoppingPeriodSettings(DefaultMutableTreeNode node);
	
	/**
	 * returns the status of the job for adding delegated access tool to user's my workspace. 
	 * This can be null or a string
	 *
	 * @return
	 */
	public String getAddDAMyworkspaceJobStatus();
	
	/**
	 * removes the old status and updates it with the string passed in
	 * @param status
	 */
	public void updateAddDAMyworkspaceJobStatus(String status);
	
	/**
	 * Schedules the AddDAMyworkspace job to run immediately
	 */
	public void scheduleAddDAMyworkspaceJobStatus();
	
	/**
	 * returns true if the user has site access and has the "allowBecomeUser" permission set for
	 * this site or any of it's parents
	 * 
	 * @param userId
	 * @param siteRef
	 * @return
	 */
	public boolean isUserAllowBecomeUser(String userId, String siteRef);
	
	/**
	 * returns the root node for DA
	 * 
	 * @return
	 */
	public HierarchyNodeSerialized getRootNodeId();
	
	/**
	 * returns a set of direct children nodes for passed in node id
	 * @param nodeId
	 * @return
	 */
	public Set<HierarchyNodeSerialized> getDirectNodes(String nodeId);
	
	/**
	 * returns a list of AccessSearchResult based on the user id
	 * @param userId
	 * @return
	 */
	public List<AccessSearchResult> getAccessForUser(User user);

	/**
	 * returns a list of results for every level that is passed in.
	 * expects a list of ordered node ids 
	 * @param nodeSelectOrder
	 * @param includeLowerPerms
	 * @return
	 */
	public List<AccessSearchResult> getAccessAtLevel(List<String> nodeSelectOrder, boolean includeLowerPerms);
	
	/**
	 * This will remove access at the node id passed in.  The types are:
	 * DelegatedAccessConstants.TYPE_ACCESS
	 * DelegatedAccessConstants.TYPE_ACCESS_SHOPPING_PERIOD_USER
	 * DelegatedAccessConstants.TYPE_ACCESS_ADMIN
	 * 
	 * 
	 * @param nodeId
	 * @param userId
	 * @param accessType
	 */
	public void removeAccess(String nodeId, String userId, int accessType);
	
	/**
	 * Removes all permissions for a user
	 * @param userId
	 */
	public void removeAllPermsForUser(String userId);
	
	public Map<String, Set<String>> getHierarchySearchOptions(Map<String, String> hierarchySearchMap);
	
	/**
	 * filters out any node id that the user doesn't have permission to modify shopping period settings
	 * @param nodeIds
	 * @return
	 */
	public Set<String> filterShoppingPeriodEditNodes(Set<String> nodeIds);
	
	/**
	 * filters out any node id that the user doesn't have permission to modify shopping period settings
	 * @param nodeIds
	 * @param userId
	 * @return
	 */
	public Set<String> filterShoppingPeriodEditNodes(Set<String> nodeIds, String userId);
}
