package org.sakaiproject.tool.messageforums.ui;
import java.util.Date;
import java.util.List;

import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Topic;
public class PrivateTopicBean implements Topic
{

  private Topic topic;
  public PrivateTopicBean(Topic topic)
  {
   this.topic= topic;    
  }

  public List getAttachments()
  {
    return topic.getAttachments();
  }

  public void setAttachments(List attachments)
  {
    // TODO Auto-generated method stub
  }

  public String getExtendedDescription()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setExtendedDescription(String extendedDescription)
  {
    // TODO Auto-generated method stub
    
  }

  public Boolean getMutable()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setMutable(Boolean mutable)
  {
    // TODO Auto-generated method stub
    
  }

  public String getShortDescription()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setShortDescription(String shortDescription)
  {
    // TODO Auto-generated method stub
    
  }

  public Integer getSortIndex()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setSortIndex(Integer sortIndex)
  {
    // TODO Auto-generated method stub
    
  }

  public String getTitle()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setTitle(String title)
  {
    // TODO Auto-generated method stub
    
  }

  public String getTypeUuid()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setTypeUuid(String typeUuid)
  {
    // TODO Auto-generated method stub
    
  }

  public BaseForum getBaseForum()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setBaseForum(BaseForum forum)
  {
    // TODO Auto-generated method stub
    
  }

  public List getMessages()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setMessages(List messages)
  {
    // TODO Auto-generated method stub
    
  }

  public Date getCreated()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setCreated(Date created)
  {
    // TODO Auto-generated method stub
    
  }

  public String getCreatedBy()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setCreatedBy(String createdBy)
  {
    // TODO Auto-generated method stub
    
  }

  public Long getId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setId(Long id)
  {
    // TODO Auto-generated method stub
    
  }

  public String getModifiedBy()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setModifiedBy(String modifiedBy)
  {
    // TODO Auto-generated method stub
    
  }

  public Date getModified()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setModified(Date modified)
  {
    // TODO Auto-generated method stub
    
  }

  public String getUuid()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setUuid(String uuid)
  {
    // TODO Auto-generated method stub
    
  }

  public Integer getVersion()
  {
    // TODO Auto-generated method stub
    return null;
  }

  public void setVersion(Integer version)
  {
    // TODO Auto-generated method stub
    
  }

}
