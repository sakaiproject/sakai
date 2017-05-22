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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.sakaiproject.api.app.messageforums.EmailNotification;
import org.sakaiproject.api.app.messageforums.EmailNotificationManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.EmailNotificationImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailNotificationManagerImpl extends HibernateDaoSupport implements EmailNotificationManager {

	private static final String QUERY_BY_USER_ID = "findEmailNotificationByUserId";
	private static final String QUERY_USERLIST_BY_NOTIFICATION_LEVEL = "findUserIdsByNotificationLevel";

	protected UserDirectoryService userDirectoryService;

	private EventTrackingService eventTrackingService;
	
	private ToolManager toolManager;

	public void init() {
		log.info("init()");
	}

	public EventTrackingService getEventTrackingService() {
		return eventTrackingService;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
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
		log.debug("getEmailNotification(userId: {})", userId);

		if (userId == null) {
			throw new IllegalArgumentException("Null Argument");
		}

		HibernateCallback<EmailNotification> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_BY_USER_ID);
            q.setParameter("userId", userId, StringType.INSTANCE);
            q.setParameter("contextId", getContextId(), StringType.INSTANCE);
            return (EmailNotification) q.uniqueResult();
        };

		EmailNotification emailNotification = getHibernateTemplate().execute(hcb);

		if (emailNotification == null) {
			// this user has not set his emailnotification option. That's okay.
			// by default it is level 1, unless otherwise specified by sakai.properties
			try {
				userDirectoryService.getUser(userId);
			} catch (UserNotDefinedException e) {
				log.error(e.getMessage());
			}
			String notificationDefault = ServerConfigurationService.getString("mc.notificationDefault", "1");
			EmailNotification newEmailNotification = new EmailNotificationImpl();
			newEmailNotification.setContextId(getContextId());
			newEmailNotification.setUserId(userId);
			log.debug("notificationDefault={}", notificationDefault);
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

			log.debug("{} didn't set watch options, creating EmailNotification with level: {}",
					userId, newEmailNotification.getNotificationLevel());

			return newEmailNotification;
		} else {
			log.debug("{} already set watch options. his option is {}",
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
		Placement placement = toolManager.getCurrentPlacement();
		String presentSiteId = placement.getContext();
		return presentSiteId;
	}

	public List<String> getUsersToBeNotifiedByLevel(String notificationlevel) {
		String contextid = this.getContextId();
		int intlevel = Integer.parseInt(notificationlevel);
 		List<String> allusers = getSiteUsersByNotificationLevel(contextid, intlevel);
 		
		log.debug("total count of users to be notified = {}", allusers.size());
		return allusers;
	}

	/**
	 * Filter the list of notification users to remove users who don't have read permission in the topic
	 */
	
	public List<String> filterUsers(List<String> allusers, Topic topic) {
		List<String> ret = new ArrayList<String>();
		Set<String> readUsers = discussionForumManager.getUsersAllowedForTopic(topic.getId(), true, false);
		for (int i = 0; i < allusers.size(); i++) {
			String userId = allusers.get(i);
			if (readUsers.contains(userId)) {
				log.debug("user {} has read in topic: {}", userId, topic.getId());
				ret.add(userId);
			} else {
				log.debug("Removing user: {} as they don't have read rights on topic: {}", userId, topic.getId());
			}
		}
		return ret;
	}

	private List<String> getSiteUsersByNotificationLevel(final String contextid,
			final int notificationlevel) {

			log.debug("getEmailNotification(userid: {})", notificationlevel);

		HibernateCallback<List<String>> hcb = session -> {
            Query q = session.getNamedQuery(QUERY_USERLIST_BY_NOTIFICATION_LEVEL);
            q.setParameter("contextId", contextid, StringType.INSTANCE);
            q.setParameter("level", notificationlevel, IntegerType.INSTANCE);
            return q.list();
        };

 
		List<String> siteusers = getHibernateTemplate().execute(hcb);

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
				log.debug("Username = {}, email: {}", user.getDisplayId(), useremail);
				emaillist.add(useremail);
			}


		}

		return emaillist;

	}

	public void saveEmailNotification(EmailNotification emailoption) {
		getHibernateTemplate().saveOrUpdate(emailoption);
		
		log.debug("saveEmailNotification executed for contextid={} userid={}",
				emailoption.getContextId(), emailoption.getUserId());

	}

}
