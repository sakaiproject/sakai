/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/MessageForumsSynopticBean.java $
 * $Id: MessageForumsSynopticBean.java $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.messageforums.PrivateMessagesTool;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageForumSynopticBean {

	/**
	 * Used to store synoptic information for a users unread messages.
	 * Whether on the Home page of a site or in MyWorkspace determines
	 * what properties are filled.
	 * <p>
	 * If in MyWorkspace, each object contains the number of unread
	 * Private Messages and number of unread Discussion Forum messages.
	 * </p>
	 * <p>
	 * If in the Home page of a site, each object contains either the
	 * number of unread Private Messages or number of unread Discussion 
	 * Forum messages.</p>
	 * 
	 * @author josephrodriguez
	 *
	 */
	public class DecoratedCompiledMessageStats {
		private String siteName;
		private String siteId;
		
		/** MyWorkspace information */
		private int unreadPrivateAmt;
		private int unreadForumsAmt;
		private String mcPageURL;
		private String privateMessagesUrl;
		private boolean messagesandForums;
		private boolean messages;
		private boolean forums;
		
		public String getSiteName() {
			return siteName;
		}

		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}

		public int getUnreadPrivateAmt() {
			return unreadPrivateAmt;
		}

		public void setUnreadPrivateAmt(int unreadPrivateAmt) {
			this.unreadPrivateAmt = unreadPrivateAmt;
		}

		public int getUnreadForumsAmt() {
			return unreadForumsAmt;
		}

		public void setUnreadForumsAmt(int unreadForumsAmt) {
			this.unreadForumsAmt = unreadForumsAmt;
		}

		public String getMcPageURL() {
			return mcPageURL;
		}

		public void setMcPageURL(String mcPageURL) {
			this.mcPageURL = mcPageURL;
		}

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		public String getPrivateMessagesUrl() {
			return privateMessagesUrl;
		}

		public void setPrivateMessagesUrl(String privateMessagesUrl) {
			this.privateMessagesUrl = privateMessagesUrl;
		}

		public boolean isMessagesandForums() {
			return messagesandForums;
		}

		public void setMessagesandForums(boolean messagesandForums) {
			this.messagesandForums = messagesandForums;
		}

		public boolean isMessages() {
			return messages;
		}

		public void setMessages(boolean messages) {
			this.messages = messages;
		}

		public boolean isForums() {
			return forums;
		}

		public void setForums(boolean forums) {
			this.forums = forums;
		}
	}

