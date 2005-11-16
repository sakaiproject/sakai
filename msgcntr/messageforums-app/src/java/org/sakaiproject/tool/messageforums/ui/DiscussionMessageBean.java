package org.sakaiproject.tool.messageforums.ui;

import org.sakaiproject.service.legacy.discussion.DiscussionMessage;

public class DiscussionMessageBean
{
  private boolean isSelected;
  private DiscussionMessage msg;

  public DiscussionMessageBean(DiscussionMessage msg)
  {
    this.msg = msg;
  }

  /**
   * @return
   */
  public boolean getIsSelected()
  {
    return isSelected;
  }

  /**
   * @param isSelected
   *          The isSelected to set.
   */
  public void setSelected(boolean isSelected)
  {
    this.isSelected = isSelected;
  }

  /**
   * @return Returns the msg.
   */
  public DiscussionMessage getMsg()
  {
    return msg;
  }

}
