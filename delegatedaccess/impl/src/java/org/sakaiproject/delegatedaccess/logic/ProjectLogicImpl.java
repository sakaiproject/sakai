package org.sakaiproject.delegatedaccess.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.model.ToolSerialized;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;


/**
 * Implementation of {@link ProjectLogic}
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class ProjectLogicImpl implements ProjectLogic {

	private static final Logger log = Logger.getLogger(ProjectLogicImpl.class);
	@Getter @Setter
	private SakaiProxy sakaiProxy;
	@Getter @Setter
	private HierarchyService hierarchyService;


	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}

	/**
	 * {@inheritDoc}
	 */
	public HierarchyNodeSerialized getNode(String id){
		return new HierarchyNodeSerialized(hierarchyService.getNodeById(id));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<HierarchyNodeSerialized> getNodesForCurrentUser() {
		return getNodesForUser(sakaiProxy.getCurrentUserId());
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateNodePermissionsForUser(NodeModel nodeModel, String userId){
		//first step, remove all permissions so you can have a clear palet
		removeAllUserPermissions(nodeModel.getNodeId(), userId);
		
		if(nodeModel.isDirectAccess()){
			//if direct access, add permissions, otherwise, leave it blank
			
			//site access permission
			hierarchyService.assignUserNodePerm(userId, nodeModel.getNodeId(), "site.visit", false);
			
			//realm & role permissions
			saveRealmAndRoleAccess(userId, nodeModel.getRealm(), nodeModel.getRole(), nodeModel.getNodeId());
			
			//tool permissions:
			List<String> restrictedTools = new ArrayList<String>();
			for(ToolSerialized tool : nodeModel.getRestrictedTools()){
				if(tool.isSelected()){
					restrictedTools.add(tool.getToolId());
				}
			}
			if(!restrictedTools.isEmpty()){
				saveRestrictedToolsForUser(userId, nodeModel.getNodeId(), restrictedTools);
			}
		}
		
		
		if(nodeModel.isDirectAccess()){
			sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_ADD_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId() + "/realm/" + nodeModel.getRealm() + "/role/" + nodeModel.getRole(), true);
		}else{
			sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_DELETE_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
		}
	}
	
	private void removeAllUserPermissions(String nodeId, String userId){
		for(String perm : hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId})){
			hierarchyService.removeUserNodePerm(userId, nodeId, perm, false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<HierarchyNodeSerialized> getNodesForUser(String userId) {
		return convertToSerializedNodeSet(hierarchyService.getNodesForUserPerm(userId, "site.visit"));
	}

	/**
	 * returns a serialized version for Hierarchy nodes.
	 * 
	 * @param nodeSet
	 * @return
	 */
	private Set<HierarchyNodeSerialized> convertToSerializedNodeSet(Set<HierarchyNode> nodeSet){
		Set<HierarchyNodeSerialized> nodesForUserSerialized = new HashSet<HierarchyNodeSerialized>();
		if(nodeSet != null){
			for(HierarchyNode node : nodeSet){
				nodesForUserSerialized.add(new HierarchyNodeSerialized(node));
			}
		}
		return nodesForUserSerialized;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initializeDelegatedAccessSession(){
		initializeDelegatedAccessSession(sakaiProxy.getCurrentUserId());
	}

	/**
	 * {@inheritDoc}
	 */
	public void initializeDelegatedAccessSession(String userId){
		if(userId != null && !"".equals(userId)){
			Set<HierarchyNodeSerialized> nodes = getNodesForUser(userId);

			if(nodes != null && nodes.size() > 0){
				List<String> directNodeIds = new ArrayList();
				for(HierarchyNodeSerialized node : nodes){
					directNodeIds.add(node.id);
				}

				List<List> userAccessTree = getTreeListForUser(sakaiProxy.getCurrentUserId(), false, true, new ArrayList<String>(), nodes);
				Map<String, String[]> accessMap = generateAuthzRoleAccessMap(userAccessTree, directNodeIds, getAccessRealmRole(userId, getRootNode().id), userId);

				Session session = sakaiProxy.getCurrentSession();
				session.setAttribute("delegatedaccess.accessmap", accessMap);
				sakaiProxy.refreshCurrentUserAuthz();
			}
		}
	}

	/**
	 * returns a map of siteRef -> List[realm, role] for the user
	 *  
	 * @param level
	 * @param directNodeIds
	 * @param parentAccess
	 * @param userId
	 * @return
	 */
	private Map<String, String[]> generateAuthzRoleAccessMap(List<List> level, List<String> directNodeIds, String[] parentAccess, String userId){
		Map<String, String[]> returnMap = new HashMap<String, String[]>();

		for(List child : level){
			if(child.size() == 2){
				//1: node  2: list of children nodes
				HierarchyNodeSerialized node = (HierarchyNodeSerialized) child.get(0);
				if(directNodeIds.contains(node.id)){
					//only need to check access for nodes that have direct access (chosen, not inherited)
					String[] nodeAccess = getAccessRealmRole(userId, node.id);
					if(nodeAccess.length == 2 && !"".equals(nodeAccess[0]) && !"".equals(nodeAccess[1])){
						parentAccess = nodeAccess;
					}
				}

				if(node.description.startsWith("/site/")){
					returnMap.put(node.description, parentAccess);
				}
				returnMap.putAll(generateAuthzRoleAccessMap((List<List>) child.get(1), directNodeIds, parentAccess, userId));
			}
		}


		return returnMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public void grantAccessToSite(String siteRef, String nodeId, String[] inheritedAccess){
		String[] userAccessGroupRole = getAccessRealmRole(sakaiProxy.getCurrentUserId(), nodeId);
		Session session = sakaiProxy.getCurrentSession();
		Object sessionDelegatedAccessMap = session.getAttribute("delegatedaccess.accessmap");
		Map<String, String[]> delegatedAccessMap = new HashMap<String, String[]>();
		if(sessionDelegatedAccessMap != null){
			delegatedAccessMap = (Map<String, String[]>) sessionDelegatedAccessMap;
		}
		if (userAccessGroupRole != null && userAccessGroupRole.length == 2
				&& userAccessGroupRole[0] != null
				&& userAccessGroupRole[1] != null
				&& !"".equals(userAccessGroupRole[0])
				&& !"".equals(userAccessGroupRole[1])) {
			delegatedAccessMap.put(siteRef, userAccessGroupRole);
		}
		else{
			delegatedAccessMap.put(siteRef, inheritedAccess);
		}
		session.setAttribute("delegatedaccess.accessmap", delegatedAccessMap);
		Map<String, List<String>> deniedToolsMap = new HashMap<String, List<String>>();
		deniedToolsMap.put("ZIP3", Arrays.asList("sakai.announcements"));
		session.setAttribute("delegatedaccess.deniedToolsMap", deniedToolsMap);
		sakaiProxy.refreshCurrentUserAuthz();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<HierarchyNodeSerialized> getDirectChildrenNodes(String nodeId){
		Set<HierarchyNode> children = hierarchyService.getChildNodes(nodeId, true);
		if(children != null && children.size() > 0){
			List<HierarchyNode> childrenList = new ArrayList<HierarchyNode>(children);
			Collections.sort(childrenList, new Comparator<HierarchyNode>(){
				@Override
				public int compare(HierarchyNode o1, HierarchyNode o2) {
					return o1.title.compareToIgnoreCase(o2.title);
				}
			});
			children = new HashSet<HierarchyNode>(childrenList);
		}
		return convertToSerializedNodeSet(children);
	}

	/**
	 * {@inheritDoc}
	 */
	public HierarchyNodeSerialized getRootNode(){
		return new HierarchyNodeSerialized(hierarchyService.getRootNode(DelegatedAccessConstants.HIERARCHY_ID));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<SearchResult> searchUsers(String search, int first, int last) {
		List<User> searchResult = sakaiProxy.searchUsers(search, first, last);
		List<SearchResult> returnList = new ArrayList<SearchResult>();
		for(User user : searchResult){
			returnList.add(getSearchResult(user));
		}

		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	private SearchResult getSearchResult(User user){
		if(user != null){
			return new SearchResult(user);
		}else{
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	private void saveRealmAndRoleAccess(String userId, String realmId, String role, String nodeId){
		if(realmId != null && role != null && !"".equals(realmId) && !"".equals(role)){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_REALM_PREFIX +realmId, false);
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_ROLE_PREFIX +role, false);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getAccessRealmRole(String userId, String nodeId){
		Set<String> perms = hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId});
		String realmId = "";
		String roleId = "";
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_REALM_PREFIX)){
				realmId = perm.substring(DelegatedAccessConstants.NODE_PERM_REALM_PREFIX.length());
			}else if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_ROLE_PREFIX)){
				roleId = perm.substring(DelegatedAccessConstants.NODE_PERM_ROLE_PREFIX.length());
			}
		}
		return new String[]{realmId, roleId};
	}

	/**
	 * {@inheritDoc}
	 */
	public List<List> getTreeListForUser(String userId, boolean addDirectChildren, boolean cascade, List<String> accessNodes){
		Set<HierarchyNodeSerialized> nodes = getNodesForUser(userId);

		return getTreeListForUser(userId, addDirectChildren, cascade, accessNodes, nodes);
	}

	/**
	 * helper funciton to generate the tree list for the user
	 * 
	 * @param userId
	 * @param addDirectChildren
	 * @param cascade
	 * @param accessNodes
	 * @param nodes
	 * @return
	 */
	private List<List> getTreeListForUser(String userId, boolean addDirectChildren, boolean cascade, List<String> accessNodes, Set<HierarchyNodeSerialized> nodes){
		List<List> l1 = new ArrayList<List>();
		List<List> currentLevel = l1;

		for(HierarchyNodeSerialized node : nodes){
			for(String parentId : node.parentNodeIds){
				HierarchyNodeSerialized parentNode = getNode(parentId);

				if(!hasNode(parentNode, currentLevel)){
					List newNode = new ArrayList();
					newNode.add(parentNode);
					newNode.add(new ArrayList());
					currentLevel.add(newNode);
				}
				currentLevel = getChildrenForNode(parentNode.id, currentLevel);
				if(addDirectChildren){
					for(List nodeList : getDirectChildren(parentNode)){
						if(!hasNode((HierarchyNodeSerialized) nodeList.get(0), currentLevel)){
							currentLevel.add(nodeList);
						}
					}
				}
			}
			if(!hasNode(node, currentLevel)){
				List child = new ArrayList();
				child.add(node);
				child.add(new ArrayList());
				currentLevel.add(child);
			}
			if(cascade){
				//we need to grab all children (children of children, ect) for this node since this an access node
				getCascadingChildren(node, getChildrenForNode(node.id, currentLevel));
			}

			accessNodes.add(node.id);

			currentLevel = l1;
		}
		if(l1.isEmpty() && addDirectChildren){
			//since we want direct children, include the root's direct children (when the node model is empty)
			HierarchyNodeSerialized root = getRootNode();
			if(root != null && root.id != null && !"".equals(root.id)){
				List child = new ArrayList();
				child.add(root);
				child.add(getDirectChildren(root));
				l1.add(child);
			}
		}

		return l1;
	}

	/**
	 * checks if the node exist in the list
	 * 
	 * @param node
	 * @param level
	 * @return
	 */
	private boolean hasNode(HierarchyNodeSerialized node, List<List> level){
		for(List nodeList : level){
			HierarchyNodeSerialized n = (HierarchyNodeSerialized) nodeList.get(0);
			if(n.id.equals(node.id)){
				return true;
			}
		}
		return false;
	}

	/**
	 * returns the children for this node
	 * 
	 * @param id
	 * @param level
	 * @return
	 */
	private List<List> getChildrenForNode(String id, List<List> level){
		for(List nodeList : level){
			HierarchyNodeSerialized n = (HierarchyNodeSerialized) nodeList.get(0);
			if(n.id.equals(id)){
				return (List<List>) nodeList.get(1);
			}
		}
		return null;
	}

	/**
	 * returns direct children for the parent.  Children will have empty lists.
	 * 
	 * @param parent
	 * @return
	 */
	private List<List> getDirectChildren(HierarchyNodeSerialized parent){
		List<List>returnList = new ArrayList<List>();

		if(parent != null){
			Set<String> parentChildren = parent.directChildNodeIds;
			for(String childId : parentChildren){
				List child = new ArrayList();
				child.add(getNode(childId));
				child.add(new ArrayList());
				returnList.add(child);
			}
		}
		return returnList;
	}

	/**
	 * Finds all children of chilren and returns the hierarchy
	 * 
	 * @param parent
	 * @param children
	 * @return
	 */
	private List<List> getCascadingChildren(HierarchyNodeSerialized parent, List<List> children){
		Set<String> parentChildren = parent.directChildNodeIds;
		for(String childId : parentChildren){
			HierarchyNodeSerialized childNode = getNode(childId);

			List childMap = getChildrenForNode(childNode.id, children);
			if(childMap == null){
				childMap = new ArrayList();
			}

			childMap = getCascadingChildren(childNode, childMap);
			if(!hasNode(childNode, children)){
				List childList = new ArrayList();
				childList.add(childNode);
				childList.add(childMap);
				children.add(childList);
			}
		}

		return children;
	}
	
	public List<ToolSerialized> getRestrictedToolSerializedList(String userId, String nodeId){
		List<ToolSerialized> returnList = new ArrayList<ToolSerialized>();
		List<String> restrictedTools = getRestrictedToolsForUser(userId, nodeId);
		for(Tool tool : sakaiProxy.getAllTools()){
			returnList.add(new ToolSerialized(tool.getId(), tool.getTitle() + "(" + tool.getId() + ")", restrictedTools.contains(tool.getId())));
		}
		Collections.sort(returnList, new Comparator<ToolSerialized>() {
			@Override
			public int compare(ToolSerialized arg0, ToolSerialized arg1) {
				return arg0.getToolName().compareTo(arg1.getToolName());
			}
		});
		return returnList;
	}
	
	private List<String> getRestrictedToolsForUser(String userId, String nodeId){
		List<String> returnList = new ArrayList<String>();
		Set<String> userPerms = hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId});
		for(String userPerm : userPerms){
			if(userPerm.startsWith(DelegatedAccessConstants.NODE_PERM_DENY_TOOL_PREFIX)){
				returnList.add(userPerm.substring(DelegatedAccessConstants.NODE_PERM_DENY_TOOL_PREFIX.length()));
			}
		}
		return returnList;
	}
	
	private void saveRestrictedToolsForUser(String userId, String nodeId, List<String> toolIds){
		//add new tools:
		for(String newTool : toolIds){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_DENY_TOOL_PREFIX + newTool, false);
		}
	}
	
}
