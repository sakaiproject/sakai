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
  private static final String ALL_INSTRUCTORS = "allInstructors";
  private static final String ALL_PARTICIPANTS = "allParticipants";
  private static final String SITE_PARTICIPANT = "siteParticipant";
  private static final String GROUP = "group";
  private static final String ROLE = "role";

  private static final String AUTHORITY = "org.sakaiproject.component.app.messageforums";
  private static final String DOMAIN = "sakai_messageforums";
  private static final String PRIVATE = "privateForums";
  private static final String DISCUSSION = "discussionForums";
  private static final String OPEN = "openForums";

  private static final String RECEIVED = "ReceivedPrivateMessageType";

  private static final String SENT = "SentPrivateMessageType";

  private static final String DELETED = "DeletedPrivateMessageType";

  private static final String DRAFT = "DraftPrivateMessageType";

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
          "Received Private Massage Type", "Received Private Massage Type")
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
          "Sent Private MassageType", "Sent Private Massage Type").getUuid());
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
          "Deleted Private Massage Type", "Deleted Private Massage Type")
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
          "Draft Private Massage Type", "Draft Private Massage Type").getUuid());
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
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getSiteParticipantType()
   */
  public String getSiteParticipantType()
  {
    LOG.debug("getSiteParticipantType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, SITE_PARTICIPANT);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, SITE_PARTICIPANT,
          SITE_PARTICIPANT, SITE_PARTICIPANT).getUuid());
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
   * @see org.sakaiproject.api.app.messageforums.MessageForumsTypeManager#getAllInstructorsType()
   */
  public String getAllInstructorsType()
  {
    LOG.debug("getAllInstructorsType()");
    Type type = typeManager.getType(AUTHORITY, DOMAIN, ALL_INSTRUCTORS);
    if (type != null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY, DOMAIN, ALL_INSTRUCTORS,
          "All Instructors", "All Instructors").getUuid());
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

}
