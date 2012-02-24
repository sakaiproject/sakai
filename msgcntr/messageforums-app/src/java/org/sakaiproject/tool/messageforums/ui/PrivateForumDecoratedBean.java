/**********************************************************************************
 * $URL$
 * $Id$
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
import java.util.List;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.PrivateForum;

public class PrivateForumDecoratedBean
{

  private PrivateForum forum;
  
  /**
   *List of decorated topics 
   */
  private List topics=new ArrayList();
  
  public PrivateForumDecoratedBean(PrivateForum forum)
  {
   this.forum= forum;    
  }
  
  /**
   * @return
   */
  public PrivateForum getForum()
  {
    return forum;
  }  
  
   
  /**
   * @return Returns the decorated topic.
   */
  public List getTopics()
  {
    return topics ;
  }

  public void addTopic(PrivateTopicDecoratedBean decoTopic)
  {
    if(!topics.contains(decoTopic))
    {
      topics.add(decoTopic);    
    }
  } 
  
	public ArrayList getAttachList()
	{
		ArrayList decoAttachList = new ArrayList();
		List attachList = forum.getAttachments(); 
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
