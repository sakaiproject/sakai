/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.tool.messageforums.ui;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrItem;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.SynopticMsgcntrItemImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.messageforums.PrivateMessagesTool;
import org.sakaiproject.tool.messageforums.SynopticSiteSemesterComparator;
import org.sakaiproject.tool.messageforums.SynopticSitesPreferencesComparator;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.cover.PreferencesService;

@Slf4j
public class MessageForumSynopticBeanLite {
	
	// transient only persists in request scope
	private transient Boolean myWorkspace = null;
	private transient Boolean anyMFToolInSite = null;
	private transient List<DecoratedSynopticMsgcntrItem> myContents = null;
	private transient DecoratedSynopticMsgcntrItem siteHomepageContent = null;
	private SynopticMsgcntrManager synopticMsgcntrManager;
	private MessageForumsForumManager forumsManager;
	private MessageForumsTypeManager typeManager;
	private AreaManager areaManager;
	private PrivateMessageManager pvtMessageManager;
	private int myContentsSize = -1;
	private Map mfPageInSiteMap, sitesMap;
	private int myDisplayedSites = 0;
	private static final String PERFORMANCE_2 = "2";
	private String performance;
	private Boolean userRequestSynoptic;
	private Boolean disableMyWorkspace;
	private Boolean disableMessages;
	private Boolean disableForums;
	private String disableMyWorkspaceDisabledMessage;
	
