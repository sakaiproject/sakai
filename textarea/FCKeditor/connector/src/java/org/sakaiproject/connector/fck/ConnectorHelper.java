/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.connector.fck;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.entitybroker.entityprovider.capabilities.CollectionResolvable;
import org.sakaiproject.entitybroker.entityprovider.extension.ActionReturn;
import org.sakaiproject.entitybroker.entityprovider.extension.EntityData;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.cover.SessionManager;

@Slf4j
public class ConnectorHelper {
	
	private static String ASSIGNMENT_TOOL_ID = "sakai.assignment.grades";
	private static String JFORUMS_TOOL_ID = "sakai.jforum.tool";
	private static String FORUMS_TOOL_ID = "sakai.forums";

	private SiteService siteService = null;
	private AssignmentService assignmentService = null;
    private SecurityService securityService = null;
	private AuthzGroupService authzGroupService = null;

	private EntityBroker entityBroker = null;

	private List sites = null;
	private String loggedInUserId = null;
	private String loggedInUserEid = null;
	private boolean userKnown = true;
	
//	private final String ASSIGNMENT_ENTITY_PREFIX = "site_assignments";
	private final String ASSIGNMENT_ENTITY_PREFIX = "assignment";
	private final String ASSESSMENT_ENTITY_PREFIX = "sam_pub";
	private final String FORUM_TOPIC_ENTITY_PREFIX = "topic";
	
	
	public void init() {
		log.info("init ConnectorHelper");
		siteService = (SiteService) ComponentManager.get("org.sakaiproject.site.api.SiteService");
		assignmentService = (AssignmentService) ComponentManager.get("org.sakaiproject.assignment.api.AssignmentService");
		authzGroupService = (AuthzGroupService) ComponentManager.get("org.sakaiproject.authz.api.AuthzGroupService");
		securityService = (SecurityService) ComponentManager.get("org.sakaiproject.authz.api.SecurityService");
		sites = siteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.UPDATE, null, null, null, SortType.TITLE_ASC, null);
		loggedInUserId = SessionManager.getCurrentSession().getUserId();
		
