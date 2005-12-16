/**********************************************************************************
 * $URL:$
 * $Id: $
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

package org.sakaiproject.component.app.messageforums;

import java.util.Date;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsForumManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;

public class MessageForumsTests extends ForumsApplicationContextBaseTest {

    private MessageForumsForumManager forumManager;

    private MessageForumsMessageManager messageManager;

    private AreaManager areaManager;
    
    private MessageForumsTypeManager typeManager;
    
    public MessageForumsTests() {
        super();
        init();
    }

    public MessageForumsTests(String name) {
        super(name);
        init();
    }

    private void init() {
        forumManager = (MessageForumsForumManager) getApplicationContext().getBean(MessageForumsForumManager.class.getName());
        messageManager = (MessageForumsMessageManager) getApplicationContext().getBean(MessageForumsMessageManager.class.getName());
        areaManager = (AreaManager) getApplicationContext().getBean(AreaManager.class.getName());
        typeManager = new MessageForumsTypeManagerImpl();
    }

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setRunningTests(true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtil.setRunningTests(false);
    }

    public void testSaveAndDeleteMessage() {
        Message message = messageManager.createMessage(typeManager.getPrivateMessageAreaType());
        message.setApproved(Boolean.TRUE);
        message.setAuthor("nate");
        message.setTitle("a message");
        message.setBody("this is a test message");
        message.setDraft(Boolean.FALSE);
        message.setTypeUuid("mess-type");
        message.addAttachment(getAttachment());

        messageManager.saveMessage(message);
        Long id = message.getId();
        messageManager.deleteMessage(message);
        Message message2 = messageManager.getMessageById(id);
        assertTrue("message2 should not exist", message2 == null);
    }

    public void testSaveAndDeleteTopic() {
        DiscussionTopic topic = getDiscussionTopic();
        forumManager.saveDiscussionForumTopic(topic);
        forumManager.deleteDiscussionForumTopic(topic);
        assertTrue(true);
    }

    public void testSaveAndDeleteForum() {
        DiscussionTopic topic = getDiscussionTopic();
        topic.setUuid("00322");
        topic.setCreated(new Date());
        topic.setCreatedBy("ed");
        topic.setModified(new Date());
        topic.setModifiedBy("jim");

        DiscussionForum forum = forumManager.createDiscussionForum();
        forum.setTypeUuid("df-type");
        forum.setTitle("A test discussion forum");
        forum.setShortDescription("short desc");
        forum.setExtendedDescription("long desc");
        forum.setSortIndex(new Integer(1));
        forum.setLocked(Boolean.FALSE);
        forum.addAttachment(getAttachment());
        forum.addTopic(topic);

        forumManager.saveDiscussionForum(forum);
        forumManager.deleteDiscussionForum(forum);
        assertTrue(true);
    }

    public void testSaveAndDeleteArea() {
        DiscussionTopic topic = getDiscussionTopic();
        topic.setUuid("00322");
        topic.setCreated(new Date());
        topic.setCreatedBy("ed");
        topic.setModified(new Date());
        topic.setModifiedBy("jim");

        DiscussionForum forum = forumManager.createDiscussionForum();
        forum.setUuid("00222");
        forum.setCreated(new Date());
        forum.setCreatedBy("ed");
        forum.setModified(new Date());
        forum.setModifiedBy("jim");
        forum.setTypeUuid("df-type");
        forum.setTitle("A test discussion forum");
        forum.setShortDescription("short desc");
        forum.setExtendedDescription("long desc");
        forum.setSortIndex(new Integer(1));
        forum.setLocked(Boolean.FALSE);
        forum.addAttachment(getAttachment());
        forum.addTopic(topic);

        Area area = areaManager.createArea(typeManager.getPrivateMessageAreaType());
        area.setContextId("area-context-id");
        area.setEnabled(Boolean.TRUE);
        area.setHidden(Boolean.FALSE);
        area.setName("a test area");
        area.setTypeUuid("discussion area type");
        area.addDiscussionForum(forum);

        areaManager.saveArea(area);
        areaManager.deleteArea(area);
        assertTrue(true);
    }

    public void testIsMessageReadForUser() {
        assertFalse(messageManager.isMessageReadForUser("joesmith", new Long(1), new Long(2)));
        messageManager.markMessageReadForUser("joesmith", new Long(1), new Long(2));
        assertTrue(messageManager.isMessageReadForUser("joesmith", new Long(1), new Long(2)));
        messageManager.deleteUnreadStatus("joesmith", new Long(1), new Long(2));
    }

    public void testFindMessageCountByTopicId() {
        DiscussionTopic topic = getDiscussionTopic();
        for (int i = 0; i < 10; i++) {
            Message message = messageManager.createMessage(typeManager.getDiscussionForumType());
            message.setApproved(Boolean.TRUE);
            message.setAuthor("nate");
            message.setTitle("a message");
            message.setBody("this is a test message");
            message.setDraft(Boolean.FALSE);
            message.setModified(new Date());
            message.setModifiedBy("jim");
            message.setTypeUuid("mess-type");
            topic.addMessage(message);
        }
        forumManager.saveDiscussionForumTopic(topic);

        assertEquals(messageManager.findMessageCountByTopicId(topic.getId()), 10);

        forumManager.deleteDiscussionForumTopic(topic);
    }

    public void testFindMessagesByTopicId() {
        DiscussionTopic topic = getDiscussionTopic();
        for (int i = 0; i < 10; i++) {
            Message message = messageManager.createMessage(typeManager.getDiscussionForumType());
            message.setApproved(Boolean.TRUE);
            message.setAuthor("nate");
            message.setTitle("a message");
            message.setBody("this is a test message");
            message.setDraft(Boolean.FALSE);
            message.setModified(new Date());
            message.setModifiedBy("jim");
            message.setTypeUuid("mess-type");
            topic.addMessage(message);
        }
        forumManager.saveDiscussionForumTopic(topic);

        assertEquals(messageManager.findMessagesByTopicId(topic.getId()).size(), 10);

        forumManager.deleteDiscussionForumTopic(topic);
    }

    public void testFindReadMessageCountByTopicId() {
        DiscussionTopic topic = getDiscussionTopic();
        for (int i = 0; i < 10; i++) {
            Message message = messageManager.createMessage(typeManager.getDiscussionForumType());
            message.setApproved(Boolean.TRUE);
            message.setAuthor("nate");
            message.setTitle("a message");
            message.setBody("this is a test message");
            message.setDraft(Boolean.FALSE);
            message.setModified(new Date());
            message.setModifiedBy("jim");
            message.setTypeUuid("mess-type");
            topic.addMessage(message);
        }
        forumManager.saveDiscussionForumTopic(topic);

        messageManager.markMessageReadForUser("nate", topic.getId(), ((Message) topic.getMessages().get(2)).getId());

        assertEquals(messageManager.findReadMessageCountByTopicId("nate", topic.getId()), 1);
        
        forumManager.deleteDiscussionForumTopic(topic);
    }

    public void testFindUnreadMessageCountByTopicId() {
        DiscussionTopic topic = getDiscussionTopic();
        for (int i = 0; i < 10; i++) {
            Message message = messageManager.createMessage(typeManager.getDiscussionForumType());
            message.setApproved(Boolean.TRUE);
            message.setAuthor("nate");
            message.setTitle("a message");
            message.setBody("this is a test message");
            message.setDraft(Boolean.FALSE);
            message.setModified(new Date());
            message.setModifiedBy("jim");
            message.setTypeUuid("mess-type");
            topic.addMessage(message);
        }
        forumManager.saveDiscussionForumTopic(topic);

        messageManager.markMessageReadForUser("nate", topic.getId(), ((Message) topic.getMessages().get(2)).getId());
        messageManager.markMessageReadForUser("nate", topic.getId(), ((Message) topic.getMessages().get(5)).getId());

        assertEquals(messageManager.findUnreadMessageCountByTopicId("nate", topic.getId()), 8);

        forumManager.deleteDiscussionForumTopic(topic);
    }

    public void testFindTopicById() {
        DiscussionTopic topic = getDiscussionTopic();
        forumManager.saveDiscussionForumTopic(topic);

        assertNotNull(forumManager.getTopicById(true,topic.getId()));

        forumManager.deleteDiscussionForumTopic(topic);
    }

    public void testFindForumById() {
        DiscussionForum forum = forumManager.createDiscussionForum();
        forum.setTypeUuid("df-type");
        forum.setTitle("A test discussion forum");
        forum.setShortDescription("short desc");
        forum.setExtendedDescription("long desc");
        forum.setSortIndex(new Integer(1));
        forum.setLocked(Boolean.FALSE);
        forumManager.saveDiscussionForum(forum);

        assertNotNull(forumManager.getForumById(true, forum.getId()));

        forumManager.deleteDiscussionForum(forum);
    }

    // helpers

    private DiscussionTopic getDiscussionTopic() {
        DiscussionTopic topic = forumManager.createDiscussionForumTopic(null);
        topic.setTypeUuid("dt-type");
        topic.setTitle("A test discussion topic");
        topic.setShortDescription("short desc");
        topic.setExtendedDescription("long desc");
        topic.setMutable(Boolean.TRUE);
        topic.setSortIndex(new Integer(1));
        topic.addAttachment(getAttachment());
        return topic;
    }

    private Attachment getAttachment() {
        Attachment attachment = new AttachmentImpl();
        attachment.setUuid("002");
        attachment.setCreated(new Date());
        attachment.setCreatedBy("nate");
        attachment.setModified(new Date());
        attachment.setModifiedBy("nate");
        attachment.setAttachmentId("a_id1");
        attachment.setAttachmentName("My First Doc.doc");
        attachment.setAttachmentSize("407KB");
        attachment.setAttachmentType("application/msword");
        attachment.setAttachmentUrl("http://www.google.com");
        return attachment;
    }

}
