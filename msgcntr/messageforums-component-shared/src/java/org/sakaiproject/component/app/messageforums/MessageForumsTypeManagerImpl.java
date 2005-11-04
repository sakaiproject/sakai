package org.sakaiproject.component.app.messageforums;

import java.util.List;

import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.common.type.Type;
import org.sakaiproject.api.common.type.TypeManager;
public class MessageForumsTypeManagerImpl implements MessageForumsTypeManager
{
  private TypeManager typeManager;
  public List getAvailableTypes()
  {
    return null;

  }

  public Type getPrivateType()
  {
    //typeManager.getType();
    return null;

  }

  public org.sakaiproject.api.common.type.Type getDiscussionForumType()
  {
    return null;

  }

  public org.sakaiproject.api.common.type.Type getOpenDiscussionForumType()
  {
    return null;

  }

}