	/** Dependency Injected   */
	private SiteService siteService;
	private SessionManager sessionManager;
	private ToolManager toolManager;

	
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	
	public List<DecoratedSynopticMsgcntrItem> getContents(){

		if(isMyWorkspace() && !isDisableMyWorkspace()){
			if(myContents != null){
				return myContents;
			}
			
			performance = getPerformance();
			userRequestSynoptic = isUserRequestSynoptic();
				
			List<SynopticMsgcntrItem> synItems;
			myContentsSize = -1;
			myDisplayedSites = 0;
			//findWorkspaceSynopticMsgcntrItems
			synItems = getSynopticMsgcntrManager().getWorkspaceSynopticMsgcntrItems(getCurrentUser());
			
			//Grab user's preferences for site order, and remove sites that user has removed
			Preferences prefs = PreferencesService.getPreferences(getCurrentUser());
			ResourceProperties props = prefs.getProperties(org.sakaiproject.user.api.PreferencesService.SITENAV_PREFS_KEY);
			List<String> orderedSites = props.getPropertyList("order");
			
			List<String> excludedSites = props.getPropertyList("exclude");

			if(excludedSites != null){
				//user has set preferences so filter out any missing sites:
				for (Iterator iterator = synItems.iterator(); iterator
						.hasNext();) {
					SynopticMsgcntrItem synItem = (SynopticMsgcntrItem) iterator.next();
					if(excludedSites.contains(synItem.getSiteId())){
						iterator.remove();
					}
				}
			}
			
			synItems = sortSynopticMsgcntrList(synItems, orderedSites);
			
			myContents = new ArrayList<DecoratedSynopticMsgcntrItem>();

			/**
			 * This sites query needs to be the same as what the portal calls... this is to take advantage of query caching.
			 * In theory sorting by TITLE_ASC seems like it would be slower, but that actually allows the query cache to find
			 * it. If this is causing a slow down, check to see if portal changed it's getSites query.
			 */
			List<Site> sites = siteService.getSites(
					org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null,
					null, org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);
			

			for (SynopticMsgcntrItem synopticMsgcntrItem : synItems) {
				boolean deleteSite = false;
				boolean resetSynopticInfo = false;
				Site site = null;
				String synopticSiteId = synopticMsgcntrItem.getSiteId();
				if(synopticSiteId != null && !"".equals(synopticSiteId)){
					for (Site itrSite : sites) {
						if(synopticSiteId.equals(itrSite.getId())){
							site = itrSite;
							break;
						}
					}				
					//only add if the site exists:
					if(site != null){
						//check if the site title has changed:
						if(synopticMsgcntrItem.getSiteTitle() != null && !synopticMsgcntrItem.getSiteTitle().equals(site.getTitle())){
							//update all site titles in table
							getSynopticMsgcntrManager().updateAllSiteTitles(synopticMsgcntrItem.getSiteId(), site.getTitle());
							//set the current synoptic item's site title to the correct title
							synopticMsgcntrItem.setSiteTitle(site.getTitle());
						}
						
						if(!PERFORMANCE_2.equals(performance)){							
							
							DecoratedSynopticMsgcntrItem dSynopticItem = new DecoratedSynopticMsgcntrItem(synopticMsgcntrItem, site);
							//this covers the case when a tool had unread messages but then the tool was 
							//removed from the site
							boolean isMessageForumsPageInSite = isMessageForumsPageInSite(site);
							if(!isDisableMessages() && !isMessageForumsPageInSite && !isMessagesPageInSite(site) && dSynopticItem.getNewMessagesCount() != 0){
								//update synoptic item since the db is out of sync:
								resetSynopticInfo = true;
							}else if(!isDisableForums() && !isMessageForumsPageInSite && !isForumsPageInSite(site) && dSynopticItem.getNewForumCount() != 0){
								//update synoptic item since it the db is out of sync:
								resetSynopticInfo = true;
							}else{
								//everything checks out, so add it to the list
								myContents.add(dSynopticItem);
							}
						}else{
							DecoratedSynopticMsgcntrItem dSynopticItem = new DecoratedSynopticMsgcntrItem(synopticMsgcntrItem, null);
							myContents.add(dSynopticItem);						
						}
					}else{
						//site is null (could not find site is access list)
						deleteSite = true;
					}
				}else{
					//site Id is null or ""
					deleteSite = true;
				}


				if(deleteSite){
					//synoptic item for site needs to be delete
					getSynopticMsgcntrManager().deleteSynopticMsgcntrItem(synopticMsgcntrItem);
				}
				if(resetSynopticInfo){
					//update synoptic item since the db is out of sync:
					getSynopticMsgcntrManager()
					.resetMessagesAndForumSynopticInfo(Arrays.asList(synopticMsgcntrItem.getUserId()), synopticMsgcntrItem.getSiteId(), Arrays.asList(synopticMsgcntrItem));
				}		
			}
			
			myContentsSize = myContents.size();
			for (Iterator iterator = myContents.iterator(); iterator.hasNext();) {
				DecoratedSynopticMsgcntrItem dSynopticMsgcntrItem = (DecoratedSynopticMsgcntrItem) iterator
						.next();
				if(!dSynopticMsgcntrItem.getSynopticMsgcntrItem().isHideItem())
					myDisplayedSites++;
			}
			
			return myContents;
		}else{
			//not workspace or workspace is disabled
			return null;
		}
	}
	
	public int returnContentsSize(){
		return getContents().size();
	}
	
	
	public void proccessActionSaveChanges(){
		List<SynopticMsgcntrItem> items = new ArrayList<SynopticMsgcntrItem>();
		for (Iterator iterator = getContents().iterator(); iterator.hasNext();) {
			DecoratedSynopticMsgcntrItem dSynItem = (DecoratedSynopticMsgcntrItem) iterator.next();
			if(dSynItem.hasChanged()){
				items.add(dSynItem.getSynopticMsgcntrItem());
			}
		}
		
		if(items.size() > 0){
			//reset my contents to update information
			myContents = null;
			getSynopticMsgcntrManager().saveSynopticMsgcntrItems(items);
		}
	}
	
	public void showSynopticInfo(){
		setUserRequestSynoptic(Boolean.valueOf(false));
	}
	
