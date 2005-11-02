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

package org.sakaiproject.tool.messageforums.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Label;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.model.MessageModel;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessageImpl;

public class MessageModelImpl implements MessageModel {
    
    private String title;
    private String body;
    private String author;
    private List attachments;
    private String label;
    private MessageModel inReplyTo;
    private String gradebook;
    private String gradebookAssignment;
    //private Type type; 
    private Boolean approved;

    // package level constructor only used for Testing
    MessageModelImpl() {}
    
    public MessageModelImpl(Message message) {
        if (message != null) {
            this.title = message.getTitle();
            this.body = message.getBody();
            this.author = message.getAuthor();
            this.label = message.getLabel();
            if (message.getInReplyTo() != null) {
                this.inReplyTo = new MessageModelImpl(message.getInReplyTo());
            }
            this.gradebook = message.getGradebook();
            this.gradebookAssignment = message.getGradebookAssignment();
            // TODO: Copy the type
            this.approved = message.getApproved();
            this.attachments = new ArrayList();
            // TODO: Create an AttachmentModel
            for (Iterator iter = message.getAttachments().iterator(); iter.hasNext();) {
                Attachment attachment = (Attachment) iter.next();
                attachments.add(attachment);
            }
        }
    }

    public Message createPersistible() {
        Message message = new MessageImpl();
        message.setApproved(approved);
        message.setAuthor(author);
        message.setBody(body);
        message.setGradebook(gradebook);
        message.setGradebookAssignment(gradebookAssignment);
        message.setDraft(Boolean.FALSE);
        if (inReplyTo != null) {
            message.setInReplyTo(inReplyTo.createPersistible());
        }
        message.setTitle(title);
        // TODO: need to walk this and convert from AttachmentModel
        if (attachments != null) {
            message.setAttachments(new TreeSet(attachments));
        }
        return message;
    }
    
    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public List getAttachments() {
        return attachments;
    }

    public void setAttachments(List attachments) {
        this.attachments = attachments;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getGradebook() {
        return gradebook;
    }

    public void setGradebook(String gradebook) {
        this.gradebook = gradebook;
    }

    public String getGradebookAssignment() {
        return gradebookAssignment;
    }

    public void setGradebookAssignment(String gradebookAssignment) {
        this.gradebookAssignment = gradebookAssignment;
    }

    public MessageModel getInReplyTo() {
        return inReplyTo;
    }

    public void setInReplyTo(MessageModel inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

//    public Type getType() {
//        return type;
//    }
//
//    public void setType(Type type) {
//        this.type = type;
//    }

}
