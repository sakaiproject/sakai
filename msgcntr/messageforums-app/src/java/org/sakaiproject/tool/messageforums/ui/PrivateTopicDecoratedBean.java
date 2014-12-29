/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/PrivateTopicDecoratedBean.java $
 * $Id: PrivateTopicDecoratedBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
package org.sakaiproject.tool.messageforums.ui;
 
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
import org.sakaiproject.api.app.messageforums.Topic;

public class PrivateTopicDecoratedBean
{
  private Topic topic;
  private int totalNoMessages;
  private int unreadNoMessages;
  private List messages;

  private boolean hasNextTopic=false;
  private boolean hasPreviousTopic=false;
  private String nextTopicId;
  private String previousTopicId;
  private String previousTopicTitle;
  private String nextTopicTitle;
  
  public PrivateTopicDecoratedBean(Topic topic)
  {
   this.topic= topic;    
  }
  public Topic getTopic()
  {
    return topic;
  }
  

  /**
   * @return
   */
  public int getTotalNoMessages()
  {
    return totalNoMessages;
  }
  
  /**
   * @param totalMessages
   */
  public void setTotalNoMessages(int totalMessages)
  {
    this.totalNoMessages = totalMessages;
  }
  
  /**
   * @return
   */
  public int getUnreadNoMessages()
  {
    return unreadNoMessages;
  }
  
  /**
   * @param unreadMessages
   */
  public void setUnreadNoMessages(int unreadMessages)
  {
    this.unreadNoMessages = unreadMessages;
  }
  
  
  /**
   * @return Returns the decorated messages.
   */
  public List getMessages()
  {
    List tmpMessages = topic.getMessages();
    if (tmpMessages !=null)
    {
      Iterator iter = tmpMessages.iterator();
      while (iter.hasNext())
      {
        PrivateMessage message = (PrivateMessage) iter.next();
        messages.add(new PrivateMessageDecoratedBean(message));
      }
    }
    return messages ;
  }
  
  
  private List msgs=new ArrayList();
  public void addPvtMessage(PrivateMessageDecoratedBean decomsg)
  {
    if(!msgs.contains(decomsg))
    {
      msgs.add(decomsg);    
    }
  }
 
  

  /**
   * @return Returns the hasNextTopic.
   */
  public boolean getHasNextTopic()
  {
    return hasNextTopic;
  }

  /**
   * @param hasNextTopic
   *          The hasNextTopic to set.
   */
  public void setHasNextTopic(boolean hasNextTopic)
  {
    this.hasNextTopic = hasNextTopic;
  }

  /**
   * @return Returns the hasPreviousTopic.
   */
  public boolean getHasPreviousTopic()
  {
    return hasPreviousTopic;
  }

  /**
   * @param hasPreviousTopic
   *          The hasPreviousTopic to set.
   */
  public void setHasPreviousTopic(boolean hasPreviousTopic)
  {
    this.hasPreviousTopic = hasPreviousTopic;
  }

  /**
   * @return Returns the nextTopicId.
   */
  public String getNextTopicId()
  {
    return nextTopicId;
  }

  /**
   * @param nextTopicId
   *          The nextTopicId to set.
   */
  public void setNextTopicId(String nextTopicId)
  {
    this.nextTopicId = nextTopicId;
  }

  /**
   * @return Returns the previousTopicId.
   */
  public String getPreviousTopicId()
  {
    return previousTopicId;
  }

  /**
   * @param previousTopicId
   *          The previousTopicId to set.
   */
  public void setPreviousTopicId(String previousTopicId)
  {
    this.previousTopicId = previousTopicId;
  }
  /**
   * @return Returns the nextTopicTitle.
   */
  public String getNextTopicTitle()
  {
    return nextTopicTitle;
  }
  /**
   * @param nextTopicTitle The nextTopicTitle to set.
   */
  public void setNextTopicTitle(String nextTopicTitle)
  {
    this.nextTopicTitle = nextTopicTitle;
  }
  /**
   * @return Returns the previousTopicTitle.
   */
  public String getPreviousTopicTitle()
  {
    return previousTopicTitle;
  }
  /**
   * @param previousTopicTitle The previousTopicTitle to set.
   */
  public void setPreviousTopicTitle(String previousTopicTitle)
  {
    this.previousTopicTitle = previousTopicTitle;
  }
 
	public ArrayList getAttachList()
	{
		ArrayList decoAttachList = new ArrayList();
		List attachList = topic.getAttachments(); 
		if(attachList != null)
		{
			for(int i=0; i<attachList.size(); i++)
			{
				DecoratedAttachment decoAttach = new DecoratedAttachment((Attachment)attachList.get(i));
				decoAttachList.add(decoAttach);
			}
		}
		return decoAttachList;
	}
}


