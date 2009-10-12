/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/MessageImpl.java $
 * $Id: MessageImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;

public class MessageImpl extends MutableEntityImpl implements Message
{

  private static final Log LOG = LogFactory.getLog(MessageImpl.class);

  private String title;
  private String body;
  private int wordCount;
  private String author;
  private Set attachmentsSet; // = new HashSet();
  private String label;
  private Message inReplyTo;
  private String gradebook;
  private String gradebookAssignment;
  private String typeUuid;
  private Boolean approved;
  private Boolean draft;
  private Topic topic;
  private Boolean hasAttachments = Boolean.FALSE;
  private String gradeComment;
  private String gradeAssignmentName; 
  private Boolean deleted;

  public static Comparator ATTACHMENT_COMPARATOR;
  public static Comparator SUBJECT_COMPARATOR;
  public static Comparator DATE_COMPARATOR;
  public static Comparator LABEL_COMPARATOR;
  public static Comparator AUTHORED_BY_COMPARATOR;

  public static Comparator ATTACHMENT_COMPARATOR_DESC;
  public static Comparator SUBJECT_COMPARATOR_DESC;
  public static Comparator DATE_COMPARATOR_DESC;
  public static Comparator LABEL_COMPARATOR_DESC;
  public static Comparator AUTHORED_BY_COMPARATOR_DESC;
  
  // indecies for hibernate
  //private int tindex;

  public Topic getTopic()
  {
    return topic;
  }

  public void setTopic(Topic topic)
  {
    this.topic = topic;
  }

  public MessageImpl()
  {
    attachmentsSet = new HashSet();
  }

  public Boolean getDraft()
  {
    return draft;
  }

  public void setDraft(Boolean draft)
  {
    this.draft = draft;
  }

  public Boolean getHasAttachments()
  {
    return hasAttachments;
  }

  public void setHasAttachments(Boolean hasAttachments)
  {
    this.hasAttachments = hasAttachments;
  }
  
  public Boolean getApproved()
  {
    return approved;
  }

  public void setApproved(Boolean approved)
  {
    this.approved = approved;
  }

  public Set getAttachmentsSet() {
      return attachmentsSet;
  }

  public void setAttachmentsSet(Set attachmentsSet) {
      this.attachmentsSet = attachmentsSet;
  }
  
  public List getAttachments()
  {
    return Util.setToList(attachmentsSet);
  }

  public void setAttachments(List attachments)
  {
    this.attachmentsSet = Util.listToSet(attachments);
  }

  public String getAuthor()
  {
    return author;
  }

  public void setAuthor(String author)
  {
    this.author = author;
  }

  public String getBody()
  {
    return body;
  }

  public void setBody(String body)
  {
    this.body = body;
  }

  public String getGradebook()
  {
    return gradebook;
  }

  public void setGradebook(String gradebook)
  {
    this.gradebook = gradebook;
  }

  public String getGradebookAssignment()
  {
    return gradebookAssignment;
  }

  public void setGradebookAssignment(String gradebookAssignment)
  {
    this.gradebookAssignment = gradebookAssignment;
  }

  public Message getInReplyTo()
  {
    return inReplyTo;
  }

  public void setInReplyTo(Message inReplyTo)
  {
    this.inReplyTo = inReplyTo;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getTypeUuid()
  {
    return typeUuid;
  }

  public void setTypeUuid(String typeUuid)
  {
    this.typeUuid = typeUuid;
  }

  public Boolean getDeleted() {
	  return deleted;
  }

  public void setDeleted(Boolean deleted) 
  {
	this.deleted = deleted;
  }

  public String toString()
  {
  	return "Message/" + id;
    //return "Message.id:" + id;
  }
  
  public boolean equals(Object obj) {
      if (obj != null && obj instanceof Message) {
          return getId().equals(((Message)obj).getId());
      }
      return false;
  }

  // needs a better impl
  public int hashCode() {
      return getId() == null ? 0 : getId().hashCode();
  }

  // //////////////////////////////////////////////////////////////////////
  // helper methods for collections
  // //////////////////////////////////////////////////////////////////////



  public void addAttachment(Attachment attachment)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("addAttachment(Attachment " + attachment + ")");
    }

