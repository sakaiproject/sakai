/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/AreaImpl.java $
 * $Id: AreaImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;

public class AreaImpl extends MutableEntityImpl implements Area
{

  private static final Log LOG = LogFactory.getLog(AreaImpl.class);

  private String contextId;

  private String name;

  private Boolean hidden;
  
  private Boolean locked;
  
  private Boolean moderated;
  
  private Boolean autoMarkThreadsRead;
  
  private Boolean enabled;
  
  private Boolean sendEmailOut;

  private String typeUuid;

  //private List openForums = new UniqueArrayList();
  //private List privateForums = new UniqueArrayList();
  //private List discussionForums = new UniqueArrayList();

  private Set openForumsSet;// = new HashSet();
  private Set privateForumsSet;// = new HashSet();
  private Set discussionForumsSet;// = new HashSet();
  private Set membershipItemSet;
  
  public void setVersion(Integer version)
  {
    this.version = version;
  }

  public String getContextId()
  {
    return contextId;
  }

  public void setContextId(String contextId)
  {
    this.contextId = contextId;
  }

  public Boolean getHidden()
  {
    return hidden;
  }

  public void setHidden(Boolean hidden)
  {
    this.hidden = hidden;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getTypeUuid()
  {
    return typeUuid;
  }

  public void setTypeUuid(String typeUuid)
  {
    this.typeUuid = typeUuid;
  }

  public Boolean getEnabled()
  {
    return enabled;
  }

  public void setEnabled(Boolean enabled)
  {
    this.enabled = enabled;
  }
  
  public Boolean getSendEmailOut()
  {
    return sendEmailOut;
  }

  public void setSendEmailOut(Boolean sendEmailOut)
  {
    this.sendEmailOut = sendEmailOut;
  }

  public List getOpenForums()
  {
    return Util.setToList(openForumsSet);
  }

  public void setOpenForums(List openForums)
  {
    this.openForumsSet = Util.listToSet(openForums);
  }

  public List getPrivateForums()
  {
      return Util.setToList(privateForumsSet);
  }

  public void setPrivateForums(List privateForums)
  {
    this.privateForumsSet = Util.listToSet(privateForums);
  }

  public List getDiscussionForums()
  {
      return Util.setToList(discussionForumsSet);
  }

  public void setDiscussionForums(List discussionForums)
  {
    this.discussionForumsSet = Util.listToSet(discussionForums);
  }
         
  public String toString() {
      //return "Area.id:" + id;
  	return "Area/" + id; 
  }
  
  public Boolean getLocked() {
      return locked;
  }

  public void setLocked(Boolean locked) {
      this.locked = locked;
  }
  
  public Boolean getModerated() {
	  return moderated;
  }
  
  public void setModerated(Boolean moderated) {
	  this.moderated = moderated;
  }

  public Boolean getAutoMarkThreadsRead() {
	return autoMarkThreadsRead;
}

public void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead) {
	this.autoMarkThreadsRead = autoMarkThreadsRead;
}

public Set getDiscussionForumsSet() {
      return discussionForumsSet;
  }

  public void setDiscussionForumsSet(Set discussionForumsSet) {
      this.discussionForumsSet = discussionForumsSet;
  }

  public Set getOpenForumsSet() {
      return openForumsSet;
  }

  public void setOpenForumsSet(Set openForumsSet) {
      this.openForumsSet = openForumsSet;
  }

  public Set getPrivateForumsSet() {
      return privateForumsSet;
  }

  public void setPrivateForumsSet(Set privateForumsSet) {
      this.privateForumsSet = privateForumsSet;            
  }
  
  public Set getMembershipItemSet() {
		return membershipItemSet;
	}

	public void setMembershipItemSet(Set membershipItemSet) {
		this.membershipItemSet = membershipItemSet;
	}
	
  ////////////////////////////////////////////////////////////////////////
  // helper methods for collections
  ////////////////////////////////////////////////////////////////////////
  
  public void addPrivateForum(BaseForum forum) {
      if (LOG.isDebugEnabled()) {
          LOG.debug("addPrivateForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("forum == null");
      }
      
      if (privateForumsSet == null) {
          privateForumsSet = new TreeSet();
      }
      forum.setArea(this);
      privateForumsSet.add(forum);
  }

  public void removePrivateForum(BaseForum forum) {
      if (LOG.isDebugEnabled()) {
          LOG.debug("removePrivateForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("Illegal topic argument passed!");
      }
      
      forum.setArea(null);
      privateForumsSet.remove(forum);
  }
       
  public void addDiscussionForum(BaseForum forum) {
      if (LOG.isDebugEnabled()) {
          LOG.debug("addForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("forum == null");
      }
      
      if (discussionForumsSet == null) {
          discussionForumsSet = new TreeSet();
      }
      forum.setArea(this);
      discussionForumsSet.add(forum);
  }

  public void removeDiscussionForum(BaseForum forum) {
      if (LOG.isDebugEnabled()) {
          LOG.debug("removeDiscussionForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("Illegal topic argument passed!");
      }
      
      forum.setArea(null);
      discussionForumsSet.remove(forum);
      openForumsSet.remove(forum);
  }
       
  public void addOpenForum(BaseForum forum) {
      if (LOG.isDebugEnabled()) {
          LOG.debug("addOpenForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("forum == null");
      }
      
      if (openForumsSet == null) {
          openForumsSet = new TreeSet();
      }      
      forum.setArea(this);
      openForumsSet.add(forum);
  }

  public void removeOpenForum(BaseForum forum) {
      if (LOG.isDebugEnabled()) {
          LOG.debug("removeOpenForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("Illegal topic argument passed!");
      }
      
      forum.setArea(null);
      openForumsSet.remove(forum);
  }
  
  public void addMembershipItem(DBMembershipItem item) {
    if (LOG.isDebugEnabled()) {
        LOG.debug("addMembershipItem(item " + item + ")");
    }
    
    if (item == null) {
        throw new IllegalArgumentException("item == null");
    }
    
    if (membershipItemSet == null) {
    	membershipItemSet = new HashSet();
    }          
    membershipItemSet.add(item);
}

  public void removeMembershipItem(DBMembershipItem item) {
    if (LOG.isDebugEnabled()) {
        LOG.debug("removeMembershipItem(item " + item + ")");
    }
    
    if (item == null) {
        throw new IllegalArgumentException("Illegal level argument passed!");
    }
        
    membershipItemSet.remove(item);
  }

}
