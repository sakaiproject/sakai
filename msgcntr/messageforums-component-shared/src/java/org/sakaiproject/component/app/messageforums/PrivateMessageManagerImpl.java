package org.sakaiproject.component.app.messageforums;

 import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.PrivateMessageManager;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;

public class PrivateMessageManagerImpl extends HibernateDaoSupport implements PrivateMessageManager
{

  public boolean isPrivateAreaUnabled()
  {
    // TODO Auto-generated method stub
    // return isPrivateAreaUnabled(MessageForumsManager.getCurrentUserId());
    return false;
  }

  private boolean isPrivateAreaUnabled(String userId)
  {
     
    // TODO Auto-generated method stub
    return false;
  }
  
  public Area getPrivateArea()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