    if (attachment == null)
    {
      throw new IllegalArgumentException("attachment == null");
    }

    attachment.setMessage(this);
    attachmentsSet.add(attachment);
    
    if (!hasAttachments.booleanValue()){
      hasAttachments = Boolean.TRUE;
    }
    
  }

  public void removeAttachment(Attachment attachment)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("removeAttachment(Attachment " + attachment + ")");
    }

    if (attachment == null)
    {
      throw new IllegalArgumentException("Illegal attachment argument passed!");
    }

    attachment.setMessage(null);
    attachmentsSet.remove(attachment);  
    
    if (attachmentsSet.size() == 0){
      hasAttachments = Boolean.FALSE;
    }
  }

//  public int getTindex()
//  {
//    try
//    {
//      return getTopic().getMessages().indexOf(this);
//    }
//    catch (Exception e)
//    {
//      return tindex;
//    }
//  }
//
//  public void setTindex(int tindex)
//  {
//    this.tindex = tindex;
//  }

  // ============================================
  static
  {
    SUBJECT_COMPARATOR = new Comparator()
    {
      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          String msg = ((Message) message).getTitle();
          String msg2 = ((Message) otherMessage).getTitle();
          return msg.compareTo(msg2);
        }
        return -1;

      }
    };

    DATE_COMPARATOR = new Comparator()
    {
      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          Date msg = ((Message) message).getCreated();
          Date msg2 = ((Message) otherMessage).getCreated();
          return msg.compareTo(msg2);
        }
        return -1;
      }
    };

    AUTHORED_BY_COMPARATOR = new Comparator()
    {
      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          String msg = ((Message) message).getAuthor();
          String msg2 = ((Message) otherMessage).getAuthor();
          return msg.compareTo(msg2);
        }
        return -1;
      }
    };

    LABEL_COMPARATOR = new Comparator()
    {

      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          String msg = ((Message) message).getLabel();
          String msg2 = ((Message) otherMessage).getLabel();
          return msg.compareTo(msg2);
        }
        return -1;
      }

    };

    
    // TODO: make more generic and reuse the above
    
    SUBJECT_COMPARATOR_DESC = new Comparator()
    {
      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          String msg2 = ((Message) message).getTitle();
          String msg = ((Message) otherMessage).getTitle();
          return msg.compareTo(msg2);
        }
        return -1;

      }
    };

    DATE_COMPARATOR_DESC = new Comparator()
    {
      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          Date msg2 = ((Message) message).getCreated();
          Date msg = ((Message) otherMessage).getCreated();
          return msg.compareTo(msg2);
        }
        return -1;
      }
    };

    AUTHORED_BY_COMPARATOR_DESC = new Comparator()
    {
      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          String msg2 = ((Message) message).getAuthor();
          String msg = ((Message) otherMessage).getAuthor();
          return msg.compareTo(msg2);
        }
        return -1;
      }
    };

    LABEL_COMPARATOR_DESC = new Comparator()
    {

      public int compare(Object message, Object otherMessage)
      {
        if (message != null && otherMessage != null
            && message instanceof Message && otherMessage instanceof Message)
        {
          String msg2 = ((Message) message).getLabel();
          String msg = ((Message) otherMessage).getLabel();
          return msg.compareTo(msg2);
        }
        return -1;
      }

    };

  }

  public String getGradeComment()
  {
    return gradeComment;
  }

  public void setGradeComment(String gradeComment)
  {
    this.gradeComment = gradeComment;
  }

  public String getGradeAssignmentName()
  {
    return gradeAssignmentName;
  }

  public void setGradeAssignmentName(String gradeAssignmentName)
  {
    this.gradeAssignmentName = gradeAssignmentName;
  }

public int getWordCount() {
	return wordCount;
}

public void setWordCount(int wordCount) {
	this.wordCount = wordCount;
}

}