	public DecoratedSynopticMsgcntrItem getSiteHomepageContent(){
		if(siteHomepageContent != null){
			return siteHomepageContent;
		}
		SynopticMsgcntrItem synItem = null;
		if(isMyWorkspace()){
			//do nothing
		}else if(getCurrentUser() == null){
			//return empty synopticMsgcntrItem for anon users
			Site site;
			try {
				site = siteService.getSite(getContext());
				synItem = new SynopticMsgcntrItemImpl();
				synItem.setSiteId(site.getId());
				siteHomepageContent = new DecoratedSynopticMsgcntrItem(synItem, site);
			} catch (IdUnusedException e) {
				//we not longer need this record so delete it
				log.error(e.getMessage(), e);
			}
			
		}else{
			//findSiteSynopticMsgcntrItems
			List<SynopticMsgcntrItem> synItems = getSynopticMsgcntrManager().getSiteSynopticMsgcntrItems(Arrays.asList(getCurrentUser()), getContext());
			if(synItems != null && synItems.size() == 1){
				synItem = synItems.get(0);
			}
			if(synItem != null){
				Site site;
				try {
					//only add if the site exists:
					site = siteService.getSite(synItem.getSiteId());
					//check if the site title has changed:
					if(synItem.getSiteTitle() != null && !synItem.getSiteTitle().equals(site.getTitle())){
						//update all site titles in table
						getSynopticMsgcntrManager().updateAllSiteTitles(site.getId(), site.getTitle());
						//set the current synoptic item's site title to the correct title
						synItem.setSiteTitle(site.getTitle());
					}
					siteHomepageContent = new DecoratedSynopticMsgcntrItem(synItem, site);	
				} catch (IdUnusedException e) {
					//we not longer need this record so delete it
					getSynopticMsgcntrManager().deleteSynopticMsgcntrItem(synItem);
					log.error(e.getMessage(), e);
				}					
			}else{
				//add a new entry to the table
				String userId = sessionManager.getCurrentSessionUserId();
				String siteId = toolManager.getCurrentPlacement().getContext();
				
				
				//calling resetMessagesAndForumSynopticInfo will create a new item (if needed) and 
				//set the correct counts for new messgaes
				getSynopticMsgcntrManager().resetMessagesAndForumSynopticInfo(Arrays.asList(userId), siteId, synItems);
				List<SynopticMsgcntrItem> synopticMsgcntrItems = getSynopticMsgcntrManager().getSiteSynopticMsgcntrItems(Arrays.asList(userId), siteId);
				SynopticMsgcntrItem synopticMsgcntrItem = null;
				if(synopticMsgcntrItems != null && synopticMsgcntrItems.size() == 1){
					synopticMsgcntrItem = synopticMsgcntrItems.get(0);
				}
				if(synopticMsgcntrItem != null){
					Site site;
					try {
						//only add if the site exists:
						site = siteService.getSite(synopticMsgcntrItem.getSiteId());
						siteHomepageContent = new DecoratedSynopticMsgcntrItem(synopticMsgcntrItem, site);	
					} catch (IdUnusedException e) {
						//we not longer need this record so delete it
						getSynopticMsgcntrManager().deleteSynopticMsgcntrItem(synopticMsgcntrItem);
						log.error(e.getMessage(), e);
					}			
				}
			}
		}
		
		return siteHomepageContent;
	}
	
	  private String getSiteTitle(){	  
		  try {
			return siteService.getSite(toolManager.getCurrentPlacement().getContext()).getTitle();
		} catch (IdUnusedException e) {
			log.error(e.getMessage(), e);
		}
		return "";
	  }
	
	/**
	 * Returns TRUE if on MyWorkspace, FALSE if on a specific site
	 * 
	 * @return
	 * 		TRUE if on MyWorkspace, FALSE if on a specific site
	 */
	public boolean isMyWorkspace() {
		// myWorkspace is a transient variable
		if (myWorkspace == null) {
			// get context id
			final String siteId = getContext();

			if (siteService.getUserSiteId("admin").equals(siteId))
				return false;

			myWorkspace = siteService.isUserSite(siteId);

			log.debug("Result of determining if My Workspace: " + myWorkspace);
		}
		
		return myWorkspace.booleanValue();
	}
	
