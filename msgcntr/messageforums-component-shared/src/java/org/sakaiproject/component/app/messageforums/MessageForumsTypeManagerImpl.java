package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;

public class MessageForumsTypeManagerImpl implements MessageForumsTypeManager
{
  private static final Log LOG = LogFactory
  .getLog(MessageForumsTypeManagerImpl.class);
  private static final String AUTHORITY ="org.sakaiproject.component.app.messageforums";
  private static final String DOMAIN ="sakai_messageforums";
  private static final String PRIVATE ="privateForums";
  private static final String DISCUSSION ="discussionForums";
  private static final String OPEN ="openForums";
  
  private static final String RECEIVED ="ReceivedPrivateMessageType";
  
  private static final String SENT ="SentPrivateMessageType";
  
  private static final String DELETED ="DeletedPrivateMessageType";
  
  private static final String DRAFT ="DraftPrivateMessageType";  
 
  private TypeManager typeManager;
  
  public void init()
  {
    ;
  }
  
  public void setTypeManager(TypeManager typeManager)
  {
    this.typeManager = typeManager;
  }

  public String getPrivateMessageAreaType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,PRIVATE);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,PRIVATE,"Private Forums", "Private Message Forums").getUuid());
    }
  }

  public String getDiscussionForumType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,DISCUSSION);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,DISCUSSION,"DISCUSSION FORUMS", "DISCUSSION Message Forums").getUuid());
    }

  }

  public String getOpenDiscussionForumType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,OPEN);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,OPEN,"OPEN DISCUSSION FORUMS", "OPEN DISCUSSION Message Forums").getUuid());
    }
  }

  public String getReceivedPrivateMessageType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,RECEIVED);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,RECEIVED,"Received Private Massage Type", "Received Private Massage Type").getUuid());
    }
  }



  public String getSentPrivateMessageType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,SENT);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,SENT,"Sent Private MassageType", "Sent Private Massage Type").getUuid());
    }
  }

  public String getDeletedPrivateMessageType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,DELETED);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,DELETED,"Deleted Private Massage Type", "Deleted Private Massage Type").getUuid());
    }
  }

  public String getDraftPrivateMessageType()
  {
    Type type = typeManager.getType(AUTHORITY,DOMAIN,DRAFT);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,DRAFT,"Draft Private Massage Type", "Draft Private Massage Type").getUuid());
    }
  }
   
}
