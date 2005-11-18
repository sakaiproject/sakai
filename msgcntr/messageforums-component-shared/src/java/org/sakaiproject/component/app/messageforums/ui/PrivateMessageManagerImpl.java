package org.sakaiproject.component.app.messageforums.ui;

import java.util.Set;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.component.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AttachmentImpl;
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

  public void savePrivateMessage(Message message)
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

  public Attachment createPvtMsgAttachmentObject(String attachId, String name)
  {
    try
    {
      Attachment attach = new AttachmentImpl();
      
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

      savePvtMsgAttachment(attach);
      
      return attach;
    }
    catch(Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  public void savePvtMsgAttachment(Attachment attach)
  {
    // TODO Auto-generated method stub
    
  }

  public void addPvtMsgAttachToPvtMsgData(PrivateMessage pvtMsgData, Attachment pvtMsgAttach)
  {
    // TODO Auto-generated method stub
    
  }

  public void removePvtMsgAttachmentObject(Attachment o)
  {
    // TODO Auto-generated method stub
    
  }

  public void removePvtMsgAttachPvtMsgData(PrivateMessage pvtMsgData, Attachment pvtMsgAttach)
  {
    // TODO Auto-generated method stub
    
  }

  public Set getPvtMsgAttachmentsForPvtMsgData(PrivateMessage pvtMsgData)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public Attachment getPvtMsgAttachment(String pvtMsgAttachId)
  {
    // TODO Auto-generated method stub
    return null;
  }

  public int getTotalNoMessages(Topic topic)
  {
    //  TODO: Implement 
    return 20;
  }

  public int getUnreadNoMessages(Topic topic)
  {
    //  TODO: Implement
    return 10;
  }

  public void saveAreaSetting()
  {
    // TODO Sace settings like activate /forwarding email
    
  }
 

}