	/**
	 * Returns current context
	 * 
	 * @return
	 * 		String The site id (context) where tool currently located
	 */
	public String getContext() {
		return toolManager.getCurrentPlacement().getContext();
	}

	public SynopticMsgcntrManager getSynopticMsgcntrManager() {
		return synopticMsgcntrManager;
	}

	public void setSynopticMsgcntrManager(
			SynopticMsgcntrManager synopticMsgcntrManager) {
		this.synopticMsgcntrManager = synopticMsgcntrManager;
	}


	public String getCurrentUser(){
		return sessionManager.getCurrentSessionUserId();
	}

	public String getServerUrl() {
		return ServerConfigurationService.getServerUrl();
	}
	
	
	public List<SynopticMsgcntrItem> sortSynopticMsgcntrList(List<SynopticMsgcntrItem> list, List<String> orderedSites) {
		
		
		if(orderedSites == null){
			//user hasn't set his preference for site order:
			
			//this sorts the list in two groups: Course sites then Non-Course Sites
			//	-A course site lists higher than a non-course site
			//	-Non course sites are ordered alphabetically
			// 	-Course sites are ordered by year, then semester, then alphabetically
			Collections.sort(list, new SynopticSiteSemesterComparator());
		}else{
			//this sorts the list by user preferences
			Collections.sort(list, new SynopticSitesPreferencesComparator(orderedSites));
		}
		
		return list;
	}
	
	/**
	 * Determines if any MF tool in site. variable is transient to be per request.
	 * 
	 * @return
	 * 		TRUE if Messages & Forums, Messages, or Forums tool in site
	 */
	public boolean isAnyMFToolInSite() {
		if (anyMFToolInSite == null) {
			anyMFToolInSite = isMessageForumsPageInSite() || isMessagesPageInSite() || isForumsPageInSite();
		}
		
		return anyMFToolInSite;
	}	
	
	
	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isForumsPageInSite() {
		boolean mfToolExists = false;

		try {
			final Site thisSite = getSite(getContext());

			mfToolExists = isForumsPageInSite(thisSite);

		} catch (IdUnusedException e) {
			log.error("IdUnusedException while trying to check if site has MF tool.");
		}

		return mfToolExists;
	}
	
	
	/**
	 * @return TRUE if Forums tool exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isForumsPageInSite(Site thisSite) {
		return isToolInSite(thisSite, DiscussionForumService.FORUMS_TOOL_ID);
	}
	
	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessagesPageInSite() {
		boolean mfToolExists = false;

		try {
			final Site thisSite = getSite(getContext());

			mfToolExists = isMessagesPageInSite(thisSite);

		} catch (IdUnusedException e) {
			log.error("IdUnusedException while trying to check if site has MF tool.");
		}

		return mfToolExists;
	}
	
	/**
	 * @return TRUE if Messages tool exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessagesPageInSite(Site thisSite) {
		return isToolInSite(thisSite, DiscussionForumService.MESSAGES_TOOL_ID);
	}
	
	
	/**
	 * @return TRUE if Message Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessageForumsPageInSite() {
		boolean mfToolExists = false;

		try {
			final Site thisSite = getSite(getContext());

			mfToolExists = isMessageForumsPageInSite(thisSite);

		} catch (IdUnusedException e) {
			log.error("IdUnusedException while trying to check if site has MF tool.");
		}

		return mfToolExists;
	}
	
	

	/**
	 * Returns the Site object for this id, if it exists.
	 * If not, returns IdUnusedException
	 * 
	 * @param siteId
	 * 			The site id to check
	 * 
	 * @return
	 * 			Site object for this id
	 */
	private Site getSite(String siteId) 
	throws IdUnusedException {
		if (sitesMap == null) {
			sitesMap = new HashMap();
		}
	
		if (sitesMap.get(siteId) == null) {
			Site site = siteService.getSite(siteId);
			sitesMap.put(site.getId(), site);
			return site;
		}
		else {
			return (Site) sitesMap.get(siteId);
		}
	}
	
	
	/**
	 * @return TRUE if Messages & Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(Site thisSite) {
		if (mfPageInSiteMap == null) {
			mfPageInSiteMap = new HashMap();
		}
		
		Boolean isMFPageInSite;
		if ((isMFPageInSite = (Boolean) mfPageInSiteMap.get(thisSite)) == null) {
			isMFPageInSite = isToolInSite(thisSite, DiscussionForumService.MESSAGE_CENTER_ID);
			mfPageInSiteMap.put(thisSite, isMFPageInSite);
		}
		
		return isMFPageInSite;
	}
	
	/**
	 * Return TRUE if tool with id passed in exists in site passed in
	 * FALSE otherwise.
	 * 
	 * @param thisSite
	 * 			Site object to check
	 * @param toolId
	 * 			Tool id to be checked
	 * 
	 * @return
	 */
	private boolean isToolInSite(Site thisSite, String toolId) {
		final Collection toolsInSite = thisSite.getTools(toolId);

		return ! toolsInSite.isEmpty();		
	}
	
