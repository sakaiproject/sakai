package org.sakaiproject.delegatedaccess.logic;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeModel;

import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.hierarchy.model.HierarchyNode;



/**
 * Delegated Access's logic interface
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public interface ProjectLogic {

	/**
	 * updates the user's Session to include the site and their access to the delegatedaccess.accessmap Session attribute.  This controls the user's 
	 * permissions for that site.  If the nodeId doesn't have an access role specified, it will grant the inherited access role.
	 * 
	 * @param nodeModel
	 */
	public void grantAccessToSite(NodeModel nodeModel);

	/**
	 * Returns a list of SearchResults for the user search
	 * 
	 * @param search
	 * @param first
	 * @param last
	 * @return
	 */
	public List<SearchResult> searchUsers(String search, int first, int last);

	/**
	 * Removes old user permissions and replaces it with the passed in information.
	 * 
	 * @param nodeModel
	 * @param userId
	 */
	public void updateNodePermissionsForUser(NodeModel nodeModel, String userId);

	/**
	 * updates the user's Session adding all of the user's site and role access to the delegatedaccess.accessmap Session attribute.  This controls the user's 
	 * permissions for that site.  If the nodeId doesn't have an access role specified, it will grant the inherited access role.
	 * 
	 */
	public void initializeDelegatedAccessSession();

	/**
	 * Searches user access sites by siteId and siteTitle
	 * 
	 * @param search
	 * @param treeModel
	 * @return
	 */
	public List<NodeModel> searchUserSites(String search, TreeModel treeModel, Map<String, String> advancedOptions);

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
	 * This returns the shopping tree model in the shopping period hierarchy.
	 * 
	 * @param includePerms
	 * @return
	 */
	public TreeModel getTreeModelForShoppingPeriod(boolean includePerms);

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
	 * @param blankTerms
	 * @return
	 */
	public boolean addChildrenNodes(Object node, String userId, List<ListOptionSerialized> blankRestrictedTools, List<ListOptionSerialized> blankTerms);

	/**
	 * returns a blank (unselected) list of all the tool options for restricting tools
	 * @return
	 */
	public List<ListOptionSerialized> getEntireToolsList();
	
	/**
	 * returns a blank (unselected) list of all the term options for restricting terms
	 * @return
	 */
	public List<ListOptionSerialized> getEntireTermsList();


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
	public HierarchyNodeSerialized getNode(String id);


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
	 * Returns the node Id for the site Ref and hierarchy Id
	 * @param siteRef
	 * @param hierarchyId
	 * @return
	 */
	public List<String> getNodesBySiteRef(String siteRef, String hierarchyId);
	
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
}
