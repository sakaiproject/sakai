package org.sakaiproject.delegatedaccess.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
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
					dao.cleanupOrphanedPermissions();
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
			log.error(e.getMessage(), e);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			sakaiProxy.sendEmail("DelegatedAccessShoppingPeriodJob error", sw.toString());
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
					log.error(e.getMessage(), e);
					StringWriter sw = new StringWriter();
					e.printStackTrace(new PrintWriter(sw));
					errors.put(nodeModel.getNode().title, sw.toString());
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

		startDate = node.getNodeShoppingPeriodStartDate();
		endDate = node.getNodeShoppingPeriodEndDate();
		String[] nodeAccessRealmRole = node.getNodeAccessRealmRole();
		String[] restrictedAuthcTools = node.getNodeRestrictedAuthTools();
		String[] restrictedPublicTools = node.getNodeRestrictedPublicTools();
		//do substring(6) b/c we need site ID and what is stored is a ref: /site/1231231
		String siteId = node.getNode().title.substring(6);
		boolean addAuth = projectLogic.isShoppingPeriodOpenForSite(startDate, endDate, nodeAccessRealmRole, restrictedAuthcTools, restrictedPublicTools);
		String restrictedAuthToolsList = "";
		String restrictedPublicToolsList = "";
		if(addAuth){
			//update the restricted tools list, otherwise it will be cleared:			
			//set the restricted tools list to a non empty string, otherwise, the site property won't be saved
			//when the string is empty (no tools allowed to view).
			restrictedAuthToolsList = ";";
			for(String tool : node.getNodeRestrictedAuthTools()){
				if("Home".equals(tool)){
					String homeToolsVal = "";
					String[] homeTools = sakaiProxy.getHomeTools();
					for(String toolId : homeTools){
						if(!"".equals(homeToolsVal)){
							homeToolsVal += ";";
						}
						homeToolsVal += toolId;
					}
					restrictedAuthToolsList += homeToolsVal;
				}else{
					restrictedAuthToolsList += tool;
				}
				restrictedAuthToolsList += ";";
			}
			
			restrictedPublicToolsList = ";";
			for(String tool : node.getNodeRestrictedPublicTools()){
				if(!"".equals(restrictedPublicToolsList)){
					restrictedPublicToolsList += ";";
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
					restrictedPublicToolsList += homeToolsVal;
				}else{
					restrictedPublicToolsList += tool;
				}
			}

			removeAnonAndAuthRoles(node.getNode().title);
			//add either .anon or .auth role:
			boolean auth = restrictedAuthcTools != null && restrictedAuthcTools.length > 0;
			boolean anon = restrictedPublicTools != null && restrictedPublicTools.length > 0;
			copyNewRole(node.getNode().title, nodeAccessRealmRole[0], nodeAccessRealmRole[1], auth, anon);

			//add node to shopping tree:
			checkAndAddNode(node);
		} else{
			//remove .anon and .auth roles
			removeAnonAndAuthRoles(node.getNode().title);
		}

		if(restrictedAuthToolsList == null || "".equals(restrictedAuthToolsList) || ";".equals(restrictedAuthToolsList)){
			//no need for property if null or blank, just remove it in case it existed before
			dao.removeSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_AUTH_TOOLS);
		}else{
			String sitePropRestrictedTools = dao.getSiteProperty(DelegatedAccessConstants.SITE_PROP_AUTH_TOOLS, siteId);
			if(sitePropRestrictedTools != null){
				dao.updateSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_AUTH_TOOLS, restrictedAuthToolsList);
			}else{
				dao.addSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_AUTH_TOOLS, restrictedAuthToolsList);
			}
		}
		
		if(restrictedPublicToolsList == null || "".equals(restrictedPublicToolsList) || ";".equals(restrictedPublicToolsList)){
			//no need for property if null or blank, just remove it in case it existed before
			dao.removeSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_PUBLIC_TOOLS);
		}else{
			String sitePropRestrictedTools = dao.getSiteProperty(DelegatedAccessConstants.SITE_PROP_PUBLIC_TOOLS, siteId);
			if(sitePropRestrictedTools != null){
				dao.updateSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_PUBLIC_TOOLS, restrictedPublicToolsList);
			}else{
				dao.addSiteProperty(siteId, DelegatedAccessConstants.SITE_PROP_PUBLIC_TOOLS, restrictedPublicToolsList);
			}
		}
	}
	
	
	private void removeAnonAndAuthRoles(String siteRef){
		AuthzGroup ag = sakaiProxy.getAuthzGroup(siteRef);
		if(ag != null){
			log.debug("Removing .auth and.anon roles for " + siteRef);
			for (Role role: ag.getRoles()){
				if (role.getId().equals(".auth") || role.getId().equals(".anon")){
					sakaiProxy.removeRoleFromAuthzGroup(ag, role);
				}
			}
		}
	}

	private void copyNewRole(String siteRef, String copyRealm, String copyRole, boolean auth, boolean anon){
		if(auth){
			log.debug("Copying " + copyRole + " to .auth for " + siteRef);
			sakaiProxy.copyNewRole(siteRef, copyRealm, copyRole, ".auth");
		}
		if(anon){
			log.debug("Copying " + copyRole + " to .anon for " + siteRef);
			sakaiProxy.copyNewRole(siteRef, copyRealm, copyRole, ".anon");
		}
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
			//copy old node's permissions for the shopping period user by changing only the node Id and saving it's permissions
			String origId = node.getNodeId();
			node.setNodeId(newNode.id);
			projectLogic.updateNodePermissionsForUser(node, DelegatedAccessConstants.SHOPPING_PERIOD_USER);
			node.setNodeId(origId);
//			for(String perm : hierarchyService.getPermsForUserNodes(DelegatedAccessConstants.SHOPPING_PERIOD_USER, new String[]{node.getNodeId()})){
//				hierarchyService.assignUserNodePerm(DelegatedAccessConstants.SHOPPING_PERIOD_USER, newNode.id, perm, false);
//			}
			migratedHierarchyIds.put(node.getNodeId(), newNode.id);
		}
	}
}