	public String getPerformance() {
		if(performance == null){
			performance = ServerConfigurationService.getString(SynopticMsgcntrManager.MYWORKSPACE_PERFORMANCE);
		}
		return performance;
	}

	public void setPerformance(String performance) {
		this.performance = performance;
	}

	public Boolean isUserRequestSynoptic() {
		if(userRequestSynoptic == null){
			userRequestSynoptic = ServerConfigurationService.getBoolean(SynopticMsgcntrManager.MYWORKSPACE_USERPROMPT, false);
		}
		return userRequestSynoptic;
	}

	public void setUserRequestSynoptic(Boolean userRequestSynoptic) {
		this.userRequestSynoptic = userRequestSynoptic;
	}
	
	public class DecoratedSynopticMsgcntrItem{
		
		private SynopticMsgcntrItem synopticMsgcntrItem;
		private Site site ;
		private String messagesUrl, forumUrl, messagesFormattedDate, 
			forumFormattedDate, siteUrl;
		private HashMap receivedFolderUuidByContextId;
		private boolean originalDisplayValue, doesForumsExist, doesMessagesExist;

		
		public boolean isDoesForumsExist() {
			if(site == null)
				return false;
			if (isMessageForumsPageInSite(site) || isForumsPageInSite(site))
				return true;
			else
				return false;
		}

		public void setDoesForumsExist(boolean doesForumsExist) {
			this.doesForumsExist = doesForumsExist;
		}

		public boolean isDoesMessagesExist() {
			if(site == null)
				return false;
			if (isMessageForumsPageInSite(site) || isMessagesPageInSite(site))
				return true;
			else
				return false;
		}

		public void setDoesMessagesExist(boolean doesMessagesExist) {
			this.doesMessagesExist = doesMessagesExist;
		}

		public DecoratedSynopticMsgcntrItem(SynopticMsgcntrItem synopticMsgcntrItem, Site site){
			this.synopticMsgcntrItem = synopticMsgcntrItem;
			this.originalDisplayValue = synopticMsgcntrItem.isHideItem();
			this.site = site;			
		}
		
		public boolean hasChanged(){
			return originalDisplayValue != synopticMsgcntrItem.isHideItem();
		}
		
		public SynopticMsgcntrItem getSynopticMsgcntrItem() {
			return synopticMsgcntrItem;
		}
		
		public void setSynopticMsgcntrItem(SynopticMsgcntrItem synopticMsgcntrItem) {
			this.synopticMsgcntrItem = synopticMsgcntrItem;
		}
		
