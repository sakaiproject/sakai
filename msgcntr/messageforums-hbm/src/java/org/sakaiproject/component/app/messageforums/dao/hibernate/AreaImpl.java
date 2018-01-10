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
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.DBMembershipItem;

@Slf4j
public class AreaImpl extends MutableEntityImpl implements Area
{

  private String contextId;

  private String name;

  private Boolean hidden;
  
  private Boolean locked;
  
  private Boolean moderated;
  
  private Boolean autoMarkThreadsRead;
  
  private Boolean enabled;
  
  private Boolean sendEmailOut=false;
  
  private int sendToEmail;

  private String typeUuid;

  //private List openForums = new UniqueArrayList();
  //private List privateForums = new UniqueArrayList();
  //private List discussionForums = new UniqueArrayList();

  private Set openForumsSet;// = new HashSet();
  private Set privateForumsSet;// = new HashSet();
  private Set discussionForumsSet;// = new HashSet();
  private Set membershipItemSet;
  private Set hiddenGroups;
  private Date openDate;
  private Date closeDate;
  
  private Boolean postFirst;
  
  /**
   * availabilityRestricted: this is the radio button the users turns on or off this feature with
   */
  private Boolean availabilityRestricted = false;
  /**
   * if availabilityRestricted, then this determines whether the area is disabled or not
   */
  private Boolean availability = true;
  
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
  
  public int getSendToEmail() {
      return sendToEmail;
  }

  public void setSendToEmail(int sendToEmail) {
      this.sendToEmail = sendToEmail;
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
      if (log.isDebugEnabled()) {
          log.debug("addPrivateForum(forum " + forum + ")");
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
      if (log.isDebugEnabled()) {
          log.debug("removePrivateForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("Illegal topic argument passed!");
      }
      
      forum.setArea(null);
      privateForumsSet.remove(forum);
  }
       
  public void addDiscussionForum(BaseForum forum) {
      if (log.isDebugEnabled()) {
          log.debug("addForum(forum " + forum + ")");
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
      if (log.isDebugEnabled()) {
          log.debug("removeDiscussionForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("Illegal topic argument passed!");
      }
      
      forum.setArea(null);
      discussionForumsSet.remove(forum);
      openForumsSet.remove(forum);
  }
       
  public void addOpenForum(BaseForum forum) {
      if (log.isDebugEnabled()) {
          log.debug("addOpenForum(forum " + forum + ")");
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
      if (log.isDebugEnabled()) {
          log.debug("removeOpenForum(forum " + forum + ")");
      }
      
      if (forum == null) {
          throw new IllegalArgumentException("Illegal topic argument passed!");
      }
      
      forum.setArea(null);
      openForumsSet.remove(forum);
  }
  
  public void addMembershipItem(DBMembershipItem item) {
    if (log.isDebugEnabled()) {
        log.debug("addMembershipItem(item " + item + ")");
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
    if (log.isDebugEnabled()) {
        log.debug("removeMembershipItem(item " + item + ")");
    }
    
    if (item == null) {
        throw new IllegalArgumentException("Illegal level argument passed!");
    }
        
    membershipItemSet.remove(item);
  }

	public Boolean getAvailabilityRestricted() {
		return availabilityRestricted;
	}
	
	public void setAvailabilityRestricted(Boolean restricted) {
		this.availabilityRestricted = restricted;
		
	}

	public Date getOpenDate() {
		return openDate;
	}

	public void setOpenDate(Date openDate) {
		this.openDate = openDate;
	}

	public Date getCloseDate() {
		return closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public Boolean getAvailability() {
		return availability;
	}

	public void setAvailability(Boolean availability) {
		this.availability = availability;
	}

	public Boolean getPostFirst() {
		return postFirst;
	}

	public void setPostFirst(Boolean postFirst) {
		this.postFirst = postFirst;
	}

	@Override
	public Set getHiddenGroups() {		
		return hiddenGroups;
	}

	@Override
	public void setHiddenGroups(Set hiddenGroups) {
		this.hiddenGroups = hiddenGroups;
	}

      /**
       * {@link Deprecated} This option was replaced by sendToEmail via MSGCNTR-708
       */
      public Boolean getSendEmailOut() {
          return sendEmailOut;
      }
    
      /**
       * {@link Deprecated} This option was replaced by sendToEmail via MSGCNTR-708
       */
      public void setSendEmailOut(Boolean sendEmailOut) {
          this.sendEmailOut = sendEmailOut;
      }

}
