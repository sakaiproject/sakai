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

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.PrivateTopicImpl;

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

        
//    public void testSendPrivateMessage(){
//      PrivateTopic topic = getPrivateTopic();      
//      PrivateMessage message = privateMessageManager.createPrivateMessage(
//        typeManager.getDraftPrivateMessageType());
//      message.setApproved(Boolean.TRUE);
//      message.setAuthor("jlannan");
//      message.setTitle("a message");
//      message.setBody("this is a test message");
//      message.setDraft(Boolean.FALSE);
//      message.setModified(new Date());
//      message.setModifiedBy("jarrod");
//      message.setTypeUuid("mess-type");
//      topic.addMessage(message);
//      
//      forumManager.savePrivateForumTopic(topic);
//                  
//      List recipients = new UniqueArrayList();
//      
//      recipients.add("dman");
//      recipients.add("pman");
//      recipients.add("qman"); 
//      /** add sender as recipient */
//      recipients.add("test-user");
//                              
//      privateMessageManager.sendPrivateMessage(message, recipients);                  
//      assertEquals(privateMessageManager.findMessageCount(topic.getId(),
//        typeManager.getSentPrivateMessageType()),1);
//      
//      assertEquals(privateMessageManager.findMessageCount(topic.getId(),
//          typeManager.getReceivedPrivateMessageType()),1);
//      
//      privateMessageManager.deletePrivateMessage(message);
//      assertEquals(privateMessageManager.findMessageCount(topic.getId(),
//          typeManager.getDeletedPrivateMessageType()),1);
//      
//      /** test getMessagesByType */
//      // todo: add sorting fields as constnats
//      List messages = privateMessageManager.getDeletedMessages(
//        PrivateMessageManager.SORT_COLUMN_DATE, PrivateMessageManager.SORT_ASC);
//      
//      System.out.println(messages);
//      
//    }
    
    
    public void testRelation(){
      Area area = areaManager.getPrivateArea();
      area.setName("aaaaaaaaaaaaaa");      
      area.setEnabled(Boolean.TRUE);
      area.setHidden(Boolean.TRUE);
      areaManager.saveArea(area);
      
      PrivateForum pf = forumManager.createPrivateForum();
      pf.setTitle("test-user" + " private forum");
      pf.setUuid("test-user");
      
      forumManager.savePrivateForum(pf);
      
      area.addPrivateForum(pf);
      
      areaManager.saveArea(area);
      
      
    }
    
//    public void testCreateUserPrivateForumAndDefaultTopics(){
//      
//      //Area area = areaManager.getAreaByContextIdAndTypeId(typeManager.getPrivateMessageAreaType());
//      Area area = areaManager.getPrivateArea();
//      //Area area = areaManager.createArea(typeManager.getPrivateMessageAreaType());
////      area.setName("PrivateMessageArea");
////      area.setName("Private Area");
////      area.setEnabled(Boolean.TRUE);
////      area.setHidden(Boolean.TRUE);
////      areaManager.saveArea(area);
//      
//      // subst getCurrentUser() for test-user
//           
//      if (forumManager.getForumByOwner("test-user") == null)      
//      {
//        PrivateForum pf = forumManager.createPrivateForum();
//        pf.setTitle("test-user" + " private forum");
//        pf.setUuid("test-user");
//        
//        forumManager.savePrivateForum(pf);
//                        
//        PrivateTopic receivedTopic = forumManager.createPrivateForumTopic(true, "test-user", pf.getId());
//        receivedTopic.setTitle("Received");
//        forumManager.savePrivateForumTopic(receivedTopic);
//        
//        PrivateTopic sentTopic = forumManager.createPrivateForumTopic(true, "test-user", pf.getId());
//        sentTopic.setTitle("Sent");
//        forumManager.savePrivateForumTopic(receivedTopic);
//        
//        PrivateTopic deletedTopic = forumManager.createPrivateForumTopic(true, "test-user", pf.getId());
//        deletedTopic.setTitle("Deleted");
//        forumManager.savePrivateForumTopic(receivedTopic);
//        
//        PrivateTopic draftTopic = forumManager.createPrivateForumTopic(true, "test-user", pf.getId());
//        draftTopic.setTitle("Drafts");
//        
//        forumManager.savePrivateForumTopic(receivedTopic);
//        forumManager.savePrivateForumTopic(sentTopic);
//        forumManager.savePrivateForumTopic(deletedTopic);
//        forumManager.savePrivateForumTopic(draftTopic);        
//                                                        
//      }            
//      
//    }

    // helpers        

    private PrivateTopic getPrivateTopic() {
      
        PrivateTopic topic = new PrivateTopicImpl();
        //PrivateTopic topic = forumManager.savePrivateForumTopic(            
        //  false, "jlannan", null);
        topic.setUuid("00001");
        topic.setCreated(new Date());
        topic.setCreatedBy("jlannan");
        topic.setModified(new Date());
        topic.setModifiedBy("jlannan");
        topic.setTypeUuid("dt-type");
        topic.setTitle("A test private topic");
        topic.setShortDescription("short desc");
        topic.setExtendedDescription("long desc");
        topic.setMutable(Boolean.TRUE);
        topic.setSortIndex(new Integer(1));
        topic.addAttachment(getAttachment());
        forumManager.savePrivateForumTopic(topic);
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
