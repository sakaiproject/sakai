package org.sakaiproject.api.app.messageforums;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public interface MessageForumsTypeManager
{
  /**
   * @return
   */
 // public List getAvailableTypes();
   
  /**
   * @return
   */
  public String getPrivateMessageAreaType();
 

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
  
  /**
   * @return
   */
  public String getRoleType();
  
  /**
   * @return
   */
  public String getGroupType();
  
  /**
   * @return
   */
  public String getSiteParticipantType();
  
  /**
   * @return
   */
  public String getAllParticipantType();
 
  /**
   * @return
   */
  public String getAllInstructorsType();
  
  /**
   * @return
   */
  public String getNotSpecifiedType();
  
  
  
  
 
}