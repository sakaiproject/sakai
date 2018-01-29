/**
 * Copyright (c) 2005-2017 The Apereo Foundation
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
package org.sakaiproject.component.app.messageforums;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.hibernate4.HibernateOptimisticLockingFailureException;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.ForumScheduleNotification;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.cover.SynopticMsgcntrManagerCover;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;

@Slf4j
public class ForumScheduleNotificationImpl implements ForumScheduleNotification {

	private static final String AREA_PREFIX = "area-";
	private static final String FORUM_PREFIX = "forum-";
	private static final String TOPIC_PREFIX = "topic-";

	private MessageForumsTypeManager typeManager;

	public void setTypeManager(MessageForumsTypeManager typeManager) {
		this.typeManager = typeManager;
	}

	private AreaManager areaManager;

	public void setAreaManager(AreaManager areaManager) {
		this.areaManager = areaManager;
	}

	private DiscussionForumManager forumManager;

	public void setForumManager(DiscussionForumManager forumManager) {
		this.forumManager = forumManager;
	}

	private ScheduledInvocationManager scheduledInvocationManager;

	public void setScheduledInvocationManager(ScheduledInvocationManager scheduledInvocationManager) {
		this.scheduledInvocationManager = scheduledInvocationManager;
	}

	public void init() {
		log.info("init()");
	}

	public void scheduleAvailability(Area area) {
		scheduleAvailability(AREA_PREFIX + area.getContextId(), area.getAvailabilityRestricted(), area.getOpenDate(),
				area.getCloseDate());
	}

	public void scheduleAvailability(DiscussionForum forum) {
		scheduleAvailability(FORUM_PREFIX + forum.getId().toString(), forum.getAvailabilityRestricted(),
				forum.getOpenDate(), forum.getCloseDate());
	}

	public void scheduleAvailability(DiscussionTopic topic) {
		scheduleAvailability(TOPIC_PREFIX + topic.getId().toString(), topic.getAvailabilityRestricted(),
				topic.getOpenDate(), topic.getCloseDate());
	}

	private void scheduleAvailability(String id, boolean availabilityRestricted, Date openDate, Date closeDate) {
		// Remove any existing notifications for this area
		scheduledInvocationManager
				.deleteDelayedInvocation("org.sakaiproject.api.app.messageforums.ForumScheduleNotification", id);
		if (availabilityRestricted) {
			Instant openTime = null;
			Instant closeTime = null;
			if (openDate != null) {
				openTime = openDate.toInstant();
			}
			if (closeDate != null) {
				closeTime = closeDate.toInstant();
			}
			// Schedule the new notification
			if (openTime != null && openTime.isAfter(Instant.now())) {
				scheduledInvocationManager.createDelayedInvocation(openTime,
						"org.sakaiproject.api.app.messageforums.ForumScheduleNotification", id);
			} else if (closeTime != null && closeTime.isAfter(Instant.now())) {
				scheduledInvocationManager.createDelayedInvocation(closeTime,
						"org.sakaiproject.api.app.messageforums.ForumScheduleNotification", id);
			}
		}
	}

	public void execute(String opaqueContext) {
		log.info("execute: {}", opaqueContext);
		if (opaqueContext.startsWith(AREA_PREFIX)) {
			String siteId = opaqueContext.substring(AREA_PREFIX.length());
			Area area = areaManager.getAreaByContextIdAndTypeId(siteId, typeManager.getDiscussionForumType());
			boolean makeAvailable = makeAvailableHelper(area.getAvailabilityRestricted(), area.getOpenDate(),
					area.getCloseDate());

			boolean madeChange = false;
			if (area.getAvailability()) {
				if (!makeAvailable) {
					// make area unavailable:
					area.setAvailability(makeAvailable);
					madeChange = true;
				}
			} else {
				if (makeAvailable) {
					// make area available:
					area.setAvailability(makeAvailable);
					madeChange = true;
				}
			}
			if (madeChange) {
				// save area and update synoptic counts
				areaManager.saveArea(area);
				SynopticMsgcntrManagerCover.resetAllUsersSynopticInfoInSite(siteId);
			}
		} else if (opaqueContext.startsWith(FORUM_PREFIX)) {
			Long forumId = Long.parseLong(opaqueContext.substring(FORUM_PREFIX.length()));
			DiscussionForum forum = forumManager.getForumById(forumId);
			boolean makeAvailable = makeAvailableHelper(forum.getAvailabilityRestricted(), forum.getOpenDate(),
					forum.getCloseDate());
			boolean madeChange = false;
			if (forum.getAvailability()) {
				if (!makeAvailable) {
					// make area unavailable:
					forum.setAvailability(makeAvailable);
					madeChange = true;
				}
			} else {
				if (makeAvailable) {
					// make area available:
					forum.setAvailability(makeAvailable);
					madeChange = true;
				}
			}
			if (madeChange) {
				// save forum and update synoptic counts
				String siteId = forumManager.getContextForForumById(forumId);
				HashMap<String, Integer> beforeChangeHM = SynopticMsgcntrManagerCover
						.getUserToNewMessagesForForumMap(siteId, forum.getId(), null);
				forumManager.saveForum(forum, forum.getDraft(), siteId, false, "-forumScheduler-");
				updateSynopticMessagesForForumComparingOldMessagesCount(siteId, forum.getId(), null, beforeChangeHM,
						SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
			}

		} else if (opaqueContext.startsWith(TOPIC_PREFIX)) {
			Long topicId = Long.parseLong(opaqueContext.substring(TOPIC_PREFIX.length()));
			DiscussionTopic topic = forumManager.getTopicById(topicId);

			boolean makeAvailable = makeAvailableHelper(topic.getAvailabilityRestricted(), topic.getOpenDate(),
					topic.getCloseDate());
			boolean madeChange = false;
			if (topic.getAvailability()) {
				if (!makeAvailable) {
					// make area unavailable:
					topic.setAvailability(makeAvailable);
					madeChange = true;
				}
			} else {
				if (makeAvailable) {
					// make area available:
					topic.setAvailability(makeAvailable);
					madeChange = true;
				}
			}
			if (madeChange) {
				// save forum and update synoptic counts
				String siteId = forumManager.getContextForTopicById(topicId);
				HashMap<String, Integer> beforeChangeHM = SynopticMsgcntrManagerCover
						.getUserToNewMessagesForForumMap(siteId, topic.getBaseForum().getId(), topic.getId());

				forumManager.saveTopic(topic, topic.getDraft(), false, "-forumScheduler-");
				updateSynopticMessagesForForumComparingOldMessagesCount(siteId, topic.getBaseForum().getId(),
						topic.getId(), beforeChangeHM, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
			}
		}
	}

	public boolean makeAvailableHelper(boolean availabilityRestricted, Date openDate, Date closeDate) {
		boolean makeAvailable = true;
		if (availabilityRestricted) {
			// availability is being restricted:
			makeAvailable = false;

			boolean afterOpen = false;
			boolean beforeClose = false;
			Instant openTime = null;
			Instant closeTime = null;
			if (openDate != null) {
				openTime = openDate.toInstant();
			}
			if (closeDate != null) {
				closeTime = closeDate.toInstant();
			}
			if (closeDate == null && openDate == null) {
				// user didn't specify either, so open topic
				makeAvailable = true;
			}

			if (openTime != null && openTime.isBefore(Instant.now())) {
				afterOpen = true;
			} else if (openTime == null) {
				afterOpen = true;
			}
			if (closeTime != null && closeTime.isAfter(Instant.now())) {
				beforeClose = true;
			} else if (closeTime == null) {
				beforeClose = true;
			}

			if (afterOpen && beforeClose) {
				makeAvailable = true;
			}
		}
		return makeAvailable;
	}

	public void updateSynopticMessagesForForumComparingOldMessagesCount(String siteId, Long forumId, Long topicId,
			HashMap<String, Integer> beforeChangeHM, int numOfAttempts) {
		try {
			// update synotpic info for forums only:
			SynopticMsgcntrManagerCover.updateSynopticMessagesForForumComparingOldMessagesCount(siteId, forumId, topicId, beforeChangeHM);
		} catch (HibernateOptimisticLockingFailureException holfe) {

			// failed, so wait and try again
			try {
				Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
			} catch (InterruptedException e) {
				log.warn("Thread intrrupted while updating synoptic info for forums", e);
			}

			numOfAttempts--;

			if (numOfAttempts <= 0) {
				log.warn("HibernateOptimisticLockingFailureException no more retries left", holfe);
			} else {
				log.warn("HibernateOptimisticLockingFailureException: attempts left: {}", numOfAttempts);
				updateSynopticMessagesForForumComparingOldMessagesCount(siteId, forumId, topicId, beforeChangeHM, numOfAttempts);
			}
		}
	}
}
