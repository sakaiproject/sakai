package org.sakaiproject.api.app.messageforums;

import java.util.List;

import org.sakaiproject.api.common.type.Type;

public interface MessageForumsTypeManager
{
  /**
   * @return
   */
 // public List getAvailableTypes();
   
  /**
   * @return
   */
  public String getPrivateType();
 

  /**
   * @return
   */
  public String getDiscussionForumType();
   

  /**
   * @return
   */
  public String getOpenDiscussionForumType();
   

}