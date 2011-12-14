package org.sakaiproject.delegatedaccess.jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.site.api.Site;

/**
 * This is the job that will populate the shopping period access tree.  It should be ran every morning (sometime after midnight).  
 * This is used to open and close the shopping period for sites based on their open and close dates.
 * 
 * @author Bryan Holladay
 *
 */
public class DelegatedAccessShoppingPeriodJob implements Job{
	private static final Logger log = Logger.getLogger(DelegatedAccessShoppingPeriodJob.class);
	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter	
	private SakaiProxy sakaiProxy;
	@Getter @Setter
	private HierarchyService hierarchyService;
	//old node Id -> new node Id
	private Map<String,String> migratedHierarchyIds;

	public void init() { }

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		SecurityAdvisor advisor = sakaiProxy.addSiteUpdateSecurityAdvisor();
		migratedHierarchyIds = new HashMap<String, String>();

		TreeModel treeModel = projectLogic.getEntireTreePlusUserPerms(DelegatedAccessConstants.SHOPPING_PERIOD_USER);
		if (treeModel != null && treeModel.getRoot() != null) {
			try{
				//delete old shopping period hierarchy:
				hierarchyService.destroyHierarchy(DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID);
			}catch(Exception e){
				//doesn't exist, don't worry
			}
			//create new hierarchy:
			HierarchyNode delegatedRootNode = hierarchyService.getRootNode(DelegatedAccessConstants.HIERARCHY_ID);
			HierarchyNode rootNode = hierarchyService.createHierarchy(DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID);
			hierarchyService.saveNodeMetaData(rootNode.id, delegatedRootNode.title, delegatedRootNode.description, null);
			migratedHierarchyIds.put(delegatedRootNode.id, rootNode.id);

			treeModelShoppingPeriodTraverser((DefaultMutableTreeNode) treeModel.getRoot());
		}

		sakaiProxy.popSecurityAdvisor(advisor);		
		log.info("DelegatedAccessShoppingPeriodJob finished in " + (System.currentTimeMillis() - startTime) + " ms");
	}

	private void treeModelShoppingPeriodTraverser(DefaultMutableTreeNode node){
		if(node != null){
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.getNode().description.startsWith("/site/")){ 
				shoppingPeriodRoleHelper(nodeModel);			}
			for(int i = 0; i < node.getChildCount(); i++){
				treeModelShoppingPeriodTraverser((DefaultMutableTreeNode) node.getChildAt(i));
			}
		}
	}

	private void shoppingPeriodRoleHelper(NodeModel node){
		Date startDate = null;
		Date endDate = null;
		Date now = new Date();

		String auth = node.getNodeShoppingPeriodAuth();
		startDate = node.getNodeShoppingPeriodStartDate();
		endDate = node.getNodeShoppingPeriodEndDate();

		//we need to grab the updated date of the modification, even if it's inherited
		//Date updated = node.getNodeUpdatedDate();
		//we are only interested in this node's process date, not the inheritance
		//Date processed = node.getProcessedDate();
		//		if(!node.isDirectAccess()){
		//			//if the node isn't a direct access node, we need to instantiate the date information
		//			processed = projectLogic.getShoppingPeriodProccessedDate(DelegatedAccessConstants.SHOPPING_PERIOD_USER, node.getNodeId());
		//		}
		//		if (processed == null){
		//			processed = new Date(0L); // will always be older than other dates
		//		}

		boolean addAuth = false;

		if(startDate != null && endDate != null){
			addAuth = startDate.before(now) && endDate.after(now);
		}else if(startDate != null){
			addAuth = startDate.before(now);
		}else if(endDate != null){
			addAuth = endDate.after(now);
		}
		String[] nodeAccessRealmRole = node.getNodeAccessRealmRole();
		if(nodeAccessRealmRole != null && nodeAccessRealmRole.length == 2 && !"".equals(nodeAccessRealmRole[0]) && !"".equals(nodeAccessRealmRole[1])
				&& !"null".equals(nodeAccessRealmRole[0]) && !"null".equals(nodeAccessRealmRole[1])){
			addAuth = addAuth && true;
		}else{
			addAuth = false;
		}
		if(auth == null || "".equals(auth)){
			addAuth = false;
		}else{
			addAuth = addAuth && true;
		}

		String restrictedToolsList = "";


		if(addAuth && (".anon".equals(auth) || ".auth".equals(auth))){
			//if (updated != null && updated.after(processed)){
			//update the restricted tools list, otherwise it will be cleared:
			
			//set the restricted tools list to a non empty string, otherwise, the site property won't be saved
			//when the string is empty (no tools allowed to view).
			restrictedToolsList = ";";
			for(String tool : node.getNodeRestrictedTools()){
				if(!"".equals(restrictedToolsList)){
					restrictedToolsList += ";";
				}
				restrictedToolsList += tool;
			}

			removeAnonAndAuthRoles(node.getNode().description);
			//add either .anon or .auth role:
			copyNewRole(node.getNode().description, nodeAccessRealmRole[0], nodeAccessRealmRole[1], auth);

			//add node to shopping tree:
			checkAndAddNode(node);

			// update the processed date
			//	processed = now;
			//	projectLogic.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, node.getNodeId(), DelegatedAccessConstants.NODE_PERM_SHOPPING_PROCESSED_DATE + processed.getTime(), false);

			//}
		} else{
			//remove .anon and .auth roles
			removeAnonAndAuthRoles(node.getNode().description);
		}

		Site site = sakaiProxy.getSiteByRef(node.getNode().description);
		if (site != null){
			site.getPropertiesEdit().addProperty(DelegatedAccessConstants.SITE_PROP_HIERARCHY_NODE_ID, node.getNode().id);
			site.getPropertiesEdit().addProperty(DelegatedAccessConstants.SITE_PROP_RESTRICTED_TOOLS, restrictedToolsList);
			sakaiProxy.saveSite(site);
		}
	}

	private void removeAnonAndAuthRoles(String siteRef){
		Site site = sakaiProxy.getSiteByRef(siteRef);
		AuthzGroup ag = sakaiProxy.getAuthzGroup(siteRef);
		log.debug("Removing .auth and.anon roles for " + siteRef);
		for (Role role: site.getRoles()){
			if (role.getId().equals(".auth") || role.getId().equals(".anon")){
				sakaiProxy.removeRoleFromAuthzGroup(ag, role);
			}
		}
	}

	private void copyNewRole(String siteRef, String copyRealm, String copyRole, String newRole){
		log.debug("Copying " + copyRole + " to " + newRole + " for " + siteRef);
		sakaiProxy.copyNewRole(siteRef, copyRealm, copyRole, newRole);
	}

	private void checkAndAddNode(NodeModel node){
		NodeModel parent = node.getParentNode();
		if(parent != null){
			checkAndAddNode(parent);
		}

		if(!migratedHierarchyIds.containsKey(node.getNodeId()) && parent != null && migratedHierarchyIds.containsKey(parent.getNodeId())){
			//if this parent/child relationship hasn't been created, create it
			HierarchyNode newNode = hierarchyService.addNode(DelegatedAccessConstants.SHOPPING_PERIOD_HIERARCHY_ID, migratedHierarchyIds.get(parent.getNodeId()));
			hierarchyService.saveNodeMetaData(newNode.id, node.getNode().title, node.getNode().description, null);
			hierarchyService.addChildRelation(migratedHierarchyIds.get(parent.getNodeId()), newNode.id);			
			migratedHierarchyIds.put(node.getNodeId(), newNode.id);
		}
	}
}