		public String getSiteUrl(){
			if(siteUrl == null){
				siteUrl = ServerConfigurationService.getPortalUrl() + "/site/" + synopticMsgcntrItem.getSiteId();			
			}
			return siteUrl;
		}
		
		public int getNewForumCount(){
			//check this to make sure the tool hasn't been removed
			if(site == null)
				return synopticMsgcntrItem.getNewForumCount();
			
			if (isMessageForumsPageInSite(site) || isForumsPageInSite(site)) 
			{
				return synopticMsgcntrItem.getNewForumCount();
			}else{
				return 0;
			}
		}
		
		public int getNewMessagesCount(){
			if(site == null)
				return synopticMsgcntrItem.getNewMessagesCount();
				
			//check this to make sure the tool hasn't been removed
			if (isMessageForumsPageInSite(site) || isMessagesPageInSite(site)){
				return synopticMsgcntrItem.getNewMessagesCount();
			}else{
				return 0;
			}
		}
		
		public String getMessagesUrl(){
			if(messagesUrl == null){
				messagesUrl = generatePrivateTopicMessagesUrl(synopticMsgcntrItem.getSiteId());
			}
			return messagesUrl;
		}
		
		public String getForumUrl(){
			if(forumUrl == null){
				forumUrl = getMCPageURL(synopticMsgcntrItem.getSiteId());
			}
			return forumUrl;
		}
		
		public String getMessagesPlacementId(){
			if(site == null)
				return "";
			
			String messagesPlacementId = "";

			ToolConfiguration tc = site.getToolForCommonId("sakai.messages");
			if(tc != null){
				messagesPlacementId = tc.getId();
			}
	
			return messagesPlacementId;
		}
		
		public String getForumPlacementId(){
			if(site == null)
				return "";
			
			String forumPlacementId = "";

			ToolConfiguration tc = site.getToolForCommonId("sakai.forums");
			if(tc != null){
				forumPlacementId = tc.getId();
			}

			return forumPlacementId;
		}
		
		public String getMessagesFormattedDate(){
			if(messagesFormattedDate == null){
				messagesFormattedDate = DateFormat.getDateTimeInstance().format(synopticMsgcntrItem.getMessagesLastVisit());
			}
			return messagesFormattedDate; 
		}
		
		public String getForumFormattedDate(){
			if(forumFormattedDate == null){
				forumFormattedDate = DateFormat.getDateTimeInstance().format(synopticMsgcntrItem.getForumLastVisit());
			}
			return forumFormattedDate; 
		}
		

		/**
		 * Returns a map of context id, Received folder uuid pairs 
		 */
		private void constructReceivedUuidMap(List receivedUuidsForAllSites) {
			receivedFolderUuidByContextId = new HashMap();
			
			for (Iterator listIter = receivedUuidsForAllSites.iterator(); listIter.hasNext();) {
				Object [] uuidRow = (Object []) listIter.next();
				
				receivedFolderUuidByContextId.put(uuidRow[0], uuidRow[1]);
			}		
		}

		/**
		 * Return Received folder uuid  
		 */
		private String getUuidFromMap(String contextId) {
			if (receivedFolderUuidByContextId == null) {
				List tempSiteList = new ArrayList();
				tempSiteList.add(contextId);
				List receivedUuidsForAllSites = getForumsManager().
													getReceivedUuidByContextId(tempSiteList);
				constructReceivedUuidMap(receivedUuidsForAllSites);
			}
		
			return (String) receivedFolderUuidByContextId.get(contextId);
		}
		
