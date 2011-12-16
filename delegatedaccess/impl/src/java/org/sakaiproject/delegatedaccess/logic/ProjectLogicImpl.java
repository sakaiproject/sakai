package org.sakaiproject.delegatedaccess.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessMutableTreeNode;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SelectionType;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.user.api.User;

import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.coursemanagement.api.CourseManagementService;


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
	@Getter @Setter
	private CourseManagementService cms;



	/**
	 * init - perform any actions required here for when this bean starts up
	 */
	public void init() {
		log.info("init");
	}

	/**
	 * returns the node for this id
	 * @param id
	 * @return
	 */
	public HierarchyNodeSerialized getNode(String id){
		return new HierarchyNodeSerialized(hierarchyService.getNodeById(id));
	}


	/**
	 * {@inheritDoc}
	 */
	public void updateNodePermissionsForUser(NodeModel nodeModel, String userId){
		//first step, remove all permissions so you can have a clear palet
		removeAllUserPermissions(nodeModel.getNodeId(), userId);

		//save shopping period admin information
		saveShoppingPeriodAdmin(nodeModel.isShoppingPeriodAdmin(), nodeModel.getNodeId(), userId);

		if(nodeModel.isDirectAccess()){
			//if direct access, add permissions, otherwise, leave it blank

			//site access permission
			hierarchyService.assignUserNodePerm(userId, nodeModel.getNodeId(), DelegatedAccessConstants.NODE_PERM_SITE_VISIT, false);

			//realm & role permissions
			saveRealmAndRoleAccess(userId, nodeModel.getRealm(), nodeModel.getRole(), nodeModel.getNodeId());

			//tool permissions:
			List<String> restrictedTools = new ArrayList<String>();
			for(ListOptionSerialized tool : nodeModel.getRestrictedTools()){
				if(tool.isSelected()){
					restrictedTools.add(tool.getId());
				}
			}
			if(!restrictedTools.isEmpty()){
				saveRestrictedToolsForUser(userId, nodeModel.getNodeId(), restrictedTools);
			}

			//term
			List<String> terms = new ArrayList<String>();
			for(ListOptionSerialized term : nodeModel.getTerms()){
				if(term.isSelected()){
					terms.add(term.getId());
				}
			}
			if(!terms.isEmpty()){
				saveTermsForUser(userId, nodeModel.getNodeId(), terms);
			}
			
			//save shopping period information
			if(DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId)){
				saveShoppingPeriodAuth(nodeModel.getShoppingPeriodAuth(), nodeModel.getNodeId());
				saveShoppingPeriodStartDate(nodeModel.getShoppingPeriodStartDate(), nodeModel.getNodeId());
				saveShoppingPeriodEndDate(nodeModel.getShoppingPeriodEndDate(), nodeModel.getNodeId());
				saveUpdatedDate(new Date(), nodeModel.getNodeId());
			}
		}

		if(nodeModel.isDirectAccess()){
			sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_ADD_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId() + "/realm/" + nodeModel.getRealm() + "/role/" + nodeModel.getRole(), true);
		}else{
			sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_DELETE_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
		}
	}

	private void saveShoppingPeriodAdmin(boolean admin, String nodeId, String userId){
		if(admin){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN, false);
		}
	}

	private void saveShoppingPeriodAuth(String auth, String nodeId){
		if(auth != null && !"".equals(auth) && !"null".equals(auth)){
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_AUTH + auth, false);
		}
	}
	private void saveShoppingPeriodStartDate(Date startDate, String nodeId){
		if(startDate != null){
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_START_DATE + startDate.getTime(), false);
		}
	}
	private void saveShoppingPeriodEndDate(Date endDate, String nodeId){
		if(endDate != null){
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_END_DATE + endDate.getTime(), false);
		}
	}
	private void saveUpdatedDate(Date updatedDate, String nodeId){
		if(updatedDate != null){
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_UPDATED_DATE + updatedDate.getTime(), false);
		}
	}

	private void removeAllUserPermissions(String nodeId, String userId){
		for(String perm : getPermsForUserNodes(userId, nodeId)){
			hierarchyService.removeUserNodePerm(userId, nodeId, perm, false);
		}
	}

	/**
	 * returns a list of nodes the user has site.access permission (aka access).  Only direct nodes, nothing inherited.
	 * @return
	 */
	public Set<HierarchyNodeSerialized> getAllNodesForUser(String userId) {

		Set<HierarchyNodeSerialized> accessNodes = getAccessNodesForUser(userId);
		Set<HierarchyNodeSerialized> adminNodes = getShoppingPeriodAdminNodesForUser(userId);

		accessNodes.addAll(adminNodes);
		return accessNodes;
	}

	public Set<HierarchyNodeSerialized> getAccessNodesForUser(String userId) {
		accessNodes = new ArrayList<String>();

		Set<HierarchyNodeSerialized> directAccessNodes = convertToSerializedNodeSet(hierarchyService.getNodesForUserPerm(userId, DelegatedAccessConstants.NODE_PERM_SITE_VISIT));
		//set the access and admin noes list for other functions to determine if a node is an access or admin node
		for(HierarchyNodeSerialized node : directAccessNodes){
			accessNodes.add(node.id);
		}
		return directAccessNodes;
	}

	public Set<HierarchyNodeSerialized> getShoppingPeriodAdminNodesForUser(String userId) {
		shoppingPeriodAdminNodes = new ArrayList<String>();

		Set<HierarchyNodeSerialized> adminNodes = convertToSerializedNodeSet(hierarchyService.getNodesForUserPerm(userId, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN));
		for(HierarchyNodeSerialized node : adminNodes){
			shoppingPeriodAdminNodes.add(node.id);
		}
		return adminNodes;
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
		String userId = sakaiProxy.getCurrentUserId();
		if(userId != null && !"".equals(userId)){
			Session session = sakaiProxy.getCurrentSession();
			Map<String, String[]> accessMap = new HashMap<String, String[]>();
			Map<String, String[]> toolMap = new HashMap<String, String[]>();
			TreeModel userTreeModel = createAccessTreeModelForUser(userId, false, true);
			if(userTreeModel != null){
				List<NodeModel> siteNodes = getSiteNodes(((DefaultMutableTreeNode) userTreeModel.getRoot()));
				for(NodeModel nodeModel : siteNodes){
					accessMap.put(nodeModel.getNode().description, nodeModel.getNodeAccessRealmRole());
					toolMap.put(nodeModel.getNode().description, nodeModel.getNodeRestrictedTools());
				}
			}
			//only worry about this if there is any delegated access:
			if(accessMap != null && accessMap.size() > 0){
				//remove any access to sites the user is a member of
				for(String ref : sakaiProxy.getUserMembershipForCurrentUser()){
					accessMap.remove(ref);
					toolMap.remove(ref);
				}
			}
			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP, accessMap);
			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS, toolMap);
			sakaiProxy.refreshCurrentUserAuthz();
		}
	}

	private List<NodeModel> getSiteNodes(DefaultMutableTreeNode treeNode){
		List<NodeModel> returnList = new ArrayList<NodeModel>();
		if(treeNode != null){
			if(((NodeModel) treeNode.getUserObject()).getNode().description.startsWith("/site/")){
				returnList.add((NodeModel) treeNode.getUserObject());
			}
			//check the rest of the children:
			for(int i = 0; i < treeNode.getChildCount(); i++){
				returnList.addAll(getSiteNodes((DefaultMutableTreeNode)treeNode.getChildAt(i)));
			}
		}

		return returnList;
	}

	/**
	 * {@inheritDoc}
	 */
	public void grantAccessToSite(NodeModel nodeModel){
		Site site = sakaiProxy.getSiteByRef(nodeModel.getNode().description);

		//only grant access to sites the user isn't a member of
		if(site != null && site.getUserRole(sakaiProxy.getCurrentUserId()) == null){
			Session session = sakaiProxy.getCurrentSession();
			Object sessionDelegatedAccessMap = session.getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP);
			Map<String, String[]> delegatedAccessMap = new HashMap<String, String[]>();
			if(sessionDelegatedAccessMap != null){
				delegatedAccessMap = (Map<String, String[]>) sessionDelegatedAccessMap;
			}
			String[] access = nodeModel.getNodeAccessRealmRole();
			if (access != null && access.length == 2
					&& access[0] != null
					&& access[1] != null
					&& !"".equals(access[0])
					&& !"".equals(access[1])
					&& !"null".equals(access[0])
					&& !"null".equals(access[1])) {
				delegatedAccessMap.put(nodeModel.getNode().description, access);
			}
			else{
				delegatedAccessMap.put(nodeModel.getNode().description, new String[]{"", ""});
			}
			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP, delegatedAccessMap);

			//Denied Tools List
			Map<String, String[]> deniedToolsMap = new HashMap<String, String[]>();
			Object sessionDeniedToolsMap = session.getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS);
			if(sessionDeniedToolsMap != null){
				deniedToolsMap = (Map<String, String[]>) sessionDeniedToolsMap;
			}

			String[] deniedTools = nodeModel.getNodeRestrictedTools();
			if(deniedTools != null){
				deniedToolsMap.put(nodeModel.getNode().description, deniedTools);
			}else{
				deniedToolsMap.put(nodeModel.getNode().description, new String[0]);
			}

			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS, deniedToolsMap);


			sakaiProxy.refreshCurrentUserAuthz();
		}
	}

	private HierarchyNodeSerialized getRootNode(){
		return new HierarchyNodeSerialized(hierarchyService.getRootNode(DelegatedAccessConstants.HIERARCHY_ID));
	}

	/**
	 * {@inheritDoc}
	 */
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
		if(realmId != null && role != null && !"".equals(realmId) && !"".equals(role) && !"null".equals(realmId) && !"null".equals(role)){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_REALM_PREFIX +realmId, false);
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_ROLE_PREFIX +role, false);
		}
	}

	private Set<String> getPermsForUserNodes(String userId, String nodeId){
		return hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId});
	}

	/**
	 * returns the user's realm and role information for the given node.  Doesn't include inherited information, will return
	 * a "" if not found.
	 * @param userId
	 * @param nodeId
	 * @return
	 */
	private String[] getAccessRealmRole(Set<String> perms){
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
	 * Returns a list of ToolSerialized that initialized the selected field
	 * @param userId
	 * @param nodeId
	 * @return
	 */
	public List<ListOptionSerialized> getRestrictedToolSerializedList(Set<String> perms){
		return getRestrictedToolSerializedList(perms, getEntireToolsList());
	}


	public List<ListOptionSerialized> getRestrictedToolSerializedList(Set<String> perms, List<ListOptionSerialized> blankList){
		List<String> restrictedTools = getRestrictedToolsForUser(perms);
		for(ListOptionSerialized tool : blankList){
			if(restrictedTools.contains(tool.getId()))
				tool.setSelected(true);
		}
		return blankList;
	}

	public List<ListOptionSerialized> getEntireToolsList(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(Tool tool : sakaiProxy.getAllTools()){
			returnList.add(new ListOptionSerialized(tool.getId(), tool.getTitle() + "(" + tool.getId() + ")", false));
		}
		Collections.sort(returnList, new Comparator<ListOptionSerialized>() {
			public int compare(ListOptionSerialized arg0, ListOptionSerialized arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		return returnList;
	}

	private List<String> getRestrictedToolsForUser(Set<String> userPerms){
		List<String> returnList = new ArrayList<String>();
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
	
	//terms
	public List<ListOptionSerialized> getTermSerializedList(Set<String> perms){
		return getTermSerializedList(perms, getEntireTermsList());
	}


	public List<ListOptionSerialized> getTermSerializedList(Set<String> perms, List<ListOptionSerialized> blankList){
		List<String> terms = getTermsForUser(perms);
		for(ListOptionSerialized term : blankList){
			if(terms.contains(term.getId()))
				term.setSelected(true);
		}
		return blankList;
	}

	public List<ListOptionSerialized> getEntireTermsList(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		
		for(AcademicSession session : cms.getAcademicSessions()){
			String termId = session.getEid();
			if(!"term_eid".equals(sakaiProxy.getTermField())){
				termId = session.getTitle();
			}
			returnList.add(new ListOptionSerialized(termId, session.getTitle(), false));
		}
		
		return returnList;
	}

	private List<String> getTermsForUser(Set<String> userPerms){
		List<String> returnList = new ArrayList<String>();
		for(String userPerm : userPerms){
			if(userPerm.startsWith(DelegatedAccessConstants.NODE_PERM_TERM_PREFIX)){
				returnList.add(userPerm.substring(DelegatedAccessConstants.NODE_PERM_TERM_PREFIX.length()));
			}
		}
		return returnList;
	}

	private void saveTermsForUser(String userId, String nodeId, List<String> termIds){
		//add new tools:
		for(String newTerm : termIds){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_TERM_PREFIX + newTerm, false);
		}
	}

	public List<NodeModel> searchUserSites(String search, TreeModel treeModel, Map<String, String> advancedOptions){
		if(search == null){
			search = "";
		}
		Map<String, SiteSearchData> siteSubset = null;
		if(!"".equals(search) || (advancedOptions != null && !advancedOptions.isEmpty())){
			siteSubset = searchSites(search, advancedOptions);
		}
		return searchUserTree((DefaultMutableTreeNode) treeModel.getRoot(), siteSubset);
	}

	private List<NodeModel> searchUserTree(DefaultMutableTreeNode node, Map<String, SiteSearchData> siteSubset){
		List<NodeModel> returnList = new ArrayList<NodeModel>();
		if(node != null && siteSubset != null && !siteSubset.isEmpty()){
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.getNode().description.startsWith("/site/")){
				String siteId = nodeModel.getNode().description.substring(6);
				if(siteSubset.containsKey(siteId)){
					String term = siteSubset.get(siteId).getTerm();
					nodeModel.setSiteTerm(term == null ? "" : term);
					String instructors = "";
					for(User user : siteSubset.get(siteId).getInstructors()){
						if(!"".equals(instructors)){
							instructors += "; ";
						}
						instructors += user.getSortName();
					}
					nodeModel.setSiteInstructors(instructors);
					returnList.add(nodeModel);
				}
			}
			for(int i = 0; i < node.getChildCount(); i++){
				returnList.addAll(searchUserTree((DefaultMutableTreeNode) node.getChildAt(i), siteSubset));
			}
		}
		return returnList;
	}

	public Map<String, SiteSearchData> searchSites(String search, Map<String, String> advancedOptions){
		Map<String, SiteSearchData> sites = new HashMap<String, SiteSearchData>();
		Map<String, String> propsMap = null;
		
		if (advancedOptions != null
				&& (advancedOptions
						.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR) || advancedOptions
						.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_TERM))) {
			//Advanced Search
			
			if (advancedOptions
					.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_TERM)) {
				propsMap = new HashMap<String, String>();
				propsMap.put("term", advancedOptions
						.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM));
			}
			// find all user's with this name/id/email
			if (advancedOptions
					.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR)) {
				List<User> searchUsers = sakaiProxy
						.searchUsers(
								advancedOptions
										.get(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR),
								1, DelegatedAccessConstants.SEARCH_RESULTS_MAX);
				for (User user : searchUsers) {
					for (Site site : getUserUpdatePermissionMembership(
							user.getId(), search, propsMap)) {
						if(sites.containsKey(site.getId())){
							sites.get(site.getId()).getInstructors().add(user);
						}else{
							List<User> usersList = new ArrayList<User>();
							usersList.add(user);
							sites.put(site.getId(), new SiteSearchData(site, usersList));
						}
					}
				}

			}
			if (advancedOptions
					.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_TERM)) {
				if (sites.isEmpty()) {
					// grab all sites in term since there are no other
					// restrictions
					for (Site site : sakaiProxy.getSites(SelectionType.ANY,
							search, propsMap)) {
						sites.put(site.getId(), new SiteSearchData(site, new ArrayList<User>()));
					}
				} else {
					for (Iterator iterator = sites.entrySet().iterator(); iterator
							.hasNext();) {
						Entry<String, SiteSearchData> entry = (Entry<String, SiteSearchData>) iterator
								.next();
						if (entry.getValue().getTerm() == null
								|| !entry.getValue().getTerm().toLowerCase().contains(
												advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM).toLowerCase())){
							sites.remove(entry.getKey());
						}
					}
				}

			}
		} else {
			// search title or id
			for (Site site : sakaiProxy.getSites(SelectionType.ANY, search,
					null)) {
				sites.put(site.getId(), new SiteSearchData(site, new ArrayList<User>()));
			}
		}
		return sites;
	}
	
	private List<Site> getUserUpdatePermissionMembership(String userId, String search, Map<String, String> propsMap){
		String currentUserId = sakaiProxy.getCurrentUserId();
		//set session user id to this id:
		Session sakaiSession = sakaiProxy.getCurrentSession();
		sakaiSession.setUserId(userId);
		sakaiSession.setUserEid(userId);
		List<Site> siteList = sakaiProxy.getSites(SelectionType.UPDATE, search, propsMap);
		//return to current user id
		sakaiSession.setUserId(currentUserId);
		sakaiSession.setUserEid(currentUserId);
		
		return siteList;
	}
	
	private String[] convertToArray(List<String> list){
		String[] returnArray = new String[]{};
		if(!list.isEmpty()){
			returnArray = new String[list.size()];
			for(int i = 0; i < list.size(); i++){
				returnArray[i] = list.get(i);
			}
		}
		return returnArray;
	}



	//TREE MODEL FUNCTIONS:

	//NodeCache stores HierarchyNodeSerialed nodes for faster lookups
	private Map<String,HierarchyNodeSerialized> nodeCache = new HashMap<String, HierarchyNodeSerialized>();
	private List<String> accessNodes = new ArrayList<String>();
	private List<String> shoppingPeriodAdminNodes = new ArrayList<String>();
	/**
	 * Creates the model that feeds the tree.
	 * 
	 * @return New instance of tree model.
	 */
	public TreeModel createEntireTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade)
	{
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		List<List> l1 = getTreeListForUser(userId, addDirectChildren, cascade, getAllNodesForUser(userId));
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, userId, getEntireToolsList(), getEntireTermsList(), addDirectChildren);
	}

	public TreeModel createAccessTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade)
	{
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		accessNodes = new ArrayList<String>();
		shoppingPeriodAdminNodes = new ArrayList<String>();

		List<List> l1 = getTreeListForUser(userId, addDirectChildren, cascade, getAccessNodesForUser(userId));
		//order tree model:
		orderTreeModel(l1);

		return trimTreeForTerms(convertToTreeModel(l1, userId, getEntireToolsList(), getEntireTermsList(), addDirectChildren));
	}

	public TreeModel getTreeModelForShoppingPeriod(){
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		Set<HierarchyNodeSerialized> rootSet = new HashSet<HierarchyNodeSerialized>();
		rootSet.add(new HierarchyNodeSerialized(hierarchyService.getRootNode(DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID)));
		List<List> l1 = getTreeListForUser("", false, true, rootSet);
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, "", getEntireToolsList(), getEntireTermsList(), false);
	}
	
	//This will search through the tree model and trim out any sites that are restricted b/c of the term value
	private TreeModel trimTreeForTerms(TreeModel treeModel){
		//need a remove map b/c you can't remove nodes while you are searching, must do it afterwards
		Map<DefaultMutableTreeNode, List<DefaultMutableTreeNode>> removeMap = new HashMap<DefaultMutableTreeNode, List<DefaultMutableTreeNode>>();
		
		trimTreeForTermsHelper((DefaultMutableTreeNode) treeModel.getRoot(), null, removeMap);
		
		//now remove everything that was found
		for(Entry<DefaultMutableTreeNode, List<DefaultMutableTreeNode>> entry : removeMap.entrySet()){
			DefaultMutableTreeNode removeParent = entry.getKey();
			for(DefaultMutableTreeNode removeNode : entry.getValue()){
				removeParent.remove(removeNode);
			}
		}
		return treeModel;
	}
	
	private void trimTreeForTermsHelper(DefaultMutableTreeNode node, DefaultMutableTreeNode parent, Map<DefaultMutableTreeNode, List<DefaultMutableTreeNode>> removeMap){
		if(node != null){
			for(int i = 0; i < node.getChildCount(); i++){
				trimTreeForTermsHelper((DefaultMutableTreeNode) node.getChildAt(i), node, removeMap);
			}
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.getNode().description.startsWith("/site/")){
				String term = nodeModel.getNode().permKey;
				if(!checkTerm(nodeModel.getNodeTerms(), term)){
					if(parent != null){
						if(removeMap.containsKey(parent)){
							((List<DefaultMutableTreeNode>) removeMap.get(parent)).add(node);
						}else{
							List<DefaultMutableTreeNode> list = new ArrayList<DefaultMutableTreeNode>();
							list.add(node);
							removeMap.put(parent, list);
						}
					}
				}
			}

		}
	}

	private boolean checkTerm(String[] terms, String siteTerm){
		boolean returnVal = true;
		if(terms != null && terms.length > 0){
			returnVal = false;
			if(siteTerm != null && !"".equals(siteTerm)){
				for(String term : terms){
					if(term.equals(siteTerm)){
						returnVal = true;
						break;
					}
				}
			}
		}
		return returnVal;
	}
	//get the entire tree for a user and populates the information that may exist
	public TreeModel getEntireTreePlusUserPerms(String userId){
		//call this to instantiated the accessNodes and shoppingPeriodAdminNodes lists
		getAllNodesForUser(userId);
		//just get the root of the tree and then ask for all cascading nodes
		Set<HierarchyNodeSerialized> rootSet = new HashSet<HierarchyNodeSerialized>();
		rootSet.add(getRootNode());
		List<List> l1 = getTreeListForUser(userId, false, true, rootSet);
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, userId, getEntireToolsList(), getEntireTermsList(), false);
	}

	public TreeModel createTreeModelForShoppingPeriod(String userId)
	{
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.

		List<List> l1 = getTreeListForUser(DelegatedAccessConstants.SHOPPING_PERIOD_USER, false, true, getShoppingPeriodAdminNodesForUser(userId));

		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, DelegatedAccessConstants.SHOPPING_PERIOD_USER, getEntireToolsList(), getEntireTermsList(), false);
	}

	/**
	 * Takes a list representation of a tree and creates the TreeModel
	 * 
	 * @param map
	 * @param userId
	 * @return
	 */
	private TreeModel convertToTreeModel(List<List> map, String userId, List<ListOptionSerialized> blankRestrictedTools, List<ListOptionSerialized> blankTerms, boolean addDirectChildren)
	{
		TreeModel model = null;
		if(!map.isEmpty() && map.size() == 1){

			DefaultMutableTreeNode rootNode = add(null, map, userId, blankRestrictedTools, blankTerms, addDirectChildren);
			model = new DefaultTreeModel(rootNode);
		}
		return model;
	}

	private Date getShoppingStartDate(Set<String> perms){
		Date returnDate = null;
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_START_DATE)){
				try{
					returnDate = new Date(Long.parseLong(perm.substring(DelegatedAccessConstants.NODE_PERM_SHOPPING_START_DATE.length())));
				}catch (Exception e) {
					//wrong format, ignore
				}
			}
		}

		return returnDate;
	}

	private Date getShoppingEndDate(Set<String> perms){
		Date returnDate = null;
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_END_DATE)){
				try{
					returnDate = new Date(Long.parseLong(perm.substring(DelegatedAccessConstants.NODE_PERM_SHOPPING_END_DATE.length())));
				}catch (Exception e) {
					//wrong format, ignore
				}
			}
		}

		return returnDate;
	}

	private Date getPermDate(Set<String> perms, String permName){
		Date returnDate = null;
		for(String perm : perms){
			if(perm.startsWith(permName)){
				try{
					returnDate = new Date(Long.parseLong(perm.substring(permName.length())));
				}catch (Exception e) {
					//wrong format, ignore
				}
			}
		}
		return returnDate;
	}

	private String getShoppingPeriodAuth(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_AUTH)){
				return perm.substring(DelegatedAccessConstants.NODE_PERM_SHOPPING_AUTH.length());
			}
		}
		return "";
	}

	private boolean isShoppingPeriodAdmin(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN)){
				return true;
			}
		}
		return false;
	}

	private boolean getIsDirectAccess(Set<String> perms){
		for(String perm : perms){
			if(perm.equals(DelegatedAccessConstants.NODE_PERM_SITE_VISIT)){
				return true;
			}
		}
		return false;
	}

	private List<ListOptionSerialized> copyListOptions(List<ListOptionSerialized> options){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(ListOptionSerialized option : options){
			returnList.add(new ListOptionSerialized(option.getId(), option.getName(), option.isSelected()));
		}
		return returnList;
	}

	/**
	 * Adds node to parent and creates the NodeModel to store in the tree
	 * @param parent
	 * @param sub
	 * @param userId
	 * @return
	 */
	private DefaultMutableTreeNode add(DefaultMutableTreeNode parent, List<List> sub, String userId, List<ListOptionSerialized> blankRestrictedTools, List<ListOptionSerialized> blankTerms, boolean addDirectChildren)
	{
		DefaultMutableTreeNode root = null;
		for (List nodeList : sub)
		{
			HierarchyNodeSerialized node = (HierarchyNodeSerialized) nodeList.get(0);
			List children = (List) nodeList.get(1);
			String realm = "";
			String role = "";
			boolean directAccess = false;
			Date startDate = null;
			Date endDate = null;
			String shoppingPeriodAuth = "";
			Date updated = null;
			Date processed = null;

			//you must copy in order not to pass changes to other nodes
			List<ListOptionSerialized> restrictedTools = copyListOptions(blankRestrictedTools);
			List<ListOptionSerialized> terms = copyListOptions(blankTerms);
			boolean shoppingPeriodAdmin = shoppingPeriodAdminNodes.contains(node.id);
			if(DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId) || accessNodes.contains(node.id) || shoppingPeriodAdminNodes.contains(node.id)){
				Set<String> perms = getPermsForUserNodes(userId, node.id);
				String[] realmRole = getAccessRealmRole(perms);
				realm = realmRole[0];
				role = realmRole[1];
				startDate = getShoppingStartDate(perms);
				endDate = getShoppingEndDate(perms);
				shoppingPeriodAuth = getShoppingPeriodAuth(perms);
				restrictedTools = getRestrictedToolSerializedList(perms, restrictedTools);
				terms = getTermSerializedList(perms, terms);
				directAccess = getIsDirectAccess(perms);
				updated = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_UPDATED_DATE);
				processed = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_PROCESSED_DATE);
			}
			NodeModel parentNodeModel = null;
			if(parent != null){
				parentNodeModel = ((NodeModel) parent.getUserObject());
			}
			DefaultMutableTreeNode child = new DelegatedAccessMutableTreeNode();
			child.setUserObject(new NodeModel(node.id, node, directAccess, realm, role, parentNodeModel, 
					restrictedTools, startDate, endDate, shoppingPeriodAuth, addDirectChildren && !children.isEmpty(), shoppingPeriodAdmin, updated, processed, terms));
			if(parent == null){
				//we have the root, set it
				root = child;
			}else{
				parent.add(child);
			}
			if(!children.isEmpty()){
				add(child, children, userId, blankRestrictedTools, blankTerms, addDirectChildren);
			}
		}
		return root;
	}

