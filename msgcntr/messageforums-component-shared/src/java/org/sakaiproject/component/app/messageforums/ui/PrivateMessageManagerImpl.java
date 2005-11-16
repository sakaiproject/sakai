package org.sakaiproject.component.app.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.component.app.messageforums.MessageForumsMessageManager;
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

 

}
