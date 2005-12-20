package org.sakaiproject.tool.messageforums.ui;

import org.sakaiproject.api.app.messageforums.Message;

import org.sakaiproject.api.kernel.component.cover.ComponentManager;
import org.sakaiproject.api.app.messageforums.MessageForumsMessageManager;



/**
 * @author <a href="mailto:rshastri@iupui.edu">Rashmi Shastri</a>
 *
 */
public class DiscussionMessageBean
{
  private boolean selected;
  private Message message;
  private boolean read;

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
    else if(message.getAttachments().size()>0)
    {
    	return true;
    }
/*    else (changed after not lazy loading attachments.)
    {
    	MessageForumsMessageManager mfmm = 
    		(org.sakaiproject.api.app.messageforums.MessageForumsMessageManager)ComponentManager.get("org.sakaiproject.api.app.messageforums.MessageForumsMessageManager");
    	Message messageWithAttach = mfmm.getMessageByIdWithAttachments(message.getId());
    	if(messageWithAttach != null)
    	{
    		if(messageWithAttach.getAttachments().size()>0)
    			return true;
    	}
    }*/
    return false;
  }



  /**
   * @return Returns the read.
   */
  public boolean isRead()
  {
    return read;
  }



  /**
   * @param read The read to set.
   */
  public void setRead(boolean read)
  {
    this.read = read;
  }

 

  
}
