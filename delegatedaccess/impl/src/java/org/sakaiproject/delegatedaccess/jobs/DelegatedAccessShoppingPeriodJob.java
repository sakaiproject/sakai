package org.sakaiproject.delegatedaccess.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.StatefulJob;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
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
public class DelegatedAccessShoppingPeriodJob implements StatefulJob{
	private static final Logger log = Logger.getLogger(DelegatedAccessShoppingPeriodJob.class);
	@Getter @Setter
	private ProjectLogic projectLogic;
	@Getter @Setter	
	private SakaiProxy sakaiProxy;
	@Getter @Setter
	private HierarchyService hierarchyService;
	@Getter @Setter
	private DelegatedAccessDao dao;
	//old node Id -> new node Id
	private Map<String,String> migratedHierarchyIds;

	private static boolean semaphore = false;
	
	private Map<String, String> errors = new HashMap<String, String>();
	
	public void init() { }

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//this will stop the job if there is already another instance running
		if(semaphore){
			log.warn("Stopping job since this job is already running");
			return;
		}
		semaphore = true;
		
		try{
			errors = new HashMap<String, String>();
			log.info("DelegatedAccessShoppingPeriodJob started");
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
				//copy old node's permissions for the shopping period user
				for(String perm : hierarchyService.getPermsForUserNodes(DelegatedAccessConstants.SHOPPING_PERIOD_USER, new String[]{delegatedRootNode.id})){
					hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, rootNode.id, perm, false);
				}
				migratedHierarchyIds.put(delegatedRootNode.id, rootNode.id);

				treeModelShoppingPeriodTraverser((DefaultMutableTreeNode) treeModel.getRoot());
			}

			sakaiProxy.popSecurityAdvisor(advisor);		
			log.info("DelegatedAccessShoppingPeriodJob finished in " + (System.currentTimeMillis() - startTime) + " ms");
			if(errors.size() > 0){
				String warning = "The following sites had errors: \n\n";
				for(Entry entry : errors.entrySet()){
					warning += entry.getKey() + ": " + entry.getValue() + "\n";
				}
				log.warn(warning);
				sakaiProxy.sendEmail("DelegatedAccessShoppingPeriodJob error", warning);
			}
		}catch (Exception e) {
			log.error(e);
			sakaiProxy.sendEmail("DelegatedAccessShoppingPeriodJob error", e.getMessage());
		}finally{
			semaphore = false;
		}
	}

	private void treeModelShoppingPeriodTraverser(DefaultMutableTreeNode node){
		if(node != null){
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.getNode().title.startsWith("/site/")){
				try{
					shoppingPeriodRoleHelper(nodeModel);
				}catch(Exception e){
					log.error(e);
					errors.put(nodeModel.getNode().title, e.getMessage());
				}
			}
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

		//do substring(6) b/c we need site ID and what is stored is a ref: /site/1231231
		String siteId = node.getNode().title.substring(6);
		
		if(addAuth && (".anon".equals(auth) || ".auth".equals(auth)) && checkTerm(node.getNodeTerms(), siteId)){
			//update the restricted tools list, otherwise it will be cleared:			
			//set the restricted tools list to a non empty string, otherwise, the site property won't be saved
			//when the string is empty (no tools allowed to view).
			restrictedToolsList = ";";
			for(String tool : node.getNodeRestrictedTools()){
				if(!"".equals(restrictedToolsList)){
					restrictedToolsList += ";";
				}
				if("Home".equals(tool)){
					String homeToolsVal = "";
					String[] homeTools = sakaiProxy.getHomeTools();
					for(String toolId : homeTools){
						if(!"".equals(homeToolsVal)){
							homeToolsVal += ";";
						}
						homeToolsVal += toolId;
					}
					restrictedToolsList += homeToolsVal;
				}else{
					restrictedToolsList += tool;
				}
			}

			removeAnonAndAuthRoles(node.getNode().title);
			//add either .anon or .auth role:
			copyNewRole(node.getNode().title, nodeAccessRealmRole[0], nodeAccessRealmRole[1], auth);

			//add node to shopping tree:
			checkAndAddNode(node);
		} else{
			//remove .anon and .auth roles
			removeAnonAndAuthRoles(node.getNode().title);
		}

		if(restrictedToolsList == null || "".equals(restrictedToolsList) || ";".equals(restrictedToolsList)){
			//no need for property if null or blank, just remove it in case it existed before
			dao.removeSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_RESTRICTED_TOOLS);
		}else{
			String sitePropRestrictedTools = dao.getSiteProperty(DelegatedAccessConstants.SITE_PROP_RESTRICTED_TOOLS, siteId);
			if(sitePropRestrictedTools != null){
				dao.updateSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_RESTRICTED_TOOLS, restrictedToolsList);
			}else{
				dao.addSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_RESTRICTED_TOOLS, restrictedToolsList);
			}
		}
	}
	
	private boolean checkTerm(String[] terms, String site){
		boolean returnVal = true;
		if(terms != null && terms.length > 0){
			String siteTerm = dao.getSiteProperty(sakaiProxy.getTermField(), site);
			if(siteTerm != null){
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
		}
		return returnVal;
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
			//copy old node's permissions for the shopping period user
			for(String perm : hierarchyService.getPermsForUserNodes(DelegatedAccessConstants.SHOPPING_PERIOD_USER, new String[]{node.getNodeId()})){
				hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, newNode.id, perm, false);
			}
			migratedHierarchyIds.put(node.getNodeId(), newNode.id);
		}
	}
}
