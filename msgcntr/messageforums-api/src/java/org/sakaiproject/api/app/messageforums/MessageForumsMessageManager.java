/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface MessageForumsMessageManager {

    public Attachment createAttachment();

    public Message createMessage(String typeId);

    public PrivateMessage createPrivateMessage();

    public Message createDiscussionMessage();

    public Message createOpenMessage();

    public void saveMessage(Message message);

    public void saveMessage(Message message, boolean logEvent);
    /**
     * 
     * @param message message
     * @param logEvent logEvent
     * @param toolId id of the forums tool 
     * @param userId user id
     * @param contextId context id
     *
     */
	public void saveMessage(Message message, boolean logEvent, String toolId, String userId, String contextId);
	
    /**
     * 
     * @param message
     * @param logEvent
     * @param ignoreLockedTopicForum set true if you want to allow the message
     * to be updated even if the topic or forum is locked (ie marking as read or
     * commenting on a moderated message)
     */
    public void saveMessage(Message message, boolean logEvent, boolean ignoreLockedTopicForum);

    public void deleteMessage(Message message);

    public Message getMessageById(Long messageId);
    
    public Message getMessageByIdWithAttachments(Long messageId);
    
    public void markMessageApproval(Long messageId, boolean approved);

    public void markMessageReadForUser(Long topicId, Long messageId, boolean read);
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read, String userId);
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read, String userId, String context, String toolId);

    public boolean isMessageReadForUser(Long topicId, Long messageId);

    public UnreadStatus findUnreadStatus(Long topicId, Long messageId);
    
    public UnreadStatus findUnreadStatusByUserId(Long topicId, Long messageId, String userId);

    public void deleteUnreadStatus(Long topicId, Long messageId);

    public int findMessageCountByTopicId(Long topicId);
    public List<Object[]> findMessageCountByForumId(Long forumId);
    
    public List<Object[]> findMessageCountTotal();
    
    public int findViewableMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findViewableMessageCountByTopicId(Long topicId);

    public int findAuhtoredMessageCountByTopicIdByUserId(final Long topicId, final String userId);
    
    public int findAuthoredMessageCountForStudent(final String userId);
    
    /**
     * @param studentId The id of the student whose authored messages we are searching for.
     * @return A list of all of the messages that the student has authored and are not flagged
     * as DRAFT or DELETED.
     */
    public List<Message> findAuthoredMessagesForStudent(String studentId);
    public List<UserStatistics> findAuthoredStatsForStudent(String studentId);
    public List<Message> findAuthoredMessagesForStudentByTopicId(String studentId, final Long topicId);
    public List<UserStatistics> findAuthoredStatsForStudentByTopicId(String studentId, final Long topicId);
    public List<Message> findAuthoredMessagesForStudentByForumId(String studentId, final Long forumId);
    public List<UserStatistics> findAuthoredStatsForStudentByForumId(String studentId, final Long forumId);
    
    /**
     * @return Each item in the list will be an array consisting of two elements.  The element
     * at index 0 will correspond to the student's id and the element at index 1 will correspond
     * to the number of messages that student has authored in the site.
     */
    public List<Object[]> findAuthoredMessageCountForAllStudents();
    public List<Object[]> findAuthoredMessageCountForAllStudentsByTopicId(final Long topicId);
    public List<Object[]> findAuthoredMessageCountForAllStudentsByForumId(final Long forumId);
    
    public int findUnreadMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findUnreadMessageCountByTopicId(Long topicId);
    
    public int findUnreadViewableMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findUnreadViewableMessageCountByTopicId(Long topicId);

    public int findReadMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findReadMessageCountByTopicId(Long topicId);
    
    public int findReadMessageCountForStudent(final String userId);
    
    /**
     * @return Each item in the list will be an array consisting of two elements.  The element
     * at index 0 will correspond to the student's id and the element at index 1 will correspond
     * to the number of messages that student has read in the site.
     */
    public List<Object[]> findReadMessageCountForAllStudents();
    public List<Object[]> findReadMessageCountForAllStudentsByTopicId(final Long topicId);
    public List<Object[]> findReadMessageCountForAllStudentsByForumId(final Long forumId);
    
    
    
    /**
     * @param studentId The id of the student whose read messages we are searching for.
     * @return A list of all of the messages that the student has read and are not flagged
     * as DRAFT or DELETED.
     */
    public List<UserStatistics> findReadStatsForStudent(String studentId);
    public List<UserStatistics> findReadStatsForStudentByTopicId(String studentId, final Long topicId);
    public List<UserStatistics> findReadStatsForStudentByForumId(String studentId, final Long forumId);
    
    public int findReadViewableMessageCountByTopicId(Long topicId);

    public List findDiscussionForumMessageCountsForAllSitesByPermissionLevelId(final List siteList, final List roleList);
    
    public List findDiscussionForumMessageCountsForAllSitesByPermissionLevelName(final List siteList, final List roleList);
    
    public List findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelId(final List siteList, final List roleList);
    
    public List findDiscussionForumReadMessageCountsForAllSitesByPermissionLevelName(final List siteList, final List roleList);

    public List findDiscussionForumMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList); // added

    public List findDiscussionForumReadMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList); // added
    
    public List<Object []> findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList);

    public List<Object []> findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList);

    /**
     * @param topicIds The list of topic ids for which we want to gather the message counts.
     *  
     * @return A list of arrays where each array will contain exactly two values.  Index 0, a Long,
     * will be the topicId and index 1, an Integer, will be the total number of messages
     * under that topic (excluding any messages flagged as DRAFT or DELETED).    
     */
    public List<Object[]> findMessageCountsForMainPage(final Collection<Long> topicIds);

    /**
     * @param topicIds The list of topic ids for which we want to gather the message counts.
     *  
     * @return A list of arrays where each array will contain exactly two values.  Index 0, a Long,
     * will be the topicId and index 1, an Integer, will be the total number of messages
     * under that topic that the current user has read (excluding any messages flagged as DRAFT 
     * or DELETED).    
     */
    public List<Object[]> findReadMessageCountsForMainPage(final Collection<Long> topicIds);
    
    public List findMessagesByTopicId(Long topicId);
  
    public List findUndeletedMessagesByTopicId(Long topicId);
    
    public Attachment getAttachmentById(Long attachmentId);
    
    public void getChildMsgs(final Long messageId, List returnList);
    
    public void deleteMsgWithChild(final Long messageId);
    
    public List getFirstLevelChildMsgs(final Long messageId);
    
    public List sortMessageBySubject(Topic topic, boolean asc);

    public List sortMessageByAuthor(Topic topic, boolean asc);

    public List sortMessageByDate(Topic topic, boolean asc);
    
    public List sortMessageByDate(List list, boolean asc);
    
    public List getAllRelatedMsgs(final Long messageId);
    
    public List findPvtMsgsBySearchText(final String typeUuid, final String searchText,final Date searchFromDate, final Date searchToDate,
        final boolean searchByText, final boolean searchByAuthor,final boolean searchByBody, final boolean searchByLabel,final boolean searchByDate);
    
    /**
     * Get a fully qualified URl
     * @param id
     * @return
     */
    public String getAttachmentUrl(String id);
    
    /**
     * Get a relative URL escaped suitable for JSF pages
     * @param id
     * @return
     */
    public String getAttachmentRelativeUrl(String id);
    
    public boolean currentToolMatch(String toolId);
    
	public boolean isToolInSite(String siteId, String toolId);

	public Map<Long, Boolean> getReadStatusForMessagesWithId(List<Long> msgIds, String userId);
	
	/**
	 * Returns list of all messages in site with Pending approval for which
	 * at least one of the given memberships has moderate perm
	 * @return
	 */
	public List getPendingMsgsInSiteByMembership(final List membershipList);
	
	/**
	 * Retrieves all pending messages in a given topic
	 * @param topicId
	 * @return
	 */
	public List getPendingMsgsInTopic(final Long topicId);

	
	/**
	 * Get all forum messages in a site
	 * @param siteId
	 * @return a list of messages
	 */
	public List<Message> getAllMessagesInSite(String siteId);

	public void saveMessageMoveHistory(Long msgid, Long desttopicId,Long sourceTopicId, boolean checkreminder);
	  
	public List findMovedMessagesByTopicId(Long id); 

	/**
	 * Returns a given number of recent threads(which are not deleted and not in draft stage)
	 * for a given list of topicIds
	 * @param topicIds
	 * @param numberOfMessages
	 * @return
	 */
	public List getRecentDiscussionForumThreadsByTopicIds(List<Long> topicIds, int numberOfMessages);
}
