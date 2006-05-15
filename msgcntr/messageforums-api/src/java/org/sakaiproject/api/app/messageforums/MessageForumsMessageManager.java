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
package org.sakaiproject.api.app.messageforums;

import java.util.Date;
import java.util.List;
import java.sql.Timestamp;

public interface MessageForumsMessageManager {

    public Attachment createAttachment();

    public Message createMessage(String typeId);

    public PrivateMessage createPrivateMessage();

    public Message createDiscussionMessage();

    public Message createOpenMessage();

    public void saveMessage(Message message);

    public void deleteMessage(Message message);

    public Message getMessageById(Long messageId);
    
    public Message getMessageByIdWithAttachments(Long messageId);

    public void markMessageReadForUser(Long topicId, Long messageId, boolean read);

    public boolean isMessageReadForUser(Long topicId, Long messageId);

    public UnreadStatus findUnreadStatus(Long topicId, Long messageId);

    public void deleteUnreadStatus(Long topicId, Long messageId);

    public int findMessageCountByTopicId(Long topicId);

    public int findUnreadMessageCountByTopicId(Long topicId);

    public int findReadMessageCountByTopicId(Long topicId);

    public List findMessagesByTopicId(Long topicId);

    public Attachment getAttachmentById(Long attachmentId);
    
    public void getChildMsgs(final Long messageId, List returnList);
    
    public void deleteMsgWithChild(final Long messageId);
    
    public List getFirstLevelChildMsgs(final Long messageId);
    
    public List sortMessageBySubject(Topic topic, boolean asc);

    public List sortMessageByAuthor(Topic topic, boolean asc);

    public List sortMessageByDate(Topic topic, boolean asc);
    
    public List getAllRelatedMsgs(final Long messageId);
    
    public List findPvtMsgsBySearchText(final String typeUuid, final String searchText,final Date searchFromDate, final Date searchToDate,
        final Long searchByText, final Long searchByAuthor,final Long searchByBody, final Long searchByLabel,final Long searchByDate);
}

