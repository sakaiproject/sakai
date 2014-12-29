/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/MessageForumsTypeManager.java $
 * $Id: MessageForumsTypeManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
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
	
	public String getCustomLevelType();
	
  /**
   * @return
   */
 // public List getAvailableTypes();		
   
  /**
   * @return
   */
  public String getPrivateMessageAreaType();
  
  public String getUserDefinedPrivateTopicType();
 

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
  
  public String renameCustomTopicType(String oldTopicTitle, String newTopicTitle);
 
}