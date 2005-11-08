package org.sakaiproject.api.app.messageforums;

public interface PrivateMessageManager
{
  /**
   * Check if private message area is enabled for the current user
   * @return boolean 
   * 
   */
  boolean isPrivateAreaUnabled(); 

  /**
   * Retrieve private message area if it is enabled for the current user
   * @return
   */
  Area getPrivateArea();  
  
  /**
   * @return
   */
  Area getDiscussionForumArea();  
  

}
