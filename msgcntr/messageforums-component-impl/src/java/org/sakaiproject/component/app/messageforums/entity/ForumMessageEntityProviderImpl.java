/**
 * Copyright (c) 2005-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.component.app.messageforums.entity;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.hibernate5.HibernateOptimisticLockingFailureException;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.PrivateMessageRecipient;
import org.sakaiproject.api.app.messageforums.SynopticMsgcntrManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.entity.ForumMessageEntityProvider;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.api.app.messageforums.ui.PrivateMessageManager;
import org.sakaiproject.api.app.messageforums.ui.UIPermissionsManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.PropertyProvideable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestStorable;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestStorage;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserDirectoryService;

@Slf4j
public class ForumMessageEntityProviderImpl implements ForumMessageEntityProvider,
    AutoRegisterEntityProvider, PropertyProvideable, RESTful, RequestStorable, RequestAware, ActionsExecutable {

  private DiscussionForumManager forumManager;
  private PrivateMessageManager privateMessageManager;
  private UIPermissionsManager uiPermissionsManager;
  private MessageForumsMessageManager messageManager;
  private ServerConfigurationService serverConfigurationService;
  private SecurityService securityService;
  private SiteService siteService;
  private UserDirectoryService userDirectoryService;

  private RequestStorage requestStorage;
  public void setRequestStorage(RequestStorage requestStorage) {
      this.requestStorage = requestStorage;
  }
  
  private RequestGetter requestGetter;
  public void setRequestGetter(RequestGetter requestGetter){
  	this.requestGetter = requestGetter;
  }
  
  public String getEntityPrefix() {
    return ENTITY_PREFIX;
  }

  public boolean entityExists(String id) {
    Topic topic = null;
    try {
      topic = forumManager.getTopicById(Long.valueOf(id));
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
    return (topic != null);
  }

  public List<String> findEntityRefs(String[] prefixes, String[] name, String[] searchValue,
      boolean exactMatch) {
    List<String> rv = new ArrayList<String>();

    String userId = null;
    String siteId = null;
    String topicId = null;

    if (ENTITY_PREFIX.equals(prefixes[0])) {

      for (int i = 0; i < name.length; i++) {
        if ("context".equalsIgnoreCase(name[i]) || "site".equalsIgnoreCase(name[i]))
          siteId = searchValue[i];
        else if ("user".equalsIgnoreCase(name[i]) || "userId".equalsIgnoreCase(name[i]))
          userId = searchValue[i];
        else if ("topic".equalsIgnoreCase(name[i]) || "topicId".equalsIgnoreCase(name[i]))
          topicId = searchValue[i];
        else if ("parentReference".equalsIgnoreCase(name[i])) {
          String[] parts = searchValue[i].split("/");
          topicId = parts[parts.length - 1];
        }
      }
      
      String siteRef = siteId;
      if(siteRef != null && !siteRef.startsWith("/site/")){
    	  siteRef = "/site/" + siteRef;
      }
      // TODO: support search by something other then topic id...
      if (topicId != null) {
        List<Message> messages =
          forumManager.getTopicByIdWithMessagesAndAttachments(Long.valueOf(topicId)).getMessages();
        for (int i = 0; i < messages.size(); i++) {
          // TODO: authz is way too basic, someone more hip to message center please improve...
          //This should also allow people with read access to an item to link to it
          if (forumManager.isInstructor(userId, siteRef)
              || userId.equals(messages.get(i).getCreatedBy())) {
            rv.add("/" + ENTITY_PREFIX + "/" + messages.get(i).getId().toString());
          }
        }
      }
    }

    return rv;
  }

  public Map<String, String> getProperties(String reference) {
    Map<String, String> props = new HashMap<String, String>();
    Message message =
      forumManager.getMessageById(Long.valueOf(reference.substring(reference.lastIndexOf("/") + 1)));

    props.put("title", message.getTitle());
    props.put("author", message.getCreatedBy());
    if (message.getCreated() != null)
      props.put("date", DateFormat.getInstance().format(message.getCreated()));
    if (message.getModifiedBy() != null) {
      props.put("modified_by", message.getModifiedBy());
      props.put("modified_date", DateFormat.getInstance().format(message.getModified()));
    }
    props.put("label", message.getLabel());
    if (message.getDraft() != null)
      props.put("draft", message.getDraft().toString());
    if (message.getApproved() != null)
      props.put("approved", message.getApproved().toString());
    if (message.getGradeAssignmentName() != null)
      props.put("assignment_name", message.getGradeAssignmentName());
    
    return props;
  }

  public String getPropertyValue(String reference, String name) {
    // TODO: don't be so lazy, just get what we need...
    Map<String, String> props = getProperties(reference);
    return props.get(name);
  }

  public void setPropertyValue(String reference, String name, String value) {
	  // This does nothing for now... we could all the setting of many published assessment properties
	  // here though... if you're feeling jumpy feel free.
  }

  public void setForumManager(DiscussionForumManager forumManager) {
	  this.forumManager = forumManager;
  }

  public String createEntity(EntityReference ref, Object entity,
		  Map<String, Object> params) {
	  // TODO Auto-generated method stub
	  return null;
  }

  public Object getSampleEntity() {
	  // TODO Auto-generated method stub
	  return null;
  }

  public void updateEntity(EntityReference ref, Object entity,
		  Map<String, Object> params) {
	  // TODO Auto-generated method stub

  }

  public Object getEntity(EntityReference ref) {
	  // TODO Auto-generated method stub
	  return null;
  }

  public void deleteEntity(EntityReference ref, Map<String, Object> params) {
	  // TODO Auto-generated method stub

  }

	private String getProfileImageURL(String authorId) {

		if (null == authorId || authorId.trim().length() == 0 ) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(serverConfigurationService.getServerUrl());
		sb.append("/api/users/");
		sb.append(authorId);
		sb.append("/profile/image/thumb");
		return sb.toString();
	}


	public List<DecoratedMessage> findReplies(List<Message> messages, Long messageId, Long topicId, Map msgIdReadStatusMap){
	  List<DecoratedMessage> replies = new ArrayList<DecoratedMessage>();

	  for (Message message : messages) {
		  if(message.getInReplyTo() != null){
			  if(messageId.equals(message.getInReplyTo().getId())){
				  if(!message.getDeleted()){
					  List<String> attachments = new ArrayList<String>();
					  if(message.getHasAttachments()){
						  for(Attachment attachment : (List<Attachment>) message.getAttachments()){
							  attachments.add(attachment.getAttachmentName());
						  }
					  }
					  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
					  if(readStatus == null)
						  readStatus = Boolean.FALSE;

					  DecoratedMessage dMessage = new DecoratedMessage(message
							  .getId(), topicId, message.getTitle(),
							  message.getBody(), "" + message.getModified().getTime(),
							  attachments, findReplies(messages, message.getId(),
									  topicId, msgIdReadStatusMap), message.getAuthor(), message.getInReplyTo() == null ? null : message.getInReplyTo().getId(),
							  getProfileImageURL(message.getAuthorId()),
											  "" + message.getCreated().getTime(), readStatus.booleanValue(), "", "");
					  replies.add(dMessage);
				  }		  
			  }
		  }		  
	  }

	  Collections.sort(replies, new Comparator<DecoratedMessage>(){

		public int compare(DecoratedMessage arg0, DecoratedMessage arg1) {
			Long date1 = Long.parseLong(arg0.getCreatedOn());
			Long date2 = Long.parseLong(arg1.getCreatedOn());
			return date1.compareTo(date2);
		}
		  
	  });
	  
	  return replies;
  }
  
  public List<DecoratedMessage> generateFlattenedMessagesListHelper(List<DecoratedMessage> messages, int indent){
		List<DecoratedMessage> flattenedList = new ArrayList<DecoratedMessage>();
		for (DecoratedMessage message : messages) {
			message.setIndentIndex(indent);
			List<DecoratedMessage> helperList = new ArrayList<DecoratedMessage>();
			if(message.getReplies().size() > 0){
				helperList = generateFlattenedMessagesListHelper(message.getReplies(), indent+1);
			}
			message.setReplies(null);
			flattenedList.add(message);
			flattenedList.addAll(helperList);
		}
		return flattenedList;
	}

  public List<?> getEntities(EntityReference ref, Search search) {
	  
	  String topicId = "";
	  String typeUuid = "";
	  String siteId = "";
	  String userId = userDirectoryService.getCurrentUser().getId();
	  if (userId == null || "".equals(userId)){
		  return null;
	  }
	  if (! search.isEmpty()) {
		  Restriction topicRes = search.getRestrictionByProperty("topicId");
		  if(topicRes != null){
			  topicId = topicRes.getStringValue();
		  }
		  Restriction typeRes = search.getRestrictionByProperty("typeUuid");
		  if(typeRes != null){
			  typeUuid = typeRes.getStringValue();
		  }
		  Restriction siteRes = search.getRestrictionByProperty("siteId");
		  if(siteRes != null){
			  siteId = siteRes.getStringValue();
		  }
	  }
	  List<DecoratedMessage> dMessages = new ArrayList<DecoratedMessage>();
	  

	  if(topicId != null && !"".equals(topicId)){
		  List<Message> messages =
			  forumManager.getTopicByIdWithMessagesAndAttachments(new Long(topicId)).getMessages();

		  DiscussionTopic dTopic = forumManager.getTopicById(Long.valueOf(topicId));
		  DiscussionForum dForum = forumManager.getForumById(dTopic.getBaseForum().getId());
		  siteId = forumManager.getContextForForumById(dForum.getId());

		  //make sure the user has access too this forum and topic and site:
		  if(dForum.getDraft().equals(Boolean.FALSE) && dTopic.getDraft().equals(Boolean.FALSE) && securityService.unlock(userId, SiteService.SITE_VISIT, "/site/" + siteId)){

			  if (getUiPermissionsManager().isRead(dTopic, dForum, userId, siteId))
			  {

				  messages = filterModeratedMessages(messages, dTopic, dForum, userId, siteId);
				  List<Long> messageIds = new ArrayList<Long>();
				  for (Message message : messages) {
					  if(message != null && !message.getDraft().booleanValue() && !message.getDeleted().booleanValue())
					  {
						  messageIds.add(message.getId());
					  }
				  }

				  Map msgIdReadStatusMap = forumManager.getReadStatusForMessagesWithId(messageIds, userId);
				  for (Message message : messages) {
					  if(message.getInReplyTo() == null){
						  if(!message.getDeleted()){
							  //this is a top message, so now create the replies list (if any exists)
							  List<String> attachments = new ArrayList<String>();
							  if(message.getHasAttachments()){
								  for(Attachment attachment : (List<Attachment>) message.getAttachments()){
									  attachments.add(attachment.getAttachmentName());
								  }
							  }
							  Boolean readStatus = (Boolean)msgIdReadStatusMap.get(message.getId());
							  if(readStatus == null)
								  readStatus = Boolean.FALSE;

							  DecoratedMessage dMessage = new DecoratedMessage(message
									  .getId(), new Long(topicId), message.getTitle(),
									  message.getBody(), "" + message.getModified().getTime(),
									  attachments, findReplies(messages, message.getId(),
											  new Long(topicId), msgIdReadStatusMap), message.getAuthor(), message.getInReplyTo() == null ? null : message.getInReplyTo().getId(),
									  getProfileImageURL(message.getAuthorId()),
													  "" + message.getCreated().getTime(), readStatus.booleanValue(), "", "");				  

							  dMessages.add(dMessage);
						  }
					  }
				  }
			  }
		  }
		  //return a sorted list
		  Collections.sort(dMessages, new Comparator<DecoratedMessage>(){

			  public int compare(DecoratedMessage arg0, DecoratedMessage arg1) {
				  Long date1 = Long.parseLong(arg0.getCreatedOn());
				  Long date2 = Long.parseLong(arg1.getCreatedOn());
				  return date1.compareTo(date2);
			  }

		  });
		  //now that we have the hierarchy and ordered list, we need to flatten the returned list since there is a max depth
		  //the entity broken can return  (currently set at 8)
		  List<DecoratedMessage> flattenedList = new ArrayList<DecoratedMessage>();
		  for (DecoratedMessage message : dMessages) {

			  List<DecoratedMessage> helperList = new ArrayList<DecoratedMessage>();
			  if(message.getReplies().size() > 0){
				  helperList = generateFlattenedMessagesListHelper(message.getReplies(), 1);
			  }
			  //clear out the replies field since this can return bad data with the max depth issues.
			  message.setReplies(null);
			  flattenedList.add(message);
			  flattenedList.addAll(helperList);
		  }

		  dMessages = flattenedList;


	  }else if(typeUuid != null && !"".equals(typeUuid) && siteId != null && !"".equals(siteId)){
		  List decoratedPvtMsgs= getPrivateMessageManager().getMessagesByTypeByContext(typeUuid, siteId, userId, PrivateMessageManager.SORT_COLUMN_DATE,
				  PrivateMessageManager.SORT_DESC);

		  for (PrivateMessage pvtMessage : (List<PrivateMessage>) decoratedPvtMsgs) {
			  PrivateMessage initPvtMessage = getPrivateMessageManager().initMessageWithAttachmentsAndRecipients(pvtMessage);
			  //this is a top message, so now create the replies list (if any exists)
			  List<String> attachments = new ArrayList<String>();
			  if(initPvtMessage.getHasAttachments()){
				  for(Attachment attachment : (List<Attachment>) initPvtMessage.getAttachments()){
					  attachments.add(attachment.getAttachmentName());
				  }
			  }

			  //getRecipients() is filtered for this particular user i.e. returned list of only one PrivateMessageRecipient object
			  boolean read = false;
			  for (Iterator iterator = pvtMessage.getRecipients().iterator(); iterator.hasNext();)
			  {
				  PrivateMessageRecipient el = (PrivateMessageRecipient) iterator.next();
				  if (el != null){
					  read = el.getRead().booleanValue();
				  }
			  }


			  DecoratedMessage dMessage = new DecoratedMessage(pvtMessage
					  .getId(), null, pvtMessage.getTitle(),
					  pvtMessage.getBody(), "" + pvtMessage.getModified().getTime(),
					  attachments, null, pvtMessage.getAuthor(), pvtMessage.getInReplyTo() == null ? null : pvtMessage.getInReplyTo().getId(),
					  getProfileImageURL(pvtMessage.getAuthorId()),
							  "" + pvtMessage.getCreated().getTime(), read, pvtMessage.getRecipientsAsText(), pvtMessage.getLabel());				  

			  dMessages.add(dMessage);
		  }	  
	  }
	  
	  return dMessages;
  }
  
  
  public void markAsRead(String userId, String siteId, String readMessageId, int numOfAttempts) {
	  try {
		  Message msg = getMessageManager().getMessageById(new Long(readMessageId));
		  if(msg instanceof PrivateMessage){
			  String toolId = DiscussionForumService.MESSAGES_TOOL_ID;				  
			  getPrivateMessageManager().markMessageAsReadForUser((PrivateMessage) msg, siteId, userId, toolId);					  
		  }else{
			  String toolId = DiscussionForumService.FORUMS_TOOL_ID;
			  String topicId = msg.getTopic().getId().toString();
			  messageManager.markMessageNotReadForUser(new Long(topicId), new Long(readMessageId), false, userId, siteId, toolId); 
		  }
	  } catch (HibernateOptimisticLockingFailureException holfe) {

		  // failed, so wait and try again
		  try {
			  Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
		  } catch (InterruptedException e) {
			  log.error(e.getMessage(), e);
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  log.info("ForumMessageEntityProviderImpl: markAsRead: HibernateOptimisticLockingFailureException no more retries left");
			  log.error(holfe.getMessage(), holfe);
		  } else {
			  log.info("ForumMessageEntityProviderImpl: markAsRead: HibernateOptimisticLockingFailureException: attempts left: "
					  + numOfAttempts);
			  markAsRead(userId, siteId, readMessageId, numOfAttempts);
		  }
	  } catch (Exception e){
		  // failed, so wait and try again
		  try {
			  Thread.sleep(SynopticMsgcntrManager.OPT_LOCK_WAIT);
		  } catch (InterruptedException ie) {
			  log.error(ie.getMessage(), ie);
		  }

		  numOfAttempts--;

		  if (numOfAttempts <= 0) {
			  log.info("ForumMessageEntityProviderImpl: markAsRead: no more retries left");
			  log.error(e.getMessage(), e);
		  } else {
			  log.info("ForumMessageEntityProviderImpl: markAsRead:  attempts left: "
					  + numOfAttempts);
			  markAsRead(userId, siteId, readMessageId, numOfAttempts);
		  }
	  }

  }
  
  /**
	 * Given a list of messages, will return all messages that meet at
	 * least one of the following criteria:
	 * 1) message is approved
	 * 
	 */
	private List filterModeratedMessages(List messages, DiscussionTopic topic, DiscussionForum forum, String userId, String siteId)
	{
		List viewableMsgs = new ArrayList();
		if (messages != null && messages.size() > 0)
		{
			boolean hasModeratePerm = getUiPermissionsManager().isModeratePostings(topic, forum, userId, siteId);
			
			if (hasModeratePerm)
				return messages;
			
			Iterator msgIter = messages.iterator();
			while (msgIter.hasNext())
			{
				Message msg = (Message) msgIter.next();
				if (msg.getApproved() != null && msg.getApproved())
					viewableMsgs.add(msg);
			}
		}
		
		return viewableMsgs;
	}
  
	/**
	 * markread/messageId/site/siteId
	 * markread/messageId/site/siteId
	 */
	@EntityCustomAction(action="markread",viewKey=EntityView.VIEW_NEW)
    public boolean getForum(EntityView view, Map<String, Object> params) {
        String messageId = view.getPathSegment(2);
        String siteId = "";
        if("site".equals(view.getPathSegment(3))){
        	siteId = view.getPathSegment(4);
        }
        String userId = userDirectoryService.getCurrentUser().getId();
		if (userId == null || "".equals(userId) || siteId == null
				|| "".equals(siteId) || messageId == null
				|| "".equals(messageId)) {
			return false;
		}
        
        markAsRead(userId, siteId, messageId, SynopticMsgcntrManager.NUM_OF_ATTEMPTS);
        
        return true;
    }
	
	/**
	 * topic/topicId
	 */
	@EntityCustomAction(action="topic",viewKey=EntityView.VIEW_LIST)
    public List<?> getTopicMessagesInSite(EntityView view, Map<String, Object> params) {
        String topicId = view.getPathSegment(2);
        if (topicId == null) {
        	topicId = (String) params.get("topicId");
            if (topicId == null) {
                throw new IllegalArgumentException("topicId must be set in order to get the topic messages, set in params or in the URL /forum_message/topic/topicId");
            }
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search("topicId", topicId));
        return l;
    }
	
	/**
	 * private/typeUuid/site/siteId
	 */
	@EntityCustomAction(action="private",viewKey=EntityView.VIEW_LIST)
    public List<?> getPrivateTopicMessagesInSite(EntityView view, Map<String, Object> params) {
        String topicId = view.getPathSegment(2);
        if (topicId == null) {
        	topicId = (String) params.get("typeUuid");
            if (topicId == null) {
                throw new IllegalArgumentException("typeUuid and siteId must be set in order to get the topic messages, set in params or in the URL /forum_message/private/typeUuid/site/siteId");
            }
        }
        String siteId = "";
        if("site".equals(view.getPathSegment(3))){
        	siteId = view.getPathSegment(4);
        }
        List<?> l = getEntities(new EntityReference(ENTITY_PREFIX, ""), 
                new Search(new String[]{"typeUuid", "siteId"}, new String[]{topicId, siteId}));
        return l;
    }


  public String[] getHandledOutputFormats() {
	  // TODO Auto-generated method stub
	  return null;
  }

  public String[] getHandledInputFormats() {
	  // TODO Auto-generated method stub
	  return null;
  }
  
  public class DecoratedMessagesTopic{
	private String title;
	private Long id;
	private int totalMessages = 0;
	private int totalUnreadMessages = 0;
	
	public DecoratedMessagesTopic(String title, Long id, int totalMessages, int totalUnreadMessages){
		this.title = title;
		this.id = id;
		this.totalMessages = totalMessages;
		this.totalUnreadMessages = totalUnreadMessages;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getTotalMessages() {
		return totalMessages;
	}
	public void setTotalMessages(int totalMessages) {
		this.totalMessages = totalMessages;
	}
	public int getTotalUnreadMessages() {
		return totalUnreadMessages;
	}
	public void setTotalUnreadMessages(int totalUnreadMessages) {
		this.totalUnreadMessages = totalUnreadMessages;
	}
	
	
  }
  
  public class DecoratedMessage{
	 private Long messageId;
	 private Long topicId;
	 private String title;
	 private String body;
	 private String lastModified;
	 private List<String> attachments;
	 private List<DecoratedMessage> replies;
	 private String authoredBy;
	 private int indentIndex = 0;
	 private String profileImageUrl;
	 private Long replyTo;
	 private String createdOn;
	 private boolean read;
	 private String recipients;
	 private String label;
	 
	public DecoratedMessage(Long messageId, Long topicId, String title, String body, String lastModified, List<String> attachments, List<DecoratedMessage> replies, String authoredBy, Long replyTo, String profileImageUrl, String createdOn, boolean read, String recipients, String label){
		  this.messageId = messageId;
		  this.topicId = topicId;
		  this.title = title;
		  this.body = body;
		  this.attachments = attachments;
		  this.replies = replies;
		  this.lastModified = lastModified;
		  this.authoredBy = authoredBy;
		  this.profileImageUrl = profileImageUrl;
		  this.replyTo = replyTo;
		  this.createdOn = createdOn;
		  this.read = read;
		  this.recipients = recipients;
		  this.label = label;
	  }
	  public Long getMessageId() {
		  return messageId;
	  }

	  public void setMessageId(Long messageId) {
		  this.messageId = messageId;
	  }

	  public Long getTopicId() {
		  return topicId;
	  }

	  public void setTopicId(Long topicId) {
		  this.topicId = topicId;
	  }

	  public String getTitle() {
		  return title;
	  }

	  public void setTitle(String title) {
		  this.title = title;
	  }

	  public String getBody() {
		  return body;
	  }

	  public void setBody(String body) {
		  this.body = body;
	  }

	  public List<String> getAttachments() {
		  return attachments;
	  }
	  public void setAttachments(List<String> attachments) {
		  this.attachments = attachments;
	  }

	  public List<DecoratedMessage> getReplies() {
		  return replies;
	  }
	  public void setReplies(List<DecoratedMessage> replies) {
		  this.replies = replies;
	  }
	  public String getLastModified() {
		  return lastModified;
	  }
	  public void setLastModified(String lastModified) {
		  this.lastModified = lastModified;
	  }
	public String getAuthoredBy() {
		return authoredBy;
	}
	public void setAuthoredBy(String authoredBy) {
		this.authoredBy = authoredBy;
	}
	public int getIndentIndex() {
		return indentIndex;
	}
	public void setIndentIndex(int indentIndex) {
		this.indentIndex = indentIndex;
	}

	  public String getProfileImageUrl() {
		  return profileImageUrl;
	  }

	  public void setProfileImageUrl(String profileImageUrl) {
		  this.profileImageUrl = profileImageUrl;
	  }

	  public Long getReplyTo() {
		return replyTo;
	}
	public void setReplyTo(Long replyTo) {
		this.replyTo = replyTo;
	}
	public String getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}
	public boolean isRead() {
		return read;
	}
	public void setRead(boolean read) {
		this.read = read;
	}
	public String getRecipients() {
		return recipients;
	}
	public void setRecipients(String recipients) {
		this.recipients = recipients;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

  }

  public PrivateMessageManager getPrivateMessageManager() {
	  return privateMessageManager;
  }


  public void setPrivateMessageManager(PrivateMessageManager privateMessageManager) {
	  this.privateMessageManager = privateMessageManager;
  }

  public UIPermissionsManager getUiPermissionsManager() {
	  return uiPermissionsManager;
  }


  public void setUiPermissionsManager(UIPermissionsManager uiPermissionsManager) {
	  this.uiPermissionsManager = uiPermissionsManager;
  }

public MessageForumsMessageManager getMessageManager() {
	return messageManager;
}

public void setMessageManager(MessageForumsMessageManager messageManager) {
	this.messageManager = messageManager;
}

	public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
	
}