		entityBroker = (EntityBroker) ComponentManager.get(EntityBroker.class);
	}
	
	public List getAssignments(){
		
		Vector returnAssignmentList = new Vector();
		if(!userKnown) return new Vector();
		Iterator siteIterator = sites.iterator();
		while(siteIterator.hasNext()){
			String placementId = null;
			Site thisSite = (Site) siteIterator.next();
			List thisSitePages = thisSite.getPages();
			boolean assignmentToolNotFound = true;
			Iterator pageIterator = thisSitePages.iterator();
			while(pageIterator.hasNext() && assignmentToolNotFound){
				SitePage thisPage = (SitePage) pageIterator.next();
				List sitePageTools = thisPage.getTools();
				Iterator sitePageToolIterator = sitePageTools.iterator();
				while(sitePageToolIterator.hasNext() && assignmentToolNotFound){
					ToolConfiguration thisToolConfiguration = (ToolConfiguration) sitePageToolIterator.next();
					if(thisToolConfiguration.getToolId().equalsIgnoreCase(ASSIGNMENT_TOOL_ID)){
						assignmentToolNotFound = false;
						placementId = thisToolConfiguration.getId();
					}
				}				
			}
			if(!securityService.unlock(loggedInUserId, "site.upd", "/site/" + thisSite.getId())){
				log.info("Assignment - no show"+loggedInUserEid+" is not an instructor.");
				return returnAssignmentList;
			}		


			java.util.Date now = new java.util.Date();
			for (Assignment thisAssignment : assignmentService.getAssignmentsForContext(thisSite.getId())) {
				Instant thisAssignmentCloseTime = thisAssignment.getCloseDate();
				boolean assignmentClosed = true;
				if(thisAssignmentCloseTime!=null){
					if(thisAssignmentCloseTime.isBefore(Instant.now())){
						assignmentClosed=false;
					}
				}else{
					assignmentClosed=false;
				}
				if(thisAssignment.getDraft() | assignmentClosed ) continue;				
				StringBuffer assignmentUrlBuildBuffer = new StringBuffer();
/*				
				assignmentUrlBuildBuffer.append("/portal/tool/");
				assignmentUrlBuildBuffer.append(placementId+"?");
				assignmentUrlBuildBuffer.append("assignmentReference=/assignment/a/");
				assignmentUrlBuildBuffer.append(thisAssignment.getContext()+"/");
				assignmentUrlBuildBuffer.append(thisAssignment.getId());
				assignmentUrlBuildBuffer.append("&panel=Main&sakai_action=doView_submission");
*/
				assignmentUrlBuildBuffer.append("/direct/assignment/");
				assignmentUrlBuildBuffer.append(thisAssignment.getId());
				String[] thisAssignmentDescriptor = new String[2];
				thisAssignmentDescriptor[0] = "Assignment in site:"+thisSite.getTitle()+" - "+thisAssignment.getTitle();
				thisAssignmentDescriptor[1] = assignmentUrlBuildBuffer.toString();
				log.info("Adding assignment:"+assignmentUrlBuildBuffer.toString());
				returnAssignmentList.add(thisAssignmentDescriptor);
			}
			
		}		
		return returnAssignmentList;
	}
	
	public List getSiteAssignments(String siteId){

		if(!userKnown) return new Vector();
		Site thisSite = null;
		String placementId = null;
		Vector returnAssignmentList = new Vector();
		boolean assignmentToolNotFound = true;		
		try {
			thisSite = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			return returnAssignmentList;
		}		
		try{
			placementId = toolPlacementInSite(siteId, ASSIGNMENT_TOOL_ID);
			assignmentToolNotFound = false;
		} catch (IdUnusedException e) {
			return returnAssignmentList;			
		}catch(Exception e){
			assignmentToolNotFound = true;
		}		

		if(!securityService.unlock(loggedInUserId, "site.upd", "/site/" + siteId)){
			log.info("Assignment - no show"+loggedInUserEid+" is not an instructor.");
			return returnAssignmentList;
		}

		java.util.Date now = new java.util.Date();
		SimpleDateFormat df = new SimpleDateFormat();
		log.info("now:"+df.format(now));
		long nowMs = now.getTime();
		if(!assignmentToolNotFound){
	        HashMap params = new HashMap();
	        ActionReturn ret = entityBroker.executeCustomAction("/assignment/site/"+siteId, "site", params, null);
	        ActionReturn dlReturn = null;
	        List returnedDlRefs = null;
	        HashMap thisDLReferenceData = null;
	        List returnedEntities = ret.getEntitiesList();
	        Iterator returnedAssignments = returnedEntities.iterator();
	        String thisAssignmentId =null;
	        while(returnedAssignments.hasNext()){
	        	EntityData thisEntityData = (EntityData) returnedAssignments.next();
	        	try{
		        	Object a = thisEntityData.getData();
		        	Method m = a.getClass().getMethod("getId",null);
		        	Object[] noArgs = null;
		        	thisAssignmentId = (String) m.invoke(a,noArgs);
		        	StringBuffer urlBuffer = new StringBuffer();
/*		        	
		        	urlBuffer.append(ServerConfigurationService.getPortalUrl());
		        	urlBuffer.append("/tool/");
		        	urlBuffer.append(placementId);
		        	urlBuffer.append("?assignmentReference=/assignment/a/");
		        	urlBuffer.append(siteId);
		        	urlBuffer.append("/");
		        	urlBuffer.append(thisAssignmentId);
		        	urlBuffer.append("&panel=Main&sakai_action=doView_submission_evap");
*/
		        	urlBuffer.append("/direct/assignment/");
		        	urlBuffer.append(thisAssignmentId);
		        	
		        	String thisAssignmentUrl = urlBuffer.toString();
		        	dlReturn  =  entityBroker.executeCustomAction("/assignment/deepLink/"+siteId+"/"+thisAssignmentId, "deepLink", params, null);
		        	EntityData thisDlReferenceEd = dlReturn.getEntityData();
		        	thisDLReferenceData = (HashMap) thisDlReferenceEd.getData();
					String[] thisAssignmentDescriptor = new String[2];
					thisAssignmentDescriptor[0] = (String) thisDLReferenceData.get("assignmentTitle");
					thisAssignmentDescriptor[1] = thisAssignmentUrl;
					returnAssignmentList.add(thisAssignmentDescriptor);
	        	}catch(Exception e){
	        		String ex = e.getMessage();
	        	}
	        }
	        
		}

		return returnAssignmentList;
	}
	
	
	public List getPublishedAssements(String siteId){
		
		if(!userKnown) return new Vector();
		Site thisSite = null;
		try {
			thisSite = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			return new Vector();
		}	

		if(!securityService.unlock(loggedInUserId, "site.upd", "/site/" + siteId)){
			log.info("Assignment - no show"+loggedInUserEid+" is not an instructor.");
			return new Vector();
		}
		
        Restriction currentUserRestriction = new Restriction("userId", loggedInUserId, Restriction.EQUALS);
        org.sakaiproject.entitybroker.entityprovider.search.Search s1 = new Search(currentUserRestriction);
        Restriction currentSiteRestriction = new Restriction("context", siteId, Restriction.EQUALS);
        s1.addRestriction(currentSiteRestriction);
        HashMap params = new HashMap();
        List entitiesObj = entityBroker.fetchEntities(ASSESSMENT_ENTITY_PREFIX, s1, params);
		Vector returnAssessmentList = new Vector();
        Iterator entitiesObjIterator = entitiesObj.iterator();
        while(entitiesObjIterator.hasNext()){
			String[] thisAssessment = new String[2];
        	PublishedAssessmentFacade thisPublishedAssessmentFacade = (PublishedAssessmentFacade) entitiesObjIterator.next();
        	thisAssessment[0]=thisPublishedAssessmentFacade.getTitle();
        	thisAssessment[1]="/direct/" + ASSESSMENT_ENTITY_PREFIX + "/" + thisPublishedAssessmentFacade.getPublishedAssessmentId();
			returnAssessmentList.add(thisAssessment);        	
        }		
		return returnAssessmentList;
	}
	
	public List getForumTopicReferences(String siteId){
		
		List forumTopicReferences = new Vector();
		List entitiesObj = null;
		boolean forumToolNotFound = true;
		String placementId = null;
		try{
			placementId = toolPlacementInSite(siteId, FORUMS_TOOL_ID);
			forumToolNotFound = false;
		} catch (IdUnusedException e) {
			return forumTopicReferences;			
		}catch(Exception e){
			forumToolNotFound = true;
		}
		if(!forumToolNotFound){
	        Restriction currentUserRestriction = new Restriction("userId", loggedInUserId, Restriction.EQUALS);
	        org.sakaiproject.entitybroker.entityprovider.search.Search s1 = new Search(currentUserRestriction);
	        Restriction siteRestriction = new Restriction(CollectionResolvable.SEARCH_LOCATION_REFERENCE, "/site/" + siteId);
	        s1.addRestriction(siteRestriction);
	        HashMap params = new HashMap();
	        entitiesObj = entityBroker.getEntities(FORUM_TOPIC_ENTITY_PREFIX, s1, params);
		}		
		return entitiesObj;
	}
		
	private String toolPlacementInSite(String siteId, String toolId) throws Exception{		
		Site thisSite = null;
		String placementId = null;
		try {
			thisSite = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			throw e;
		}
		List thisSitePages = thisSite.getPages();
		boolean toolNotFound = true;
		Iterator pageIterator = thisSitePages.iterator();
		while(pageIterator.hasNext() && toolNotFound){
			SitePage thisPage = (SitePage) pageIterator.next();
			List sitePageTools = thisPage.getTools();
			Iterator sitePageToolIterator = sitePageTools.iterator();
			while(sitePageToolIterator.hasNext() && toolNotFound){
				ToolConfiguration thisToolConfiguration = (ToolConfiguration) sitePageToolIterator.next();
				if(thisToolConfiguration.getToolId().equalsIgnoreCase(toolId)){
					toolNotFound = false;
					placementId = thisToolConfiguration.getId();
				}
			}				
		}
		if(toolNotFound){
			throw new Exception("tool not placed in:"+siteId);
		}else{
			return placementId;
		}
	}
}
