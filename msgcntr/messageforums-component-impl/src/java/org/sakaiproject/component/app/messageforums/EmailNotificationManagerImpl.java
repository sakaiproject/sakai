/**********************************************************************************
 * $URL$
 * $Id$
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
package org.sakaiproject.component.app.messageforums;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.EmailNotification;
import org.sakaiproject.api.app.messageforums.EmailNotificationManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.EmailNotificationImpl;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class EmailNotificationManagerImpl extends HibernateDaoSupport implements
		EmailNotificationManager {

	private static final Log LOG = LogFactory
			.getLog(EmailNotificationManagerImpl.class);

	private static final String QUERY_BY_USER_ID = "findEmailNotificationByUserId";
	private static final String QUERY_USERLIST_BY_NOTIFICATION_LEVEL = "findUserIdsByNotificationLevel";

	protected UserDirectoryService userDirectoryService;

	private EventTrackingService eventTrackingService;

	public void init() {
		LOG.info("init()");
	}

	public EventTrackingService getEventTrackingService() {
		return eventTrackingService;
	}

	public void setEventTrackingService(
			EventTrackingService eventTrackingService) {
		this.eventTrackingService = eventTrackingService;
	}


	private DiscussionForumManager discussionForumManager;
	public void setDiscussionForumManager(
			DiscussionForumManager discussionForumManager) {
		this.discussionForumManager = discussionForumManager;
	}

	public EmailNotification getEmailNotification(final String userId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getEmailNotification(userId: " + userId + ")");
		}

		if (userId == null) {
			throw new IllegalArgumentException("Null Argument");
		}

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_BY_USER_ID);
				q.setParameter("userId", userId, Hibernate.STRING);
				q.setParameter("contextId", getContextId(), Hibernate.STRING);
				return q.uniqueResult();
			}
		};

		EmailNotification emailNotification = (EmailNotification) getHibernateTemplate()
				.execute(hcb);

		if (emailNotification == null) {
			// this user has not set his emailnotification option. That's okay.
			// by default it is level 1
			try {
				userDirectoryService.getUser(userId);
			} catch (UserNotDefinedException e) {
				e.printStackTrace();
			}
			EmailNotification newEmailNotification = new EmailNotificationImpl();
			newEmailNotification.setContextId(getContextId());
			newEmailNotification.setUserId(userId);
			newEmailNotification
					.setNotificationLevel(EmailNotification.EMAIL_REPLY_TO_MY_MESSAGE);

			// create a new emailnotification if this user doesn't have a record
			// yet
			saveEmailNotification(newEmailNotification);

			if (LOG.isDebugEnabled()) {
				LOG
						.debug(userId
								+ "  didn't set watch options.  creating a new EmailNotification this user. his level : "
								+ newEmailNotification.getNotificationLevel());
			}

			return newEmailNotification;
		} else {

			if (LOG.isDebugEnabled()) {
				LOG.debug(userId
						+ "  already set watch options. his option is "
						+ emailNotification.getNotificationLevel());
			}

			return emailNotification;
		}
	}

	public void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	private String getContextId() {
		if (TestUtil.isRunningTests()) {
			return "test-context";
		}
		Placement placement = ToolManager.getCurrentPlacement();
		String presentSiteId = placement.getContext();
		return presentSiteId;
	}

	public List<String> getUsersToBeNotifiedByLevel(String notificationlevel) {
		String contextid = this.getContextId();
		int intlevel = Integer.parseInt(notificationlevel);
 		List<String> allusers = getSiteUsersByNotificationLevel(contextid, intlevel);
 		
 		if (LOG.isDebugEnabled()){
			LOG.debug("total count of users to be notified = " + allusers.size());
		}
		return allusers;
	}

/*	
	public List getLevel2UsersToBeNotified(String authorUserId) {
		String contextid = this.getContextId();
		List<String> allusers = new ArrayList<String>();
		List<String> level2users = getSiteUsersByNotificationLevel(contextid, 2);
		allusers.addAll(level2users);
		
		EmailNotification authorNotificationLevel = getEmailNotification(authorUserId);
		if ("1".equalsIgnoreCase(authorNotificationLevel.getNotificationLevel())){
			if (LOG.isDebugEnabled()){
				LOG.debug("The author: " + authorUserId + " wants to be notified");
			}

			allusers.add(authorUserId);
		}
		if (LOG.isDebugEnabled()){
			LOG.debug("total count of users to be notified = " + allusers.size());
		}
		return allusers;
	}
	
	*/

	/**
	 * Filter the list of notification users to remove users who don't have read permission in the topic
	 */
	
	public List<String> filterUsers(List<String> allusers, Topic topic) {
		List<String> ret = new ArrayList<String>();
		Set<String> readUsers = discussionForumManager.getUsersAllowedForTopic(topic.getId(), true, false);
		for (int i = 0; i < allusers.size(); i++) {
			String userId = allusers.get(i);
			if (readUsers.contains(userId)) {
				LOG.debug("user " + userId + " has read in topic: " + topic.getId());
				ret.add(userId);
			} else {
				LOG.debug("Removing user: " + userId + "as they don't have read rights on topic: " + topic.getId());
			}
		}
		return ret;
	}

	private List<String> getSiteUsersByNotificationLevel(final String contextid,
			final int notificationlevel) {

		if (LOG.isDebugEnabled()) {
			LOG
					.debug("getEmailNotification(userid: " + notificationlevel
							+ ")");
		}

		HibernateCallback hcb = new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query q = session.getNamedQuery(QUERY_USERLIST_BY_NOTIFICATION_LEVEL);
				q.setParameter("contextId", contextid, Hibernate.STRING);
				q.setParameter("level", notificationlevel, Hibernate.INTEGER);
				return q.list();
			}
		};

 
		List<String> siteusers = (List) getHibernateTemplate().execute(hcb);

		// get all site users that are
		// either want all notification
		// or reply to their own message

		return siteusers;

	}

	public List<String> getUserEmailsToBeNotifiedByLevel(List<String> userlist) {
		List<String> emaillist = new ArrayList<String>();
		List<User> usersList = userDirectoryService.getUsers(userlist);


		for (int i = 0; i < usersList.size(); i++) {
			User user = usersList.get(i);


			// find emails for each user
			String useremail = user.getEmail();
			if (useremail != null && !"".equalsIgnoreCase(useremail)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Username = " + user.getDisplayId()
							+ " , useremail : " + useremail);
				}
				emaillist.add(useremail);
			}


		}

		return emaillist;

	}

	public void saveEmailNotification(EmailNotification emailoption) {
		getHibernateTemplate().saveOrUpdate(emailoption);
		
		LOG.debug("saveEmailNotification executed for contextid= "
				+ emailoption.getContextId() + " userid= "
				+ emailoption.getUserId());

	}

}
