package org.sakaiproject.api.app.messageforums;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public interface MessageForumsTypeManager
{
	public String getOwnerLevelType();
	
	public String getAuthorLevelType();
	
	public String getNoneditingAuthorLevelType();
	
	public String getReviewerLevelType();
	
	public String getContributorLevelType();
	
	public String getNoneLevelType();
	
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
  public String getUserType(); 
  
  /**
   * @return
   */
  public String getAllParticipantType(); 
  
  /**
   * @return
   */
  public String getNotSpecifiedType();
 
  public String getCustomTopicType(String topicTitle);
  
  public void renameCustomTopicType(String oldTopicTitle, String newTopicTitle);
 
}