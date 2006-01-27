/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

