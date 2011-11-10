package org.sakaiproject.delegatedaccess.logic;

import java.util.List;
import java.util.Set;

import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.model.ToolSerialized;



/**
 * Delegated Access's logic interface
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public interface ProjectLogic {
	/**
	 * returns a list of nodes the current user has site.access permission (aka access).  Only direct nodes, nothing inherited.
	 * @return
	 */
	public Set<HierarchyNodeSerialized> getNodesForCurrentUser();
	/**
	 * returns a list of nodes the user has site.access permission (aka access).  Only direct nodes, nothing inherited.
	 * @param userId
	 * @return
	 */
	public Set<HierarchyNodeSerialized> getNodesForUser(String userId);

	/**
	 * returns the node for this id
	 * @param id
	 * @return
	 */
	public HierarchyNodeSerialized getNode(String id);

	/**
	 * updates the user's Session to include the site and their access to the delegatedaccess.accessmap Session attribute.  This controls the user's 
	 * permissions for that site.  If the nodeId doesn't have an access role specified, it will grant the inherited access role.
	 * 
	 * @param siteRef
	 * @param nodeId
	 * @param inheritedAccess
	 */
	public void grantAccessToSite(String siteRef, String nodeId, String[] inheritedAccess);

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
	 * returns a set of nodes that are direct children of the given nodeId
	 * @param nodeId
	 * @return
	 */
	public Set<HierarchyNodeSerialized> getDirectChildrenNodes(String nodeId);

	/**
	 * returns the root node for Delegated Access's hierarchy
	 * @return
	 */
	public HierarchyNodeSerialized getRootNode();

	/**
	 * Removes old user permissions and replaces it with the passed in information.
	 * 
	 * @param nodeModel
	 * @param userId
	 */
	public void updateNodePermissionsForUser(NodeModel nodeModel, String userId);

	/**
	 * returns the user's realm and role information for the given node.  Doesn't include inherited information, will return
	 * a "" if not found.
	 * @param userId
	 * @param nodeId
	 * @return
	 */
	public String[] getAccessRealmRole(String userId, String nodeId);

	/**
	 * Returns a list representation of the user's access tree.  Tree structure looks like:
	 * 
	 * List<List<node, List>, List<node, List>, ...>
	 * 
	 * @param userId
	 * @param addDirectChildren
	 * @param cascade
	 * @param accessNodes
	 * @return
	 */
	public List<List> getTreeListForUser(String userId, boolean addDirectChildren, boolean cascade, List<String> accessNodes);

	/**
	 * updates the user's Session adding all of the user's site and role access to the delegatedaccess.accessmap Session attribute.  This controls the user's 
	 * permissions for that site.  If the nodeId doesn't have an access role specified, it will grant the inherited access role.
	 * 
	 */
	public void initializeDelegatedAccessSession();
	/**
	 * updates the user's Session adding all of the user's site and role access to the delegatedaccess.accessmap Session attribute.  This controls the user's 
	 * permissions for that site.  If the nodeId doesn't have an access role specified, it will grant the inherited access role.
	 * 
	 */
	public void initializeDelegatedAccessSession(String userId);

	
	/**
	 * Returns a list of ToolSerialized that initialized the selected field
	 * @param userId
	 * @param nodeId
	 * @return
	 */
	public List<ToolSerialized> getRestrictedToolSerializedList(String userId, String nodeId);
}