		/**
		 * Returns the URL using a helper to go to MC home page directly.
		 * 
		 * @return String A URL so the user can click to go to Message Center.
		 *         Needed since tool could possibly by in MyWorkspace
		 */
		private String getMCPageURL(String siteId) {
		    ToolConfiguration mcTool = null;
		    String url = null;
		    
		    try {
		    	String toolId = "";
		    	final Site site = getSite(siteId);
		    	
		    	if (isMessageForumsPageInSite(site)) {
		    		toolId = DiscussionForumService.MESSAGE_CENTER_ID;
		    	}
		    	else if (isForumsPageInSite(site)) {
		    		toolId = DiscussionForumService.FORUMS_TOOL_ID;
		    	}
		    	else if (isMessagesPageInSite(site)) {
		    		toolId = DiscussionForumService.MESSAGES_TOOL_ID;
		    	}

	    		mcTool = site.getToolForCommonId(toolId);

		    	if (mcTool != null) {
		    		if (toolId == DiscussionForumService.MESSAGE_CENTER_ID) {
		    			url = ServerConfigurationService.getPortalUrl() + "/directtool/"
		    							+ mcTool.getId() + "/sakai.messageforums.helper.helper/main";
		    		}
		    		else if (toolId == DiscussionForumService.FORUMS_TOOL_ID) {
		    			url = ServerConfigurationService.getPortalUrl() + "/directtool/"
		    							+ mcTool.getId() + "/sakai.messageforums.helper.helper/discussionForum/forumsOnly/dfForums";
		    		}
		    		else if (toolId == DiscussionForumService.MESSAGES_TOOL_ID) {
		    			url = ServerConfigurationService.getPortalUrl() + "/directtool/"
		    							+ mcTool.getId() + "/sakai.messageforums.helper.helper/privateMsg/pvtMsgHpView";
		    		}
		    	}
			}
			catch (IdUnusedException e) {
				// Weirdness since site ids used gotten from SiteService
				log.error("IdUnusedException while trying to check if site has MF tool.");

			}

			return url;

		}
		
		
		/**
		 * Construct the Url to bring up the Private Message section
		 * for the site whose id is passed in
		 * 
		 * @param contextId
		 * 				The site id
		 * 
		 * @return
		 * 			String containing the Url to call the helper to move
		 * 			to the Private Message section of a site
		 */
		public String generatePrivateTopicMessagesUrl(String contextId) {
			if (messagesUrl != null) {
				return messagesUrl;
			}
			else {
				Topic receivedTopic = null;
				String receivedTopicUuid = null;
			
	    		if ((receivedTopicUuid = getUuidFromMap(contextId)) == null) {
	    			Area area = getAreaManager().getAreaByContextIdAndTypeId(contextId, getTypeManager().getPrivateMessageAreaType());

	    			if (area != null && getCurrentUser() != null) {
	    				if (isMessagesPageInSite() || area.getEnabled().booleanValue() || getPvtMessageManager().isInstructor()){
			    			/* TODO: determine if receivedTopicUuid = ""; // is OK? */
			    			PrivateForum pf = getPvtMessageManager().initializePrivateMessageArea(area, new ArrayList());
			    			pf = getPvtMessageManager().initializationHelper(pf, area);
			    			List pvtTopics = pf.getTopics();
			    			Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator

			    			Iterator it = pvtTopics.iterator();
			    			if(it.hasNext()){
			    				receivedTopic = (Topic) it.next();
			    				receivedTopicUuid = receivedTopic.getUuid();
			    				receivedFolderUuidByContextId.put(contextId, receivedTopicUuid);
			    			}
			    		}
					}
	    		}

				ToolConfiguration mcTool = null;
				String url = null;
		    
				try {
					String toolId = "";
					final Site site = getSite(contextId);
			    	
					if (isMessageForumsPageInSite(site)) {
						toolId = DiscussionForumService.MESSAGE_CENTER_ID;
					}
					else if (isMessagesPageInSite(site)) {
						toolId = DiscussionForumService.MESSAGES_TOOL_ID;
					}
					else if (isForumsPageInSite(site)) {
						toolId = DiscussionForumService.FORUMS_TOOL_ID;
					}

					mcTool = site.getToolForCommonId(toolId);

					if (mcTool != null) {
						messagesUrl = ServerConfigurationService.getPortalUrl() + "/directtool/"
			    					+ mcTool.getId() + "/sakai.messageforums.helper.helper/privateMsg/pvtMsg?pvtMsgTopicId=" 
			    					+ receivedTopicUuid + "&contextId=" + contextId + "&selectedTopic=" + PrivateMessagesTool.PVTMSG_MODE_RECEIVED;
		    			return messagesUrl;
		    		}
		    	}
		    	catch (IdUnusedException e) {
		    		log.error("IdUnusedException attempting to move to Private Messages for a site. Site id used is: " + contextId);
		    	}
		    }

		    return "";
	    }
		
		



		
		/**
		 * @return TRUE if Forums tool exists in this site,
		 *         FALSE otherwise
		 */
		private boolean isForumsPageInSite(Site thisSite) {
			return isToolInSite(thisSite, DiscussionForumService.FORUMS_TOOL_ID);
		}
		
	}

