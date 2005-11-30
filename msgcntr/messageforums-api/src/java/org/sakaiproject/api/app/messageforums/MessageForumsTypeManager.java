package org.sakaiproject.api.app.messageforums;

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
   
  
  /**
   * @return
   */
  public String getReceivedPrivateMessageType();
  
  
  /**
   * @return
   */
  public String getSentPrivateMessageType();
  
  /**
   * @return
   */
  public String getDeletedPrivateMessageType();
  
  /**
   * @return
   */
  public String getDraftPrivateMessageType();
  
}