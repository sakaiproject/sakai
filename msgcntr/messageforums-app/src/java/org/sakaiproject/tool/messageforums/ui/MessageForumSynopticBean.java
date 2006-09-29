package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;


public class MessageForumSynopticBean {
	
	public class DecoratedCompiledMessageStats {
		private String siteName;
		private String unreadPrivate;
		private String unreadForums;
		
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

		
	}
	
	private final String UNREAD_STRING = " unread";
	private final String MF_TITLE = "Message Center";
	
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("messageforums");

	/** to get accces to log file */
	private static final Log LOG = LogFactory.getLog(MessageForumSynopticBean.class);

	  /** Needed if within a site so we only need stats for this site */
	private MessageForumsMessageManager messageManager;

	/** Needed to get topics if tool within a site */
	private DiscussionForumManager forumManager;
	
	/** Needed to get Uuids for private messages and discussions */
	private MessageForumsTypeManager typeManager;

	/** Needed to grab unread message count if tool within site */
	private PrivateMessageManager pvtMessageManager;
	
	/** Needed to get site name if tool within a site */
	private ToolManager toolManager;

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
	 * @param tm
	 */
	public void setToolManager(ToolManager tm) {
		toolManager = tm;
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

		// TODO: determine if tool in My Workspace (ie, need global) or 
		// within site (just get this imformation
		if (SiteService.getUserSiteId("admin").equals(siteId)) return false;
		
		final boolean where = SiteService.isUserSite(siteId);

		LOG.info("Result of determinig if My Workspace: " + where);
		
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
		if (isMyWorkspace() ) {
			// Get stats for "all" sites this user is a member of
			// TODO:
			//	1. query db for all sites and unread messages for this user
			//  getCompiledStats()

			//	2. construct a List of decoratedCompiledMessageStats
			
			// TODO: replace with query to get info from db

			// get the sites the user has access to
			List mySites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null, null,
					org.sakaiproject.site.api.SiteService.SortType.TITLE_ASC, null);

			// check for message center 
			for (Iterator i = mySites.iterator(); i.hasNext();)
			{
				Site site = (Site) i.next();

				DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();
			
				dcms.setSiteName(site.getTitle());
				dcms.setUnreadPrivate("7 Private");
				dcms.setUnreadForums("0 Forum");
			
				
				contents.add(dcms);
			}

		}
		else {
			// Get stats for just this site
			
			if (isMessageForumsPageInSite()) {
				DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();
			
				dcms.setSiteName(getSiteName());

				// Get private message area so we can determine number of
				// unread messages
				// Already done so we just need to copy
				final Area area = pvtMessageManager.getPrivateMessageArea();
				PrivateForum pf = pvtMessageManager.initializePrivateMessageArea(area);

				final List pt = pf.getTopics();
				final Topic privateTopic = (Topic) pt.iterator().next();

				int unreadPrivate = messageManager.findUnreadMessageCountByTopicId(privateTopic.getId());
			
				dcms.setUnreadPrivate(unreadPrivate + " private" );

				// Number of unread forum messages is a little harder
				// need to loop through all topics and add them up
				List topicsList = forumManager.getDiscussionForums();
				long unreadForum = 0;
			
				final Iterator forumIter = topicsList.iterator();
			
				while (forumIter.hasNext()) {
					final DiscussionForum df = (DiscussionForum) forumIter.next();
					
					final List topics = df.getTopics();
					final Iterator topicIter = topics.iterator();
					
					while (topicIter.hasNext()) {
						final Topic topic = (Topic) topicIter.next();

						unreadForum += messageManager.findUnreadMessageCountByTopicId(topic.getId());
					
					}
				}
				dcms.setUnreadForums(unreadForum + " forum");
			
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
			return SiteService.getSite(toolManager.getCurrentPlacement().getContext()).getTitle();
		}
		catch (IdUnusedException e) {
			LOG.error("IdUnusedException when trying to access site " + e.getMessage());
		}
		
		return null;
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

	private boolean isMessageForumsPageInSite() {
		boolean mfToolExists = false;
		
		try {
			// loop thru tools on this site looking for
			// Message Center tool
			Site thisSite = SiteService.getSite(ToolManager
					.getCurrentPlacement().getContext());
			List pageList = thisSite.getPages();

			Iterator iterator = pageList.iterator();
			while (iterator.hasNext()) {
				SitePage pgelement = (SitePage) iterator.next();

				if (pgelement.getTitle().equals(MF_TITLE)) {
					mfToolExists = true;
					break;
				}
			}
		} 
		catch (IdUnusedException e) {
			LOG.error("No Site found while trying to check if site has MF tool.");
			
			// TODO: if we are in My Workspace, do we go here?
		}
		
		return mfToolExists;
	}

}
