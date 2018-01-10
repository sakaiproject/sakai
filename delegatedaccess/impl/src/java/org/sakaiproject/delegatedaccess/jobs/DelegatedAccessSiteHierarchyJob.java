/*
* The Trustees of Columbia University in the City of New York
* licenses this file to you under the Educational Community License,
* Version 2.0 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of the
* License at:
*
* http://opensource.org/licenses/ecl2.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.sakaiproject.delegatedaccess.jobs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.sakaiproject.delegatedaccess.dao.DelegatedAccessDao;
import org.sakaiproject.delegatedaccess.logic.ProjectLogic;
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
@Slf4j
public class DelegatedAccessSiteHierarchyJob implements Job{
	@Getter @Setter
	private HierarchyService hierarchyService;
	@Getter @Setter	
	private SakaiProxy sakaiProxy;
	@Getter @Setter
	private DelegatedAccessDao dao;
	@Getter @Setter
	private ProjectLogic projectLogic;
		
	private static AtomicBoolean jobIsRunning = new AtomicBoolean(false);

	public void init() {

	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		//this will stop the job if there is already another instance running
		if(!jobIsRunning.compareAndSet(false, true)){
			log.warn("Stopping job since this job is already running");
			return;
		}

		try{
			log.info("DelegatedAccessSiteHierarchyJob started");
			Date startTime = new Date();

//			newHiearchyNodeIds = new HashSet<String>();

			HierarchyNode rootNode = hierarchyService.getRootNode(DelegatedAccessConstants.HIERARCHY_ID);
			Date hierarchyJobLastRunDate = null;
			if (rootNode == null) {
				// create the hierarchy if it is not there already
				rootNode = hierarchyService.createHierarchy(DelegatedAccessConstants.HIERARCHY_ID);
				String rootTitle = sakaiProxy.getRootName();
				hierarchyService.saveNodeMetaData(rootNode.id, rootTitle, rootTitle, null);
				log.info("Created the root node for the delegated access hierarchy: " + DelegatedAccessConstants.HIERARCHY_ID);
			}else{
				hierarchyJobLastRunDate = projectLogic.getHierarchyJobLastRunDate(rootNode.id);
			}

			//get hierarchy structure:
			String[] hierarchy = sakaiProxy.getServerConfigurationStrings(DelegatedAccessConstants.HIERARCHY_SITE_PROPERTIES);
			if(hierarchy == null || hierarchy.length == 0){
				hierarchy = DelegatedAccessConstants.DEFAULT_HIERARCHY;
			}

			int page = 1;
			int pageFirstRecord = 1;
			int pageLastRecord = DelegatedAccessConstants.MAX_SITES_PER_PAGE;
			boolean hasMoreSites = true;
			int processedSites = 0;
			String errors = "";
			Map<String,String> propsMap = null;
			//only care about modified date if the job has ran at least once (otherwise, we don't want to slow down anything in the search)
			boolean orderByModifiedDate = hierarchyJobLastRunDate != null;
			if(!orderByModifiedDate){
				//we can only limit the sites search to known properties if the job has never ran before,
				//this is because a site could have been removed from the hierarchy (doesn't have props anymore)
				//which needs to be removed.  The date will limit the length of the job enough to make this
				//speed up not matter as much
				propsMap = new HashMap<String, String>();
				for(String prop : hierarchy){
					propsMap.put(prop, "");
				}
			}
			while (hasMoreSites) {
				//sites are ordered by
				List<Site> sites = sakaiProxy.getAllSitesByPages(propsMap, pageFirstRecord, pageLastRecord, orderByModifiedDate);
				log.info("DelegatedAccessSiteHierarchyJob: Processing site results: " + pageFirstRecord + " to " + pageLastRecord);
				for(Site site : sites){
					if(orderByModifiedDate){
						//check the date to see if we can break out:
						if(site.getModifiedDate() != null && hierarchyJobLastRunDate.after(site.getModifiedDate())){
							hasMoreSites = false;
							break;
						}
						if(site.getModifiedDate() == null && site.getCreatedDate() != null && hierarchyJobLastRunDate.after(site.getCreatedDate())){
							break;
						}
					}
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
							checkAndAddNode(siteParentNode, site.getReference(), site.getTitle(), props.getProperty(sakaiProxy.getTermField()));
						}else{
							if(orderByModifiedDate){
								//the job grabs all sites when orderBy is set, so this site was recently updated
								//we need to make sure it wasn't removed from the hierarchy:
								Map<String, List<String>> nodeIds = dao.getNodesBySiteRef(new String[]{site.getReference()}, DelegatedAccessConstants.HIERARCHY_ID);
								if(nodeIds != null && nodeIds.containsKey(site.getReference())){
									for(String nodeId : nodeIds.get(site.getReference())){
										projectLogic.removeNode(hierarchyService.getNodeById(nodeId));
									}
								}
							}
						}
						processedSites++;
					}catch (Exception e) {
						log.error(e.getMessage(), e);
						if("".equals(errors)){
							errors += "The following sites had errors: \n\n";
						}
						errors += site.getId() + ": " + e.getMessage() + "\n\n";
						StringWriter sw = new StringWriter();
						errors += sw.toString();
					}
				}
				pageFirstRecord = (DelegatedAccessConstants.MAX_SITES_PER_PAGE * page) + 1;
				page ++;
				pageLastRecord = DelegatedAccessConstants.MAX_SITES_PER_PAGE * page;
				if (sites.isEmpty()) {
					hasMoreSites = false;
				}
			}

			//Deletet empty sites:
			projectLogic.deleteEmptyNonSiteNodes(DelegatedAccessConstants.HIERARCHY_ID);
			
			//report the errors
			if(!"".equals(errors)){
				log.warn(errors);
				sakaiProxy.sendEmail("DelegatedAccessSiteHierarchyJob error", errors);
			}else{
				//no errors, so let's save this date so we can save time next run.
				//
				//we use the time recorded when the job started
				//to ensure that any modifications made while
				//this job was running will be picked up by the
				//next job run.
				projectLogic.saveHierarchyJobLastRunDate(startTime, rootNode.id);
			}

			projectLogic.clearNodeCache();
			//remove any sites that don't exist in the hierarchy (aka properties changed or site has been deleted):
	//		removeMissingNodes(rootNode);

			log.info("DelegatedAccessSiteHierarchyJob finished in " + (System.currentTimeMillis() - startTime.getTime()) + " ms and processed " + processedSites + " sites.");		
		}catch (Exception e) {
			log.error(e.getMessage(), e);
			sakaiProxy.sendEmail("Error occurred in DelegatedAccessSiteHierarchyJob", e.getMessage());
		}finally{
			jobIsRunning.set(false);
		}
	}

	private HierarchyNode checkAndAddNode(HierarchyNode parentNode, String title, String description, String term){
		HierarchyNode node = null;
		if(title != null && !"".equals(title)){

			Map<String, List<String>> nodeIds = dao.getNodesBySiteRef(new String[]{title}, DelegatedAccessConstants.HIERARCHY_ID);
			boolean hasChild = false;
			String childNodeId = "";
			if(nodeIds != null && nodeIds.containsKey(title) && nodeIds.get(title).size() > 0){
				for(String id : nodeIds.get(title)){
					if(parentNode.directChildNodeIds.contains(id)){
						hasChild = true;
						childNodeId = id;
					}else if(title.startsWith("/site/")){
						//If this is a site, there should (and can only be) 1 parent, delete
						//delete the other nodes since they are old
						projectLogic.removeNode(hierarchyService.getNodeById(id));
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
				if(!node.description.equals(description) || !node.title.equals(title)){
					node = hierarchyService.saveNodeMetaData(node.id, title, description, term);
				}
			}
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
