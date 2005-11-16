/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.tool.messageforums;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.ui.DiscussionForumManager;
import org.sakaiproject.tool.messageforums.ui.DiscussionTopicBean;

public class DiscussionForumTool 
{
    private static final Log LOG = LogFactory.getLog(DiscussionForumTool.class);
    /**
     *Dependency Injected 
     */
    private DiscussionForumManager forumManager;
    private List forums=new ArrayList();
    /**
     * @param forumManager
     */
    public void setForumManager(DiscussionForumManager forumManager)
    {
      if(LOG.isDebugEnabled())
      {
        LOG.debug("setForumManager(DiscussionForumManager "+forumManager+")");
      }
      this.forumManager = forumManager;
    }
    
    /**
     * @return
     */
    public List getForums()
    {
       List tempForum= forumManager.getDiscussionForumArea().getDiscussionForums();
       if(tempForum!=null)
       {
         Iterator iter = tempForum.iterator();
         while (iter.hasNext())
        {
          Topic topic = (Topic) iter.next();
          if(topic!=null)
          {
            DiscussionTopicBean decoTopic= new DiscussionTopicBean(topic);
            //TODO: remove this 
            decoTopic.setTotalNoMessages(forumManager.getTotalNoMessages(topic));
            decoTopic.setTotalNoMessages(forumManager.getUnreadNoMessages(topic));
            forums.add(decoTopic);
          }          
        }
       }
       return forums;
    }
        
    /**
     * TODO:// complete featute
     * @return
     */
    public boolean getUnderconstruction()
    {
      return true;
    }
    
    /**
     * @return
     */
    public String processCreateNewForum()
    {
      return "main";
    }

    /**
     * @return
     */
    public String processOrganize()
    {
      return "main";
    }

    /**
     * @return
     */
    public String processStatistics()
    {
      return "main";
    }
    
    /**
     * @return
     */
    public String processTemplateSettings()
    {
      return "main";
    }
    
    /**
     * @return
     */
    public String processForumSettings()
    {
      return "main";
    }
    
    /**
     * @return
     */
    public String processCreateNewTopic()
    {
      return "main";
    }
    
    /**
     * @return
     */
    public String processTopicSettings()
    {
      return "main";
    }
    
    
}