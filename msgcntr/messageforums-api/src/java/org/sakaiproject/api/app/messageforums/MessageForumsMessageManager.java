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

import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.UnreadStatus;

public interface MessageForumsMessageManager {

    public Attachment createAttachment();

    public Message createMessage(String typeId);

    public PrivateMessage createPrivateMessage();

    public Message createDiscussionMessage();

    public Message createOpenMessage();

    public void saveMessage(Message message);

    public void deleteMessage(Message message);

    public Message getMessageById(final Long messageId);
    
    public Message getMessageByIdWithAttachments(final Long messageId);

    public void markMessageReadForUser(String userId, Long topicId, Long messageId);

    public boolean isMessageReadForUser(String userId, Long topicId, Long messageId);

    public UnreadStatus findUnreadStatus(String userId, Long topicId, Long messageId);

    public void deleteUnreadStatus(String userId, Long topicId, Long messageId);

    public int findMessageCountByTopicId(Long topicId);

    public int findUnreadMessageCountByTopicId(String userId, Long topicId);

    public int findReadMessageCountByTopicId(String userId, Long topicId);

    public List findMessagesByTopicId(Long topicId);

    public Attachment getAttachmentById(Long attachmentId);

    }