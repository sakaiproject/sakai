/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/MessageForumsForumManager.java $
 * $Id: MessageForumsForumManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.List;


public interface MessageForumsForumManager {
 
	public List getReceivedUuidByContextId(final List siteList);
	
    public List searchTopicMessages(Long topicId, String searchText);
    
     public List getTopicsByIdWithMessages(final Long forumId);
  
    public List getTopicsByIdWithMessagesAndAttachments(final Long forumId);
    
    public List getTopicsByIdWithMessagesMembershipAndAttachments(final Long forumId);
  
    public Topic getTopicByIdWithMessages(final Long topicId);
    
    public Topic getTopicWithAttachmentsById(final Long topicId);
          
    public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId);
     
    public BaseForum getForumByIdWithTopics(final Long forumId);
  
    public List getForumByTypeAndContext(final String typeUuid);
    
    public List getForumByTypeAndContextWithTopicsAllAttachments(final String typeUuid);
    
    public List getForumByTypeAndContext(final String typeUuid, final String contextId);
      
    public Topic getTopicByIdWithAttachments(final Long topicId);
    
    /**
     * Retrieve a given forum for the current user
     */
    public BaseForum getForumById(boolean open, Long forumId);
    public BaseForum getForumByUuid(String forumId);

    /**
     * Create and save an empty discussion forum
     * @return discussion forum
     */
    public DiscussionForum createDiscussionForum();
    
    /**
     * create private forum 
     * @param title of forum
     * @return private forum
     */
    public PrivateForum createPrivateForum(String title);
    
    /**
     * save private forum
     * @param forum to save
     */
    public void savePrivateForum(PrivateForum forum);         

    /**
     * Save a discussion forum
     */
    public void saveDiscussionForum(DiscussionForum forum);
    public void saveDiscussionForum(DiscussionForum forum, boolean draft);
    public void saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent);

    /**
     * Create and save an empty discussion forum topic
     */
    public DiscussionTopic createDiscussionForumTopic(DiscussionForum forum);

    /**
     * Save a discussion forum topic
     */
    public void saveDiscussionForumTopic(DiscussionTopic topic);
    public void saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus);
    
    /**
     * Create and save an empty private discussion forum topic
     */
    public PrivateTopic createPrivateForumTopic(String title, boolean forumIsParent, boolean topicIsMutable, String userId, Long parentId);    

    /**
     * Save a discussion forum topic
     */
    public void savePrivateForumTopic(PrivateTopic topic);
    
    /**
     * Delete a private forum topic
     */
    public void deletePrivateForumTopic(PrivateTopic topic);
    
    
    /**
     * Create and save an empty open discussion forum topic
     */
    public OpenTopic createOpenForumTopic(OpenForum forum);

    /**
     * Save an open forum topic
     */
    public void saveOpenForumTopic(OpenTopic topic);

    /**
     * Delete a discussion forum and all topics/messages
     */
    public void deleteDiscussionForum(DiscussionForum forum);

    /**
     * Delete a discussion forum topic
     */
    public void deleteDiscussionForumTopic(DiscussionTopic topic);

    /**
     * Delete an open forum topic
     */
    public void deleteOpenForumTopic(OpenTopic topic);

    /**
     * Returns a given number of messages if available in the time 
     * provided
     * @param numberMessages the number of messages to retrieve
     * @param numberDaysInPast the number days to look back
     */
    public List getRecentPrivateMessages(int numberMessages, int numberDaysInPast);

    /**
     * Returns a given number of discussion forum messages if available in 
     * the time provided
     * @param numberMessages the number of forum messages to retrieve
     * @param numberDaysInPast the number days to look back
     */
    public List getRecentDiscussionForumMessages(int numberMessages, int numberDaysInPast);

    /**
     * Returns a given number of open forum messages if available in 
     * the time provided
     * @param numberMessages the number of forum messages to retrieve
     * @param numberDaysInPast the number days to look back
     */
    public List getRecentOpenForumMessages(int numberMessages, int numberDaysInPast);
    
    public Topic getTopicById(boolean open, Long topicId);
    public Topic getTopicByUuid(String uuid);
    
    /**
     * Returns all moderated topics in the site
     * @param areaId
     * @return
     */
    public List getModeratedTopicsInSite(String contextId);
    
    /**
     * @return
     */
    public ActorPermissions createDefaultActorPermissions();
    
    public PrivateForum getPrivateForumByOwnerArea(final String owner, final Area area);

    public PrivateForum getPrivateForumByOwnerAreaNull(final String owner);
    
    public PrivateForum getPrivateForumByOwnerAreaWithAllTopics(final String owner, final Area area);
    
    public PrivateForum getPrivateForumByOwnerAreaNullWithAllTopics(final String owner);
    
    public List getForumByTypeAndContextWithTopicsMembership(final String typeUuid, final String contextId);
    
    /**
	 * Returns # moderated topics that the current user has moderate
	 * perm for, given the user's memberships and contextid
	 * @param membershipItems
	 * @param contextId
	 * @return
	 */
	public int getNumModTopicCurrentUserHasModPermFor(final List membershipItems);
	
	/**
	 * Returns forum with topics, topic attachments, and topic messages
	 * @param forumId
	 * @return
	 */
	public BaseForum getForumByIdWithTopicsAttachmentsAndMessages(final Long forumId);
	
	/**
	 * 
	 * @param typeUuid
	 * @param contextId
	 * @return all forums of the given type in the given context with all topics,
	 * messages, and attachments populated
	 */
	public List getForumByTypeAndContextWithTopicsAllAttachments(final String typeUuid, final String contextId);
}