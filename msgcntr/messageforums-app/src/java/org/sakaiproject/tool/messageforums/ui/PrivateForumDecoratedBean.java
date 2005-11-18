/**********************************************************************************
*
* $Header$
*
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
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

package org.sakaiproject.tool.messageforums.ui;

import java.util.ArrayList;
import java.util.List;

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
  
}