//	/**
//	 * returns a map of all realms and their roles from sakaiProxy.getSiteTemplates()
//	 * 
//	 * @return
//	 */
//	private Map<String, List<String>> getRealmMap(){
//		List<AuthzGroup> siteTemplates = sakaiProxy.getSiteTemplates();
//		final Map<String, List<String>> realmMap = new HashMap<String, List<String>>();
//		for(AuthzGroup group : siteTemplates){
//			List<String> roles = new ArrayList<String>();
//			for(Role role : group.getRoles()){
//				roles.add(role.getId());
//			}
//			realmMap.put(group.getId(), roles);
//		}
//		return realmMap;
//	}

	/**
	 * takes a list representation of the tree and orders it Alphabetically
	 * @param hierarchy
	 */
	private void orderTreeModel(List<List> hierarchy){
		if(hierarchy != null){
			for(List nodeList : hierarchy){
				orderTreeModel((List)nodeList.get(1));
			}
			Collections.sort(hierarchy, new NodeListComparator());
		}
	}

	/**
	 * This is a simple comparator to order the tree nodes alphabetically
	 *
	 */
	private class NodeListComparator implements Comparator<List>{
		public int compare(List o1, List o2) {
			return ((HierarchyNodeSerialized) o1.get(0)).title.compareToIgnoreCase(((HierarchyNodeSerialized) o2.get(0)).title);
		}
	}

	private List<List> getTreeListForUser(String userId, boolean addDirectChildren, boolean cascade, Set<HierarchyNodeSerialized> nodes){
		List<List> l1 = new ArrayList<List>();
		List<List> currentLevel = l1;

		for(HierarchyNodeSerialized node : nodes){
			for(String parentId : node.parentNodeIds){
				HierarchyNodeSerialized parentNode = getCachedNode(parentId);

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
	 * Checks nodeCache for node with given id.  If not found,
	 * looks up the node in the db and saves it in the cache
	 * 
	 * @param id
	 * @return
	 */
	private HierarchyNodeSerialized getCachedNode(String id){
		HierarchyNodeSerialized node = nodeCache.get(id);
		if(node == null){
			node = getNode(id);
			nodeCache.put(id, node);
		}
		return node;
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
				child.add(getCachedNode(childId));
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
			HierarchyNodeSerialized childNode = getCachedNode(childId);

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
	 * Adds children node to a node that hasn't had it's children populated.  This is used to increase the efficiency
	 * of the tree so you can create the structure on the fly with ajax
	 * 
	 * @param node
	 * @param tree
	 * @param target
	 * @param userId
	 * @return
	 */
	public boolean addChildrenNodes(Object node, String userId, List<ListOptionSerialized> blankRestrictedTools, List<ListOptionSerialized> blankTerms){
		boolean anyAdded = false;
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node;
		NodeModel nodeModel = (NodeModel) ((DefaultMutableTreeNode) node).getUserObject();
		if(nodeModel.getNode() != null){
			List<List> childrenNodes = getDirectChildren(nodeModel.getNode());
			Collections.sort(childrenNodes, new NodeListComparator());
			for(List childList : childrenNodes){
				boolean newlyAdded = addChildNodeToTree((HierarchyNodeSerialized) childList.get(0), parentNode, userId, blankRestrictedTools, blankTerms);
				anyAdded = anyAdded || newlyAdded;
			}
		}
		return anyAdded;
	}

	/**
	 * This is a helper function for addChildrenNodes.  It will add the child nodes to the parent node and create the NodeModel.
	 * 
	 * @param childNode
	 * @param parentNode
	 * @param realmMap
	 * @param userId
	 * @return
	 */
	private boolean addChildNodeToTree(HierarchyNodeSerialized childNode, DefaultMutableTreeNode parentNode, String userId, List<ListOptionSerialized> blankRestrictedTools, List<ListOptionSerialized> blankTerms){
		boolean added = false;
		if(!doesChildExist(childNode.id, parentNode)){
			//just create a blank child since the user should already have all the nodes with information in the db
			String realm = "";
			String role = "";
			boolean selected = false;
			Date startDate = null;
			Date endDate = null;
			String shoppingPeriodAuth = "";
			//you must copy to not pass changes to other nodes
			List<ListOptionSerialized> restrictedTools = copyListOptions(blankRestrictedTools);
			List<ListOptionSerialized> terms = copyListOptions(blankTerms);
			Date updated= null;
			Date processed = null;
			boolean shoppingPeriodAdmin = false;
			DefaultMutableTreeNode child = new DelegatedAccessMutableTreeNode();
			child.setUserObject(new NodeModel(childNode.id, childNode, false, realm, role,
					((NodeModel) parentNode.getUserObject()), restrictedTools, startDate, endDate, 
					shoppingPeriodAuth, false, shoppingPeriodAdmin, updated, processed, terms));

			parentNode.add(child);
			added = true;
		}
		return added;
	}

	/**
	 * Determines if the child exists in the tree structure.  This is a helper function for addChildNodeToTree to ensure 
	 * the duplicate child nodes aren't added
	 * 
	 * @param childNodeId
	 * @param parentNode
	 * @return
	 */
	private boolean doesChildExist(String childNodeId, DefaultMutableTreeNode parentNode){
		boolean exists = false;

		for(int i = 0; i < parentNode.getChildCount(); i++){
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parentNode.getChildAt(i);
			if(childNodeId.equals(((NodeModel) child.getUserObject()).getNodeId())){
				exists = true;
				break;
			}
		}

		return exists;
	}

	public NodeModel getNodeModel(String nodeId, String userId){
		HierarchyNodeSerialized node = getNode(nodeId);
		NodeModel parentNodeModel = null;
		if(node.parentNodeIds != null && node.parentNodeIds.size() > 0){
			//grad the last parent in the Set (this is the closest parent)
			parentNodeModel = getNodeModel((String) node.parentNodeIds.toArray()[node.parentNodeIds.size() -1], userId);
		}
		Set<String> nodePerms = hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId});
		Set<String> perms = getPermsForUserNodes(userId, node.id);
		String[] realmRole = getAccessRealmRole(perms);
		String realm = realmRole[0];
		String role = realmRole[1];
		Date startDate = getShoppingStartDate(perms);
		Date endDate = getShoppingEndDate(perms);
		String shoppingPeriodAuth = getShoppingPeriodAuth(perms);
		List<ListOptionSerialized> restrictedTools = getRestrictedToolSerializedList(perms, getEntireToolsList());
		List<ListOptionSerialized> terms = getTermSerializedList(perms, getEntireToolsList());
		boolean direct = getIsDirectAccess(perms);
		boolean shoppingPeriodAdmin = isShoppingPeriodAdmin(perms);
		Date updated = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_UPDATED_DATE);
		Date processed = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_PROCESSED_DATE);

		NodeModel nodeModel = new NodeModel(node.id, node, getIsDirectAccess(nodePerms),
				realm, role, parentNodeModel, restrictedTools, startDate, endDate, shoppingPeriodAuth, false, shoppingPeriodAdmin, updated, processed, terms);
		return nodeModel;
	}

	/**
	 * {@inheritDoc}
	 */
	public void assignUserNodePerm(String userId, String nodeId, String perm, boolean cascade) {		
		hierarchyService.assignUserNodePerm(userId, nodeId, perm, false);
	}

	public Date getShoppingPeriodProccessedDate(String userId, String nodeId){
		Date returnDate = null;
		Set<String> perms = getPermsForUserNodes(userId, nodeId);
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_PROCESSED_DATE)){
				try{
					returnDate = new Date(Long.parseLong(perm.substring(DelegatedAccessConstants.NODE_PERM_SHOPPING_PROCESSED_DATE.length())));
				}catch (Exception e) {
					log.warn(e);
				}
				break;
			}
		}
		return returnDate;
	}
	
	private class SiteSearchData{
		private Site site;
		private List<User> instructors = new ArrayList();
		
		public SiteSearchData(Site site, List<User> instructors){
			this.site = site;
			this.instructors = instructors;
		}
		public Site getSite() {
			return site;
		}
		public void setSite(Site site) {
			this.site = site;
		}
		public List<User> getInstructors() {
			return instructors;
		}
		public void setInstructors(List<User> instructors) {
			this.instructors = instructors;
		}
		
		public String getTerm(){
			return site.getProperties().getProperty("term");
		}
	}
}
