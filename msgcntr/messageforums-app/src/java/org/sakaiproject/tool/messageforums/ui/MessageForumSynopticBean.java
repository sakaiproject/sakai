/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/MessageForumsSynopticBean.java $
 * $Id: MessageForumsSynopticBean.java $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class MessageForumSynopticBean {
	
	public class DecoratedCompiledMessageStats {
		private String siteName;
		private String siteId;
		
		/** MyWorkspace information */
		private int unreadPrivateAmt;
		private int unreadForumsAmt;
		private String mcPageURL;
		
		/** site Home page information */
		private int unreadMessages;
		private String heading;

		public String getHeading() {
			return heading;
		}

		public void setHeading(String heading) {
			this.heading = heading;
		}

		/**
		 * 
		 * @return
		 */
		public String getSiteName() {
			return siteName;
		}

		/**
		 * 
		 * @param siteName
		 */
		public void setSiteName(String siteName) {
			this.siteName = siteName;
		}

		/**
		 * 
		 * @return
		 */
		public int getUnreadPrivateAmt() {
			return unreadPrivateAmt;
		}

		/**
		 * 
		 * @param unreadPrivateAmt
		 */
		public void setUnreadPrivateAmt(int unreadPrivateAmt) {
			this.unreadPrivateAmt = unreadPrivateAmt;
		}

		/**
		 * 
		 * @return
		 */
		public int getUnreadForumsAmt() {
			return unreadForumsAmt;
		}

		/**
		 * 
		 * @param unreadForumsAmt
		 */
		public void setUnreadForumsAmt(int unreadForumsAmt) {
			this.unreadForumsAmt = unreadForumsAmt;
		}

		/**
		 * 
		 * @return
		 */
		public String getMcPageURL() {
			return mcPageURL;
		}

		/**
		 * 
		 * @param mcPageURL
		 */
		public void setMcPageURL(String mcPageURL) {
			this.mcPageURL = mcPageURL;
		}

		/**
		 * 
		 * @return
		 */
		public String getSiteId() {
			return siteId;
		}

		/**
		 * 
		 * @param siteId
		 */
		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

		/**
		 * 
		 * @return
		 */
		public int getUnreadMessages() {
			return unreadMessages;
		}

		/**
		 * 
		 * @param unreadMessages
		 */
		public void setUnreadMessages(int unreadMessages) {
			this.unreadMessages = unreadMessages;
		}

	}

/* =========== End of DecoratedCompiledMessageStats =========== */

	/** Used to display 'Private Messages' on tool when in home page of site */
	private final String PRIVATE_HEADING = "syn_private_heading";
	
	/** Use to display 'Discussion Forums' on tool when in home page of site */
	private final String DISCUSSION_HEADING = "syn_discussion_heading";
	
	/** Used to determine if MessageCenter tool part of site */
	private final String MESSAGE_CENTER_ID = "sakai.messagecenter";

	/** Used to get contextId when tool on MyWorkspace to set all private messages to Read status */
	private final String CONTEXTID="contextId";

	// *** Options page bean values ***
	private List notificationSitesItems = new ArrayList();
	private String [] notificationSites;
	private List nonNotificationSitesItems = new ArrayList();
	private String [] nonNotificationSites;
	
	/** Resource loader to grab bundle messages */
	private static ResourceLoader rb = new ResourceLoader("org.sakaiproject.tool.messageforums.bundle.Messages");

	/** to get accces to log file */
	private static final Log LOG = LogFactory.getLog(MessageForumSynopticBean.class);

	/** Needed if within a site so we only need stats for this site */
	private MessageForumsMessageManager messageManager;

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
	
	/**
	 * 
	 * @param messageManager
	 */
	public void setMessageManager(MessageForumsMessageManager messageManager) {
		this.messageManager = messageManager;
	}

	/**
	 * 
	 * @param forumManager
	 */
	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}

	/**
	 * 
	 * @param pvtMessageManager
	 */
	public void setPvtMessageManager(PrivateMessageManager pvtMessageManager) {
		this.pvtMessageManager = pvtMessageManager;
	}

	/**
	 * 
	 * @param typeManager
	 */
	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}

	/**
	 * 
	 * @param areaManager
	 */
	public void setAreaManager(AreaManager areaManager) {
		this.areaManager = areaManager;
	}

	/**
	 * 
	 * @param uiPermissionsManager
	 */
	public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
		this.uiPermissionsManager = uiPermissionsManager;
	}

	/**
	 * Returns TRUE if on MyWorkspace, FALSE if on a specific site
	 * 
	 * @return
	 * 		TRUE if on MyWorkspace, FALSE if on a specific site
	 */
	public boolean isMyWorkspace() {

		// get Site id
		final String siteId = getContext();

		if (SiteService.getUserSiteId("admin").equals(siteId))
			return false;

		final boolean where = SiteService.isUserSite(siteId);

		LOG.debug("Result of determinig if My Workspace: " + where);

		return where;
	}

	/**
	 * Returns TRUE if there is at least one site user can access
	 *  
	 * @return
	 * 			TRUE if there are/is site(s) user can access
	 * 			FALSE if not
	 */
	public boolean isSitesToView() {
		return ! getSiteList().isEmpty();
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
		List resultList = new ArrayList();
		
		// ****** if either list is empty, return currentList unchanged ******
		if (currentList.isEmpty() || removeList.isEmpty()) {
			return currentList;
		}

		// ****** Set up our iterator ******
		final Iterator currentIter = currentList.iterator();
		final Iterator removeIter = removeList.iterator();
		
		// from above, they each have at least one element
		Object [] currentValues = (Object []) currentIter.next();
		Object [] removeValues = (Object []) removeIter.next();
		Object [] resultValues = new Object [2];
		
		while (currentIter.hasNext()) {
			if (((String) currentValues[0]).equals((String) removeValues[0])) {
				resultValues[0] = currentValues[0];
				resultValues[1] = new Integer(((Integer) currentValues[1]).intValue() - ((Integer) removeValues[1]).intValue());
				
				resultList.add(resultValues);
				
				// get ready for next iteration
				if (currentIter.hasNext()) {
					currentValues = (Object []) currentIter.next();
				}
				
				if (removeIter.hasNext()) {
					removeValues = (Object []) removeIter.next();
				}
				else {
					removeValues[0] = "";
				}
			}
		}
		
		return resultList;
	}

	private List getUserRoles(List siteList) {
		List roles = new ArrayList();
		
		final Iterator siteIter = siteList.iterator();
		
		while (siteIter.hasNext()) {
			
			Site curSite = null;
			
			try {
				curSite = getSite((String) siteIter.next());
			}
			catch (IdUnusedException e) {
				// Mucho weirdness, found by getSites() but now cannot find
				LOG.error("IdUnusedException will accessing site to determine user role");
			}
			
			if (curSite != null) {
				final String curRole = AuthzGroupService.getUserRole(SessionManager.getCurrentSessionUserId(), "/site/" + curSite.getId());
			
				if (! roles.contains(curRole) && curRole != null) {
					roles.add(curRole);
				}
			}
		}
		
		return roles;
	}

	private List selectCorrectRemoveMessageCount(List removeMessageCounts, List siteList) {
		if (removeMessageCounts.isEmpty()) {
			return removeMessageCounts;
		}
		
		final List resultList = new ArrayList();
		
		// ***** set up iterators *****
		final Iterator siteIter = siteList.iterator();
		final Iterator rmIter = removeMessageCounts.iterator();
		
		while (siteIter.hasNext()) {
			final Site site = (Site) siteIter.next();
			Object [] resultSet = (Object []) rmIter.next();
			
			// permissions based on roles, so need to check if user's role has messages
			// that need to be removed from totals (either total or unread)
			while (site.getId().equals((String) resultSet[0])) {
				final String curRole = AuthzGroupService.getUserRole(SessionManager.getCurrentSessionUserId(), site.getId());
				
				if (curRole.equals((String) resultSet[1])) {
					resultList.add(resultSet);
				}
				
				if (rmIter.hasNext()) {
					resultSet = (Object []) rmIter.next();

					while (site.getId().equals(resultSet[0])) {
						if (rmIter.hasNext()) {
							resultSet = (Object []) rmIter.next();
						}
						else {
							resultSet[0] = "";
						}
					}
				}
			}
		}
		
		return resultList;
	}
	
	/**
	 * Return List to populate page if in MyWorkspace
	 * 
	 * @return
	 * 		List of DecoratedCompiledMessageStats to populate MyWorkspace page
	 */
	private List getMyWorkspaceContents() {
		final List contents = new ArrayList();
		
		// ============= This section pulls counts from DB ============= 

		// ******* Pulls unread private message counts from DB ******* 
		final List privateMessageCounts = pvtMessageManager
					.getPrivateMessageCountsForAllSites();

		// retrieve what sites is this user a member of
		final List siteList = getSiteList();

		// no sites to work with, just return
		if (siteList.isEmpty()) { return contents; }

		// retrieve what possible roles user could be in sites
		final List roleList = getUserRoles(siteList);
		
		// ******* Pulls discussion forum message counts from DB ******* 
		List discussionForumMessageCounts = messageManager
						.findDiscussionForumMessageCountsForAllSites(siteList);

		List discussionForumRemoveMessageCounts = messageManager
						.findDiscussionForumMessageRemoveCountsForAllSites(siteList, roleList);

		discussionForumRemoveMessageCounts = selectCorrectRemoveMessageCount(discussionForumRemoveMessageCounts, siteList);

		if (! discussionForumRemoveMessageCounts.isEmpty()) {
			discussionForumMessageCounts = filterNoAccessMessages(
											discussionForumMessageCounts,
											discussionForumRemoveMessageCounts);
		}
		
		List unreadDFMessageCounts = new ArrayList();

		if (!discussionForumMessageCounts.isEmpty()) {
			// Pulls read discussion forum message counts from DB
			List discussionForumReadMessageCounts = messageManager
								.findDiscussionForumReadMessageCountsForAllSites();

			if (! discussionForumReadMessageCounts.isEmpty()) {
				List discussionForumRemoveReadMessageCounts = messageManager
									.findDiscussionForumReadMessageRemoveCountsForAllSites(getUserRoles(siteList));

				discussionForumRemoveReadMessageCounts = selectCorrectRemoveMessageCount(discussionForumRemoveReadMessageCounts, siteList);

				discussionForumReadMessageCounts = filterNoAccessMessages(
													discussionForumReadMessageCounts,
													discussionForumRemoveReadMessageCounts);
			}

			if (! discussionForumReadMessageCounts.isEmpty()) {
				unreadDFMessageCounts = computeUnreadDFMessages(
											discussionForumMessageCounts,
											discussionForumReadMessageCounts);
			} 
			else {
				unreadDFMessageCounts = discussionForumMessageCounts;
			}
		}

		// If both are empty, no unread messages so just return.
		if (privateMessageCounts.isEmpty()
				&& discussionForumMessageCounts.isEmpty()) {
			return contents;
		}

		//============= At least some unread messages so process =============

		// ************ Set up private message counts for processing ************
		final Iterator pmi = privateMessageCounts.iterator();
		Object[] pmCounts;

		// May be empty. if so, create dummy private message site id for comparison
		// when compiling stats
		if (pmi.hasNext()) {
			pmCounts = (Object[]) pmi.next();
		} 
		else {
			pmCounts = new Object[1];
			pmCounts[0] = "";
		}

		// ************ Set up discussion forums for processing ************ 
		final Iterator urmci = unreadDFMessageCounts.iterator();
		Object[] unreadDFCount;

		// May be empty. if so, create dummy discussion forum site id for comparsion
		// when compiling stats
		if (urmci.hasNext()) {
			unreadDFCount = (Object[]) urmci.next();
		} 
		else {
			unreadDFCount = new Object[1];
			unreadDFCount[0] = "";
		}

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
				// find it. Log and skip
				LOG.error("IdUnusedException attempting to access site " + siteId);
				continue;
			}

			// ************ Each row on page gets info stored in DecoratedCompiledMessageStats bean ************ 
			final DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

			// fill site title
			dcms.setSiteName(site.getTitle());
			dcms.setSiteId(site.getId());

			// Put check here because if not in site, skip
			if (isMessageForumsPageInSite(site)) {

				// ************ checking for unread private messages for this site ************  
				if (siteId.equals(pmCounts[0])) {
					dcms.setUnreadPrivateAmt(((Integer) pmCounts[1]).intValue());

					hasPrivate = true;
				}
				else {
					// check if there are actually no unread messages or if
					// not enabled
					final Area area = areaManager
										.getAreaByContextIdAndTypeId(site.getId(), 
													typeManager.getPrivateMessageAreaType());
					if (area != null) {
						if (area.getEnabled().booleanValue()) {
							dcms.setUnreadPrivateAmt(0);
							hasPrivate = true;
						}
					}
					
					// should I skip to next pmCount?
					// TODO: check on possibly unread messages on unpublished site?
					if (siteId.compareTo((String) pmCounts[0]) >= 0) {
						if (pmi.hasNext()) {
							pmCounts = (Object[]) pmi.next();
						} 
						else {
							pmCounts[0] = "";
						}
					}
				}

				// ************ check for unread discussion forum messages on this site ************
				// TODO: Filter out topics/forums they don't have access to
				if (siteId.equals(unreadDFCount[0])) {
					dcms.setUnreadForumsAmt(((Integer) unreadDFCount[1]).intValue());

					hasDF = true;
				} 
				else {
					if (areaManager.getDiscusionArea().getEnabled().booleanValue()) {
						dcms.setUnreadForumsAmt(0);
						hasDF = true;
					}
					
					// should I skip to next unreadDFCount?
					// possibly unread forum msgs on unpublished site?
					if (siteId.compareTo((String) unreadDFCount[0]) >= 0) {
						if (urmci.hasNext()) {
							unreadDFCount = (Object []) urmci.next();
						}
						else {
							unreadDFCount[0] = "";
						}
					}
				}

				// ************ get the page URL for Message Center************
				// only if unread messages, ie, only if row will appear on page 
				if (hasPrivate || hasDF) {
					dcms.setMcPageURL(getMCPageURL(site.getId()));

					contents.add(dcms);
				}
			}
		}
		
		return contents;
	}

	/**
	 * Returns List to populate page if on Home page of a site
	 * 
	 * @return
	 * 		List of DecoratedCompiledMessageStats for a particular site
	 */
	private List getSiteContents() {
		List contents = new ArrayList();
		
		// Check if tool within site
		// if so, get stats for just this site
		if (isMessageForumsPageInSite()) {
			int unreadPrivate = 0;

			DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

			dcms.setSiteName(getSiteName());

			// Get private message area so we can get the
			// private messasge forum so we can get the
			// List of topics so we can get the Received topic
			// to finally determine number of unread messages
			final Area area = pvtMessageManager.getPrivateMessageArea();
			
			if (pvtMessageManager.getPrivateMessageArea().getEnabled().booleanValue()) {
				PrivateForum pf = pvtMessageManager.initializePrivateMessageArea(area);
				
				unreadPrivate = pvtMessageManager.findUnreadMessageCount(
									typeManager.getReceivedPrivateMessageType());

				dcms.setUnreadMessages(unreadPrivate);
				dcms.setUnreadPrivateAmt(unreadPrivate);
			}
			else {
				dcms.setUnreadMessages(0);
			}

			dcms.setHeading(rb.getString(PRIVATE_HEADING));

			contents.add(dcms);
			
			dcms = new DecoratedCompiledMessageStats();

			// Number of unread forum messages is a little harder
			// need to loop through all topics and add them up
			final List topicsList = forumManager.getDiscussionForums();
			int unreadForum = 0;

			final Iterator forumIter = topicsList.iterator();

			while (forumIter.hasNext()) {
				final DiscussionForum df = (DiscussionForum) forumIter.next();

				final List topics = df.getTopics();
				final Iterator topicIter = topics.iterator();

				while (topicIter.hasNext()) {
					final Topic topic = (Topic) topicIter.next();
					
					if (uiPermissionsManager.isRead((DiscussionTopic) topic, df)) {
						// TODO: Does user have permission to read this topic?
						unreadForum += messageManager.findUnreadMessageCountByTopicId(topic.getId());
					
					}
				}
			}
			dcms.setUnreadMessages(unreadForum);
			dcms.setHeading(rb.getString(DISCUSSION_HEADING));
			dcms.setMcPageURL(getMCPageURL());

			contents.add(dcms);
		}
		else {
			// TODO: what to put on page? Alert? Leave Blank?
		}
		
		return contents;
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
			return getMyWorkspaceContents();
		}
		else {
			return getSiteContents();
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
			LOG.error("IdUnusedException when trying to access site "
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
			LOG.error("IdUnusedException when trying to access site "
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
		List unreadDFMessageCounts = new ArrayList();

		// Constructs the unread message counts from above 4 lists
		final Iterator dfMessagesIter = totalMessages.iterator();
		final Iterator dfReadMessagesIter = readMessages.iterator();
		
		Object [] dfNoAccMessageCountForASite;
		Object [] dfNoAccReadMessageCountForASite;

		Object [] dfReadMessageCountForASite = (Object []) dfReadMessagesIter.next();

		// NOTE: dfMessagesIter.count >= dfReadMessagesIter, so use
		// dfMessagesIter for compilation loop
		while (dfMessagesIter.hasNext()) {
			final Object [] dfMessageCountForASite = (Object[]) dfMessagesIter.next();
			
			final Object[] siteDFInfo = new Object[2];

			siteDFInfo[0] = (String) dfMessageCountForASite[0];

			if (((String) dfMessageCountForASite[0])
							.equals((String) dfReadMessageCountForASite[0])) {
				siteDFInfo[1] = new Integer(((Integer) dfMessageCountForASite[1]).intValue()
												- ((Integer) dfReadMessageCountForASite[1]).intValue());

				if (dfReadMessagesIter.hasNext()) {
					dfReadMessageCountForASite = (Object[]) dfReadMessagesIter.next();
				}
			} 
			else {
				// No messages read for this site so message count = unread message count
				siteDFInfo[1] = (Integer) dfMessageCountForASite[1];
			}

			unreadDFMessageCounts.add(siteDFInfo);
		}

		return unreadDFMessageCounts;
	}

	/**
	 * Change display options for synoptic Message Center screen
	 * 
	 * @return String to handle navigation
	 */
	public String processOptionsChange() {
		return "synMain";
	}

	/**
	 * Cancel changes to display settings for synoptic Message Center screen
	 * 
	 * @return String to return to main page
	 */
	public String processOptionsCancel() {
		return "synMain";
	}

	/**
	 * Returns TRUE if Message Forums (Message Center) exists in this site,
	 * FALSE otherwise Called if tool placed on home page of a site
	 * 
	 * @return TRUE if Message Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	public boolean isMessageForumsPageInSite() {
		boolean mfToolExists = false;

		try {
			final Site thisSite = getSite(getContext());

			mfToolExists = isMessageForumsPageInSite(thisSite);

		} catch (IdUnusedException e) {
			LOG.error("IdUnusedException while trying to check if site has MF tool.");
		}

		return mfToolExists;
	}

	/**
	 * Returns TRUE if Message Forums (Message Center) exists in this site,
	 * FALSE otherwise Called if tool placed on My Workspace
	 * 
	 * @return TRUE if Message Forums (Message Center) exists in this site,
	 *         FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(Site thisSite) {
		Collection toolsInSite = thisSite.getTools(MESSAGE_CENTER_ID);

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
	 * Returns the URL for the page the Message Center tool is on.
	 * 
	 * @return String A URL so the user can click to go to Message Center.
	 *         Needed since tool could possibly by in MyWorkspace
	 */
	private String getMCPageURL(String siteId) {
		try {
			Collection toolsInSite = getSite(siteId).getTools(MESSAGE_CENTER_ID);
			ToolConfiguration mcTool;

			if (!toolsInSite.isEmpty()) {
				Iterator iter = toolsInSite.iterator();
				mcTool = (ToolConfiguration) iter.next();

				SitePage pgelement = mcTool.getContainingPage();

				return pgelement.getUrl();
			}

		}
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException while trying to check if site has MF tool.");

			// TODO: What do we do?
		}

		return "";

	}

	/**
	 * This marks all Private messages as read
	 * 
	 * @param ActionEvent e
	 */
	public void processReadAll(ActionEvent e) {
		final String typeUuid = typeManager.getReceivedPrivateMessageType();

		if (isMyWorkspace()) {
			// if within MyWorkspace, need to find the siteId
			FacesContext context = FacesContext.getCurrentInstance();
			Map requestParams = context.getExternalContext()
										 .getRequestParameterMap();

			final String contextId = (String) requestParams.get(CONTEXTID);

			final List privateMessages = pvtMessageManager
					.getMessagesByTypeByContext(typeUuid, contextId);

			if (privateMessages == null) {
				LOG.error("No messages found while attempting to mark all as read "
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
				LOG.error("No messages found while attempting to mark all as read "
								+ "from synoptic Message Center tool.");
			} 
			else {
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
		return SiteService.getSite(siteId);
	}
	
	/**
	 * Returns current context
	 * 
	 * @return
	 * 		String The site id (context) where tool currently located
	 */
	private String getContext() {
		return ToolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * 
	 * @return
	 * 		List A List of site ids that is published and the user is a member of
	 */
	public List getSiteList() {
		List mySites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
				null,null,null,org.sakaiproject.site.api.SiteService.SortType.ID_ASC,
				null);

		Iterator lsi = mySites.iterator();

		if (!lsi.hasNext()) {
			// TODO: Add user id to log message
			LOG.warn("User " + SessionManager.getCurrentSession().getUserId() + " does not belong to any sites.");

			return mySites;
		}

		final List siteList = new ArrayList();

		// only display sites that are published
		while (lsi.hasNext()) {
			Site site = (Site) lsi.next();

			// filter out unpublished and site w/ no messsage center
			if (site.isPublished() && isMessageForumsPageInSite(site)) {
				siteList.add(site.getId());
			}
		}

		return siteList;
	}

// ======================== Options Page Methods ======================== 

	public List getNonNotificationSitesItems() {
		return nonNotificationSitesItems;
	}

	public void setNonNotificationSitesItems(List nonNotificationSites) {
		this.nonNotificationSitesItems = nonNotificationSites;
	}

	public String[] getNonNotificationSites() {
		return nonNotificationSites;
	}

	public void setNonNotificationSites(String[] nonNotificationSites) {
		this.nonNotificationSites = nonNotificationSites;
	}

	public List getNotificationSitesItems() {
		return notificationSitesItems;
	}

	public void setNotificationSitesItems(List notificationSitesItems) {
		this.notificationSitesItems = notificationSitesItems;
	}

	public String[] getNotificationSites() {
		return notificationSites;
	}

	public void setNotificationSites(String[] notificationSites) {
		this.notificationSites = notificationSites;
	}

	/**
	 * Loads the List of sites to display info and those to not
	 * before moving to Options page.
	 * 
	 * @return
	 * 		String to move to Option page
	 */
	public String processGotoOptions() {
		List sites = getSiteList();
		notificationSitesItems.clear();
		nonNotificationSitesItems.clear();
		
		// TODO: get excluded sites, if any

		// Now convert to SelectItem for display in JSF
		for (Iterator iter = sites.iterator(); iter.hasNext();)
		{
			Site element = null;
			
			try {
				element = getSite((String) iter.next());
			}
			catch (IdUnusedException e) {
				// Weirdness, pulled by SiteService as a valid site but when try
				// to retrieve throws exception
				LOG.error("IdUnusedException while trying to load site for Message Center Notifications Options page.");
				
				// skip this invalid site
				continue;
			}
			
			SelectItem excludeItem = new SelectItem(element.getId(), element.getTitle());

			notificationSitesItems.add(excludeItem);
		}
		
		return "synOptions";
	}
	
	/**
	 * Move site(s) from getting displaying unread info to not
	 * 
	 * @return
	 * 		String to stay on Options page
	 */
	public String processActionRemove() {
		String[] values = getNotificationSites();
		
		if (values.length < 1)
		{
			// TODO: Error message for when 
			// FacesContext.getCurrentInstance().addMessage(null,
			//		new FacesMessage("Please select a site to remove from Sites Visible in Tabs."));
			//return "tab";
		}

		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];
			getNonNotificationSitesItems().add(removeItems(value, getNotificationSitesItems()));
		}
		
		return "synOption";
	}

	/**
	 * Move site(s) from not displaying unread info to displaying
	 * 
	 * @return
	 * 		String to stay on Options page
	 */
	public String processActionAdd() {
		String[] values = getNonNotificationSites();
		
		if (values.length < 1)
		{
			// TODO: Error message for when 
			// FacesContext.getCurrentInstance().addMessage(null,
			//		new FacesMessage("Please select a site to remove from Sites Visible in Tabs."));
			//return "tab";
		}

		for (int i = 0; i < values.length; i++)
		{
			String value = values[i];
			getNotificationSitesItems().add(removeItems(value, getNonNotificationSitesItems()));
		}
		
		return "synOption";
	}

	/**
	 * Used by processActionAdd() and processActionRemove() to move site
	 * from one list to the other
	 * 
	 * @param value
	 * 			The site to be moved
	 * 
	 * @param items
	 * 			Where the site currently lives
	 * 
	 * @return
	 * 		The SelectItem removed from items
	 */
	private SelectItem removeItems(String value, List items)
	{
		if (LOG.isDebugEnabled())
		{
			LOG.debug("removeItems(String " + value + ", List " + items + ")");
		}

		SelectItem result = null;
		for (int i = 0; i < items.size(); i++)
		{
			SelectItem item = (SelectItem) items.get(i);
			if (value.equals(item.getValue()))
			{
				result = (SelectItem) items.remove(i);
				break;
			}
		}
		return result;
	}

	/**
	 * Process Add All action
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionAddAll()
	{
		LOG.debug("processActionAddAll()");

		getNotificationSitesItems().addAll(getNonNotificationSitesItems());
		getNonNotificationSitesItems().clear();

		return "synOptions";
	}

	/**
	 * Process Add All action
	 * 
	 * @return navigation output to tab customization page (edit)
	 */
	public String processActionRemoveAll()
	{
		LOG.debug("processActionAddAll()");

		getNonNotificationSitesItems().addAll(getNotificationSitesItems());
		getNotificationSitesItems().clear();

		return "synOptions";
	}

}
