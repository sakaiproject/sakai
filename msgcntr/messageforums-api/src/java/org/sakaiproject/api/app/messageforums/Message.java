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

import java.util.Set;

//import org.sakaiproject.component.app.messageforums.dao.hibernate.Type;

// TODO: Needs to be able to get to the MutableEntity stuff
// TODO: Make Type an interface too

public interface Message extends MutableEntity {

    public Boolean getDraft();
    public void setDraft(Boolean draft);
    public Boolean getApproved();
    public void setApproved(Boolean approved);
    public Set getAttachments();
    public void setAttachments(Set attachments);
    public String getAuthor();
    public void setAuthor(String author);
    public String getBody();
    public void setBody(String body);
    public String getGradebook();
    public void setGradebook(String gradebook);
    public String getGradebookAssignment();
    public void setGradebookAssignment(String gradebookAssignment);
    public Message getInReplyTo();
    public void setInReplyTo(Message inReplyTo);
    public String getLabel();
    public void setLabel(String label);
    public String getTitle();
    public void setTitle(String title);
    public String getTypeUuid();
    public void setTypeUuid(String typeUuid); 
    public void setTopic(Topic topic);
    public Topic getTopic();

}