	public int getMyContentsSize() {
		if(myContentsSize == -1){
			myContentsSize = getContents().size();
		}
		return myContentsSize;
	}

	public void setMyContentsSize(int myContentsSize) {
		this.myContentsSize = myContentsSize;
	}

	public MessageForumsForumManager getForumsManager() {
		return forumsManager;
	}

	public void setForumsManager(MessageForumsForumManager forumsManager) {
		this.forumsManager = forumsManager;
	}

	public MessageForumsTypeManager getTypeManager() {
		return typeManager;
	}

	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}

	public AreaManager getAreaManager() {
		return areaManager;
	}

	public void setAreaManager(AreaManager areaManager) {
		this.areaManager = areaManager;
	}

	public PrivateMessageManager getPvtMessageManager() {
		return pvtMessageManager;
	}

	public void setPvtMessageManager(PrivateMessageManager pvtMessageManager) {
		this.pvtMessageManager = pvtMessageManager;
	}

	public int getMyDisplayedSites() {
		return myDisplayedSites;
	}

	public void setMyDisplayedSites(int myDisplayedSites) {
		this.myDisplayedSites = myDisplayedSites;
	}

	public Boolean isDisableMyWorkspace() {
		if(disableMyWorkspace != null){
			return disableMyWorkspace;
		}
		disableMyWorkspace = ServerConfigurationService.getBoolean(SynopticMsgcntrManager.DISABLE_MYWORKSPACE, false);
		return disableMyWorkspace;
	}

	public void setDisableMyWorkspace(Boolean disableMyWorkspace) {
		this.disableMyWorkspace = disableMyWorkspace;
	}

	public boolean isDisableMessages() {
		if(disableMessages != null){
			return disableMessages;
		}
		disableMessages = ServerConfigurationService.getBoolean(SynopticMsgcntrManager.DISABLE_MESSAGES, false);
		return disableMessages;
	}

	public void setDisableMessages(Boolean disableMessages) {
		this.disableMessages = disableMessages;
	}

	public boolean isDisableForums() {
		if(disableForums != null){
			return disableForums;
		}
		disableForums = ServerConfigurationService.getBoolean(SynopticMsgcntrManager.DISABLE_FORUMS, false);
		return disableForums;
	}

	public void setDisableForums(Boolean disableForums) {
		this.disableForums = disableForums;
	}

	public String getDisableMyWorkspaceDisabledMessage() {
		if(disableMyWorkspaceDisabledMessage != null){
			return disableMyWorkspaceDisabledMessage;
		}
		disableMyWorkspaceDisabledMessage = ServerConfigurationService.getString(SynopticMsgcntrManager.DISABLE_MYWORKSPACE_DISABLEDMESSAGE);
		return disableMyWorkspaceDisabledMessage;
	}

	public void setDisableMyWorkspaceDisabledMessage(String disableMyWorkspaceMessage) {
		this.disableMyWorkspaceDisabledMessage = disableMyWorkspaceMessage;
	}

}
