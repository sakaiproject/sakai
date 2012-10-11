package org.sakaiproject.delegatedaccess.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import org.sakaiproject.coursemanagement.api.AcademicSession;
import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
import org.sakaiproject.delegatedaccess.model.AccessNode;
import org.sakaiproject.delegatedaccess.model.HierarchyNodeSerialized;
import org.sakaiproject.delegatedaccess.model.ListOptionSerialized;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.model.SearchResult;
import org.sakaiproject.delegatedaccess.model.SiteSearchResult;
import org.sakaiproject.delegatedaccess.model.SiteSerialized;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessMutableTreeNode;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SelectionType;
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
	@Getter @Setter
	private DelegatedAccessDao dao;
	//NodeCache stores HierarchyNodeSerialed nodes for faster lookups
	@Getter @Setter
	private Cache nodeCache;
	//Stores restricted tools map for users when they log back in
	@Getter @Setter
	private Cache restrictedAuthToolsCache;
	
	@Getter @Setter
	private Cache restrictedPublicToolsCache;
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

		//save access admin setting
		saveAccessAdmin(nodeModel.isAccessAdmin(), nodeModel.getNodeId(), userId);
		
		//save shopping period admin information
		saveShoppingPeriodAdmin(nodeModel.isShoppingPeriodAdmin(), nodeModel.getNodeId(), userId);

		if(nodeModel.isDirectAccess()){
			//if direct access, add permissions, otherwise, leave it blank

			//site access permission
			hierarchyService.assignUserNodePerm(userId, nodeModel.getNodeId(), DelegatedAccessConstants.NODE_PERM_SITE_VISIT, false);

			//realm & role permissions
			saveRealmAndRoleAccess(userId, nodeModel.getRealm(), nodeModel.getRole(), nodeModel.getNodeId());

			//tool permissions:
			List<String> restrictedAuthTools = new ArrayList<String>();
			for(ListOptionSerialized tool : nodeModel.getRestrictedAuthTools()){
				if(tool.isSelected()){
					restrictedAuthTools.add(tool.getId());
				}
			}
			if(!restrictedAuthTools.isEmpty()){
				saveRestrictedAuthToolsForUser(userId, nodeModel.getNodeId(), restrictedAuthTools);
			}
			
			//public tool permissions:
			List<String> restrictedPublicTools = new ArrayList<String>();
			for(ListOptionSerialized tool : nodeModel.getRestrictedPublicTools()){
				if(tool.isSelected()){
					restrictedPublicTools.add(tool.getId());
				}
			}
			if(!restrictedPublicTools.isEmpty()){
				saveRestrictedPublicToolsForUser(userId, nodeModel.getNodeId(), restrictedPublicTools);
			}

			//save shopping period information
			if(DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId)){
				saveShoppingPeriodStartDate(nodeModel.getShoppingPeriodStartDate(), nodeModel.getNodeId());
				saveShoppingPeriodEndDate(nodeModel.getShoppingPeriodEndDate(), nodeModel.getNodeId());
				saveShoppingPeriodRevokeInstructorEditable(nodeModel.isShoppingPeriodRevokeInstructorEditable(), nodeModel.getNodeId());
				saveShoppingPeriodRevokeInstructorPublicOpt(nodeModel.isShoppingPeriodRevokeInstructorPublicOpt(), nodeModel.getNodeId());
			}
		}
		
		//Modification Date Tracking and Event posting:
		
		//if the user still has access of some kind, post a modification event (since only modified nodes get saved) as well as update the modification timestamp
		if(nodeModel.isDirectAccess() || nodeModel.isShoppingPeriodAdmin() || nodeModel.isAccessAdmin()){
			saveModifiedData(userId, nodeModel.getNodeId());
			sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_MODIFIED_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId() + "/realm/" + nodeModel.getRealm() + "/role/" + nodeModel.getRole(), true);
		}
		
		//if the user added or removed direct access permissions, post an event
		if(nodeModel.isDirectAccess() != nodeModel.isDirectAccessOrig()){
			if(nodeModel.isDirectAccess()){
				sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_ADD_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId() + "/realm/" + nodeModel.getRealm() + "/role/" + nodeModel.getRole(), true);
			}else{
				sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_DELETE_USER_PERMS, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
			}
		}
		//If ths user has been granted or removed shopping admin access, post an event and save the shopping modification timestamp
		//Theoretically the shopping period user would never be set to be a shopping period admin, but just in case someone tries, check for it
		if(!DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId) && nodeModel.isShoppingPeriodAdmin() != nodeModel.isShoppingPeriodAdminOrig()){
			saveShoppingPeriodAdminModifiedData(userId, nodeModel.getNodeId());
			if(nodeModel.isShoppingPeriodAdmin()){
				sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_ADD_USER_SHOPPING_ADMIN, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
			}else{
				sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_DELETE_USER_SHOPPING_ADMIN, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
			}
		}else if(!DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId) && nodeModel.getShoppingAdminModified() != null){
			//If there was no modification to shopping admin permission but there is a timestamp of previous modifications,
			//we need to resave it so we don't lose this information:
			hierarchyService.assignUserNodePerm(userId, nodeModel.getNodeId(), DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED + nodeModel.getShoppingAdminModified().getTime(), false);
			hierarchyService.assignUserNodePerm(userId, nodeModel.getNodeId(), DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED_BY + nodeModel.getShoppingAdminModifiedBy(), false);
		}
		if(nodeModel.isAccessAdmin() != nodeModel.isAccessAdminOrig()){
			if(nodeModel.isAccessAdmin()){
				sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_ADD_USER_ACCESS_ADMIN, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
			}else{
				sakaiProxy.postEvent(DelegatedAccessConstants.EVENT_DELETE_USER_ACCESS_ADMIN, "/user/" + userId + "/node/" + nodeModel.getNodeId(), true);
			}
		}
	}

	private void saveShoppingPeriodAdmin(boolean admin, String nodeId, String userId){
		//only save shopping period admin flag for real users
		if(admin && !DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId)){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN, false);
		}
	}
	
	private void saveAccessAdmin(boolean accessAdmin, String nodeId, String userId){
		//only save shopping period admin flag for real users
		if(accessAdmin && !DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId)){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_ACCESS_ADMIN, false);
		}
	}
	
	private void saveShoppingPeriodAdminModifiedData(String userId, String nodeId){
		hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED + new Date().getTime(), false);
		hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED_BY + sakaiProxy.getCurrentUserId(), false);
	}
	
	
	private void saveModifiedData(String userId, String nodeId){
		hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_MODIFIED + new Date().getTime(), false);
		hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_MODIFIED_BY + sakaiProxy.getCurrentUserId(), false);
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
	
	private void saveShoppingPeriodRevokeInstructorEditable(boolean shoppingPeriodRevokeInstructorEditable, String nodeId){
		if(shoppingPeriodRevokeInstructorEditable){
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_EDITABLE, false);
		}
	}
	
	private void saveShoppingPeriodRevokeInstructorPublicOpt(boolean bool, String nodeId){
		if(bool){
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_PUBLIC_OPT, false);
		}
	}
		
	public void saveHierarchyJobLastRunDate(Date runDate, String nodeId){
		if(runDate != null){
			clearHierarchyJobLastRunDate(nodeId);
			hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SITE_HIERARCHY_USER, nodeId, DelegatedAccessConstants.NODE_PERM_SITE_HIERARCHY_JOB_LAST_RUN_DATE + runDate.getTime(), false);
		}
	}
	private void clearHierarchyJobLastRunDate(String nodeId){
		for(String perm : hierarchyService.getPermsForUserNodes(DelegatedAccessConstants.SITE_HIERARCHY_USER, new String[]{nodeId})){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SITE_HIERARCHY_JOB_LAST_RUN_DATE)){
				hierarchyService.removeUserNodePerm(DelegatedAccessConstants.SITE_HIERARCHY_USER, nodeId, perm, false);
			}	
		}
	}
	
	public Date getHierarchyJobLastRunDate(String nodeId){
		Date returnDate = null;
		for(String perm : hierarchyService.getPermsForUserNodes(DelegatedAccessConstants.SITE_HIERARCHY_USER, new String[]{nodeId})){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SITE_HIERARCHY_JOB_LAST_RUN_DATE)){
				try{
					returnDate = new Date(Long.parseLong(perm.substring(DelegatedAccessConstants.NODE_PERM_SITE_HIERARCHY_JOB_LAST_RUN_DATE.length())));
				}catch (Exception e) {
					//wrong format, ignore
				}
			}	
		}
		return returnDate;
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
		Set<HierarchyNodeSerialized> accessAdminNodes = getAccessAdminNodesForUser(userId);

		accessNodes.addAll(adminNodes);
		accessNodes.addAll(accessAdminNodes);
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
	
	public Set<HierarchyNodeSerialized> getAccessAdminNodesForUser(String userId) {
		accessAdminNodes = new ArrayList<String>();

		Set<HierarchyNodeSerialized> adminNodes = convertToSerializedNodeSet(hierarchyService.getNodesForUserPerm(userId, DelegatedAccessConstants.NODE_PERM_ACCESS_ADMIN));
		for(HierarchyNodeSerialized node : adminNodes){
			accessAdminNodes.add(node.id);
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
			Set accessNodes = getAccessNodesForUser(userId);
			if(accessNodes != null && accessNodes.size() > 0){
				session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DELEGATED_ACCESS_FLAG, true);
				//need to clear sakai realm permissions cache for user since Denied Tools list is tied to
				//session and permissions are a saved in a system cache
				Element el = restrictedAuthToolsCache.get(userId);
				if(el != null){
					session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS, el.getObjectValue());
				}
				Element elPub = restrictedPublicToolsCache.get(userId);
				if(elPub != null){
					session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS2, elPub.getObjectValue());
				}
			}
		}
	}

	private List<NodeModel> getSiteNodes(DefaultMutableTreeNode treeNode){
		List<NodeModel> returnList = new ArrayList<NodeModel>();
		if(treeNode != null){
			if(((NodeModel) treeNode.getUserObject()).getNode().title.startsWith("/site/")){
				returnList.add((NodeModel) treeNode.getUserObject());
			}
			//check the rest of the children:
			for(int i = 0; i < treeNode.getChildCount(); i++){
				returnList.addAll(getSiteNodes((DefaultMutableTreeNode)treeNode.getChildAt(i)));
			}
		}

		return returnList;
	}

	private HierarchyNodeSerialized getRootNode(){
		return new HierarchyNodeSerialized(hierarchyService.getRootNode(DelegatedAccessConstants.HIERARCHY_ID));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<SearchResult> searchUsers(String search) {
		List<User> searchResult = sakaiProxy.searchUsers(search);
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
	public List<ListOptionSerialized> getRestrictedAuthToolSerializedList(Set<String> perms){
		return getRestrictedAuthToolSerializedList(perms, getEntireToolsList());
	}
	
	public List<ListOptionSerialized> getRestrictedPublicToolSerializedList(Set<String> perms){
		return getRestrictedPublicToolSerializedList(perms, getEntireToolsList());
	}


	public List<ListOptionSerialized> getRestrictedAuthToolSerializedList(Set<String> perms, List<ListOptionSerialized> blankList){
		List<String> restrictedTools = getRestrictedAuthToolsForUser(perms);
		for(ListOptionSerialized tool : blankList){
			if(restrictedTools.contains(tool.getId()))
				tool.setSelected(true);
		}
		return blankList;
	}
	
	public List<ListOptionSerialized> getRestrictedPublicToolSerializedList(Set<String> perms, List<ListOptionSerialized> blankList){
		List<String> restrictedTools = getRestrictedPublicToolsForUser(perms);
		for(ListOptionSerialized tool : blankList){
			if(restrictedTools.contains(tool.getId()))
				tool.setSelected(true);
		}
		return blankList;
	}

	public List<ListOptionSerialized> getEntireToolsList(){
		List<ListOptionSerialized> returnList = new ArrayList<ListOptionSerialized>();
		for(Tool tool : sakaiProxy.getAllTools()){
			returnList.add(new ListOptionSerialized(tool.getId(), tool.getTitle(), false));
		}
		//the home tool is special, so add this case
		String[] homeTools = sakaiProxy.getHomeTools();
		if(homeTools != null && homeTools.length > 0){
			returnList.add(new ListOptionSerialized("Home", "Home", false));
		}
		Collections.sort(returnList, new Comparator<ListOptionSerialized>() {
			public int compare(ListOptionSerialized arg0, ListOptionSerialized arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		return returnList;
	}

	private List<String> getRestrictedAuthToolsForUser(Set<String> userPerms){
		List<String> returnList = new ArrayList<String>();
		for(String userPerm : userPerms){
			if(userPerm.startsWith(DelegatedAccessConstants.NODE_PERM_DENY_TOOL_PREFIX)){
				returnList.add(userPerm.substring(DelegatedAccessConstants.NODE_PERM_DENY_TOOL_PREFIX.length()));
			}
		}
		return returnList;
	}
	
	private List<String> getRestrictedPublicToolsForUser(Set<String> userPerms){
		List<String> returnList = new ArrayList<String>();
		for(String userPerm : userPerms){
			if(userPerm.startsWith(DelegatedAccessConstants.NODE_PERM_DENY_TOOL2_PREFIX)){
				returnList.add(userPerm.substring(DelegatedAccessConstants.NODE_PERM_DENY_TOOL2_PREFIX.length()));
			}
		}
		return returnList;
	}

	private void saveRestrictedAuthToolsForUser(String userId, String nodeId, List<String> toolIds){
		//add new tools:
		for(String newTool : toolIds){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_DENY_TOOL_PREFIX + newTool, false);
		}
	}
	
	private void saveRestrictedPublicToolsForUser(String userId, String nodeId, List<String> toolIds){
		//add new tools:
		for(String newTool : toolIds){
			hierarchyService.assignUserNodePerm(userId, nodeId, DelegatedAccessConstants.NODE_PERM_DENY_TOOL2_PREFIX + newTool, false);
		}
	}
	
	public List<SiteSearchResult> searchUserSites(String search, Map<String, String> advancedOptions, boolean shoppingPeriod, boolean activeShoppingData){
		List<SiteSearchResult> returnList = new ArrayList<SiteSearchResult>();
		List<String> resultSiteIds = new ArrayList<String>();
		if(search == null){
			search = "";
		}
		Collection<SiteSearchResult> siteSubset = null;
		Map<String, String> userSortNameCache = new HashMap<String, String>();
		if(!"".equals(search) || (advancedOptions != null && advancedOptions.size() > 0)){
			siteSubset = searchSites(search, advancedOptions, shoppingPeriod);
			List<String> siteRefs = new ArrayList<String>();
			for(SiteSearchResult siteResult : siteSubset){
				siteRefs.add(siteResult.getSiteReference());
			}
			Map<String,AccessNode> accessList = grantAccessToSites(siteRefs, shoppingPeriod, activeShoppingData);
			for(SiteSearchResult siteResult : siteSubset){
				AccessNode access = accessList.get(siteResult.getSiteReference());
				if(access != null){
					siteResult.setAccess(access.getAccess());
					siteResult.setShoppingPeriodStartDate(access.getStartDate());
					siteResult.setShoppingPeriodEndDate(access.getEndDate());
					siteResult.setRestrictedAuthTools(access.getDeniedAuthTools());
					siteResult.setRestrictedPublicTools(access.getDeniedPublicTools());
					siteResult.setModified(access.getModified());
					siteResult.setModifiedBy(access.getModifiedBy());
					if(!userSortNameCache.containsKey(access.getModifiedBy())){
						User user = sakaiProxy.getUser(access.getModifiedBy());
						String sortName = "";
						if(user != null){
							sortName = user.getSortName();
						}
						userSortNameCache.put(access.getModifiedBy(), sortName);
					}
					siteResult.setModifiedBySortName(userSortNameCache.get(access.getModifiedBy()));
					
					returnList.add(siteResult);
					resultSiteIds.add(siteResult.getSiteId());
				}
			}
		}
		//only look up the terms for display if the search didn't already look it up
		//this is done after the subsite search and the access filtering to limit the number
		//of sites we need to look up
		if (!(advancedOptions != null && advancedOptions.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_TERM)
				&& advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM) != null
				&& !"".equals(advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM).trim()))) {
			String termField = sakaiProxy.getTermField();
			Map<String, Map<String, String>> termProps = dao.searchSitesForProp(new String[]{termField}, resultSiteIds.toArray(new String[resultSiteIds.size()]));
			for(SiteSearchResult result : returnList){
				if(termProps.containsKey(result.getSiteId())){
					result.getSite().setTerm(termProps.get(result.getSiteId()).get(termField));
				}
			}
		}
		if(sakaiProxy.isActiveSiteFlagEnabled()){
			//DAC-40 Highlight Inactive Courses in site search
			//requires the job "InactiveCoursesJob" attached in the jira
			List<String> activeSites = dao.findActiveSites(resultSiteIds.toArray(new String[resultSiteIds.size()]));
			if(activeSites != null){
				for(SiteSearchResult result : returnList){
					if(activeSites.contains(result.getSiteId())){
						result.setActive(true);
					}else{
						result.setActive(false);
					}
				}
			}
		}
		
		return returnList;
	}

	public Collection<SiteSearchResult> searchSites(String search, Map<String, String> advancedOptions, boolean publishedSitesOnly){
		if("".equals(search)){
			search = null;
		}
		Map<String, SiteSearchResult> sites = new HashMap<String, SiteSearchResult>();
		Site searchByIdSite = sakaiProxy.getSiteById(search);
		String termField = sakaiProxy.getTermField();
		String termValue = "";

		//Since we know the hierarchy is site properties, we can use them to speed up our search
		Map<String,String> propsMap = new HashMap<String, String>();
		
		//add term field restriction if it exist:
		if (advancedOptions != null && advancedOptions.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_TERM)
				&& advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM) != null
				&& !"".equals(advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM).trim())) {
			//add term field to propMap for search
			termValue = advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM);
			propsMap.put(termField, termValue);
			//check if we need to remove the searchByIdSite b/c of the term
			if(searchByIdSite != null && searchByIdSite.getProperties() != null
					&& searchByIdSite.getProperties().getProperty(termField) != null
					&& searchByIdSite.getProperties().getProperty(termField).toLowerCase().contains(advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_TERM))){
				//do nothing, we found it
			}else{
				//doesn't exist in this term, remove it
				searchByIdSite = null;
			}	
		}
		
		//add instructor restriction
		Map<String, User> instructorMap = new HashMap<String, User>();
		if (advancedOptions != null && advancedOptions.containsKey(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR)
				&& advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR) != null
				&& !"".equals(advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR).trim())) {
			List<User> searchUsers = sakaiProxy.searchUsers(advancedOptions.get(DelegatedAccessConstants.ADVANCED_SEARCH_INSTRUCTOR));
			//since we added a site by searching for ID, we need to make sure that at least 1 user is a member,
			//otherwise, remove it from the results:
			boolean foundSearchByIdMember = searchByIdSite == null ? true : false;
			for (User user : searchUsers) {
				if(!foundSearchByIdMember && searchByIdSite.getMember(user.getId()) != null){
					foundSearchByIdMember = true;
				}
				instructorMap.put(user.getId(), user);
			}
			
			if(!foundSearchByIdMember && searchByIdSite != null){
				//we didn't find any members for this site in the user search, so remove it:
				searchByIdSite = null;
			}
		}
		// search title, props, or instructors
		List<String[]> siteResults = dao.searchSites(search, propsMap, instructorMap.keySet().toArray(new String[instructorMap.keySet().size()]), publishedSitesOnly);
		if(siteResults != null && siteResults.size() > 0){
			//create an array of the siteIds returned:
			String[] siteIds = new String[siteResults.size()];
			int i = 0;
			for(String[] site : siteResults){
				siteIds[i] = site[0];
				i++;
			}
			Map<String, Map<String, String>> termProps = dao.searchSitesForProp(new String[]{termField}, siteIds);
			for(String[] site : siteResults){
				List<User> instructors = new ArrayList<User>();
				if(site.length == 3){
					//this means the results came back with instructor data:
					instructors.add(instructorMap.get(site[2]));
				}
				sites.put(site[0], new SiteSearchResult(new SiteSerialized(site[0], site[1], termValue), instructors, termField));
			}	
		}
		
		if(searchByIdSite != null && !sites.containsKey(searchByIdSite.getId()) && (!publishedSitesOnly || searchByIdSite.isPublished())){
			sites.put(searchByIdSite.getId(), new SiteSearchResult(searchByIdSite, new ArrayList<User>(), termField));
		}
		
		
		return sites.values();
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


	private List<String> accessNodes = new ArrayList<String>();
	private List<String> shoppingPeriodAdminNodes = new ArrayList<String>();
	private List<String> accessAdminNodes = new ArrayList<String>();
	/**
	 * Creates the model that feeds the tree.
	 * 
	 * @return New instance of tree model.
	 */
	public TreeModel createEntireTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade)
	{
		//this is a list of sub-admin access nodes.  if the user is a super-admin, then this will be null (as a flag that this is a super admin)
		List<String> accessAdminNodeIds = null;
		Set<HierarchyNodeSerialized>  accessAdminNodeSet = null;
		List<String> subAdminsSiteAccessNodes = null;
		Set<HierarchyNodeSerialized>  subAdminSiteAccessNodesSet = null;
		//check if the user is a super admin, if not, then get the accessAdmin nodes connected to the current user
		if(!sakaiProxy.isSuperUser()){
			//only allow the current user to modify permissions for this user on the nodes that
			//has been assigned accessAdmin for currentUser
			accessAdminNodeSet = getAccessAdminNodesForUser(sakaiProxy.getCurrentUserId());
			accessAdminNodeIds = new ArrayList<String>();
			if(accessAdminNodeSet != null){
				for(HierarchyNodeSerialized node : accessAdminNodeSet){
					accessAdminNodeIds.add(node.id);
				}
			}
			String[] subAdminOrderedRealmRoles = sakaiProxy.getSubAdminOrderedRealmRoles();
			if(subAdminOrderedRealmRoles != null && subAdminOrderedRealmRoles.length > 0){
				//we need to restrict sub admin's to only realms&roles they have permissions to set
				//we do this by finding all the subadmin's access nodes and permissions
				subAdminSiteAccessNodesSet = getAccessNodesForUser(sakaiProxy.getCurrentUserId());
				subAdminsSiteAccessNodes = new ArrayList<String>();
				if(subAdminSiteAccessNodesSet != null){
					for(HierarchyNodeSerialized node : subAdminSiteAccessNodesSet){
						subAdminsSiteAccessNodes.add(node.id);
					}
				}
			}
		}
		//these are the nodes that the edit user has permissions in
		Set<HierarchyNodeSerialized> userNodes = getAllNodesForUser(userId);
		if(accessAdminNodeSet != null){
			//make sure we insert the nodes for access admin so the sub-admin
			//can modify these nodes if they want to
			userNodes.addAll(accessAdminNodeSet);
		}
		if(subAdminSiteAccessNodesSet != null){
			//make sure we process these nodes as well
			userNodes.addAll(subAdminSiteAccessNodesSet);
		}
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		List<List> l1 = getTreeListForUser(userId, addDirectChildren, cascade, userNodes);
		//Remove the shopping period nodes:
		if(l1 != null){
			HierarchyNode shoppingRoot = hierarchyService.getRootNode(DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID);
			String shoppingPeriodRootId = "-1";
			if(shoppingRoot != null){
				shoppingPeriodRootId = shoppingRoot.id;
			}
			for (Iterator iterator = l1.iterator(); iterator.hasNext();) {
				List list = (List) iterator.next();
				if(shoppingPeriodRootId.equals(((HierarchyNodeSerialized) list.get(0)).id)){
					iterator.remove();
				}
			}
		}
		
		
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, userId, getEntireToolsList(), addDirectChildren, accessAdminNodeIds, subAdminsSiteAccessNodes);
	}

	public TreeModel createAccessTreeModelForUser(String userId, boolean addDirectChildren, boolean cascade)
	{
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		accessNodes = new ArrayList<String>();
		shoppingPeriodAdminNodes = new ArrayList<String>();
		accessAdminNodes = new ArrayList<String>();

		List<List> l1 = getTreeListForUser(userId, addDirectChildren, cascade, getAccessNodesForUser(userId));
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, userId, getEntireToolsList(), addDirectChildren, null, null);
	}

	public TreeModel getTreeModelForShoppingPeriod(boolean includePerms){
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.
		String userId = "";
		if(includePerms){
			userId = DelegatedAccessConstants.SHOPPING_PERIOD_USER;
		}
		Set<HierarchyNodeSerialized> rootSet = new HashSet<HierarchyNodeSerialized>();
		rootSet.add(new HierarchyNodeSerialized(hierarchyService.getRootNode(DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID)));
		List<List> l1 = getTreeListForUser(userId, false, true, rootSet);
		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, userId, getEntireToolsList(), false, null, null);
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

		return convertToTreeModel(l1, userId, getEntireToolsList(), false, null, null);
	}

	public TreeModel createTreeModelForShoppingPeriod(String userId)
	{
		//Returns a List that represents the tree/node architecture:
		//  List{ List{node, List<children>}, List{node, List<children>}, ...}.

		List<List> l1 = getTreeListForUser(DelegatedAccessConstants.SHOPPING_PERIOD_USER, false, false, getShoppingPeriodAdminNodesForUser(userId));

		//order tree model:
		orderTreeModel(l1);

		return convertToTreeModel(l1, DelegatedAccessConstants.SHOPPING_PERIOD_USER, getEntireToolsList(), false, null, null);
	}

	/**
	 * Takes a list representation of a tree and creates the TreeModel
	 * 
	 * @param map
	 * @param userId
	 * @return
	 */
	private TreeModel convertToTreeModel(List<List> map, String userId, List<ListOptionSerialized> blankRestrictedTools, 
			boolean addDirectChildren, List<String> accessAdminNodeIds, List<String> subAdminsSiteAccessNodes)
	{
		TreeModel model = null;
		if(!map.isEmpty() && map.size() == 1){

			DefaultMutableTreeNode rootNode = add(null, map, userId, blankRestrictedTools, addDirectChildren, accessAdminNodeIds, subAdminsSiteAccessNodes);
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

	private String getShoppingAdminModifiedBy(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED_BY)){
				return perm.substring(DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED_BY.length());
			}
		}
		return null;
	}
	
	private String getModifiedBy(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_MODIFIED_BY)){
				return perm.substring(DelegatedAccessConstants.NODE_PERM_MODIFIED_BY.length());
			}
		}
		return null;
	}
	
	
	private boolean isShoppingPeriodAdmin(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isShoppingPeriodRevokeInstructorEditable(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_EDITABLE)){
				return true;
			}
		}
		return false;
	}
	
	private boolean isShoppingPeriodRevokeInstructorPublicOpt(Set<String> perms){
		for(String perm : perms){
			if(perm.startsWith(DelegatedAccessConstants.NODE_PERM_SHOPPING_REVOKE_INSTRUCTOR_PUBLIC_OPT)){
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
	
	private boolean getIsAccessAdmin(Set<String> perms){
		for(String perm : perms){
			if(perm.equals(DelegatedAccessConstants.NODE_PERM_ACCESS_ADMIN)){
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
	private DefaultMutableTreeNode add(DefaultMutableTreeNode parent, List<List> sub, String userId, List<ListOptionSerialized> blankRestrictedTools, 
			boolean addDirectChildren, List<String> accessAdminNodeIds, List<String> subAdminsSiteAccessNodes)
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
			Date shoppingAdminModified = null;
			String shoppingAdminModifiedBy = null;
			Date modified = null;
			String modifiedBy = null;
			boolean shoppingPeriodRevokeInstructorEditable = false;
			boolean shoppingPeriodRevokeInstructorPublicOpt = false;
			
			//you must copy in order not to pass changes to other nodes
			List<ListOptionSerialized> restrictedAuthTools = copyListOptions(blankRestrictedTools);
			List<ListOptionSerialized> restrictedPublicTools = copyListOptions(blankRestrictedTools);
			boolean accessAdmin = accessAdminNodes.contains(node.id);
			boolean shoppingPeriodAdmin = shoppingPeriodAdminNodes.contains(node.id);
			if(DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId) || accessNodes.contains(node.id) || shoppingPeriodAdminNodes.contains(node.id)){
				Set<String> perms = getPermsForUserNodes(userId, node.id);
				String[] realmRole = getAccessRealmRole(perms);
				realm = realmRole[0];
				role = realmRole[1];
				startDate = getShoppingStartDate(perms);
				endDate = getShoppingEndDate(perms);
				restrictedAuthTools = getRestrictedAuthToolSerializedList(perms, restrictedAuthTools);
				restrictedPublicTools = getRestrictedPublicToolSerializedList(perms, restrictedPublicTools);
				directAccess = getIsDirectAccess(perms);
				shoppingAdminModified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED);
				shoppingAdminModifiedBy = getShoppingAdminModifiedBy(perms);
				modified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_MODIFIED);
				modifiedBy = getModifiedBy(perms);
				shoppingPeriodRevokeInstructorEditable = isShoppingPeriodRevokeInstructorEditable(perms);
				shoppingPeriodRevokeInstructorPublicOpt = isShoppingPeriodRevokeInstructorPublicOpt(perms);
			}
			NodeModel parentNodeModel = null;
			if(parent != null){
				parentNodeModel = ((NodeModel) parent.getUserObject());
			}
			DefaultMutableTreeNode child = new DelegatedAccessMutableTreeNode();
			NodeModel childNodeModel = new NodeModel(node.id, node, directAccess, realm, role, parentNodeModel, 
					restrictedAuthTools, restrictedPublicTools, startDate, endDate, addDirectChildren && !children.isEmpty(), shoppingPeriodAdmin,
					modifiedBy, modified, shoppingAdminModified, shoppingAdminModifiedBy, accessAdmin, shoppingPeriodRevokeInstructorEditable,
					shoppingPeriodRevokeInstructorPublicOpt);
			//this could be an accessAdmin modifying another user, let's check:
			if(accessAdminNodeIds != null){
				//if accessAdminNodeIds isn't null, this means we need to restrict this tree to these nodes by
				//setting the editable flag
				childNodeModel.setEditable(false);

				boolean found = false;
				for(String nodeId : accessAdminNodeIds){
					if(nodeId.equals(node.id)){
						found = true;
						break;
					}
				}
				if(found){
					childNodeModel.setEditable(true);
				}
			}
			if(subAdminsSiteAccessNodes != null && subAdminsSiteAccessNodes.size() > 0){
				//we need to make sure we keep track of the subadmin's permissions if subAdminsSiteAccessNodes is set
				for(String nodeId : subAdminsSiteAccessNodes){
					if(childNodeModel.getNodeId().equals(nodeId)){
						Set<String> perms = getPermsForUserNodes(sakaiProxy.getCurrentUserId(), nodeId);
						String[] realmRole = getAccessRealmRole(perms);
						childNodeModel.setSubAdminSiteAccess(realmRole);
					}
				}

			}
			child.setUserObject(childNodeModel);
			
			if(parent == null){
				//we have the root, set it
				root = child;
			}else{
				parent.add(child);
			}
			if(!children.isEmpty()){
				add(child, children, userId, blankRestrictedTools, addDirectChildren, accessAdminNodeIds, subAdminsSiteAccessNodes);
			}
		}
		return root;
	}


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

	/**
	 * returns the order of the parent id's from highest to lowest in the hierarchy
	 * @return
	 */
	private List<String> getOrderedParentsList(HierarchyNodeSerialized node){
		String directParentId = null;
		if(node.directParentNodeIds != null && node.directParentNodeIds.size() > 0){
			directParentId = node.directParentNodeIds.toArray(new String[node.directParentNodeIds.size()])[0];
			return getOrderedParentsListHelper(getCachedNode(directParentId));
		}else{
			return new ArrayList<String>();
		}
	}
	private List<String> getOrderedParentsListHelper(HierarchyNodeSerialized node){
		if(node.directParentNodeIds == null || node.directParentNodeIds.size() == 0){
			List<String> returnList = new ArrayList<String>();
			returnList.add(node.id);
			return returnList;
		}else{
			List<String> parents = getOrderedParentsListHelper(getCachedNode(node.directParentNodeIds.toArray(new String[node.directParentNodeIds.size()])[0]));
			parents.add(node.id);
			return parents;
		}
	}
	private List<List> getTreeListForUser(String userId, boolean addDirectChildren, boolean cascade, Set<HierarchyNodeSerialized> nodes){
		List<List> l1 = new ArrayList<List>();
		List<List> currentLevel = l1;

		for(HierarchyNodeSerialized node : nodes){
			for(String parentId : getOrderedParentsList(node)){
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
		Element el = nodeCache.get(id);
		HierarchyNodeSerialized node = null;
		if(el == null){
			node = getNode(id);
			try{
				nodeCache.put(new Element(id, node));
			}catch (Exception e) {
				log.error("getCachedNode: " + id, e);
			}
		}else if(el.getObjectValue() instanceof HierarchyNodeSerialized){
			node = (HierarchyNodeSerialized) el.getObjectValue();
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
	 * @param userId
	 * @param blankRestrictedTools
	 * @param onlyAccessNodes
	 * @param accessAdminNodes
	 * @return
	 */
	public boolean addChildrenNodes(Object node, String userId, List<ListOptionSerialized> blankRestrictedTools, boolean onlyAccessNodes, List<String> accessAdminNodes){
		boolean anyAdded = false;
		DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node;
		NodeModel nodeModel = (NodeModel) ((DefaultMutableTreeNode) node).getUserObject();
		if(nodeModel.getNode() != null){
			List<List> childrenNodes = getDirectChildren(nodeModel.getNode());
			Collections.sort(childrenNodes, new NodeListComparator());
			for(List childList : childrenNodes){
				//check if the user can edit this node:
				if(accessAdminNodes != null && !((NodeModel) parentNode.getUserObject()).isNodeEditable()){
					//if accessAdmin nodes isn't null this means that the user is restricted to edit just those nodes and their children
					if(!accessAdminNodes.contains(((HierarchyNodeSerialized) childList.get(0)).id)){
						//since the parent node isn't editable and this node doesn't show up in the editable nodes list
						//we will not add this node
						continue;
					}
				}
				boolean newlyAdded = addChildNodeToTree((HierarchyNodeSerialized) childList.get(0), parentNode, userId, blankRestrictedTools, onlyAccessNodes);
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
	private boolean addChildNodeToTree(HierarchyNodeSerialized childNode, DefaultMutableTreeNode parentNode, String userId, List<ListOptionSerialized> blankRestrictedTools, boolean onlyAccessNodes){
		boolean added = false;
		if(!doesChildExist(childNode.id, parentNode)){
			//just create a blank child since the user should already have all the nodes with information in the db
			String realm = "";
			String role = "";
			boolean selected = false;
			Date startDate = null;
			Date endDate = null;
			//you must copy to not pass changes to other nodes
			List<ListOptionSerialized> restrictedAuthTools = copyListOptions(blankRestrictedTools);
			List<ListOptionSerialized> restrictedPublicTools = copyListOptions(blankRestrictedTools);
			boolean shoppingPeriodAdmin = false;
			boolean directAccess = false;
			Date shoppingAdminModified = null;
			String shoppingAdminModifiedBy = null;
			Date modified = null;
			String modifiedBy = null;
			boolean accessAdmin = false;
			boolean shoppingPeriodRevokeInstructorEditable = false;
			boolean shoppingPeriodRevokeInstructorPublicOpt = false;
			
			DefaultMutableTreeNode child = new DelegatedAccessMutableTreeNode();
			if(DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId)){
				Set<String> perms = getPermsForUserNodes(userId, childNode.id);
				String[] realmRole = getAccessRealmRole(perms);
				realm = realmRole[0];
				role = realmRole[1];
				startDate = getShoppingStartDate(perms);
				endDate = getShoppingEndDate(perms);
				restrictedAuthTools = getRestrictedAuthToolSerializedList(perms, restrictedAuthTools);
				restrictedPublicTools = getRestrictedPublicToolSerializedList(perms, restrictedPublicTools);
				directAccess = getIsDirectAccess(perms);
				shoppingAdminModified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED);
				shoppingAdminModifiedBy = getShoppingAdminModifiedBy(perms);
				modified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_MODIFIED);
				modifiedBy = getModifiedBy(perms);
				accessAdmin = getIsAccessAdmin(perms);
				shoppingPeriodRevokeInstructorEditable = isShoppingPeriodRevokeInstructorEditable(perms);
				shoppingPeriodRevokeInstructorPublicOpt = isShoppingPeriodRevokeInstructorPublicOpt(perms);
			}
			NodeModel node = new NodeModel(childNode.id, childNode, directAccess, realm, role,
					((NodeModel) parentNode.getUserObject()), restrictedAuthTools, restrictedPublicTools, startDate, endDate, 
					false, shoppingPeriodAdmin,
					modifiedBy, modified, shoppingAdminModified, shoppingAdminModifiedBy, accessAdmin, shoppingPeriodRevokeInstructorEditable, shoppingPeriodRevokeInstructorPublicOpt);
			child.setUserObject(node);

			if(!onlyAccessNodes || node.getNodeAccess()){
				parentNode.add(child);
				added = true;
			}
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
		if(node.directParentNodeIds != null && node.directParentNodeIds.size() > 0){
			//grad the last parent in the Set (this is the closest parent)
			List<String> orderedParents = getOrderedParentsList(node);
			parentNodeModel = getNodeModel(orderedParents.get(orderedParents.size() -1), userId);
		}
		Set<String> nodePerms = hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId});
		Set<String> perms = getPermsForUserNodes(userId, node.id);
		String[] realmRole = getAccessRealmRole(perms);
		String realm = realmRole[0];
		String role = realmRole[1];
		Date startDate = getShoppingStartDate(perms);
		Date endDate = getShoppingEndDate(perms);
		List<ListOptionSerialized> restrictedAuthTools = getRestrictedAuthToolSerializedList(perms, getEntireToolsList());
		List<ListOptionSerialized> restrictedPublicTools = getRestrictedPublicToolSerializedList(perms, getEntireToolsList());
		boolean direct = getIsDirectAccess(perms);
		boolean shoppingPeriodAdmin = isShoppingPeriodAdmin(perms);
		boolean accessAdmin = getIsAccessAdmin(perms);
		Date shoppingAdminModified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN_MODIFIED);
		String shoppingAdminModifiedBy = getShoppingAdminModifiedBy(perms);
		Date modified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_MODIFIED);
		String modifiedBy = getModifiedBy(perms);
		boolean shoppingPeriodRevokeInstructorEditable = isShoppingPeriodRevokeInstructorEditable(perms);
		boolean shoppingPeriodRevokeInstructorPublicOpt = isShoppingPeriodRevokeInstructorPublicOpt(perms);
		
		NodeModel nodeModel = new NodeModel(node.id, node, getIsDirectAccess(nodePerms),
				realm, role, parentNodeModel, restrictedAuthTools, restrictedPublicTools, startDate, endDate, false, shoppingPeriodAdmin,
				modifiedBy, modified, shoppingAdminModified, shoppingAdminModifiedBy, accessAdmin, shoppingPeriodRevokeInstructorEditable,
				shoppingPeriodRevokeInstructorPublicOpt);
		return nodeModel;
	}

	/**
	 * {@inheritDoc}
	 */
	public void assignUserNodePerm(String userId, String nodeId, String perm, boolean cascade) {		
		hierarchyService.assignUserNodePerm(userId, nodeId, perm, false);
	}
	
	public void removeNode(String nodeId){
		removeNode(hierarchyService.getNodeById(nodeId));
	}
	
	public void removeNode(HierarchyNode node){
		if(node != null){
			if(node.childNodeIds != null && !node.childNodeIds.isEmpty()){
				//we can delete this, otherwise, delete the children first the children
				for(String childId : node.childNodeIds){		
					removeNode(hierarchyService.getNodeById(childId));
				}
			}
			//all the children nodes have been deleted, now its safe to delete
			hierarchyService.removeNode(node.id);
			Set<String> userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{node.id}, DelegatedAccessConstants.NODE_PERM_SITE_VISIT);
			for(String userId : userIds){
				removeAllUserPermissions(node.id, userId);
			}
			//since the hierarchy service doesn't really delete the nodes,
			//we need to distinguish between deleted nodes
			hierarchyService.setNodeDisabled(node.id, true);
		}
	}
	
	public void deleteEmptyNonSiteNodes(String hierarchyId){
		List<String> emptyNodes = dao.getEmptyNonSiteNodes(hierarchyId);
		//I don't like loops, loops shouldn't happen but never say never
		int loopProtection = 1;
		while(emptyNodes != null && emptyNodes.size() > 0 && loopProtection < 1000000){
			for(String id : emptyNodes){
				removeNode(hierarchyService.getNodeById(id));
			}
			//check again
			emptyNodes = dao.getEmptyNonSiteNodes(hierarchyId);
			loopProtection++;
		}
	}
	public Map<String, String> getRealmRoleDisplay(boolean shopping){
		if(shopping){
			return convertRealmRoleToSingleList(sakaiProxy.getShoppingRealmOptions());
		}else{
			return convertRealmRoleToSingleList(sakaiProxy.getDelegatedAccessRealmOptions());
		}
	}
	
	private Map<String, String> convertRealmRoleToSingleList(Map<String, List<String>> realmMap){
		//First get a list of all roles:
		List<String> allRoles = new ArrayList<String>();
		for(Entry<String, List<String>> entry : realmMap.entrySet()){
			for(String role : entry.getValue()){
				allRoles.add(role);
			}
		}
		//convert this map to a single role dropdown representation:
		Map<String, String> returnMap = new HashMap<String, String>();
		for(Entry<String, List<String>> entry : realmMap.entrySet()){
			String realm = entry.getKey();
			for(String role : entry.getValue()){
				String roleTitle = role;
				if(countNumOfOccurances(allRoles, role) > 1){
					roleTitle += " (" + realm + ")";
				}
				returnMap.put(realm + ":" + role, roleTitle);
			}
		}

		return returnMap;
	}
	
	private int countNumOfOccurances(List<String> list, String str){
		int i = 0;
		for(String check : list){
			if(check.equals(str)){
				i++;
			}
		}
		return i;
	}
	
	public boolean hasShoppingPeriodAdminNodes(String userId){
		if(userId == null || "".equals(userId)){
			return false;
		}
		Set<HierarchyNode> shoppingAdminNodes = hierarchyService.getNodesForUserPerm(userId, DelegatedAccessConstants.NODE_PERM_SHOPPING_ADMIN); 
		return shoppingAdminNodes != null && shoppingAdminNodes.size() > 0;
	}
	
	public boolean hasDelegatedAccessNodes(String userId){
		if(userId == null || "".equals(userId)){
			return false;
		}
		Set<HierarchyNode> delegatedAccessNodes = hierarchyService.getNodesForUserPerm(userId, DelegatedAccessConstants.NODE_PERM_SITE_VISIT); 
		return delegatedAccessNodes != null && delegatedAccessNodes.size() > 0;
	}
	
	public boolean hasAccessAdminNodes(String userId){
		if(userId == null || "".equals(userId)){
			return false;
		}
		Set<HierarchyNode> accessAdminNodes = hierarchyService.getNodesForUserPerm(userId, DelegatedAccessConstants.NODE_PERM_ACCESS_ADMIN); 
		return accessAdminNodes != null && accessAdminNodes.size() > 0;
	}
	
	public Map<String, List<String>> getNodesBySiteRef(String[] siteRefs, String hierarchyId){
		return dao.getNodesBySiteRef(siteRefs, hierarchyId);
	}
	
	public void clearNodeCache(){
		nodeCache.removeAll();
	}
	
	public String[] getCurrentUsersAccessToSite(String siteRef){
		String currentUserId = sakaiProxy.getCurrentUserId();
		if(currentUserId == null || "".equals(currentUserId)){
			return null;
		}

		//check the session first:
		if(sakaiProxy.getCurrentSession().getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP) != null 
				&& ((Map) sakaiProxy.getCurrentSession().getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP)).containsKey(siteRef)){
			return (String[]) ((Map) sakaiProxy.getCurrentSession().getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP)).get(siteRef);
		}else{
			List<String> siteRefs = new ArrayList<String>();
			siteRefs.add(siteRef);
			Map<String, AccessNode> accessNodes = grantAccessToSites(siteRefs, false, false);
			if(accessNodes != null && accessNodes.containsKey(siteRef) && accessNodes.get(siteRef) != null){
				return accessNodes.get(siteRef).getAccess();
			}else{
				return null;
			}
		}
	}
	
	private Map<String, AccessNode> grantAccessToSites(List<String> siteRefs, boolean shoppingPeriod, boolean activeShoppingData){
		String userId = sakaiProxy.getCurrentUserId();
		if(shoppingPeriod){
			userId = DelegatedAccessConstants.SHOPPING_PERIOD_USER;
		}
		return grantAccessToSites(siteRefs, shoppingPeriod, activeShoppingData, userId);
	}
	
	private Map<String, AccessNode> grantAccessToSites(List<String> siteRefs, boolean shoppingPeriod, boolean activeShoppingData, String userId){
		Map<String, AccessNode> returnNodes = new HashMap<String, AccessNode>();
		
		if(!shoppingPeriod && (userId == null || "".equals(userId))){
			//skip since the user id is empty
			return returnNodes;
		}

		//we don't want to use the session if it's the shopping period user or we are looking up info for a different user
		//otherwise, use the session
		boolean useSession = userId != null && userId.equals(sakaiProxy.getCurrentUserId());
		Session session = null;;
		if(useSession){
			session = sakaiProxy.getCurrentSession();
		}
		Map<String, String[]> deniedAuthToolsMap = new HashMap<String, String[]>();
		if(useSession){
			//only worry about the session for non shopping period queries
			Object sessionDeniedToolsMap = session.getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS);
			if(sessionDeniedToolsMap != null){
				deniedAuthToolsMap = (Map<String, String[]>) sessionDeniedToolsMap;
			}
		}
		
		Map<String, String[]> deniedPublicToolsMap = new HashMap<String, String[]>();
		if(useSession){
			//only worry about the session for non shopping period queries
			Object sessionDeniedTools2Map = session.getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS2);
			if(sessionDeniedTools2Map != null){
				deniedPublicToolsMap = (Map<String, String[]>) sessionDeniedTools2Map;
			}
		}

		Map<String, String[]> accessMap = new HashMap<String, String[]>();
		if(useSession){
			//only worry about the session for non shopping period queries
			Object sessionaccessMap = session.getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP);
			if(sessionaccessMap != null){
				accessMap = (Map<String, String[]>) sessionaccessMap;
			}
		}

		for (Iterator iterator = siteRefs.iterator(); iterator.hasNext();) {
			String siteRef = (String) iterator.next();
			if(!shoppingPeriod && accessMap.containsKey(siteRef) && accessMap.get(siteRef) == null){
				//we already know this result is null, so return null
				returnNodes.put(siteRef, null);
				iterator.remove();
			}else{
				//set default to no access and override it if the user does have access
				//this is so we don't have to keep looking up their access for the same site:
				deniedAuthToolsMap.put(siteRef, null);
				deniedPublicToolsMap.put(siteRef, null);
				accessMap.put(siteRef, null);
			}
		}
		//Set user Id and hierarchy id
		String hierarchyId = DelegatedAccessConstants.HIERARCHY_ID;
		if(shoppingPeriod){
			userId = DelegatedAccessConstants.SHOPPING_PERIOD_USER;
			if(activeShoppingData){
				hierarchyId = DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID;
			}
		}

		//this is a simple flag set in the delegated access login observer which
		//determines if there is a need to lookup access information for this user.
		//if it's not set, then don't worry about looking up anything
		Object dAMapFlag = null;
		if(useSession){
			dAMapFlag = session.getAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DELEGATED_ACCESS_FLAG);
		}
		if(dAMapFlag != null || !useSession){
			//get list of all site ref nodes:
			Map<String, List<String>> siteRefToNodeMap = getNodesBySiteRef(siteRefs.toArray(new String[siteRefs.size()]), hierarchyId);
			if(siteRefToNodeMap != null){
				Set<String> nodeIds = new HashSet<String>();
				for(List<String> nodeIdsList : siteRefToNodeMap.values()){
					nodeIds.addAll(nodeIdsList);
				}
				Map<String, HierarchyNode> siteNodes = hierarchyService.getNodesByIds(nodeIds.toArray(new String[nodeIds.size()]));
				//create a set of parent ID's and lookup the user's permissions for this sub-set:
				Set<String> subSetNodeIds = new HashSet<String>();
				if(siteNodes != null){
					for(HierarchyNode node : siteNodes.values()){
						subSetNodeIds.add(node.id);
						if(node.parentNodeIds != null){
							for(String pId : node.parentNodeIds){
								subSetNodeIds.add(pId);
							}
						}
					}
				}
				//find the node for the site
				Map<String, Set<String>> userNodesAndPerms = dao.getNodesAndPermsForUser(userId, subSetNodeIds.toArray(new String[subSetNodeIds.size()]));
				Map<String, String> memberRoles = new HashMap<String, String>();
				if(!shoppingPeriod){
					memberRoles = sakaiProxy.isUserMember(userId, siteRefs);
				}
				if(userNodesAndPerms != null){
					for(String siteRef : siteRefs){
						if(siteRefToNodeMap != null && siteRefToNodeMap.containsKey(siteRef) && siteRefToNodeMap.get(siteRef) != null && siteRefToNodeMap.get(siteRef).size() > 0){
							//find the first access node for this user, if none found, then that means they don't have access
							String nodeId = siteRefToNodeMap.get(siteRef).get(0);
							while(nodeId != null && !"".equals(nodeId)){
								Set<String> perms = userNodesAndPerms.get(nodeId);
								if(perms != null && getIsDirectAccess(perms)){
									//check first that the user's isn't a member of the site, if so, return null:
									//don't waste time checking until we get to this level (for bulk searching speed)
									if(!shoppingPeriod && memberRoles.get(siteRef) != null){
										returnNodes.put(siteRef, null);
									}else{
										if(shoppingPeriod && activeShoppingData){
											//do substring(6) b/c we need site ID and what is stored is a ref: /site/1231231
											String siteId = siteRef.substring(6);
											if(!isShoppingAvailable(perms, siteId)){
												//check that shopping period is still available unless activeShoppingData is false
												break;
											}
										}
										//Access Map:
										String[] access = getAccessRealmRole(perms);
										if (access == null || access.length != 2
												|| access[0] == null
												|| access[1] == null
												|| "".equals(access[0])
												|| "".equals(access[1])
												|| "null".equals(access[0])
												|| "null".equals(access[1])) {
											access = new String[]{"", ""};
										}

										accessMap.put(siteRef, access);

										//Denied Auth Tools List
										List<String> deniedAuthTools = getRestrictedAuthToolsForUser(perms);
										String[] deniedAuthToolsArr = (String[]) deniedAuthTools.toArray(new String[deniedAuthTools.size()]);
										if(deniedAuthToolsArr != null){
											deniedAuthToolsMap.put(siteRef, deniedAuthToolsArr);
										}else{
											deniedAuthToolsMap.put(siteRef, new String[0]);
										}
										
										//Denied Public List
										List<String> deniedPublicTools = getRestrictedPublicToolsForUser(perms);
										String[] deniedPublicToolsArr = (String[]) deniedPublicTools.toArray(new String[deniedPublicTools.size()]);
										if(deniedPublicToolsArr != null){
											deniedPublicToolsMap.put(siteRef, deniedPublicToolsArr);
										}else{
											deniedPublicToolsMap.put(siteRef, new String[0]);
										}

										Date startDate = getShoppingStartDate(perms);
										Date endDate = getShoppingEndDate(perms);
										Date modified = getPermDate(perms, DelegatedAccessConstants.NODE_PERM_MODIFIED);
										String modifiedBy = getModifiedBy(perms);

										//set returnNode
										AccessNode returnNode = new AccessNode(userId, siteRef, access, deniedAuthToolsArr, deniedPublicToolsArr, startDate, endDate, modified, modifiedBy);
										returnNodes.put(siteRef, returnNode);
									}
									//break out of loop
									nodeId = null;
									break;
								}else{
									Set<String> parentIds = null;
									if(siteNodes != null && siteNodes.containsKey(nodeId)){
										//we've already spent the time looking this up in bulk
										parentIds = siteNodes.get(nodeId).parentNodeIds;
									}else{
										parentIds = getCachedNode(nodeId).parentNodeIds;
									}
									nodeId = getFirstAccessParent(parentIds, userNodesAndPerms);
								}
							}
						}
					}
				}
			}
		}
		if(useSession){
			//we want to set the session map no matter what so we don't have to look it up again:
			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS, deniedAuthToolsMap);
			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_DENIED_TOOLS2, deniedPublicToolsMap);
			session.setAttribute(DelegatedAccessConstants.SESSION_ATTRIBUTE_ACCESS_MAP, accessMap);
			//update restrictedToolsCache
			try{
				restrictedAuthToolsCache.put(new Element(userId, deniedAuthToolsMap));
			}catch (Exception e) {
				log.error("grantAccessToSite: " + userId, e);
			}
			try{
				restrictedPublicToolsCache.put(new Element(userId, deniedPublicToolsMap));
			}catch (Exception e) {
				log.error("grantAccessToSite: " + userId, e);
			}
		}

		return returnNodes;
	}
	
	private String getFirstAccessParent(Set<String> parentIds, Map<String, Set<String>> userNodesAndPerms){
		String accessParent = null;
		
		List<String> accessParents = new ArrayList<String>();
		for(String parent: parentIds){
			for(Entry<String, Set<String>> entry : userNodesAndPerms.entrySet()){
				if(parent.equals(entry.getKey()) && getIsDirectAccess(entry.getValue())){
					accessParents.add(parent);
				}
			}
		}
		if(accessParents.size() == 1){
			//there is only one parent which is an access node, so lets set it to that:
			accessParent = accessParents.get(0);
		}else if(accessParents.size() > 1){
			//there are more than 1 parents with access, we need to find out which one is the closest to this node
			
			//reverse order since most of the time (not guaranteed) parents are ordered from top to bottom,
			//so by starting at the bottom we have a better chance of using less cylces
			Collections.reverse(accessParents);

			for(String parent: accessParents){
				HierarchyNodeSerialized pNode = getCachedNode(parent);
				boolean foundAccessChild = false;
				for(String child : pNode.childNodeIds){
					for(String childCheck: accessParents){
						if(childCheck.equals(child)){
							//there is a parent with access permissions at a lower level,
							//skip this parent and go to the next one
							foundAccessChild = true;
							break;
						}
					}
					if(foundAccessChild){
						break;
					}
				}
				if(!foundAccessChild){
					accessParent = parent;
					break;
				}
			}
		}
		
		return accessParent;
	}
	
	private boolean isShoppingAvailable(Set<String> perms, String siteId){
		Date startDate = getShoppingStartDate(perms);
		Date endDate = getShoppingEndDate(perms);
		String[] nodeAccessRealmRole = getAccessRealmRole(perms);
		List<String> restrictedAuthTools = getRestrictedAuthToolsForUser(perms);
		String[] restrictedAuthToolsArr = null;
		if(restrictedAuthTools != null){
			restrictedAuthToolsArr = restrictedAuthTools.toArray(new String[restrictedAuthTools.size()]); 
		}
		List<String> restrictedPublicTools = getRestrictedPublicToolsForUser(perms);
		String[] restrictedPublicToolsArr = null;
		if(restrictedPublicTools != null){
			restrictedPublicToolsArr = restrictedPublicTools.toArray(new String[restrictedPublicTools.size()]);
		}
		
		return isShoppingPeriodOpenForSite(startDate, endDate, nodeAccessRealmRole, restrictedAuthToolsArr, restrictedPublicToolsArr);
	}
	
	public boolean isShoppingPeriodOpenForSite(Date startDate, Date endDate, String[] nodeAccessRealmRole, String[] restrictedAuthTools, String[] restrictedPublicTools){
		Date now = new Date();
		boolean isOpen = false;
		if(startDate != null && endDate != null){
			isOpen = startDate.before(now) && endDate.after(now);
		}else if(startDate != null){
			isOpen = startDate.before(now);
		}else if(endDate != null){
			isOpen = endDate.after(now);
		}
		if(nodeAccessRealmRole != null && nodeAccessRealmRole.length == 2 && !"".equals(nodeAccessRealmRole[0]) && !"".equals(nodeAccessRealmRole[1])
				&& !"null".equals(nodeAccessRealmRole[0]) && !"null".equals(nodeAccessRealmRole[1])){
			isOpen = isOpen && true;
		}else{
			isOpen = false;
		}
		if((restrictedAuthTools == null || restrictedAuthTools.length == 0) && (restrictedPublicTools == null || restrictedPublicTools.length == 0)){
			isOpen = false;
		}else{
			isOpen = isOpen && true;
		}
		
		return isOpen;
	}
	
	
	@Override
	public void syncMyworkspaceToolForUser(String userId) {
		if(sakaiProxy.getSyncMyworkspaceTool()){
			Site workspace = sakaiProxy.getSiteById("~" + userId);
			if(workspace != null){
				boolean hasAnyAccess = false;
				//check if user has any access at all to determine if we need to add or remove the tool
				hasAnyAccess = hasDelegatedAccessNodes(userId);
				if(!hasAnyAccess)
					hasAnyAccess = hasShoppingPeriodAdminNodes(userId);
				if(!hasAnyAccess)
					hasAnyAccess = hasAccessAdminNodes(userId);
				
				ToolConfiguration tool = workspace.getToolForCommonId("sakai.delegatedaccess");
				if(hasAnyAccess && tool == null){
					//user has access but doesn't have the DA tool, we need to add it
					SitePage page = workspace.addPage();
					page.addTool("sakai.delegatedaccess");
					sakaiProxy.saveSite(workspace);
				}else if(!hasAnyAccess && tool != null){
					//user doesn't have any access in DA but their MyWorkspace still has the DA tool, remove it:
					workspace.removePage(tool.getContainingPage());
					sakaiProxy.saveSite(workspace);
				}
			}
		}
	}
	
	public Map<String, AccessNode> getUserAccessForSite(String siteRef){
		Map<String, AccessNode> returnMap = new HashMap<String, AccessNode>();
		Map<String, List<String>> siteNodeMap = getNodesBySiteRef(new String[]{siteRef}, DelegatedAccessConstants.HIERARCHY_ID);
		if(siteNodeMap != null && siteNodeMap.containsKey(siteRef) && siteNodeMap.get(siteRef) != null && siteNodeMap.get(siteRef).size() > 0){
			//there should only be 1 node with a siteRef like this, so just grab the first
			HierarchyNodeSerialized node = getCachedNode(siteNodeMap.get(siteRef).get(0));
			Set<String> nodeIds = node.parentNodeIds;
			nodeIds.add(node.id);
			Map<String, Map<String, Set<String>>> userPerms = hierarchyService.getUsersAndPermsForNodes(nodeIds.toArray(new String[nodeIds.size()]));
			List<String> siteRefs = new ArrayList<String>();
			siteRefs.add(siteRef);
			Set<String> usersWithAccess = new HashSet<String>();
			//map returns nodeId -> {userId -> {perms}}, so lets just grab all the user Ids and look up their
			//permissions later
			for(Map<String, Set<String>> nodeUsers : userPerms.values()){
				for(String userId : nodeUsers.keySet()){
					if(!DelegatedAccessConstants.SHOPPING_PERIOD_USER.equals(userId))
						usersWithAccess.add(userId);
				}
			}
			for(String userId : usersWithAccess){
				Map<String, AccessNode> userAccess = grantAccessToSites(siteRefs, false, false, userId);
				if(userAccess != null && userAccess.containsKey(siteRef) && userAccess.get(siteRef) != null){
					returnMap.put(userId, userAccess.get(siteRef));
				}
			}
		}
		return returnMap;
	}
}
