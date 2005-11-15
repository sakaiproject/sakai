/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.messageforums.ui;

import java.util.Date;
import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;

public class PrivateMessageDecoratedBean implements PrivateMessage
{

  private PrivateMessage msg;
  public PrivateMessageDecoratedBean(PrivateMessage msg)
  {
    this.msg=msg ;
  }
  
  //Wrapper for JSF Selection
  private boolean isSelected;
  public boolean getIsSelected() {
    return isSelected;
  }
  public void setIsSelected(boolean isSelected) {
    this.isSelected=isSelected ;    
  }
  
  
  public PrivateMessage getMessage()
  {
    return msg;
  }
  
  public Boolean getExternalEmail()
  {
    return msg.getExternalEmail();
  }

  public void setExternalEmail(Boolean externalEmail)
  {
    msg.setExternalEmail(externalEmail);
  }

  public String getExternalEmailAddress()
  {
    return msg.getExternalEmailAddress();
  }

  public void setExternalEmailAddress(String externalEmailAddress)
  {
    msg.setExternalEmailAddress(externalEmailAddress);
  }

  public List getRecipients()
  {
    return msg.getRecipients();
  }

  public void setRecipients(List recipients)
  {
    msg.setRecipients(recipients);
  }

  public Boolean getDraft()
  {
    return msg.getDraft();
  }

  public void setDraft(Boolean draft)
  {
    msg.setDraft(draft);
  }

  public Boolean getApproved()
  {
    return msg.getApproved();
  }

  public void setApproved(Boolean approved)
  {
    msg.setApproved(approved);
  }

  public List getAttachments()
  {
    return msg.getAttachments();
  }

  public void setAttachments(List attachments)
  {
    msg.setAttachments(attachments) ;
  }

  public String getAuthor()
  {
   return msg.getAuthor();
  }

  public void setAuthor(String author)
  {
    msg.setAuthor(author) ;
  }

  public String getBody()
  {
    return msg.getBody();
  }

  public void setBody(String body)
  {
    msg.setBody(body) ;
  }

  public String getGradebook()
  {
    return msg.getGradebook();
  }

  public void setGradebook(String gradebook)
  {
    msg.setGradebook(gradebook) ;
  }

  public String getGradebookAssignment()
  {
    return msg.getGradebookAssignment();
  }

  public void setGradebookAssignment(String gradebookAssignment)
  {
    msg.setGradebookAssignment(gradebookAssignment);
  }

  public Message getInReplyTo()
  {
    return msg.getInReplyTo();
  }

  public void setInReplyTo(Message inReplyTo)
  {
    msg.setInReplyTo(inReplyTo) ;
  }

  public String getLabel()
  {
    return msg.getLabel();
  }

  public void setLabel(String label)
  {
    msg.setLabel(label) ;
  }

  public String getTitle()
  {
    return msg.getTitle();
  }

  public void setTitle(String title)
  {
    msg.setTitle(title) ;
  }

  public String getTypeUuid()
  {
    return msg.getTypeUuid();
  }

  public void setTypeUuid(String typeUuid)
  {
    msg.setTypeUuid(typeUuid) ;
  }

  public void setTopic(Topic topic)
  {
    msg.setTopic(topic) ;
  }

  public Topic getTopic()
  {
    return msg.getTopic();
  }

  //TODO 
  public void addAttachment(Attachment attachment)
  {
    
  }

  public void removeAttachment(Attachment attachment)
  {
    // TODO Auto-generated method stub
    
  }

  public Date getCreated()
  {
    return msg.getCreated();
  }

  public void setCreated(Date created)
  {
    msg.setCreated(created) ;
  }

  public String getCreatedBy()
  {
    return msg.getCreatedBy();
  }

  public void setCreatedBy(String createdBy)
  {
    msg.setCreatedBy(createdBy) ;
  }

  public Long getId()
  {
    return msg.getId();
  }

  public void setId(Long id)
  {
    msg.setId(id) ;
  }

  public String getModifiedBy()
  {
    return msg.getModifiedBy() ;
  }

  public void setModifiedBy(String modifiedBy)
  {
    msg.setModifiedBy(modifiedBy) ;
  }

  public Date getModified()
  {
    return msg.getModified();
  }

  public void setModified(Date modified)
  {
    msg.setModified(modified) ;
  }

  public String getUuid()
  {
    return msg.getUuid();
  }

  public void setUuid(String uuid)
  {
    msg.setUuid(uuid) ;
  }

  public Integer getVersion()
  {
    return msg.getVersion();
  }

  public void setVersion(Integer version)
  {
    msg.setVersion(version) ;
  }

}

/**********************************************************************************
*
* $Header$
*
**********************************************************************************/