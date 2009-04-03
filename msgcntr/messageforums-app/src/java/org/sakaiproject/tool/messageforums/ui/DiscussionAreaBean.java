/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-app/src/java/org/sakaiproject/tool/messageforums/ui/DiscussionAreaBean.java $
 * $Id: DiscussionAreaBean.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.messageforums.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.app.messageforums.Area;

/**
 * 
 * @author <a href="mailto:wagnermr@iupui.edu">Michelle Wagner</a>
 *
 */
public class DiscussionAreaBean 
{
	 private static final Log LOG = LogFactory.getLog(DiscussionAreaBean.class);
	 
	 private Area area;
	 private int numPendingMsgs;
	 
	 public DiscussionAreaBean(Area area)
	 {
	    if(LOG.isDebugEnabled())
	    {
	      LOG.debug("DiscussionAreaBean(DiscussionArea " + area + ")");
	    }
	    
		this.area = area;
	 }
	 
	 /**
	   * Returns whether the forum is moderated or not
	   * @return
	   */
	  public String getModerated()
	  {
		  LOG.debug("getModerated()");
		  if (area == null || area.getModerated() == null || 
			  area.getModerated().booleanValue() == false)
		  {
			  return Boolean.FALSE.toString();
		  }

		  return Boolean.TRUE.toString();
	  }
	  
	  /**
	   * Set the "moderated" setting for the forum
	   * @param moderated
	   */
	  public void setModerated(String moderated)
	  {
		  LOG.debug("setModerated()");
		  if (moderated.equals(Boolean.TRUE.toString()))
		  {
			  area.setModerated(new Boolean(true));
		  }
		  else
		  {
			  area.setModerated(new Boolean(false));
		  }
	  }
	  
	  /**
	   * 
	   * @return
	   */
	  public Area getArea()
	  {
		  return area;
	  }
	  
	  /**
	   * returns boolean moderated status for area
	   * @return
	   */
	  public boolean isAreaModerated()
	  {
		  return area.getModerated().booleanValue();
	  }
	  
	  /**
	   * Returns number of msgs pending in moderated topics in which
	   * user has moderate perm
	   * @return
	   */
	  public int getNumPendingMsgs()
	  {
		  return numPendingMsgs;
	  }
	  
	  /**
	   * Set num of pending msgs in area
	   *
	   */
	  public void setNumPendingMsgs(int numPendingMsgs)
	  {
		  this.numPendingMsgs = numPendingMsgs;
	  }
}
