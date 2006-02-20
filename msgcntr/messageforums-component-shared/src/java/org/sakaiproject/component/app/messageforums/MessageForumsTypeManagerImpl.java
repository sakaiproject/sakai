package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;

/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public class MessageForumsTypeManagerImpl implements MessageForumsTypeManager
{
  private static final Log LOG = LogFactory
      .getLog(MessageForumsTypeManagerImpl.class);
  private static final String NOT_SPECIFIED = "notSpecified";
  private static final String ALL_PARTICIPANTS = "allParticipants";
  private static final String GROUP = "group";
  private static final String ROLE = "role";
  private static final String USER = "user";

  private static final String AUTHORITY = "org.sakaiproject.component.app.messageforums";
  private static final String DOMAIN = "sakai_messageforums";
  private static final String PRIVATE = "privateForums";
  private static final String DISCUSSION = "discussionForums";
  private static final String OPEN = "openForums";

  private static final String RECEIVED = "ReceivedPrivateMessageType";

  private static final String SENT = "SentPrivateMessageType";

  private static final String DELETED = "DeletedPrivateMessageType";

  private static final String DRAFT = "DraftPrivateMessageType";
  
  // Permission Level Types
  private static final String OWNER = "Owner Permission Level";
  private static final String AUTHOR = "Author Permission Level";
  private static final String NONEDITING_AUTHOR = "Nonediting Author Permission Level";
  private static final String CONTRIBUTOR = "Contributor Permission Level";
  private static final String REVIEWER = "Reviewer Permission Level";  
  private static final String NONE = "None Permission Level";  
  private static final String CUSTOM = "Custom Permission Level";
  
  private TypeManager typeManager;

  public void init()
  {
    ;
  }

  /**
   * @param typeManager
   */
  public void setTypeManager(TypeManager typeManager)
  {
    if(LOG.isDebugEnabled())
    {
      LOG.debug("setTypeManager(TypeManager "+typeManager +")");
    }
    this.typeManager = typeManager;
  }
  
  public String getOwnerLevelType(){
  	LOG.debug("getOwnerLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, OWNER);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, OWNER,
          "Owner Permission Level", "Owner Permission Level").getUuid());
    }
  }
  
  public String getAuthorLevelType(){
  	LOG.debug("getAuthorLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, AUTHOR);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, AUTHOR,
          "Author Permission Level", "Author Permission Level").getUuid());
    }
  }
  
  public String getNoneditingAuthorLevelType(){
  	LOG.debug("getNoneditingAuthorLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, NONEDITING_AUTHOR);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, NONEDITING_AUTHOR,
          "Nonediting Author Permission Level", "Nonediting Author Permission Level").getUuid());
    }
  }
  
  public String getReviewerLevelType(){
  	LOG.debug("getReviewerLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, REVIEWER);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, REVIEWER,
          "Reviewer Permission Level", "Reviewer Permission Level").getUuid());
    }
  }
  
  public String getContributorLevelType(){
  	LOG.debug("getContributorLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, CONTRIBUTOR);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, CONTRIBUTOR,
          "Contributor Permission Level", "Contributor Permission Level").getUuid());
    }
  }
  
  public String getNoneLevelType(){
  	LOG.debug("getNoneLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, NONE);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, NONE,
          "None Permission Level", "None Permission Level").getUuid());
    }
  }
  
  public String getCustomLevelType(){
  	LOG.debug("getCustomLevelType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, CUSTOM);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, CUSTOM,
          "Custom Permission Level", "Custom Permission Level").getUuid());
    }
  }  
  
  

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getPrivateMessageAreaType()
   */
  public String getPrivateMessageAreaType()
  {
    LOG.debug("getPrivateMessageAreaType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, PRIVATE);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, PRIVATE,
          "Private Forums", "Private Message Forums").getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getDiscussionForumType()
   */
  public String getDiscussionForumType()
  {
    LOG.debug("getDiscussionForumType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, DISCUSSION);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, DISCUSSION,
          "DISCUSSION FORUMS", "DISCUSSION Message Forums").getUuid());
    }

  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getOpenDiscussionForumType()
   */
  public String getOpenDiscussionForumType()
  {
    LOG.debug("getOpenDiscussionForumType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, OPEN);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, OPEN,
          "OPEN DISCUSSION FORUMS", "OPEN DISCUSSION Message Forums").getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getReceivedPrivateMessageType()
   */
  public String getReceivedPrivateMessageType()
  {
    LOG.debug("getReceivedPrivateMessageType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, RECEIVED);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, RECEIVED,
          "Received Private Message Type", "Received Private Message Type")
          .getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getSentPrivateMessageType()
   */
  public String getSentPrivateMessageType()
  {
    LOG.debug("getSentPrivateMessageType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, SENT);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, SENT,
          "Sent Private MessageType", "Sent Private Message Type").getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getDeletedPrivateMessageType()
   */
  public String getDeletedPrivateMessageType()
  {
    LOG.debug("getDeletedPrivateMessageType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, DELETED);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, DELETED,
          "Deleted Private Message Type", "Deleted Private Message Type")
          .getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getDraftPrivateMessageType()
   */
  public String getDraftPrivateMessageType()
  {
    LOG.debug("getDraftPrivateMessageType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, DRAFT);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, DRAFT,
          "Draft Private Message Type", "Draft Private Message Type").getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getRoleType()
   */
  public String getRoleType()
  {
    LOG.debug("getRoleType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, ROLE);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, ROLE, "ROLES",
          "Site Roles").getUuid());
    }
  }
  

  public String getUserType()
  {
    LOG.debug("getUserType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, USER);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, USER, "USERS",
          "Users").getUuid());
    }
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getGroupType()
   */
  public String getGroupType()
  {
    LOG.debug("getGroupType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, GROUP);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, GROUP, "Groups",
          "Site Groups").getUuid());
    }
  }
 

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getAllParticipantType()
   */
  public String getAllParticipantType()
  {
    LOG.debug("getAllParticipantType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, ALL_PARTICIPANTS);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, ALL_PARTICIPANTS,
          "All Participants", "All Site Participants").getUuid());
    }
  }
   
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getNotSpecifiedType()
   */
  public String getNotSpecifiedType()
  {
    LOG.debug("getNotSpecifiedType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, NOT_SPECIFIED);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, NOT_SPECIFIED,
          "Not Specified", "Not Specified").getUuid());
    }
  }

  
  /** Return the typeUUId fro custom created topic  */
  public String getCustomTopicType(String topicTitle)
  {
    LOG.debug("getCustomTopicType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, topicTitle);
    if (type != null)
    {
      return type.getUuid();
    }
    else {
      return (typeManager.createType(AUTHORITY, DOMAIN, topicTitle, topicTitle, topicTitle)).getUuid();
    }
  }
  
  public void renameCustomTopicType(String oldTopicTitle, String newTopicTitle)
  {
    Type type = typeManager.getType(AUTHORITY, DOMAIN, oldTopicTitle);
    if (type != null)
    {
      type.setDisplayName(newTopicTitle);
      type.setDescription(newTopicTitle);
      type.setKeyword(newTopicTitle);
      
      typeManager.saveType(type);
    }
  }
}
