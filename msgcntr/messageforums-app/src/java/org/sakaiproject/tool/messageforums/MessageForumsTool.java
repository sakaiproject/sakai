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

package org.sakaiproject.tool.messageforums;

import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsManager;
import org.sakaiproject.api.app.messageforums.proxy.TopicProxy;
import org.sakaiproject.component.app.messageforums.dao.hibernate.Topic;
import org.sakaiproject.tool.messageforums.proxy.TopicProxyImpl;

public class MessageForumsTool {
    
    private TopicProxy topicProxy;
    private MessageForumsManager messageForumsManager; 
    private ErrorMessages errorMessages;
    
    public MessageForumsTool() {
        errorMessages = new ErrorMessages();
        errorMessages.setDisplayTitleErrorMessage(false);
        
        Topic topic = new Topic();
        topic.setTitle("Dubai Port Authority Case");
        topic.setShortDescription("What scope and partners do you recommend for the proposed system?  Provide one sentence of support for your position.");
        topic.setExtendedDescription("...");
        topicProxy = new TopicProxyImpl(topic);
    }
    
    // start injections
    public void setMessageForumsManager(MessageForumsManager messageForumsManager) {
        this.messageForumsManager = messageForumsManager;
    }
       
    public MessageForumsManager getMessageForumsManager() {
        return messageForumsManager;
    }
    // end injections
    
    
    
    
    

    public String processCDFMPostMessage() {
        Message message = topicProxy.getMessageModel().createPersistible();
        messageForumsManager.saveMessage(message);
        return "compose";
    }
    
    public String processCDFMSaveDraft() {
        Message message = topicProxy.getMessageModel().createPersistible();
        message.setDraft(Boolean.TRUE);
        messageForumsManager.saveMessage(message);
        return "compose";
    }

    public String processCDFMCancel() {
        return "compose";
    }
    
    public TopicProxy getTopicProxy() {
        return topicProxy;
    }

    public ErrorMessages getErrorMessages() {
        return errorMessages;
    }
}