package org.sakaiproject.delegatedaccess.jobs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.Setter;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
import org.sakaiproject.delegatedaccess.logic.SakaiProxy;
import org.sakaiproject.delegatedaccess.util.DelegatedAccessConstants;
import org.sakaiproject.entity.api.ResourceProperties;
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
	@Getter @Setter
	private DelegatedAccessDao dao;
	
	private static final String[] defaultHierarchy = new String[]{DelegatedAccessConstants.SCHOOL_PROPERTY, DelegatedAccessConstants.DEPEARTMENT_PROPERTY, DelegatedAccessConstants.SUBJECT_PROPERTY};
	private Set<String> newHiearchyNodeIds;
	
	private static boolean semaphore = false;

	public void init() {

	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//this will stop the job if there is already another instance running
		if(semaphore){
			log.warn("Stopping job since this job is already running");
			return;
		}
		semaphore = true;

		try{
			log.info("DelegatedAccessSiteHierarchyJob started");
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

			int page = 1;
			int pageFirstRecord = 1;
			int pageLastRecord = DelegatedAccessConstants.MAX_SITES_PER_PAGE;
			boolean hasMoreSites = true;
			int processedSites = 0;
			String errors = "";
			while (hasMoreSites) {
				List<Site> sites = sakaiProxy.getAllSitesByPages(pageFirstRecord, pageLastRecord);
				for(Site site : sites){
					//search through all sites and add it to the hierarchy if the site has information (otherwise skip)
					try{
						HierarchyNode siteParentNode = rootNode;
						ResourceProperties props = site.getProperties();

						//find lowest hierarchy node:
						for(String hiearchyProperty : hierarchy){
							String siteProperty = props.getProperty(hiearchyProperty);
							if(siteProperty != null && !"".equals(siteProperty)){
								siteParentNode = checkAndAddNode(siteParentNode, siteProperty, siteProperty, null);
							}else{
								//nothing, so break
								break;
							}
						}


						if(!rootNode.id.equals(siteParentNode.id)){
							//save the site under the parent hierarchy if any data was found
							//Site
							checkAndAddNode(siteParentNode, site.getTitle(), site.getReference(), props.getProperty(sakaiProxy.getTermField()));
						}
						processedSites++;
					}catch (Exception e) {
						log.error(e);
						if("".equals(errors)){
							errors += "The following sites had errors: \n\n";
						}
						errors += site.getId() + ": " + e.getMessage();
					}
				}
				pageFirstRecord = (DelegatedAccessConstants.MAX_SITES_PER_PAGE * page) + 1;
				page ++;
				pageLastRecord = DelegatedAccessConstants.MAX_SITES_PER_PAGE * page;
				if (sites.isEmpty()) {
					hasMoreSites = false;
				}
			}

			//report the errors
			if(!"".equals(errors)){
				log.warn(errors);
				sakaiProxy.sendEmail("DelegatedAccessShoppingPeriodJob error", errors);
			}


			//remove any sites that don't exist in the hierarchy (aka properties changed or site has been deleted):
			removeMissingNodes(rootNode);

			log.info("DelegatedAccessSiteHierarchyJob finished in " + (System.currentTimeMillis() - startTime) + " ms and processed " + processedSites + " sites.");		
		}catch (Exception e) {
			log.error(e);
			sakaiProxy.sendEmail("Error occurred in DelegatedAccessSiteHierarchyJob", e.getMessage());
		}finally{
			semaphore = false;
		}
	}

	private void removeMissingNodes(HierarchyNode rootNode){
		for(String child : rootNode.childNodeIds){
			try{
				if(!newHiearchyNodeIds.contains(child)){
					//this site has either moved or been deleted
					removeMissingNodesHelper(hierarchyService.getNodeById(child));
				}
			}catch(Exception e){
				log.error(e);
			}
		}
	}

	private void removeMissingNodesHelper(HierarchyNode node){
		if(node != null){
			if(node.childNodeIds != null && !node.childNodeIds.isEmpty()){
				//we can delete this, otherwise, delete the children first the children
				for(String childId : node.childNodeIds){I		
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



	private HierarchyNode checkAndAddNode(HierarchyNode parentNode, String title, String description, String term){
		HierarchyNode node = null;
		if(title != null && !"".equals(title)){

			List<String> nodeIds = dao.getNodesBySiteRef(description, DelegatedAccessConstants.HIERARCHY_ID);
			boolean hasChild = false;
			String childNodeId = "";
			if(nodeIds != null && nodeIds.size() > 0){
				for(String id : nodeIds){
					if(parentNode.directChildNodeIds.contains(id)){
						hasChild = true;
						childNodeId = id;
						break;
					}
				}
			}
			if(!hasChild){
				//if this parent/child relationship hasn't been created, create it
				HierarchyNode newNode = hierarchyService.addNode(DelegatedAccessConstants.HIERARCHY_ID, parentNode.id);
				hierarchyService.saveNodeMetaData(newNode.id, title, description, term);
				hierarchyService.addChildRelation(parentNode.id, newNode.id);
				node = newNode;
				//since we don't want to keep lookup up the parent id after every child is added,
				//(b/c the data is stale), just add this id to the set
				parentNode.directChildNodeIds.add(node.id);
			}else{
				//just grab the node
				node = hierarchyService.getNodeById(childNodeId);
			}
			newHiearchyNodeIds.add(node.id);
		}
		return node;
	}
//
//	//Only checks direct children b/c we want to ensure hierarchy didn't change (a child could have moved in the hierarchy)
//	private String findChildIdFromReference(HierarchyNode parentNode, String childRef){
//		String childId = null;
//		Set<String> directChildred = parentNode.directChildNodeIds;
//		for(String child : directChildred){
//			if(childRef.equals(child.description)){
//				childId = child;
//				break;
//			}
//		}
//		return childId;
//	}

}
