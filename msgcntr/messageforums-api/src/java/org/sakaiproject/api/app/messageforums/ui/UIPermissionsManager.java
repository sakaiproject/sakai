/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/ui/UIPermissionsManager.java $
 * $Id: UIPermissionsManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums.ui;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Topic;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 */
public interface UIPermissionsManager
{

  /**
   * @return
   */
  public boolean isNewForum();
  
  /**
   * @return
   */
  public boolean isChangeSettings(DiscussionForum forum);
  
  /**     
   * @param forum
   * @return
   */
  public boolean isNewTopic(DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @param contextId
   * @return
   */
  public boolean isNewResponse(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);

  /**
   * @param topic
   * @return
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @param contextId
   * @return
   */
  public boolean isNewResponseToResponse(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);

  /**
   * @param topic
   * @return
   */
  public boolean isMovePostings(DiscussionTopic topic, DiscussionForum forum);

  /**
   * @param topic
   * @return
   */
  public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @return
   */
  public boolean isChangeSettings(DiscussionTopic topic, DiscussionForum forum, String userId);

  /**
   * @param topic
   * @return
   */
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum);
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum, String userId);
  public boolean isPostToGradebook(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);

  /**
   * @param topic
   * @return
   */
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum );
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum, String userId);
  public boolean isRead(DiscussionTopic topic, DiscussionForum forum, String userId, String siteContextId);
  public boolean isRead(Long topicId, Boolean isTopicDraft, Boolean isForumDraft, String userId, String siteContextId);

  /**
   * @param topic
   * @return
   */
  public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @param contextId
   * @return
   */
  public boolean isReviseAny(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);

  /**
   * @param topic
   * @return
   */
  public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum);

  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @param contextId
   * @return
   */
  public boolean isReviseOwn(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);
  
  /**
   * @param topic
   * @return
   */
  public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @param contextId
   * @return
   */
  public boolean isDeleteAny(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);

  /**
   * @param topic
   * @return
   */
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * 
   * @param topic
   * @param forum
   * @param userId
   * @param contextId
   * @return
   */
  public boolean isDeleteOwn(DiscussionTopic topic, DiscussionForum forum, String userId, String contextId);

  /**
   * @param topic
   * @return
   */
  public boolean isMarkAsRead(DiscussionTopic topic, DiscussionForum forum);
  
  /**
   * Returns whether current user has perm to moderate in this situation
   * @param topic
   * @param forum
   * @return
   */
  public boolean isModeratePostings(Long topicId, Boolean isForumLocked, Boolean isForumDraft, Boolean isTopicLocked, Boolean isTopicDraft, String userId, String siteId);
  public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum, String userId, String siteId);
  public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum, String userId);
  public boolean isModeratePostings(DiscussionTopic topic, DiscussionForum forum);

  /**
   * Returns whether current user has perm to identify anonymous users
   */
  public boolean isIdentifyAnonAuthors(Topic topic);
  
  /**
   * Returns list of current user's memberships 
   * (role + groups/sections) 
   * @return
   */
  public List getCurrentUserMemberships();
  public List getCurrentUserMemberships(String siteId);
  
  public Set getAreaItemsSet(Area area);

  public Set getForumItemsSet(DiscussionForum forum);
  
  public Set getTopicItemsSet(DiscussionTopic topic);
}
