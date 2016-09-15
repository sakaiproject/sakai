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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.sakaiproject.component.cover.ServerConfigurationService;

public class EmailNotificationManagerImpl extends HibernateDaoSupport implements
		EmailNotificationManager {

	private static final Logger LOG = LoggerFactory.getLogger(EmailNotificationManagerImpl.class);

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
		LOG.debug("getEmailNotification(userId: {})", userId);

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
			// by default it is level 1, unless otherwise specified by sakai.properties
			try {
				userDirectoryService.getUser(userId);
			} catch (UserNotDefinedException e) {
				LOG.error(e.getMessage());
			}
			String notificationDefault = ServerConfigurationService.getString("mc.notificationDefault", "1");
			EmailNotification newEmailNotification = new EmailNotificationImpl();
			newEmailNotification.setContextId(getContextId());
			newEmailNotification.setUserId(userId);
			LOG.debug("notificationDefault={}", notificationDefault);
			if ("0".equals(notificationDefault)) {
			    newEmailNotification
					.setNotificationLevel(EmailNotification.EMAIL_NONE);
			} else if ("2".equals(notificationDefault)) {
			    newEmailNotification
					.setNotificationLevel(EmailNotification.EMAIL_REPLY_TO_ANY_MESSAGE);
			} else {
			    newEmailNotification
					.setNotificationLevel(EmailNotification.EMAIL_REPLY_TO_MY_MESSAGE);
			}

			// create a new emailnotification if this user doesn't have a record
			// yet
			saveEmailNotification(newEmailNotification);

			LOG.debug("{} didn't set watch options, creating EmailNotification with level: {}",
					userId, newEmailNotification.getNotificationLevel());

			return newEmailNotification;
		} else {
			LOG.debug("{} already set watch options. his option is {}",
					userId, emailNotification.getNotificationLevel());

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
 		
		LOG.debug("total count of users to be notified = {}", allusers.size());
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
				LOG.debug("user {} has read in topic: {}", userId, topic.getId());
				ret.add(userId);
			} else {
				LOG.debug("Removing user: {} as they don't have read rights on topic: {}", userId, topic.getId());
			}
		}
		return ret;
	}

	private List<String> getSiteUsersByNotificationLevel(final String contextid,
			final int notificationlevel) {

			LOG.debug("getEmailNotification(userid: {})", notificationlevel);

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
				LOG.debug("Username = {}, email: {}", user.getDisplayId(), useremail);
				emaillist.add(useremail);
			}


		}

		return emaillist;

	}

	public void saveEmailNotification(EmailNotification emailoption) {
		getHibernateTemplate().saveOrUpdate(emailoption);
		
		LOG.debug("saveEmailNotification executed for contextid={} userid={}",
				emailoption.getContextId(), emailoption.getUserId());

	}

}