/* =========== End of DecoratedCompiledMessageStats =========== */

	// transient only persists in request scope
	private transient Boolean myWorkspace = null;
	private transient Boolean pmEnabled = null;
	private transient Boolean anyMFToolInSite = null;
	private transient List myWorkspaceContents = null;
	private transient String pvtTopicMessageUrl = null;
	private transient List groupedSitesCounts = null;
	private transient List userRoles = null;
	private transient List multiMembershipSites = null;
	private transient List compiledDFMessageCounts = null;
	private transient Map receivedFolderUuidByContextId = null;
	private transient List siteList = null;
	private transient Map sitesMap = null;
	private transient Map currentUserMembershipsBySite = null;
	private transient Map mfPageInSiteMap = null;
	
	
	// transient variable for when on home page of site
	private transient DecoratedCompiledMessageStats siteContents;
	
	/** Used to get contextId when tool on MyWorkspace to set all private messages to Read status */
	private final String CONTEXTID="contextId";

	/** Used to retrieve non-notification sites for MyWorkspace page */
	private final String TAB_EXCLUDED_SITES = "exclude";
	
	/** Preferences service (injected dependency) */
	protected PreferencesService preferencesService = null;
	
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

	/** =============== Main page bean values =============== */
	/** Used to determine if there are sites to display on page */
	private boolean sitesToView;
	private boolean sitesToViewSet = false;

	/** Needed if within a site so we only need stats for this site */
	private MessageForumsMessageManager messageManager;

	/** Needed to grab unread counts for sites current user has group membership in */
	private MessageForumsForumManager forumsManager;
	
	/** Needed to get topics if tool within a site */
	private DiscussionForumManager forumManager;

	/** Needed to grab unread message count if tool within site */
	private PrivateMessageManager pvtMessageManager;

	/** Needed to get forum message counts as well as Uuids for private messages and discussions */
	private MessageForumsTypeManager typeManager;

	/** Needed to set up the counts for the private messages and forums */
	private AreaManager areaManager;
	
	/** Needed to determine if user has read permission of topic */
	private UIPermissionsManager uiPermissionsManager;
	
	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	public void setForumsManager(MessageForumsForumManager forumsManager) {
		this.forumsManager = forumsManager;
	}

	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}

	public void setPvtMessageManager(PrivateMessageManager pvtMessageManager) {
		this.pvtMessageManager = pvtMessageManager;
	}

	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}

	public void setAreaManager(AreaManager areaManager) {
		this.areaManager = areaManager;
	}

	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}

	public void setPreferencesService(PreferencesService preferencesService) {
		this.preferencesService = preferencesService;
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
	 * Returns TRUE if there is at least one site user can access
	 * and user has not set it to be excluded
	 *  
	 * @return
	 * 			TRUE if there are/is site(s) user can access
	 * 			FALSE if not
	 */
	public boolean isSitesToView() {
		if (sitesToViewSet) {
			return sitesToView;
		}
		else {
			return ! filterOutExcludedSites(getSiteList()).isEmpty();
		}
	}

	/**
	 * @return
	 * 		TRUE if (Private) Messages enabled, FALSE otherwise
	 */
	public boolean isPmEnabled() {
		if (pmEnabled == null) {
			final Area area = pvtMessageManager.getPrivateMessageArea();
		
			pmEnabled =  (area != null) && area.getEnabled().booleanValue();
		}
		
		return pmEnabled;
	}

	/**
	 * @return 
	 * 		DecoratedCompiledMessageStats for a single site
	 */
	public DecoratedCompiledMessageStats getSiteInfo() {
		return getSiteContents();
	}

	/**
	 * Need to grab the correct count for the role user has for each site.
	 * Use for sites where user is not part of any group within a site.
	 */
	private List filterMessageCountsByRole(List dfCounts) {
		if (dfCounts.isEmpty()) {
			return dfCounts;
		}

		List resultList = new ArrayList();
		List roles;
		
		for (Iterator dfCountIter = dfCounts.iterator(); dfCountIter.hasNext();) {
			final Object [] aCount = (Object []) dfCountIter.next();
			
			roles = getCurrentUserMembershipsBySite((String) aCount[0]);
			
			String roleId = ((String) roles.get(0));
				
			if (roleId.equals((String) aCount[1])) {
				resultList.add(aCount);
			}
		}
		
		return resultList;
	}

	/**
	 * Removes from message counts messages that the user currently
	 * does not have read access to.
	 * 
	 * @param currentList
	 * 				List of message counts
	 * 
	 * @param removeList
	 * 				List of messages that user actually does not have
	 * 				read access to
	 * @return
	 * 		List with the adjusted message counts
	 */
	private List filterNoAccessMessages(List currentList, List removeList) {
		final List resultList = new ArrayList();
		
		// ****** if either list is empty, return currentList unchanged ******
		if (currentList.isEmpty() || removeList.isEmpty()) {
			return currentList;
		}

		// ****** Set up our iterator ******
		final Iterator currentIter = currentList.iterator();
		
		while (currentIter.hasNext()) {
			final Object [] resultValues = new Object [3];
			Object [] removeValues;
			Object [] currentValues = null;
			
			// get current values for this iteration
			if (currentIter.hasNext()) {
				currentValues = (Object []) currentIter.next();
			}

			if (currentValues == null)
				throw new IllegalStateException("currentValues == null");
			// is current site in the removeList. if so, return index where
			final int pos = indexOf((String) currentValues[0], getSiteIds(removeList));
			
			// if there are messages to remove, do so otherwise just add current values
			if (pos != -1) {
				removeValues = (Object []) removeList.get(pos);

				if (((String) currentValues[1]).equals(removeValues[1])) {
					resultValues[0] = currentValues[0];
					resultValues[2] = Integer.valueOf( ((Integer) currentValues[2]).intValue() - 
													((Integer) removeValues[2]).intValue() );
				}
				
				resultList.add(resultValues);
				
				removeList.remove(pos);
			} 
			else {
				resultList.add(currentValues);
			}
		}
		
		return resultList;
	}

	/**
	 * Returns a List of all roles a user has for all sites
	 * they are a member of
	 * 
	 * @param siteList
	 * 				The List of site ids the user is a member of
	 * 
	 * @return
	 * 		List of role ids user has for all sites passed in
	 */
	private List getUserRoles(List siteList) {
		if (userRoles == null) {
			userRoles = new UniqueArrayList();
			final Iterator siteIter = siteList.iterator();
			List roles;
			
			while (siteIter.hasNext()) {
				String siteId = (String) siteIter.next();
				
				roles = getCurrentUserMembershipsBySite(siteId);

				for (Iterator i = roles.iterator(); i.hasNext();) {
					String roleGroupName = (String) i.next();
				
					if (! userRoles.contains(roleGroupName)) {
						userRoles.addAll(roles);						
					}
				}
			}
		}
		
		return userRoles;
	}

	/**
	 * For this particular site, pick the correct role's count
	 * that needs to be removed from the total count
	 * 
	 * @param removeMessageCounts
	 * 				List of counts to be removed ordered by site id
	 * 
	 * @param siteList
	 * 				List of sites this user is a member of
	 * 
	 * @return
	 * 			List of correct counts, at most one per site
	 */
	private List selectCorrectRemoveMessageCount(List removeMessageCounts, List siteList) {
		// if message counts empty, nothing to do so return
		if (removeMessageCounts.isEmpty()) {
			return removeMessageCounts;
		}
		
		Object [] resultSet = null;		
		final List resultList = new ArrayList();
		final Iterator siteIter = siteList.iterator();

		while (siteIter.hasNext()) {
			final String siteId = (String) siteIter.next();

			// does current site contain counts to remove. if so, return index where
			// once processed, site id removed, so regenerate site list
			int pos = indexOf(siteId, getSiteIds(removeMessageCounts));

			// found, so get it and add to result list
			if (pos != -1) {
				resultSet = (Object []) removeMessageCounts.get(pos);
				
				while (siteId.equals((String) resultSet[0])) {
					// permissions based on roles, so need to check if user's role has messages
					// that need to be removed from totals (either total or unread)
					List roleAndGroups = getCurrentUserMembershipsBySite(siteId);
				
					if (roleAndGroups.contains((String) resultSet[1])) {
						resultList.add(resultSet);
					}

					removeMessageCounts.remove(pos++);
					
					pos = indexOf(siteId, getSiteIds(removeMessageCounts));
					
					if (pos != -1) {
						resultSet = (Object []) removeMessageCounts.get(pos);
					}
					else {
						// nope, no more for this site so do this to stop loop
						resultSet = new Object [3];
						resultSet[0] = "";
					}  // end if setting up for next iteration of while
				}   // end while (site id = remove message site id)
			}  // end if (pos != -1)
		}  // end while (sites to check)
		
		return resultList;
	}

	/**
	 * Returns a list of all sites that the current user has
	 * multiple memberships (ie, grouped).
	 */
	private List getMultiMembershipSites(List siteList) {
		if (multiMembershipSites == null) {
			multiMembershipSites = new ArrayList();
		
			for (Iterator siteIter = siteList.iterator(); siteIter.hasNext();) {
				final String siteId = (String) siteIter.next();
			
				List roles = getCurrentUserMembershipsBySite(siteId);
			
				if (roles.size() > 1) {
					multiMembershipSites.add(siteId);
				}
			}
		}
		
		return multiMembershipSites;
	}

	private List getCurrentUserMembershipsBySite(String siteId) {
		if (currentUserMembershipsBySite == null) {
			currentUserMembershipsBySite = new HashMap();
		}
		
		List roles;
		if ((roles = (List) currentUserMembershipsBySite.get(siteId)) == null) {
			currentUserMembershipsBySite.put(siteId, uiPermissionsManager.getCurrentUserMemberships(siteId));
		}
		
		return (List) currentUserMembershipsBySite.get(siteId);
	}
	
	private List filterAndAggragateGroupCounts(List counts)
	{
		if (counts.isEmpty())
		{
			return counts;
		}
		
		List results = new ArrayList();
		Object [] anotherCount;
		
		Iterator countIter = counts.iterator(); 

		Object [] aCount = (Object []) countIter.next();
		int forumCount = ((Long) aCount[3]).intValue();
		Long currentTopicId = (Long) aCount[1];
		String currentContextId = (String) aCount[0];
		String oldContextId;
		List currentUserMemberships = (List) getCurrentUserMembershipsBySite(currentContextId);
		
		while (countIter.hasNext())
		{
			anotherCount = (Object []) countIter.next();

			// if still in current site, add this count
			if (currentContextId.equals((String) anotherCount[0]))
			{
				if (currentTopicId.longValue() != ((Long) anotherCount[1]) &&
						currentUserMemberships.contains((String) anotherCount[2]) )
				{
					forumCount += ((Long) anotherCount[3]).intValue();
					
					currentTopicId = (Long) anotherCount[1];
				}
			}
			else
			{
				// new site, save final count
				Object [] finalCount = new Object [2];
				finalCount[0] = currentContextId;
				finalCount[1] = forumCount;
				results.add(finalCount);

				// set up for new site
				forumCount = ((Long) anotherCount[3]).intValue();
				currentTopicId = (Long) anotherCount[1];
				oldContextId = currentContextId;
				currentContextId = (String) anotherCount[0];
			}
		}

		// last set needs to be saved into list 
		Object [] finalCount = new Object [2];
		finalCount[0] = currentContextId;
		finalCount[1] = forumCount;
		results.add(finalCount);			
		
		return results;		
	}

	/**
	 * Returns a count whose context id (index 0) matches the
	 * value passed in. Otherwise null is returned.
	 */
	private Object [] getReadCount(List counts, String contextId)
	{
		for (Iterator countsIter = counts.iterator(); countsIter.hasNext();)
		{
			Object [] count = (Object []) countsIter.next();
			
			if (contextId.equals((String) count [0]))
			{
				// we found the count, remove from list for efficiency (?)
/*				while (contextId.equals((String) count [0]))
				{
					counts.remove(count);
					
					if (countsIter.hasNext())
					{
						count = (Object []) countsIter.next();
					}
					else
					{
						count = new Object [2];
						count[0] = "";
					}
				}
*/
				return count;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the unread message count for each site.
	 */
	private List computeGroupedSitesUnreadCounts(List totalCounts, List readCounts)
	{
		if (readCounts.isEmpty())
		{
			return totalCounts;
		}
		
		List results = new ArrayList();
		
		for (Iterator totalCountsIter = totalCounts.iterator(); totalCountsIter.hasNext();)
		{
			Object [] totalCount = (Object []) totalCountsIter.next();
			Object [] readCount = getReadCount(readCounts, (String) totalCount[0]);
			
			if (readCount == null)
			{
				results.add(totalCount);
			}
			else
			{
				Object [] finalCount = new Object [2];
				
				finalCount[0] = totalCount[0];
				finalCount[1] = ((Integer) totalCount[1]).intValue() - ((Integer) readCount[1]).intValue();
				
				results.add(finalCount);
			}
		}
		
		return results;
	}
	
	/**
	 * Returns unread message counts for sites this user is a 
	 * member of a group. Need this since memberships caused
	 * overlapping access and counts too high.
	 */
	private List getGroupedSitesCounts(List groupedSites) 
	{
		if (groupedSitesCounts == null) {
			if (groupedSites.isEmpty()) {
				groupedSitesCounts = groupedSites;
			}
			else {
				List results;

				final List roleList = getUserRoles(groupedSites);


				List dfTopicCounts = messageManager.findDiscussionForumMessageCountsForGroupedSitesByTopic(groupedSites, roleList);
				List dfTopicReadCounts = messageManager.findDiscussionForumReadMessageCountsForGroupedSitesByTopic(groupedSites, roleList);
		
				dfTopicCounts = filterAndAggragateGroupCounts(dfTopicCounts);
				dfTopicReadCounts = filterAndAggragateGroupCounts(dfTopicReadCounts);
		
				groupedSitesCounts = computeGroupedSitesUnreadCounts(dfTopicCounts, dfTopicReadCounts);
			}			
		}
		
		return groupedSitesCounts;
	}
	
 	/**
	 * Computes the counts for sites with topics that don't have
	 * membership items (permissions) associate with them, ie, it
	 * uses the defaults
	 * @param siteList
	 * @return
	 */
	private List<Object []> computeNonMICounts(List<String> siteList)
	{
		// Get List of total counts - kept as list since will
		// be accessing all elements
		final List<Object []> nonMICounts = messageManager.
					findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites(siteList);
	
		// Convert List of counts of read messages to Map since
		// will be accessing specific sites' counts
		final List<Object []> nonMIReadCounts = messageManager.
						findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites(siteList);
		Map<String, Integer> nonMIReadCountsMap = new HashMap<String, Integer>();
	
		for (Object [] nonMIReadCount: nonMIReadCounts)
		{
			nonMIReadCountsMap.put((String) nonMIReadCount[0], ((Long) nonMIReadCount[1]).intValue());
		}
	
		// Loop through all elements of nonMICounts and 
		// do the actual computation if read count exists for current site
		for (Object [] nonMIcount: nonMICounts)
		{
			Integer nonMIReadCount = nonMIReadCountsMap.get((String) nonMIcount[0]);
				if (nonMIReadCount != null)
			{
				// Need to subtract int values, not Integer
				nonMIcount[1] = ((Long) nonMIcount[1]).intValue() - nonMIReadCount;
			}
		}
		
		return nonMICounts;
	}
	
	/**
	 * Determines the number of unread messages for each site.
	 * Filters out messages user does not have read permission for.
	 * 
	 * @param siteList
	 * 			List of sites user is a member of/has access to
	 * 
	 * @return
	 * 		List of unread message counts grouped by site
	 */
	private List compileDFMessageCount(List siteList) {
		if (compiledDFMessageCounts == null) {
			compiledDFMessageCounts = new ArrayList();

			// retrieve what possible roles user could be in sites
			final List roleList = getUserRoles(siteList);

			// retrieve counts for topics within sites that don't
			// have membership item permissions saved in the db
			final List<Object []> nonMICounts = computeNonMICounts(siteList);
			
			final List siteListMinusGrouped = new ArrayList();
			siteListMinusGrouped.addAll(siteList);
		
			// get List of sites where user is part of a group since
			// need to process differently since could affect which messages
			// able to view.
			final List groupedSites = getMultiMembershipSites(siteList);
		
			siteListMinusGrouped.removeAll(groupedSites);

			final List groupedSitesCounts = getGroupedSitesCounts(groupedSites);
		
			// ******* Pulls total discussion forum message counts from DB *******
			// If grouped in all sites, no processing needed
			if (! siteListMinusGrouped.isEmpty()) {
				List discussionForumMessageCounts = messageManager
						.findDiscussionForumMessageCountsForAllSitesByPermissionLevelId(siteListMinusGrouped, roleList);
				
				
				discussionForumMessageCounts.addAll(messageManager
						.findDiscussionForumMessageCountsForAllSitesByPermissionLevelName(siteListMinusGrouped, roleList));

				discussionForumMessageCounts = filterMessageCountsByRole(discussionForumMessageCounts);
		
				// if still messages, keep processing
				if (! discussionForumMessageCounts.isEmpty()) {
					// Pulls read discussion forum message counts from DB
					List discussionForumReadMessageCounts = messageManager
						.findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelId(siteListMinusGrouped, roleList);
					
					discussionForumReadMessageCounts.addAll(messageManager
						.findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelName(siteListMinusGrouped, roleList));

					// if no read messages, totals are current message counts
					if (discussionForumReadMessageCounts.isEmpty()) {
						for (Iterator iter = discussionForumMessageCounts.iterator(); iter.hasNext();) {
							Object [] count = (Object []) iter.next();
						
							Object [] finalCount = new Object [2];
							finalCount[0] = count[0];
							finalCount[1] = count[2];
						
							compiledDFMessageCounts.add(finalCount);
						}
					}
					else {
						// else get correct read count
						discussionForumReadMessageCounts = filterMessageCountsByRole(discussionForumReadMessageCounts);
				
						// subtract read from total to get unread counts
						compiledDFMessageCounts = computeUnreadDFMessages(
												discussionForumMessageCounts,
												discussionForumReadMessageCounts);
					} // end (discussionForumReadMessageCounts.isEmpty()) - after retrieving read messages from db 
				} // end (! discussionForumMessageCounts.isEmpty()) - after initial retrieval of messages from db
			} // end (! discussionForumMessageCounts.isEmpty())

			compiledDFMessageCounts.addAll(groupedSitesCounts);
			
			// add in counts for sites with topics that don't have
			// membership items associated with them (ie, defaults)
			addNonMICounts(nonMICounts, compiledDFMessageCounts);
		}
		
		return compiledDFMessageCounts;
	}

 	/**
	 * Adds in counts for all sites that have topics with no associated
	 * membership items (permissions), ie, uses defaults
	 * @param nonMICounts
	 * @param compiledDFMessageCounts
	 */
	private void addNonMICounts(List<Object []> nonMICounts, List<Object []> compiledDFMessageCounts)
	{
		Map<String, Object []> compiledCountsMap = new HashMap<String, Object []>();
		List<Object []> countsToAdd = new ArrayList<Object []>();
		
		for (Object [] compiledCount: compiledDFMessageCounts)
		{
			compiledCountsMap.put((String) compiledCount[0], compiledCount);
		}
		
		for (Object [] nonMIcount: nonMICounts)
		{
			Object [] currentUnreadCount = compiledCountsMap.get((String) nonMIcount[0]);
	
			if (currentUnreadCount != null) {
				currentUnreadCount[1] = Integer.valueOf((Integer) currentUnreadCount[1]).intValue() +
										Integer.valueOf((Integer) nonMIcount[1]).intValue();
			}
			else
			{
				countsToAdd.add(nonMIcount);
			}
		}
		
		compiledDFMessageCounts.addAll(countsToAdd);
	}
		
	/**
	 * Removes all sites user does not want message info about and
	 * returns all sites left
	 * 
	 * @param allSites
	 * 				List of all sites user is a member of
	 * 
	 * @return
	 * 		List of sites user wants notification about
	 */
	private List filterOutExcludedSites(List allSites) {
		final List excludedSites = getExcludedSitesFromTabs();
		
		if (excludedSites != null) {
			for (Iterator excludeIter = excludedSites.iterator(); excludeIter.hasNext(); ) {
				final String siteId = (String) excludeIter.next();
				allSites.remove(siteId);
			}
		}
		
		return allSites;
	}
	
	/**
	 * Return List to populate page if in MyWorkspace
	 * 
	 * @return
	 * 		List of DecoratedCompiledMessageStats to populate MyWorkspace page
	 */
	private List getMyWorkspaceContents() {
		if (myWorkspaceContents != null) {
			return myWorkspaceContents;
		}
		else {
			
		final List contents = new ArrayList();
		Object[] unreadDFCount;
		Object[] pmCounts;
		
		// Used to determine if there are any sites to view on UI
		sitesToView = false;

		// retrieve what sites is this user a member of
		final List siteList = filterOutExcludedSites(getSiteList());

		// no sites to work with, set boolean variable and return
		if (siteList.isEmpty()) { 
			sitesToView = false;
			myWorkspaceContents = contents;
			return contents; 
		}

		// ******* Pulls unread private message counts from DB ******* 
		final List privateMessageCounts = pvtMessageManager
					.getPrivateMessageCountsForAllSites();

		// ******* Pulls unread discussion forum message counts from DB *******
		List unreadDFMessageCounts = compileDFMessageCount(siteList);
		
		// If both are empty, no unread messages so
		// create 0 count beans for both types for all sites not filtered so
		// displays proper messages
		if (privateMessageCounts.isEmpty() && unreadDFMessageCounts.isEmpty()) {
			
			for (Iterator siteIter = siteList.iterator(); siteIter.hasNext(); ) {
				String siteId = "";
				Site site= null;
			
				// ************ Get next site from List ************ 
				try {
					siteId = (String) siteIter.next();
					site = getSite(siteId);
				}
				catch (IdUnusedException e) {
					// Wierdness has happened - pulled from SiteService but now can't
					// find it. Logger and skip
					log.error("IdUnusedException attempting to access site " + siteId);
					continue;
				}
				
				// ************ Each row on page gets info stored in DecoratedCompiledMessageStats bean ************ 
				final DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

				// fill site title
				dcms.setSiteName(site.getTitle());
				dcms.setSiteId(siteId);
				
				dcms.setUnreadForumsAmt(0);
				dcms.setUnreadPrivateAmt(0);
				
				dcms.setMcPageURL(getMCPageURL(siteId));
				dcms.setPrivateMessagesUrl(generatePrivateTopicMessagesUrl(siteId));
				
				try {
					dcms.setMessagesandForums(isMessageForumsPageInSite(getSite(siteId)));
					dcms.setMessages(isMessagesPageInSite(getSite(siteId)));
					dcms.setForums(isForumsPageInSite(getSite(siteId)));
				}
				catch (IdUnusedException e) {
					log.error("IdUnusedException while trying to determine what tools are in site "
									+ siteId + "to set decorated synoptic messages & forums bean values.");
				}

				contents.add(dcms);
				
				sitesToView = true;
			}
			
			myWorkspaceContents = contents;
			return contents;
		}

		//============= At least some unread messages so process =============

		// ************ loop through info to fill decorated bean ************ 
		for (Iterator si = siteList.iterator(); si.hasNext();) {
			boolean hasPrivate = false;
			boolean hasDF = false;
			String siteId = "";
			Site site= null;
		
			// ************ Get next site from List ************ 
			try {
				siteId = (String) si.next();
				site = getSite(siteId);
			}
			catch (IdUnusedException e) {
				// Wierdness has happened - pulled from SiteService but now can't
				// find it. Logger and skip
				log.error("IdUnusedException attempting to access site " + siteId);
				continue;
			}

			// Determine if current site has unread private messages
			final int PMpos = indexOf(siteId, getSiteIds(privateMessageCounts));
			
			if (PMpos != -1) {
				pmCounts = (Object []) privateMessageCounts.get(PMpos);
				
				// to make searching for remaining counts more efficient
				privateMessageCounts.remove(pmCounts);
			}
			else {
				pmCounts = new Object[1];
				pmCounts[0] = "";
			}

			// Determine if current site has unread discussion forum messages
			final int DFpos = indexOf(siteId, getSiteIds(unreadDFMessageCounts));
			
			if (DFpos != -1) {
				unreadDFCount = (Object []) unreadDFMessageCounts.get(DFpos);
				
				// to make searching for remaining counts more efficient
				unreadDFMessageCounts.remove(DFpos);
			}
			else {
				unreadDFCount = new Object[1];
				unreadDFCount[0] = "";
			}

			// ************ Each row on page gets info stored in DecoratedCompiledMessageStats bean ************ 
			final DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

			// fill site title
			dcms.setSiteName(site.getTitle());
			dcms.setSiteId(siteId);

			// Put check here because if not in site, skip
			if (isMessageForumsPageInSite(site) || isMessagesPageInSite(site)) {

				// ************ checking for unread private messages for this site ************  
				if (siteId.equals(pmCounts[0])) {
					if (isMessagesPageInSite(site)) {
						dcms.setUnreadPrivateAmt(((Integer) pmCounts[1]).intValue());
						hasPrivate = true;						
					}
					else {
						// check if not enabled
						final Area area = areaManager.getAreaByContextIdAndTypeId(siteId, 
													typeManager.getPrivateMessageAreaType());

						if (area != null) {
							if (area.getEnabled().booleanValue()) {
								dcms.setUnreadPrivateAmt(((Integer) pmCounts[1]).intValue());
								hasPrivate = true;
							}
							else {
								dcms.setUnreadPrivateAmt(0);
								hasPrivate = true;
							}
						}
						else {
							dcms.setUnreadPrivateAmt(0);
							hasPrivate = true;
						}
					}
				}
				else {
					dcms.setUnreadPrivateAmt(0);
					hasPrivate = true;
				}
			}
				
			if (isMessageForumsPageInSite(site) || isForumsPageInSite(site)) {
				// ************ check for unread discussion forum messages on this site ************
				if (siteId.equals(unreadDFCount[0])) {
					// counts exist, so put it in decorated bean
					dcms.setUnreadForumsAmt(((Integer) unreadDFCount[1]).intValue());

					hasDF = true;
				} 
				else {
					// no unread counts, so set to zero
					dcms.setUnreadForumsAmt(0);
					hasDF = true;
					}
			}

			// ************ get the page URL for Message Center************
			// only if unread messages, ie, only if row will appear on page 
			if (hasPrivate || hasDF) {
				dcms.setMcPageURL(getMCPageURL(siteId));
				dcms.setPrivateMessagesUrl(generatePrivateTopicMessagesUrl(siteId));

				try {
					dcms.setMessagesandForums(isMessageForumsPageInSite(getSite(siteId)));
					dcms.setMessages(isMessagesPageInSite(getSite(siteId)));
					dcms.setForums(isForumsPageInSite(getSite(siteId)));
				}
				catch (IdUnusedException e) {
					log.error("IdUnusedException while trying to determine what tools are in site "
									+ siteId + "to set decorated synoptic messages & forums bean values.");
				}

				contents.add(dcms);
				
				sitesToView = true;
			}
		}
		
		myWorkspaceContents = contents;
		return contents;
		
		}
	}

	/**
	 * Returns a list of Strings stored in index 0 of Object [] members of
	 * list passed in.
	 * 
	 * @param counts
	 * 			List of Object [] members whose element at index 0 is a String
	 * 
	 * @return
	 * 			List of Strings extracted from list passed in
	 */
	private List getSiteIds(List counts) {
		final List results = new ArrayList();
		
		if (! counts.isEmpty()) {
			for (final Iterator iter = counts.iterator(); iter.hasNext(); ) {
				final Object [] pmCount = (Object []) iter.next();
				
				results.add(pmCount[0]);
			}
		}
		
		return results;
	}
	
	/**
	 * Returns List to populate page if on Home page of a site
	 * 
	 * @return
	 * 		List of DecoratedCompiledMessageStats for a particular site
	 */
	private DecoratedCompiledMessageStats getSiteContents() 
	{
		if (siteContents == null) 
		{
			final DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();
		
			// Check if tool within site
			// if so, get stats for just this site
			if (isMessageForumsPageInSite() || isMessagesPageInSite()) 
			{
				int unreadPrivate = 0;

				dcms.setSiteName(getSiteName());
				dcms.setSiteId(getSiteId());

				// Get private message area so we can get the private message forum so we can get the
				// List of topics so we can get the Received topic to finally determine number of unread messages
				// only check if Messages & Forums in site, just Messages is on by default
				boolean isEnabled;
				final Area area = pvtMessageManager.getPrivateMessageArea();
			
				if (isMessageForumsPageInSite()) {
					isEnabled = area.getEnabled().booleanValue();
				}
				else {
					isEnabled = true;
				}
			
				if (isEnabled) {
					List aggregateList = new ArrayList();
					pvtMessageManager.initializePrivateMessageArea(area, aggregateList);

					unreadPrivate = pvtMessageManager.findUnreadMessageCount(
										typeManager.getReceivedPrivateMessageType(), aggregateList);

					dcms.setUnreadPrivateAmt(unreadPrivate);
					dcms.setPrivateMessagesUrl(generatePrivateTopicMessagesUrl(getSiteId()));
				}
				else {
					dcms.setUnreadPrivateAmt(0);
					dcms.setPrivateMessagesUrl(getMCPageURL());
				}
			}

			if (isMessageForumsPageInSite() || isForumsPageInSite()) 
			{
				// Number of unread forum messages is a little harder
				// need to loop through all topics and add them up
				final List topicsList = forumManager.getDiscussionForums();
				int unreadForum = 0;

				final Iterator forumIter = topicsList.iterator();

				while (forumIter.hasNext()) 
				{
					final DiscussionForum df = (DiscussionForum) forumIter.next();

					final List topics = df.getTopics();
					final Iterator topicIter = topics.iterator();

					while (topicIter.hasNext()) 
					{
						final DiscussionTopic topic = (DiscussionTopic) topicIter.next();
					
						if (uiPermissionsManager.isRead(topic, df))
						{
							if (!topic.getModerated().booleanValue() || (topic.getModerated().booleanValue() && 
										uiPermissionsManager.isModeratePostings(topic, df)))
							{
								unreadForum += messageManager.findUnreadMessageCountByTopicId(topic.getId());
							}
							else
							{	
								// b/c topic is moderated and user does not have mod perm, user may only
								// see approved msgs or pending/denied msgs authored by user
								unreadForum += messageManager.findUnreadViewableMessageCountByTopicId(topic.getId());
							}
						}
					}
				}
			
				dcms.setUnreadForumsAmt(unreadForum);
				dcms.setMcPageURL(getMCPageURL());
			}
			else 
			{
				// TODO: what to put on page? Alert? Leave Blank?
			}

			siteContents = dcms;
		}
		
		return siteContents;
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
	 * Returns List of decoratedCompiledMessageStats. Called by
	 * jsp page and main processing of list to be displayed.
	 * <p>
	 * Used by both MyWorkspace and site Home page.
	 * 
	 * @return 
	 * 		List of decoratedCompiledMessageStats
	 */
	public List getContents() {
		if (isMyWorkspace()) {
			// Get stats for "all" sites this user is a member of
			// and has not turned displaying info off
			return getMyWorkspaceContents();			
		}
		else {
			// refactored to not use dataTable 12/12/06
			return new ArrayList();
		}
	}

	/**
	 * Retrieve the site display title
	 * 
	 * @return
	 * 		String of the title of the site
	 */
	private String getSiteName() {
		try {
			return getSite(getContext()).getTitle();
		} 
		catch (IdUnusedException e) {
			log.error("IdUnusedException when trying to access site "
					+ e.getMessage());
		}

		return null;
	}

	/**
	 * Returns the site id String
	 * 
	 * @return 
	 * 		The id for current site
	 */
	private String getSiteId() {
		try {
			return getSite(getContext()).getId();
		} 
		catch (IdUnusedException e) {
			log.error("IdUnusedException when trying to access site "
					+ e.getMessage());
		}

		return null;
	}

	/**
	 * Returns List of unread messages organized by site
	 * 
	 * @param totalMessages
	 * 			List of all messages by site
	 * 
	 * @param readMessages
	 * 			List of all read messages by site
	 * 
	 * @param totalNoAccessMessages
	 * 			List of all messages user does not have access to by site
	 * 
	 * @pararm totalNoAccessReadMessages
	 * 			List of all read messages user does not have access to by site
	 * 			(ie, no read permission for that topic)
	 * 
	 * @return
	 * 			List of unread messages by site
	 */
	private List computeUnreadDFMessages(List totalMessages, List readMessages) {
		final List unreadDFMessageCounts = new ArrayList();
		final List readSiteIds = getSiteIds(readMessages);
		
		// Constructs the unread message counts
		final Iterator dfMessagesIter = totalMessages.iterator();
		
		while (dfMessagesIter.hasNext()) {
			final Object [] dfMessageCountForASite = (Object[]) dfMessagesIter.next();
			
			final Object[] siteDFInfo = new Object[2];

			siteDFInfo[0] = (String) dfMessageCountForASite[0];

			int pos = indexOf((String) siteDFInfo[0], readSiteIds);

			// read message count for this site found, so subtract it
			if (pos != -1) {
				final Object [] dfReadMessageCountForASite = (Object []) readMessages.get(pos);
				
				siteDFInfo[1] = ((Long) dfMessageCountForASite[2]).intValue() - ((Long) dfReadMessageCountForASite[2]).intValue();
				
				// done with it, remove from list
				readMessages.remove(pos);
				readSiteIds.remove(pos);
			} 
			else {
				// No messages read for this site so message count = unread message count
				siteDFInfo[1] = ((Long) dfMessageCountForASite[2]).intValue();
			}

			unreadDFMessageCounts.add(siteDFInfo);
		}

		return unreadDFMessageCounts;
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

	/**
	 * Returns the URL for the page the Message Center tool is on. Called if
	 * tool on home page of a site.
	 * 
	 * @return String A URL so the user can click to go to Message Center.
	 *         Needed since tool could possibly by in MyWorkspace
	 */
	private String getMCPageURL() {
		return getMCPageURL(getContext());
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
	 * This marks all Private messages as read for a particular site
	 * 
	 * @param ActionEvent e
	 */
	public void processReadAll(ActionEvent e) {
		//need modified to support internationalization
		final String typeUuid = typeManager.getReceivedPrivateMessageType();

		if (isMyWorkspace()) {
			// if within MyWorkspace, need to find the siteId
			final FacesContext context = FacesContext.getCurrentInstance();
			final Map requestParams = context.getExternalContext()
												.getRequestParameterMap();

			final String contextId = (String) requestParams.get(CONTEXTID);

			final List privateMessages = pvtMessageManager
											.getMessagesByTypeByContext(typeUuid, contextId);

			if (privateMessages == null) {
				log.error("No messages found while attempting to mark all as read "
								+ "from synoptic Message Center tool.");
			} 
			else {
				for (Iterator iter = privateMessages.iterator(); iter.hasNext();) {
					pvtMessageManager.markMessageAsReadForUser(
											(PrivateMessage) iter.next(), contextId);
				}
			}
		} 
		else {
			// Get the site id and user id and call query to
			// mark them all as read
			List privateMessages = pvtMessageManager.getMessagesByType(
										typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
											PrivateMessageManager.SORT_DESC);

			if (privateMessages == null) {
				log.error("No messages found while attempting to mark all as read "
								+ "from synoptic Message Center tool.");
			} 
			else {
				// TODO: construct query to be one roundtrip to DB
				for (Iterator iter = privateMessages.iterator(); iter.hasNext();) {
					pvtMessageManager.markMessageAsReadForUser((PrivateMessage) iter.next());
				}
			}
		}
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
	 * Returns current context
	 * 
	 * @return
	 * 		String The site id (context) where tool currently located
	 */
	private String getContext() {
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * Returns a list of site ids as well as populating a Map of site objects
	 * 
	 * @return
	 * 		List A List of site ids that is published and the user is a member of
	 */
	public List getSiteList() {
		if (siteList == null) { 
			siteList = new ArrayList();
			
			if (sitesMap == null) {
				sitesMap = new HashMap();
			}
			
			List mySites = siteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
					null,null,null,org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC,
					null);

			Iterator lsi = mySites.iterator();

			if (!lsi.hasNext()) {
				log.debug("User " + sessionManager.getCurrentSessionUserId() + " does not belong to any sites.");

				return mySites;
			}

			// only display sites that are published and have Message Center in them
			while (lsi.hasNext()) {
				Site site = (Site) lsi.next();

				// filter out unpublished
				if (site.isPublished()) {
					siteList.add(site.getId());
					
					sitesMap.put(site.getId(), site);
				}
			}
		}

		return siteList;
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
			tempSiteList.addAll(filterOutExcludedSites(getSiteList()));
			List receivedUuidsForAllSites = new ArrayList();
			
			if (tempSiteList.size() > 0) {
				receivedUuidsForAllSites = forumsManager.
												getReceivedUuidByContextId(tempSiteList);
			}
			
			constructReceivedUuidMap(receivedUuidsForAllSites);
		}
	
		return (String) receivedFolderUuidByContextId.get(contextId);
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
		if (pvtTopicMessageUrl != null && pvtTopicMessageUrl.contains(contextId)) {
			return pvtTopicMessageUrl;
		}
		else {
			Topic receivedTopic = null;
			String receivedTopicUuid = null;
		
    		if ((receivedTopicUuid = getUuidFromMap(contextId)) == null) {
    			Area area = areaManager.getAreaByContextIdAndTypeId(contextId, typeManager.getPrivateMessageAreaType());

    			if (area != null) {
    				if (isMessagesPageInSite() || area.getEnabled().booleanValue() || pvtMessageManager.isInstructor()){
		    			/* TODO: determine if receivedTopicUuid = ""; // is OK? */
		    			PrivateForum pf = pvtMessageManager.initializePrivateMessageArea(area, new ArrayList());
		    			pf = pvtMessageManager.initializationHelper(pf, area);
		    			List pvtTopics = pf.getTopics();
		    			Collections.sort(pvtTopics, PrivateTopicImpl.TITLE_COMPARATOR);   //changed to date comparator

						receivedTopic = (Topic) pvtTopics.iterator().next();
						receivedTopicUuid = receivedTopic.getUuid();
						receivedFolderUuidByContextId.put(contextId, receivedTopicUuid);
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
					pvtTopicMessageUrl = ServerConfigurationService.getPortalUrl() + "/directtool/"
		    					+ mcTool.getId() + "/sakai.messageforums.helper.helper/privateMsg/pvtMsg?pvtMsgTopicId=" 
		    					+ receivedTopicUuid + "&contextId=" + contextId + "&selectedTopic=" + PrivateMessagesTool.PVTMSG_MODE_RECEIVED;
	    			return pvtTopicMessageUrl;
	    		}
	    	}
	    	catch (IdUnusedException e) {
	    		log.error("IdUnusedException attempting to move to Private Messages for a site. Site id used is: " + contextId);
	    	}
	    }

	    return "";
    }

	/**
	 * Pulls excluded site ids from Tabs preferences
	 */
	private List getExcludedSitesFromTabs() {
		final Preferences prefs = preferencesService.getPreferences(
								sessionManager.getCurrentSessionUserId());

		final ResourceProperties props = prefs.getProperties(PreferencesService.SITENAV_PREFS_KEY);
		final List l = props.getPropertyList(TAB_EXCLUDED_SITES);

		return l;		
	}


	/**
	 * Find the object in the list that has this value - return the position.
	 * 
	 * @param value
	 *        The site id to find.
	 * @param siteList
	 *        The list of Site objects.
	 * @return The index position in siteList of the site with site id = value, or -1 if not found.
	 */
	protected int indexOf(String value, List siteList) {
		if (log.isDebugEnabled()) {
			log.debug("indexOf(String " + value + ", List " + siteList + ")");
		}

		for (int i = 0; i < siteList.size(); i++) {
			final String siteId = (String) siteList.get(i);

			if (siteId.equals(value)) {
				return i;
			}
		}
		
		return -1;
	}

	/**
	 * Is a user logged in.
	 * @return <code>true</code> if the current request is being made by a logged in user. 
	 */
	public boolean isLoggedIn() {
		return sessionManager.getCurrentSessionUserId() != null;
	}

}
