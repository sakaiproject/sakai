package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
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
	 * Returns TRUE if on specific site, FALSE if on MyWorkspace
	 * 
	 * @return
	 */
	public boolean isWithinSite() {
		
		// get Site id
		String siteId = ToolManager.getCurrentPlacement().getContext();

		// TODO: determine if tool in My Workspace (ie, need global) or 
		// within site (just get this imformation
		if (SiteService.getUserSiteId("admin").equals(siteId)) return false;
		
		final boolean where = SiteService.isUserSite(siteId);

		LOG.info("Result of determinig if within a site: " + where);
		
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
		if (! isWithinSite() ) {
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
			
			//TODO: determine if Message Center itself is part of site
			DecoratedCompiledMessageStats dcms = new DecoratedCompiledMessageStats();
			
			dcms.setSiteName(getSiteName());
			
			List topicsList = forumManager.getDiscussionForums();
			long unreadForum = 0;
			
			final Iterator forumIter = topicsList.iterator();
			
			while (forumIter.hasNext()) {
				final Topic topic = (Topic) forumIter.next();

				if (topic.getTypeUuid().equals(typeManager.getPrivateMessageAreaType())) {
					dcms.setUnreadPrivate(pvtMessageManager.getUnreadNoMessages(topic) + UNREAD_STRING);
										
				}
				else {
					unreadForum += messageManager.findUnreadMessageCountByTopicId(topic.getId());
				}
			}
			
			dcms.setUnreadForums(unreadForum + UNREAD_STRING);
			
			contents.add(dcms);
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

}
