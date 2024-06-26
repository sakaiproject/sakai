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

import java.util.List;


public interface MessageForumsForumManager {
 
	public List getReceivedUuidByContextId(final List siteList);
	
    public List searchTopicMessages(Long topicId, String searchText);
    
     public List getTopicsByIdWithMessages(final Long forumId);
  
    public List getTopicsByIdWithMessagesAndAttachments(final Long forumId);
    
    public List getTopicsByIdWithMessagesMembershipAndAttachments(final Long forumId);
    
    /**
   * @return Returns a list of forums specific to the current site and with the necessary 
   * information to be displayed on the main forums page (the page first displayed when the 
   * forums tool is loaded).  This information includes the forums' topics, the forums' 
   * attachments, and the topics' attachments.
   */
    public List<DiscussionForum> getForumsForMainPage();

    public List<DiscussionForum> getForumsForSite(String siteId);
  
    public Topic getTopicByIdWithMessages(final Long topicId);
    
    public Topic getTopicWithAttachmentsById(final Long topicId);
          
    public Topic getTopicByIdWithMessagesAndAttachments(final Long topicId);
     
    public BaseForum getForumByIdWithTopics(final Long forumId);
  
    public List getForumByTypeAndContext(final String typeUuid);
    
    public List getForumByTypeAndContextWithTopicsAllAttachments(final String typeUuid);
    
    public List getForumByTypeAndContext(final String typeUuid, final String contextId);
      
    public Topic getTopicByIdWithAttachments(final Long topicId);
    
    /**
     * Get the attachment list for a topic
     * used to avoid a hibernate stale object exception
     * @param topicId
     * @return
     */
    public List<Attachment> getTopicAttachments(final Long topicId);
    
    /**
     * Retrieve a given forum for the current user
     */
    public BaseForum getForumById(boolean open, Long forumId);
    public BaseForum getForumByUuid(String forumId);

    /**
     * Retrieve FAQ Forum from a given area
     * @return existing faq forum or null
     */
    public DiscussionForum getFaqForumForArea(Area area);

    /**
     * Retrieve FAQ Topic from a given forum
     * @return existing faq forum or null
     */
    public DiscussionTopic getFaqTopicForForum(DiscussionForum discussionForum);

    /**
     * Create and save FAQ Forum for a given area
     * @return newly created faq forum
     */
    public DiscussionForum createFaqForum(Area discussionArea);

    /**
     * Create and save FAQ Topic for a given forum
     * @return newly created faq topic
     */
    public DiscussionTopic createFaqTopic(DiscussionForum discussionForum);

    /**
     * Retrieve or create and save FAQ Forum from a given area
     * Create and save FAQ Forum for a given area
     * @return existing or newly created faq forum
     */
    public DiscussionForum getOrCreateFaqForumForArea(Area area);

    /**
     * Retrieve or create and save FAQ Topic from a given forum
     * Create and save FAQ Forum for a given area
     * @return existing or newly created faq topic
     */
    public DiscussionTopic getOrCreateFaqTopicForForum(DiscussionForum discussionForum);

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
    public PrivateForum createPrivateForum(String title, String userId);
    
    /**
     * save private forum
     * @param forum to save
     */
    public PrivateForum savePrivateForum(PrivateForum forum);
    public PrivateForum savePrivateForum(PrivateForum forum, String userId);

    /**
     * Save a discussion forum
     */
    public DiscussionForum saveDiscussionForum(DiscussionForum forum);
    public DiscussionForum saveDiscussionForum(DiscussionForum forum, boolean draft);
    public DiscussionForum saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent);
    public DiscussionForum saveDiscussionForum(DiscussionForum forum, boolean draft, boolean logEvent, String currentUser);


    /**
     * Create and save an empty discussion forum topic
     */
    public DiscussionTopic createDiscussionForumTopic(DiscussionForum forum);

    /**
     * Save a discussion forum topic
     */
    public DiscussionTopic saveDiscussionForumTopic(DiscussionTopic topic);
    public DiscussionTopic saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus);
    public DiscussionTopic saveDiscussionForumTopic(DiscussionTopic topic, boolean parentForumDraftStatus, String currentUser, boolean logEvent);
    /**
     * Create and save an empty private discussion forum topic
     */
    public PrivateTopic createPrivateForumTopic(String title, boolean forumIsParent, boolean topicIsMutable, String userId, Long parentId);    

    /**
     * Save a discussion forum topic
     */
    public PrivateTopic savePrivateForumTopic(PrivateTopic topic);
    public PrivateTopic savePrivateForumTopic(PrivateTopic topic, String userId);
    public PrivateTopic savePrivateForumTopic(PrivateTopic topic, String userId, String siteId);
    
    /**
     * Delete a private forum topic
     */
    public void deletePrivateForumTopic(PrivateTopic topic);

    /**
     * Create an empty message
     */
    Message createMessage(final DiscussionTopic topic);

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
    
    public List getForumByTypeAndContextWithTopicsMembership(final String typeUuid, final String contextId);
    
    /**
	 * Returns # moderated topics that the current user has moderate
	 * perm for, given the user's memberships and topics
	 * based on permissionLevelId (custom permissions)
	 * @param membershipItems
	 * @param moderatedTopics
	 * @return
	 */
	public int getNumModTopicCurrentUserHasModPermForWithPermissionLevel(final List<String> membershipItems, final List<Topic> moderatedTopics);
	
	/**
	 * Returns # moderated topics that the current user has moderate
	 * perm for, given the user's memberships and topics
	 * based on permissionLevelName (non-custom permissions)
	 * @param membershipItems
	 * @param moderatedTopics
	 * @return
	 */
	public int getNumModTopicCurrentUserHasModPermForWithPermissionLevelName(final List<String> membershipItems, final List<Topic> moderatedTopics);
	
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
	
	/**
	 * 
	 * @param topicId
	 * @return the Topic with the given id with the DBMembershipItems initialized.
	 * Does not initialize attachments or messages.
	 */
	public Topic getTopicByIdWithMemberships(final Long topicId);

	/**
	 * @param contextId the context in which we are seeking topics
	 * @return all topics within this context
	 */
	public List<Topic> getTopicsInSite(final String contextId);

	/**
	 * @param contextId the context in which we are seeking anonymous topics
	 * @return all topics such that postAnonymous is true within this context
	 */
	public List<Topic> getAnonymousTopicsInSite(final String contextId);

	/*
	 * @return true if getAnonymousTopicsInSite(contextId) is not empty
	 */
	public boolean isSiteHasAnonymousTopics(String contextId);

	/**
	 * Returns true/false whether the role has the named permission.
	 * Created this method to avoid iterating over hundreds of DBMembershipItems.
	 * @param topicId
	 * @param roleName
	 * @param permissionName
	 * @return
	 */
	public boolean doesRoleHavePermissionInTopic(final Long topicId, final String roleName, final String permissionName);

	public List<String> getAllowedGroupForRestrictedForum(final Long forumId, final String permissionName);
	public List<String> getAllowedGroupForRestrictedTopic(final Long topicId, final String permissionName);
}
