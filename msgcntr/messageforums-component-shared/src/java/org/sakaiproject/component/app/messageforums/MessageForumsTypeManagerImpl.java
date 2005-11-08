package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager; 
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;
public class MessageForumsTypeManagerImpl extends HibernateDaoSupport implements MessageForumsTypeManager
{
  private static final Log LOG = LogFactory
  .getLog(MessageForumsTypeManagerImpl.class);
  private static final String AUTHORITY ="org.sakaiproject.component.app.messageforums";
  private static final String DOMAIN ="sakai_messageforums";
  private static final String PRIVATE ="privateForums";
  private static final String DISCUSSION ="discussionForums";
  private static final String OPEN ="openForums";
 
  private TypeManager typeManager;
  

  public String getPrivateType()
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
    Type type = typeManager.getType(AUTHORITY,DOMAIN,DISCUSSION);
    if(type!=null)
    {
      return type.getUuid();
    }
    else
    {
      return (typeManager.createType(AUTHORITY,DOMAIN,OPEN,"OPEN DISCUSSION FORUMS", "OPEN DISCUSSION Message Forums").getUuid());
    }
  }

  public void setTypeManager(TypeManager typeManager)
  {
    this.typeManager = typeManager;
  }
 
}
