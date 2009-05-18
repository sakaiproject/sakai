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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

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

    public void deleteMessage(Message message);

    public Message getMessageById(Long messageId);
    
    public Message getMessageByIdWithAttachments(Long messageId);
    
    public void markMessageApproval(Long messageId, boolean approved);

    public void markMessageReadForUser(Long topicId, Long messageId, boolean read);
    
    public void markMessageReadForUser(Long topicId, Long messageId, boolean read, String userId);

    public boolean isMessageReadForUser(Long topicId, Long messageId);

    public UnreadStatus findUnreadStatus(Long topicId, Long messageId);
    
    public UnreadStatus findUnreadStatusByUserId(Long topicId, Long messageId, String userId);

    public void deleteUnreadStatus(Long topicId, Long messageId);

    public int findMessageCountByTopicId(Long topicId);
    
    public int findViewableMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findViewableMessageCountByTopicId(Long topicId);

    public int findAuhtoredMessageCountByTopicIdByUserId(final Long topicId, final String userId);
    
    public int findUnreadMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findUnreadMessageCountByTopicId(Long topicId);
    
    public int findUnreadViewableMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findUnreadViewableMessageCountByTopicId(Long topicId);

    public int findReadMessageCountByTopicIdByUserId(Long topicId, String userId);
    
    public int findReadMessageCountByTopicId(Long topicId);
    
    public int findReadViewableMessageCountByTopicId(Long topicId);

    public List findDiscussionForumMessageCountsForAllSites(final List siteList, final List roleList);
    
    public List findDiscussionForumReadMessageCountsForAllSites(final List siteList, final List roleList);

    public List findDiscussionForumMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList); // added

    public List findDiscussionForumReadMessageCountsForGroupedSitesByTopic(final List siteList, final List roleList); // added
    
    public List<Object []> findDiscussionForumMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList);

    public List<Object []> findDiscussionForumReadMessageCountsForTopicsWithMissingPermsForAllSites(final List<String> siteList);

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

	public Map getReadStatusForMessagesWithId(List msgIds, String userId);
	
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
}