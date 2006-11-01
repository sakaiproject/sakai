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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class MessageForumSynopticBean {
	
	public class DecoratedCompiledMessageStats {
		private String siteName;
		private String siteId;
		private String unreadPrivate;
		private int unreadPrivateAmt;
		private String unreadForums;
		private String privateMessagesURL;

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
		public String getUnreadPrivate() {
			return unreadPrivate;
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
		 * @param unreadPrivate
		 */
		public void setUnreadPrivate(String unreadPrivate) {
			this.unreadPrivate = unreadPrivate;
		}

		/**
		 * 
		 * @return
		 */
		public String getUnreadForums() {
			return unreadForums;
		}

		/**
		 * 
		 * @param unreadForums
		 */
		public void setUnreadForums(String unreadForums) {
			this.unreadForums = unreadForums;
		}

		/**
		 * 
		 * @return
		 */
		public String getPrivateMessagesURL() {
			return privateMessagesURL;
		}

		/**
		 * 
		 * @param privateMessagesURL
		 */
		public void setPrivateMessagesURL(String privateMessagesURL) {
			this.privateMessagesURL = privateMessagesURL;
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

	}

/* =========== End of DecoratedCompiledMessageStats =========== */

	/** Used to determine if Message Center tool part of a site */
	private final String MF_TITLE = "Message Center";
	
	/** Used to determine if MessageCenter tool part of site */
	private final String MESSAGE_CENTER_ID = "sakai.messagecenter";

	/** Used to get contextId when tool on MyWorkspace to set all private messages to Read status */
	private final String CONTEXTID="contextId";

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("messageforums");

	/** to get accces to log file */
	private static final Log LOG = LogFactory
			.getLog(MessageForumSynopticBean.class);

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
	 * Returns TRUE if on MyWorkspace, FALSE if on a specific site
	 * 
	 * @return
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
	 * Returns List of decoratedCompiledMessageStats
	 * 
	 * @return List of decoratedCompiledMessageStats
	 */
	public List getContents() {
		final List contents = new ArrayList();

		if (isMyWorkspace()) {
			// Get stats for "all" sites this user is a member of

			// Pulls unread private message counts from DB
			List privateMessageCounts = pvtMessageManager
						.getPrivateMessageCountsForAllSites();

			// get the sites the user has access to
			// TODO: change to grab all sites user has membership in no matter
			// what type (?)
			List mySites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS,
								null,null,null,org.sakaiproject.site.api.SiteService.SortType.ID_ASC,
								null);

			Iterator lsi = mySites.iterator();
			
			if (!lsi.hasNext()) {
				// TODO: Add user id to log message
				LOG.warn("User " + SessionManager.getCurrentSession().getUserId() + " does not belong to any sites.");

				return contents;
			}

			final List siteList = new ArrayList();

			// needed to filter out discussion forum messages to just those
			// for sites this use is a part of
			while (lsi.hasNext()) {
				Site site = (Site) lsi.next();

				siteList.add(site.getId());
			}

			// Pulls discussion forum message counts from DB
			final List discussionForumMessageCounts = messageManager
							.findDiscussionForumMessageCountsForAllSites(siteList);

			List unreadDFMessageCounts = new ArrayList();

			if (!discussionForumMessageCounts.isEmpty()) {
				// Pulls read discussion forum message counts from DB
				final List discussionForumReadMessageCounts = messageManager
									.findDiscussionForumReadMessageCountsForAllSites();

				if (!discussionForumReadMessageCounts.isEmpty()) {
					unreadDFMessageCounts = computeUnreadDFMessages(
												discussionForumMessageCounts,
												discussionForumReadMessageCounts);
				} 
				else {
					unreadDFMessageCounts = discussionForumMessageCounts;
				}
			}

			// If both are empty, just return.
			if (privateMessageCounts.isEmpty()
					&& discussionForumMessageCounts.isEmpty()) {
				return contents;
			}

			// Set up to look through all info to compile decorated bean
			final Iterator pmi = privateMessageCounts.iterator();
			Object[] pmCounts;

			if (pmi.hasNext()) {
				pmCounts = (Object[]) pmi.next();

			} 
			else {
				// Since empty, create dummy private message site id for comparison
				// when compiling stats
				pmCounts = new Object[1];
				pmCounts[0] = "";
			}

			final Iterator urmci = unreadDFMessageCounts.iterator();
			Object[] unreadDFCount;

			if (urmci.hasNext()) {
				unreadDFCount = (Object[]) urmci.next();

			} 
			else {
				// create dummy discussion forum site id for comparsion
				// when compiling stats
				unreadDFCount = new Object[1];
				unreadDFCount[0] = "";
			}

			// loop through info to fill decorated bean
			for (Iterator si = mySites.iterator(); si.hasNext();) {
				boolean hasPrivate = false;
				boolean hasDF = false;

				final Site site = (Site) si.next();
				final DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

				// fill site title
				dcms.setSiteName(site.getTitle());
				dcms.setSiteId(site.getId());

				if (site.getId().equals(pmCounts[0])) {
					// info from db matches
					// fill unread private messages
					dcms.setUnreadPrivate(pmCounts[2] + " Private");

					dcms.setUnreadPrivateAmt(((Integer) pmCounts[2])
									.intValue());

					if (pmi.hasNext()) {
						pmCounts = (Object[]) pmi.next();

					} 
					else {
						pmCounts[0] = "";
					}

					hasPrivate = true;
				} 
				else {
					// enabled but all messages read
					if (isMessageForumsPageInSite(site)) {
						final Area area = areaManager
											.getAreaByContextIdAndTypeId(site.getId(), 
												typeManager.getPrivateMessageAreaType());

						if (area != null) {
							if (area.getEnabled().booleanValue()) {
								dcms.setUnreadPrivate("0 Private");
								hasPrivate = true;
							}
						}
					}
				}

				// fill unread discussion forum messages
				if (site.getId().equals(unreadDFCount[0])) {
					dcms.setUnreadForums(unreadDFCount[1] + " Forum");

					if (urmci.hasNext()) {
						unreadDFCount = (Object[]) urmci.next();
					}

					hasDF = true;
				} 
				else {
					// Might be there but all messages read (or no messages at all)
					if (isMessageForumsPageInSite(site)) {
						if (areaManager.getDiscusionArea().getEnabled().booleanValue()) {
							dcms.setUnreadForums("0 Forum");
							hasDF = true;
						}
					}
				}

				if (hasPrivate || hasDF) {
					// get the page URL for Message Center
					dcms.setPrivateMessagesURL(getMCPageURL(site.getId()));

					contents.add(dcms);
				}
			}

		} 
		else {
			// Check if tool within site
			// if so, get stats for just this site
			if (isMessageForumsPageInSite()) {
				int unreadPrivate = 0;

				final DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

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

					dcms.setUnreadPrivate(unreadPrivate + " Private");

					dcms.setUnreadPrivateAmt(unreadPrivate);
				}

				// Number of unread forum messages is a little harder
				// need to loop through all topics and add them up
				final List topicsList = forumManager.getDiscussionForums();
				long unreadForum = 0;

				final Iterator forumIter = topicsList.iterator();

				while (forumIter.hasNext()) {
					final DiscussionForum df = (DiscussionForum) forumIter.next();

					final List topics = df.getTopics();
					final Iterator topicIter = topics.iterator();

					while (topicIter.hasNext()) {
						final Topic topic = (Topic) topicIter.next();

						unreadForum += messageManager.findUnreadMessageCountByTopicId(
														topic.getId());

					}
				}
				dcms.setUnreadForums(unreadForum + " Forum");

				dcms.setPrivateMessagesURL(getMCPageURL());

				contents.add(dcms);
			}
			else {
				// TODO: what to put on page? Alert? Leave Blank?
			}
		}

		return contents;
	}

	/**
	 * Retrieve the site display title
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
	 * 
	 * @return The id for current site
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
	 * 
	 * @param totalMessages
	 * @param readMessages
	 * @return
	 */
	private List computeUnreadDFMessages(List totalMessages, List readMessages) {
		List unreadDFMessageCounts = new ArrayList();

		// Constructs the unread message counts from above 2 lists
		final Iterator dfMessagesIter = totalMessages.iterator();
		final Iterator dfReadMessagesIter = readMessages.iterator();

		Object[] dfReadMessageCountForASite = (Object[]) dfReadMessagesIter.next();

		// NOTE: dfMessagesIter.count >= dfReadMessagesIter, so use
		// dfMessagesIter for compilation loop
		while (dfMessagesIter.hasNext()) {
			final Object[] dfMessageCountForASite = (Object[]) dfMessagesIter.next();

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
	private boolean isMessageForumsPageInSite() {
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
	 * 
	 * @param siteId
	 * @return
	 */
	private Site getSite(String siteId) 
		throws IdUnusedException {
		return SiteService.getSite(siteId);
	}
	
	/**
	 * 
	 * @return
	 * 		String The site id (context) where tool currently located
	 */
	private String getContext() {
		return ToolManager.getCurrentPlacement().getContext();
	}
}
