package org.sakaiproject.delegatedaccess.jobs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
import org.sakaiproject.delegatedaccess.model.NodeModel;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;

public class DelegatedAccessShoppingPeriodJob  implements Job{
	private static final Logger log = Logger.getLogger(DelegatedAccessShoppingPeriodJob.class);
	@Getter @Setter
	private ProjectLogic projectLogic;
	
	public void init() {

	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		long startTime = System.currentTimeMillis();
		
		TreeModel treeModel = projectLogic.createTreeModelForUser(DelegatedAccessConstants.SHOPPING_PERIOD_USER, false, true);
		if (treeModel != null && treeModel.getRoot() != null) {
			treeModelShoppingPeriodTraverser((DefaultMutableTreeNode) treeModel.getRoot());
		}
		log.info("PopulateSiteHierarchyJob finished in " + (System.currentTimeMillis() - startTime) + " ms");
	}
	
	private void treeModelShoppingPeriodTraverser(DefaultMutableTreeNode node){
		List<NodeModel> returnList = new ArrayList<NodeModel>();
		if(node != null){
			NodeModel nodeModel = (NodeModel) node.getUserObject();
			if(nodeModel.getNode().description.startsWith("/site/")){
				
			}
			for(int i = 0; i < node.getChildCount(); i++){
				treeModelShoppingPeriodTraverser((DefaultMutableTreeNode) node.getChildAt(i));
			}
		}
	}
	
	private void shoppingPeriodRoleHelper(NodeModel node){
		Date startDate = null;
		Date endDate = null;
		String auth = node.getShoppingPeriodAuth();
		
		if(auth == null || "".equals(auth)){
			auth = node.getInheritedShoppingPeriodAuth();
		}
		
		startDate = node.getShoppingPeriodStartDate();
		endDate = node.getShoppingPeriodEndDate();
		if(startDate == null)
			startDate = node.getInheritedShoppingPeriodStartDate();
		if(endDate == null)
			endDate = node.getInheritedShoppingPeriodEndDate();
		
		
		
		boolean addAuth = false;
		
		if(startDate != null && endDate != null){
			addAuth = startDate.before(new Date()) && endDate.after(new Date());
		}else if(startDate != null){
			addAuth = startDate.before(new Date());
		}else if(endDate != null){
			addAuth = endDate.after(new Date());
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
		
		if(addAuth && (".anon".equals(auth) || ".auth".equals(auth))){
			//remove old roles
			//TODO: We could either remove all .anon and .auth roles every time or
			//just skip if the role to add already exists in the site????
			removeAnonAndAuthRoles(node.getNode().description);
			//add either .anon or .auth role:
			copyNewRole(node.getNode().description, nodeAccessRealmRole[0], nodeAccessRealmRole[1], auth);
		}else{
			//remove .anon and .auth roles
			removeAnonAndAuthRoles(node.getNode().description);
		}
		
	}
	
	private void removeAnonAndAuthRoles(String siteRef){
		
	}
	
	private void copyNewRole(String siteRef, String copyRealm, String copyRole, String newRole){
		
	}
}
