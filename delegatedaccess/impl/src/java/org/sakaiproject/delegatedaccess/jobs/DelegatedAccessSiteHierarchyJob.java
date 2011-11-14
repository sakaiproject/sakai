package org.sakaiproject.delegatedaccess.jobs;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.hierarchy.HierarchyService;
import org.sakaiproject.hierarchy.model.HierarchyNode;
import org.sakaiproject.site.api.Site;

/**
 * 
 * This is a default quartz job to populate/update(add/remove) the Delegated Access site hierarchy.  It searches through all sites in Sakai
 * and looks for structure properties tied to the site.  The default properties are (in order):
 * 
 * 	School
 *	Department
 *	Subject
 * 
 * you can overwrite these in sakai.properties with: delegatedaccess.hierarchy.site.properties 
 * ex:
 * delegatedaccess.hierarchy.site.properties.count=3
 * delegatedaccess.hierarchy.site.properties.1=School
 * delegatedaccess.hierarchy.site.properties.2=Department
 * delegatedaccess.hierarchy.site.properties.3=Subject
 * 
 * 
 * You can run it as many times as you want.  Best bet would be to set up a quartz trigger to go off after every time your site integration runs.
 * 
 * @author Bryan Holladay (holladay@longsight.com)
 *
 */
public class DelegatedAccessSiteHierarchyJob implements Job{

	private static final Logger log = Logger.getLogger(DelegatedAccessSiteHierarchyJob.class);
	@Getter @Setter
	private HierarchyService hierarchyService;
	@Getter @Setter	
	private SakaiProxy sakaiProxy;
	private static final String[] defaultHierarchy = new String[]{DelegatedAccessConstants.SCHOOL_PROPERTY, DelegatedAccessConstants.DEPEARTMENT_PROPERTY, DelegatedAccessConstants.SUBJECT_PROPERTY};
	private Set<String> newHiearchyNodeIds;

	public void init() {

	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {


		long startTime = System.currentTimeMillis();

		newHiearchyNodeIds = new HashSet<String>();

		HierarchyNode rootNode = hierarchyService.getRootNode(DelegatedAccessConstants.HIERARCHY_ID);
		if (rootNode == null) {
			// create the hierarchy if it is not there already
			rootNode = hierarchyService.createHierarchy(DelegatedAccessConstants.HIERARCHY_ID);
			String rootTitle = sakaiProxy.getRootName();
			hierarchyService.saveNodeMetaData(rootNode.id, rootTitle, rootTitle, null);
			log.info("Created the root node for the delegated access hierarchy: " + DelegatedAccessConstants.HIERARCHY_ID);
		}

		//get hierarchy structure:
		String[] hierarchy = sakaiProxy.getServerConfigurationStrings(DelegatedAccessConstants.HIERARCHY_SITE_PROPERTIES);
		if(hierarchy == null || hierarchy.length == 0){
			hierarchy = defaultHierarchy;
		}

		for(Site site : sakaiProxy.getAllSites()){
			//search through all sites and add it to the hierarchy if the site has information (otherwise skip)

			String siteParentId = rootNode.id;
			//find lowest hierarchy node:
			for(String hiearchyProperty : hierarchy){
				String siteProperty = site.getProperties().getProperty(hiearchyProperty);
				if(siteProperty != null && !"".equals(siteProperty)){
					siteParentId = checkAndAddNode(siteParentId, siteProperty, siteProperty);
				}else{
					//nothing, so break
					break;
				}
			}

			if(!rootNode.id.equals(siteParentId)){
				//save the site under the parent hierarchy if any data was found
				//Site
				checkAndAddNode(siteParentId, site.getTitle(), site.getReference());
			}
		}

		//remove any sites that don't exist in the hierarchy (aka properties changed or site has been deleted):
		removeMissingNodes(rootNode);

		log.info("PopulateSiteHierarchyJob finished in " + (System.currentTimeMillis() - startTime) + " ms");
	}

	private void removeMissingNodes(HierarchyNode rootNode){
		for(String child : rootNode.childNodeIds){
			if(!newHiearchyNodeIds.contains(child)){
				//this site has either moved or been deleted
				removeMissingNodesHelper(hierarchyService.getNodeById(child));
			}
		}
	}

	private void removeMissingNodesHelper(HierarchyNode node){
		if(node != null){
			if(node.childNodeIds != null && !node.childNodeIds.isEmpty()){
				//we can delete this, otherwise, delete the children first the children
				for(String childId : node.childNodeIds){
					removeMissingNodesHelper(hierarchyService.getNodeById(childId));
				}
			}
			//all the children nodes have been deleted, no its safe to delete
			hierarchyService.removeNode(node.id);
			Set<String> userIds = hierarchyService.getUserIdsForNodesPerm(new String[]{node.id}, DelegatedAccessConstants.NODE_PERM_SITE_VISIT);
			for(String userId : userIds){
				removeAllUserPermissions(node.id, userId);
			}
		}
	}
	
	private void removeAllUserPermissions(String nodeId, String userId){
		for(String perm : hierarchyService.getPermsForUserNodes(userId, new String[]{nodeId})){
			hierarchyService.removeUserNodePerm(userId, nodeId, perm, false);
		}
	}



	private String checkAndAddNode(String parentId, String title, String description){
		String nodeId = "";
		if(title != null && !"".equals(title)){

			nodeId = findChildIdFromReference(parentId, description);
			if(nodeId == null){
				//if this parent/child relationship hasn't been created, create it
				HierarchyNode newNode = hierarchyService.addNode(DelegatedAccessConstants.HIERARCHY_ID, parentId);
				hierarchyService.saveNodeMetaData(newNode.id, title, description, null);
				hierarchyService.addChildRelation(parentId, newNode.id);
				nodeId = newNode.id;
			}
			newHiearchyNodeIds.add(nodeId);
		}
		return nodeId;
	}

	//Only checks direct children b/c we want to ensure hierarchy didn't change (a child could have moved in the hierarchy)
	private String findChildIdFromReference(String parentId, String childRef){
		String childId = null;
		Set<HierarchyNode> directChildred = hierarchyService.getChildNodes(parentId, true);
		for(HierarchyNode child : directChildred){
			if(childRef.equals(child.description)){
				childId = child.id;
				break;
			}
		}
		return childId;
	}

}
