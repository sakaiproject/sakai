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

package org.sakaiproject.tool.messageforums.proxy;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.model.MessageModel;
import org.sakaiproject.api.app.messageforums.proxy.TopicProxy;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;
import org.sakaiproject.component.app.messageforums.model.MessageModelImpl;

/*
 * TopicProxy provides all methods needed by a jsf page.  It is merely
 * a helper to get to topic fields, concatenations of fields, etc.
 */
public class TopicProxyImpl implements TopicProxy {

    private Topic topic;
    private MessageModel messageModel;   
    
    public TopicProxyImpl(Topic topic) {
        this.topic = topic;
        
        Message message = new MessageImpl();
        
        message.setApproved(new Boolean(true));
        message.setAuthor("nate");
        
        AttachmentImpl attachment = new AttachmentImpl();
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
        
        Set attachments = new TreeSet();
        attachments.add(attachment);
        message.setAttachments(attachments);
        
        messageModel = new MessageModelImpl(message);
    }
    
    public MessageModel getMessageModel() {
        return messageModel;
    }
    
    public String getCrumbTrail() {
        // TODO: append the forum title here
        return "Case Studies -- " + topic.getTitle();
    }

    public String getShortDescription() {
        return topic.getShortDescription();
    }
    
}
