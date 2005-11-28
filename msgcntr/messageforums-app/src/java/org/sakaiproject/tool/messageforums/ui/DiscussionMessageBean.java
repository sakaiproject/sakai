package org.sakaiproject.tool.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Message;



public class DiscussionMessageBean
{
  private boolean selected;
  private Message message;

  public DiscussionMessageBean(Message msg)
  {
    this.message = msg;
  }

 

  /**
   * @return Returns the selected.
   */
  public boolean isSelected()
  {
    return selected;
  }



  /**
   * @param selected The selected to set.
   */
  public void setSelected(boolean selected)
  {
    this.selected = selected;
  }



  /**
   * @return Returns the msg.
   */
  public Message getMessage()
  {
    return message;
  }

  /**
   * @return Returns the hasAttachment.
   */
  public boolean isHasAttachment()
  {
    if(message==null)
    {
      return false;
    }
    if(message.getAttachments()==null)
    {
      return false;
    }
    if(message.getAttachments().size()>0)
    {
      return true;
    }
    return false;
  }

 

  
}
