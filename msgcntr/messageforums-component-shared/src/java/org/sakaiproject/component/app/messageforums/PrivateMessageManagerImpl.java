package org.sakaiproject.component.app.messageforums;

 import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.PrivateMessageManager;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
import org.sakaiproject.api.app.messageforums.DummyDataHelperApi;;

public class PrivateMessageManagerImpl extends HibernateDaoSupport implements PrivateMessageManager
{
 
  
//  DummyDataHelperApi helper =  new DummyDataHelper();
  
  private DummyDataHelperApi helper; 
  public boolean isPrivateAreaUnabled()
  {
    // TODO Auto-generated method stub
    // return isPrivateAreaUnabled(MessageForumsManager.getCurrentUserId());
    return helper.isPrivateAreaUnabled();
  }

  public void init()
  {
    ;
  }
 
  private boolean isPrivateAreaUnabled(String userId)
  {
     //TODO:     
    return false;
  }
  
  public Area getPrivateArea()
  {
    return helper.getPrivateArea();
  }

  public Area getDiscussionForumArea()
  {    
    return helper.getDiscussionForumArea();
  }
  
  public void setHelper(DummyDataHelperApi helper)
  {
    this.helper = helper;
  }
  

}
