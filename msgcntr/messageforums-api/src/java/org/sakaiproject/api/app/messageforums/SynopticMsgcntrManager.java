/**
 * Copyright (c) 2003-2014 The Apereo Foundation
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
package org.sakaiproject.api.app.messageforums;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface SynopticMsgcntrManager {
	
	public static final long OPT_LOCK_WAIT = 1000;
	public static final int NUM_OF_ATTEMPTS = 20;
	public static final String DISABLE_MYWORKSPACE = "msgcntr.synoptic.myworkspace.disable";
	public static final String DISABLE_FORUMS = "msgcntr.synoptic.disable.forums";
	public static final String DISABLE_MESSAGES = "msgcntr.synoptic.disable.messages";
	public static final String MYWORKSPACE_PERFORMANCE = "msgcntr.synoptic.myworkspace.performance";
	public static final String MYWORKSPACE_USERPROMPT = "msgcntr.synoptic.myworkspace.userRequestSynoptic";
	public static final String DISABLE_MYWORKSPACE_DISABLEDMESSAGE = "msgcntr.synoptic.myworkspace.disabledMessage";

	
	public List<SynopticMsgcntrItem> getWorkspaceSynopticMsgcntrItems(final String userId);

	public List<SynopticMsgcntrItem> getSiteSynopticMsgcntrItems(final List<String> userIds, final String siteId);
	
	public SynopticMsgcntrItem createSynopticMsgcntrItem(String userId, String siteId, String siteTitle);
	
	public void saveSynopticMsgcntrItems(List<SynopticMsgcntrItem> items);
	
	public void incrementMessagesSynopticToolInfo(List<String> userIds, String siteId);
	
	public void incrementForumSynopticToolInfo(List<String> userIds, String siteId);
	
	public void decrementMessagesSynopticToolInfo(List<String> userIds, String siteId);
	
	public void decrementForumSynopticToolInfo(List<String> userIds, String siteId);
	
	public void setMessagesSynopticInfoHelper(String userId, String siteId, int newMessageCount);
	
	public void setForumSynopticInfoHelper(String userId, String siteId, int newMessageCount);
		
	public void resetMessagesAndForumSynopticInfo(List<String> userIds, String siteId, List<SynopticMsgcntrItem> items);
	
	public void resetAllUsersSynopticInfoInSite(String siteId);
	
	public void resetAllUsersSynopticInfoInSite(String siteId, List<String> users);
	
	public void deleteSynopticMsgcntrItem(SynopticMsgcntrItem item);

	public void createOrUpdateSynopticToolInfo(List<String> userIds, String siteId, String siteTitle, Map<String, Integer[]> unreadCounts);
	
	/**
	 * This method is used to get live information regarding the new message count per user for a forum ID
	 * 
	 * This information can be used to compare against a later current information set after an event takes place
	 * (ie. after saving a forum)  By only looking at the one forum that was updated, we can compare the two results
	 * and update the synoptic data based on the difference while avoiding calling unneeded forums, which can slow
	 * down this process significantly
	 * 
	 * If topic ID is null, then it is ignored
	 * 
	 * @param siteId
	 * @param forumId
	 * @param topicId
	 * @return
	 */
	public HashMap<String, Integer> getUserToNewMessagesForForumMap(String siteId, Long forumId, Long topicId);
	
	/**
	 * This is used (in conjunction with getUserToNewMessagesForForumMap) to update the difference between a 
	 * previousCountHM and the current new message count for the forumId passed.
	 * 
	 * If topic ID is null, then it is ignored
	 * 
	 * @param siteId
	 * @param forumId
	 * @param topicId
	 * @param previousCountHM
	 */
	public void updateSynopticMessagesForForumComparingOldMessagesCount(String siteId, Long forumId, Long topicId, HashMap<String, Integer> previousCountHM);
	
	public void updateAllSiteTitles(String siteId, String siteTitle);
}
