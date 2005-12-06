package org.sakaiproject.component.app.messageforums.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.component.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.service.legacy.content.ContentResource;
import org.sakaiproject.service.legacy.content.cover.ContentHostingService;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;



public class PrivateMessageManagerImpl extends HibernateDaoSupport implements
    PrivateMessageManager
{

  private AreaManager areaManager;
  private MessageForumsMessageManager messageManager;
  private DummyDataHelperApi helper;
  private boolean usingHelper = true; // just a flag until moved to database from helper

  public void init()
  {
    ;
  }

  // start injection
  public void setHelper(DummyDataHelperApi helper)
  {
    this.helper = helper;
  }

  public AreaManager getAreaManager()
  {
    return areaManager;
  }

  public void setAreaManager(AreaManager areaManager)
  {
    this.areaManager = areaManager;
  }

  public MessageForumsMessageManager getMessageManager()
  {
    return messageManager;
  }

  public void setMessageManager(MessageForumsMessageManager messageManager)
  {
    this.messageManager = messageManager;
  }

  // end injection

  public boolean isPrivateAreaUnabled()
  {
    if (usingHelper)
    {
      return helper.isPrivateAreaUnabled();
    }
    return areaManager.isPrivateAreaEnabled();
  }

  public Area getPrivateArea()
  {
    if (usingHelper)
    {
      return helper.getPrivateArea();
    }
    return areaManager.getPrivateArea();
  }

  public void savePrivateMessage(Message message, String userId, List recipients)
  {
    messageManager.saveMessage(message);
  }

  public void deletePrivateMessage(Message message)
  {
    messageManager.deleteMessage(message);
  }

  public Message getMessageById(String id)
  {
    return messageManager.getMessageById(id);
  }

  
  //Attachment
  public Attachment createPvtMsgAttachment(String attachId, String name)
  {
    try
    {
      Attachment attach = messageManager.createAttachment();
      
      attach.setAttachmentId(attachId);
      
      attach.setAttachmentName(name);

      ContentResource cr = ContentHostingService.getResource(attachId);
      attach.setAttachmentSize((new Integer(cr.getContentLength())).toString());
      attach.setCreatedBy(cr.getProperties().getProperty(cr.getProperties().getNamePropCreator()));
      attach.setModifiedBy(cr.getProperties().getProperty(cr.getProperties().getNamePropModifiedBy()));
      attach.setAttachmentType(cr.getContentType());
      String tempString = cr.getUrl();
      String newString = new String();
      char[] oneChar = new char[1];
      for(int i=0; i<tempString.length(); i++)
      {
        if(tempString.charAt(i) != ' ')
        {
          oneChar[0] = tempString.charAt(i);
          String concatString = new String(oneChar);
          newString = newString.concat(concatString);
        }
        else
        {
          newString = newString.concat("%20");
        }
      } 
      //tempString.replaceAll(" ", "%20");
      attach.setAttachmentUrl(newString);

      return attach;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  // Himansu: I am not quite sure this is what you want... let me know.
  // Before saving a message, we need to add all the attachmnets to a perticular message
  public void addAttachToPvtMsg(PrivateMessage pvtMsgData, Attachment pvtMsgAttach)
  {
    pvtMsgData.addAttachment(pvtMsgAttach);    
  }

  // Required for editing multiple attachments to a message. 
  // When you reply to a message, you do have option to edit attachments to a message
  public void removePvtMsgAttachment(Attachment o)
  {
    o.getMessage().removeAttachment(o);    
  }

  public Attachment getPvtMsgAttachment(String pvtMsgAttachId)
  {
    return messageManager.getAttachmentById(pvtMsgAttachId);
  }

  public int getTotalNoMessages(Topic topic)
  {
    return messageManager.findMessageCountByTopicId(topic.getId().toString());
  }

  public int getUnreadNoMessages(String userId, Topic topic)
  {
    return messageManager.findUnreadMessageCountByTopicId(userId, topic.getId().toString());
  }

  /**
   * Area Setting
   */
  public void saveAreaSetting()
  {
    // TODO Sace settings like activate /forwarding email
    
  }
  
  /**
   * Topic Folder Setting
   */
  public boolean isMutableTopicFolder(String parentTopicId)
  {
    return false;
  }

  
  public String createTopicFolderInForum(String parentForumId, String userId, String name) {
      return null;
  }
  
  public String createTopicFolderInTopic(String parentTopicId, String userId, String name) {
      return null;
  }
  
  public String renameTopicFolder(String parentTopicId, String userId, String newName)
  {
    return null;
  }
  
  public void deleteTopicFolder(String topicId) {
      
  }

  public PrivateMessage createPrivateMessage()
  {
    return messageManager.createPrivateMessage();
  }

  
  
  
  
  public boolean hasNextMessage(PrivateMessage message)
  {
    // TODO: Needs optimized
    boolean next = false;
    if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
        for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
            Message m = (Message) iter.next();
            if (next) {
                return true;
            }
            if (m.getId().equals(message.getId())) {
                next = true;
            }
        }
    }

    // if we get here, there is no next message
    return false;
  }

  public boolean hasPreviousMessage(PrivateMessage message)
  {
      // TODO: Needs optimized
      PrivateMessage prev = null;
      if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
          for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
              Message m = (Message) iter.next();
              if (m.getId().equals(message.getId())) {
                  // need to check null because we might be on the first message
                  // which means there is no previous one
                  return prev != null;
              }
              prev = (PrivateMessage)m;
          }
      }

      // if we get here, there is no previous message
      return false; 
  }

  public PrivateMessage getNextMessage(PrivateMessage message)
  {
      // TODO: Needs optimized
      boolean next = false;
      if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
          for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
              Message m = (Message) iter.next();
              if (next) {
                  return (PrivateMessage) m;
              }
              if (m.getId().equals(message.getId())) {
                  next = true;
              }
          }
      }

      // if we get here, there is no next message
      return null;
  }

  public PrivateMessage getPreviousMessage(PrivateMessage message)
  {
      // TODO: Needs optimized
      PrivateMessage prev = null;
      if (message != null && message.getTopic() != null && message.getTopic().getMessages() != null) {
          for (Iterator iter = message.getTopic().getMessages().iterator(); iter.hasNext();) {
              Message m = (Message) iter.next();
              if (m.getId().equals(message.getId())) {
                  return prev;
              }
              prev = (PrivateMessage)m;
          }
      }

      // if we get here, there is no previous message
      return null; 
  }

  public List getReceivedMessages(String userId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getSentMessages(String userId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getDeletedMessages(String userId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getDraftedMessages(String userId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public List getMessagesByTopic(String userId, String topicId)
  {
    // TODO Auto-generated method stub
    return null;
  }
  
}
