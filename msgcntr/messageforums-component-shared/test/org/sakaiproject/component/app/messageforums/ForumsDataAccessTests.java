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

package org.sakaiproject.component.app.messageforums;

import java.util.Date;
import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.exception.IdUnusedException;

public class ForumsDataAccessTests extends ForumsDataAccessBaseTest {
        
    public ForumsDataAccessTests() {
        super();
        init();
    }

    public ForumsDataAccessTests(String name) {
        super(name);
        init();
    }

    private void init() {
    }

    protected void setUp() throws Exception {
        super.setUp();
        TestUtil.setRunningTests(true);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        TestUtil.setRunningTests(false);
    }

        
    public void testSendPrivateMessage(){
      DiscussionTopic topic = getDiscussionTopic();      
      PrivateMessage message = privateMessageManager.createPrivateMessage();
      message.setApproved(Boolean.TRUE);
      message.setAuthor("jlannan");
      message.setTitle("a message");
      message.setBody("this is a test message");
      message.setDraft(Boolean.FALSE);
      message.setModified(new Date());
      message.setModifiedBy("jarrod");
      message.setTypeUuid("mess-type");
      topic.addMessage(message);
      
      forumManager.saveDiscussionForumTopic(topic);
      
      List recipients = new UniqueArrayList();
      
      recipients.add("dman");
      recipients.add("pman");
      recipients.add("qman");
      
      try{
        privateMessageManager.sendPrivateMessage(message, recipients);  
      }
      catch (IdUnusedException e){
        e.printStackTrace();
      }
      
                 
    }

    // helpers        

    private DiscussionTopic getDiscussionTopic() {
        DiscussionTopic topic = forumManager.createDiscussionForumTopic();
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
