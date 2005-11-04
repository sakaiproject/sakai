package org.sakaiproject.api.app.messageforums;

import java.util.List;

import org.sakaiproject.api.common.type.Type;

public interface MessageForumsTypeManager
{
  /**
   * @return
   */
  public List getAvailableTypes();
   
  /**
   * @return
   */
  public Type getPrivateType();
 

  /**
   * @return
   */
  public org.sakaiproject.api.common.type.Type getDiscussionForumType();
   

  /**
   * @return
   */
  public org.sakaiproject.api.common.type.Type getOpenDiscussionForumType();
   

}