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
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionForumImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.DiscussionTopicImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;

public class MessageForumsManagerTest extends ForumsApplicationContextBaseTest {
    
    private MessageForumsForumManager messageForumsForumManager; 
    private MessageForumsMessageManager messageForumsMessageManager;
    private AreaManager messageForumsAreaManager;
 
    public MessageForumsManagerTest() {
        super();
        init();
    }

    public MessageForumsManagerTest(String name) {
        super(name);
        init();
    }

    private void init() {
        messageForumsForumManager = (MessageForumsForumManager) getApplicationContext().getBean(MessageForumsForumManager.class.getName());
        messageForumsMessageManager = (MessageForumsMessageManager) getApplicationContext().getBean(MessageForumsMessageManager.class.getName());
        messageForumsAreaManager = (AreaManager) getApplicationContext().getBean(AreaManager.class.getName());
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
 
    public void testSaveAndDeleteMessage() {
        Message message = new MessageImpl();        
        message.setApproved(Boolean.TRUE);
        message.setAuthor("nate");
        message.setTitle("a message");
        message.setBody("this is a test message");
        message.setDraft(Boolean.FALSE);
        message.setTypeUuid("mess-type");
        message.addAttachment(getAttachment());        

        messageForumsMessageManager.saveMessage(message);
        Long id = message.getId();
        messageForumsMessageManager.deleteMessage(message);
        Message message2 = messageForumsMessageManager.getMessageById(id.toString());
        assertTrue("message2 should not exist", message2 == null);
    }

    public void testSaveAndDeleteTopic() {
        DiscussionTopic topic = getDiscussionTopic();        
        messageForumsForumManager.saveDiscussionForumTopic(topic);
        messageForumsForumManager.deleteDiscussionForumTopic(topic);
        assertTrue(true);
    }
    
    public void testSaveAndDeleteForum() {
        DiscussionTopic topic = getDiscussionTopic();        
        topic.setUuid("00322");
        topic.setCreated(new Date());
        topic.setCreatedBy("ed");
        topic.setModified(new Date());
        topic.setModifiedBy("jim");
        
        DiscussionForum forum = new DiscussionForumImpl();
        forum.setTypeUuid("df-type");
        forum.setTitle("A test discussion forum");
        forum.setShortDescription("short desc");
        forum.setExtendedDescription("long desc");
        forum.setSortIndex(new Integer(1));
        forum.setLocked(Boolean.FALSE);
        forum.addAttachment(getAttachment());
        forum.addTopic(topic);
        
        messageForumsForumManager.saveDiscussionForum(forum);
        messageForumsForumManager.deleteDiscussionForum(forum);
        assertTrue(true);
    }
    
    public void testSaveAndDeleteArea() {
        DiscussionTopic topic = getDiscussionTopic();        
        topic.setUuid("00322");
        topic.setCreated(new Date());
        topic.setCreatedBy("ed");
        topic.setModified(new Date());
        topic.setModifiedBy("jim");
        
        DiscussionForum forum = new DiscussionForumImpl();
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

        Area area = new AreaImpl();   
        area.setContextId("area-context-id");
        area.setEnabled(Boolean.TRUE);
        area.setHidden(Boolean.FALSE);
        area.setName("a test area");
        area.setTypeUuid("discussion area type");
        area.addDiscussionForum(forum);
                
        messageForumsAreaManager.saveArea(area);
        messageForumsAreaManager.deleteArea(area);
        assertTrue(true);
    }
    
    public void testIsMessageReadForUser() {
        assertFalse(messageForumsMessageManager.isMessageReadForUser("joesmith", "1", "2"));
        messageForumsMessageManager.markMessageReadForUser("joesmith", "1", "2");
        assertTrue(messageForumsMessageManager.isMessageReadForUser("joesmith", "1", "2"));        
        // clean up
        messageForumsMessageManager.deleteUnreadStatus("joesmith", "1", "2");
    }
       
    
    // helpers
    
    private DiscussionTopic getDiscussionTopic() {
        DiscussionTopic topic = new DiscussionTopicImpl();
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
