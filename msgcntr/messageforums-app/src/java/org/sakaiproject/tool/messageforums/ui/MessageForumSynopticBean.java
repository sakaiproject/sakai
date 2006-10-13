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
import org.sakaiproject.site.cover.SiteService;
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

		public String getSiteId() {
			return siteId;
		}

		public void setSiteId(String siteId) {
			this.siteId = siteId;
		}

	}

	/** Used to determine if Message Center tool part of a site */
	private final String MF_TITLE = "Message Center";

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

	/** Needed to get Uuids for private messages and discussions */
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
	 * Returns TRUE if on specific site, FALSE if on MyWorkspace
	 * 
	 * @return
	 */
	public boolean isMyWorkspace() {

		// get Site id
		String siteId = ToolManager.getCurrentPlacement().getContext();

		if (SiteService.getUserSiteId("admin").equals(siteId))
			return false;

		final boolean where = SiteService.isUserSite(siteId);

		LOG.debug("Result of determinig if My Workspace: " + where);

		return where;
	}

	/**
	 * Returns List of decoratedCompiledMessageStats
	 * 
	 * @return 
	 * 			List of decoratedCompiledMessageStats
	 */
	public List getContents() {
		List contents = new ArrayList();

		// TODO: Actually generate the list of messages
		if (isMyWorkspace()) {
			// Get stats for "all" sites this user is a member of

			// Pulls unread private message counts from DB
			List privateMessageCounts = pvtMessageManager
					.getPrivateMessageCountsForAllSites();

			// get the sites the user has access to
			// TODO: change to grab all sites user has membership in no matter what type
			List mySites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null, null,
					org.sakaiproject.site.api.SiteService.SortType.ID_ASC, null);

			Iterator lsi = mySites.iterator();
			if (!lsi.hasNext()) {
				// TODO: Add user id to log message
				LOG.warn("User does not belong to any sites.");
				return contents;
			}

			List siteList = new ArrayList();

			// needed to filter out discussion forum messages to just those
			// for sites this use is a part of
			while (lsi.hasNext()) {
				Site site = (Site) lsi.next();

				siteList.add(site.getId());
			}

			// Pulls discussion forum message counts from DB
			final List discussionForumMessageCounts = messageManager
					.findDiscussionForumMessageCountsForAllSites(siteList);

			// Pulls read discussion forum message counts from DB
			final List discussionForumReadMessageCounts = messageManager
					.findDiscussionForumReadMessageCountsForAllSites();

			List unreadDFMessageCounts = computeUnreadDFMessages(discussionForumMessageCounts, discussionForumReadMessageCounts);

			//	2. construct a List of decoratedCompiledMessageStats			

			// Set up to look through all info to compile decorated bean
			Iterator pmi = privateMessageCounts.iterator();
			Object[] pmCounts;
			
			if (pmi.hasNext()) {
				pmCounts = (Object[]) pmi.next();
			
			}
			else {
				// create dummy private message amt for comparison
				pmCounts = new Object [1];
				pmCounts[0] = "";
			}

			Iterator urmci = unreadDFMessageCounts.iterator();
			Object[] unreadDFCount = (Object[]) urmci.next();

			// loop through info to fill decorated bean
			for (Iterator si = mySites.iterator(); si.hasNext();) {
				boolean hasPrivate = false;
				boolean hasDF = false;
				
				Site site = (Site) si.next();
				DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

				// fill site title
				dcms.setSiteName(site.getTitle());
				dcms.setSiteId(site.getId());

				if (site.getId().equals(pmCounts[0])) {
					// info from db matches 
					// fill unread private messages
					dcms.setUnreadPrivate(pmCounts[2] + " Private");
					
					dcms.setUnreadPrivateAmt(((Integer) pmCounts[2]).intValue());

					if (pmi.hasNext()) {
						pmCounts = (Object[]) pmi.next();

					}
					else {
						pmCounts[0] = "";
					}
					
					hasPrivate = true;
				}
				else {
					Area area = areaManager.getAreaByContextIdAndTypeId(site.getId(), typeManager.getPrivateMessageAreaType());
					
					if (area != null) {
						if (area.getEnabled().booleanValue()){
							dcms.setUnreadPrivate("0 Private");
							hasPrivate = true;
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
					Area area = areaManager.getDiscusionArea();
					
					if (area.getEnabled().booleanValue()) {
						dcms.setUnreadForums("0 Forum");
						hasDF = true;
					}
				}

				// get the page URL for Message Center
				dcms.setPrivateMessagesURL(getMCPageURL(site.getId()));

				if (hasPrivate || hasDF) {
					contents.add(dcms);
				
				}
			}

		}
		else {
			// Tool within site, get stats for just this site

			if (isMessageForumsPageInSite()) {
				int unreadPrivate = 0;

				DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();

				dcms.setSiteName(getSiteName());

				// Get private message area so we can get the
				// private messasge forum so we can get the
				// List of topics so we can get the Received topic
				// to finally determine number of unread messages
				final Area area = pvtMessageManager.getPrivateMessageArea();
				PrivateForum pf = pvtMessageManager
						.initializePrivateMessageArea(area);
				final List pt = pf.getTopics();
				final Topic privateTopic = (Topic) pt.iterator().next();

				String typeUuid = typeManager.getReceivedPrivateMessageType();

				unreadPrivate = pvtMessageManager
						.findUnreadMessageCount(typeUuid);

				dcms.setUnreadPrivate(unreadPrivate + " Private");
				
				dcms.setUnreadPrivateAmt(unreadPrivate);

				// Number of unread forum messages is a little harder
				// need to loop through all topics and add them up
				// TODO: Construct single query to get sum
				List topicsList = forumManager.getDiscussionForums();
				long unreadForum = 0;

				final Iterator forumIter = topicsList.iterator();

				while (forumIter.hasNext()) {
					final DiscussionForum df = (DiscussionForum) forumIter
							.next();

					final List topics = df.getTopics();
					Iterator topicIter = topics.iterator();

					while (topicIter.hasNext()) {
						final Topic topic = (Topic) topicIter.next();

						unreadForum += messageManager
								.findUnreadMessageCountByTopicId(topic.getId());

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
	 * Retrieve the site id
	 */
	private String getSiteName() {
		try {
			return SiteService.getSite(
					ToolManager.getCurrentPlacement().getContext()).getTitle();
		} catch (IdUnusedException e) {
			LOG.error("IdUnusedException when trying to access site "
					+ e.getMessage());
		}

		return null;
	}

	/**
	 * 
	 * @return
	 * 		The id for current site
	 */
	private String getSiteId() {
		try {
			return SiteService.getSite(
					ToolManager.getCurrentPlacement().getContext()).getId();

		} catch (IdUnusedException e) {
			LOG.error("IdUnusedException when trying to access site "
					+ e.getMessage());
		}

		return null;
	}

	private List computeUnreadDFMessages(List totalMessages, List readMessages) {
		List unreadDFMessageCounts = new ArrayList();

		// Constructs the unread message counts from above 2 lists
		final Iterator dfMessagesIter = totalMessages.iterator();
		final Iterator dfReadMessagesIter = readMessages.iterator();

		Object[] dfReadMessageCountForASite = (Object[]) dfReadMessagesIter
				.next();

		// NOTE: dfMessagesIter.count >= dfReadMessagesIter, so use dfMessagesIter for compilation loop
		while (dfMessagesIter.hasNext()) {
			Object[] dfMessageCountForASite = (Object[]) dfMessagesIter
					.next();

			Object[] siteDFInfo = new Object[2];

			siteDFInfo[0] = (String) dfMessageCountForASite[0];

			if (((String) dfMessageCountForASite[0])
					.equals((String) dfReadMessageCountForASite[0])) {
				siteDFInfo[1] = new Integer(
						((Integer) dfMessageCountForASite[1]).intValue()
								- ((Integer) dfReadMessageCountForASite[1])
										.intValue());

				if (dfReadMessagesIter.hasNext()) {
					dfReadMessageCountForASite = (Object[]) dfReadMessagesIter
							.next();

				}
			} else {
				siteDFInfo[1] = (Integer) dfMessageCountForASite[1];
			}

			unreadDFMessageCounts.add(siteDFInfo);
		}
		
		return unreadDFMessageCounts;
	}
	
	/**
	 * Change display options for synoptic Message Center screen
	 * 
	 * @return
	 * 			String to handle navigation
	 */
	public String processOptionsChange() {
		return "synMain";
	}

	/**
	 * Cancel changes to display settings for synoptic Message Center screen
	 * 
	 * @return
	 * 			String to return to main page
	 */
	public String processOptionsCancel() {
		return "synMain";
	}

	/**
	 * Returns TRUE if Message Forums (Message Center) exists in this site, FALSE otherwise
	 * Called if tool placed on home page of a site
	 * 
	 * @return
	 * 		TRUE if Message Forums (Message Center) exists in this site, FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite() {
		boolean mfToolExists = false;

		try {
			Site thisSite = SiteService.getSite(ToolManager
					.getCurrentPlacement().getContext());

			mfToolExists = isMessageForumsPageInSite(thisSite);

		} catch (IdUnusedException e) {
			LOG
					.error("No Site found while trying to check if site has MF tool.");
		}

		return mfToolExists;
	}

	/**
	 * Returns TRUE if Message Forums (Message Center) exists in this site, FALSE otherwise
	 * Called if tool placed on My Workspace
	 * 
	 * @return
	 * 		TRUE if Message Forums (Message Center) exists in this site, FALSE otherwise
	 */
	private boolean isMessageForumsPageInSite(Site thisSite) {
		boolean mfToolExists = false;

		// loop thru tools on this site looking for
		// Message Center tool
		List pageList = thisSite.getPages();

		Iterator iterator = pageList.iterator();
		while (iterator.hasNext()) {
			SitePage pgelement = (SitePage) iterator.next();
			if (pgelement.getTitle().equals(MF_TITLE)) {
				mfToolExists = true;
				break;
			}
		}

		return mfToolExists;
	}

	private String getMCPageURL() {
		return getMCPageURL(ToolManager.getCurrentPlacement().getContext());
	}

	private String getMCPageURL(String siteId) {
		try {
			// loop thru tools on this site looking for
			// Message Center tool
			Site thisSite = SiteService.getSite(siteId);
			List pageList = thisSite.getPages();

			Iterator iterator = pageList.iterator();
			while (iterator.hasNext()) {
				SitePage pgelement = (SitePage) iterator.next();
				if (pgelement.getTitle().equals(MF_TITLE)) {
					return pgelement.getUrl();
				}
			}
		} catch (IdUnusedException e) {
			LOG
					.error("No Site found while trying to check if site has MF tool.");

			// TODO: if we are in My Workspace, do we go here?
		}

		return "";

	}

	/**
	 * This marks all Private messages as read
	 */
	public void processReadAll(ActionEvent e) {
		
		String typeUuid = typeManager.getReceivedPrivateMessageType();        
	      
		if (isMyWorkspace()) {
			// if within MyWorkspace, need to find the siteId
			FacesContext context = FacesContext.getCurrentInstance();
			Map requestParams = context.getExternalContext()
					.getRequestParameterMap();

			final String contextId = (String) requestParams.get(CONTEXTID);

			List privateMessages = pvtMessageManager.getMessagesByTypeByContext(typeUuid, contextId);

			if (privateMessages == null) {
				LOG.error("No messages found while attempting to mark all as read from synoptic Message Center tool.");
			}
			else {
				for (Iterator iter = privateMessages.iterator(); iter.hasNext();) {
					pvtMessageManager.markMessageAsReadForUser((PrivateMessage) iter.next(), contextId);
		    	  
		      }
			
			}
		}
		else {
			// Get the site id and user id and call query to 
			// mark them all as read
		    List privateMessages = pvtMessageManager.getMessagesByType(typeUuid, PrivateMessageManager.SORT_COLUMN_DATE,
		          PrivateMessageManager.SORT_DESC);
			
			if (privateMessages == null) {
				LOG.error("No messages found while attempting to mark all as read from synoptic Message Center tool.");
			}
			else {
				for (Iterator iter = privateMessages.iterator(); iter.hasNext();) {
					pvtMessageManager.markMessageAsReadForUser((PrivateMessage) iter.next());
		    	  
		      }
			
			}
		}

	}
}
