package org.sakaiproject.component.app.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.component.app.messageforums.MessageForumsAreaManager;
import org.sakaiproject.component.app.messageforums.MessageForumsMessageManager;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

;

public class DiscussionForumManagerImpl extends HibernateDaoSupport implements
    DiscussionForumManager
{

  private MessageForumsAreaManager areaManager;
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

  public MessageForumsAreaManager getAreaManager()
  {
    return areaManager;
  }

  public void setAreaManager(MessageForumsAreaManager areaManager)
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

  public Area getDiscussionForumArea()
  {
    if (usingHelper)
    {
      return helper.getDiscussionForumArea();
    }
    // TODO: Implement Me!
    throw new UnsupportedOperationException();
  }

  public Message getMessageById(String id)
  {
    return messageManager.getMessageById(id);
  }

  public void saveMessage(Message message)
  {
    messageManager.saveMessage(message);
  }

  public void deleteMessage(Message message)
  {
    messageManager.deleteMessage(message);
  }

  public int getTotalNoMessages(Topic topic)
  {
     return 20;
  }

  public int getUnreadNoMessages(Topic topic)
  {
    return 10;
  }

}
