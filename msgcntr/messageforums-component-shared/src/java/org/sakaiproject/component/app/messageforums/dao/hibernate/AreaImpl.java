/*******************************************************************************************************
 * $URL: $ $Id: $ **********************************************************************************
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University, Board
 * of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation Licensed under the
 * Educational Community License Version 1.0 (the "License"); By obtaining, using and/or copying this
 * Original Work, you agree that you have read, understand, and will comply with the terms and
 * conditions of the Educational Community License. You may obtain a copy of the License at:
 * http://cvs.sakaiproject.org/licenses/license_1_0.html THE SOFTWARE IS PROVIDED "AS IS", WITHOUT
 * WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************************************/

package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BaseForum;

public class AreaImpl extends MutableEntityImpl implements Area
{

  private static final Log LOG = LogFactory.getLog(AreaImpl.class);

  private String contextId;

  private String name;

  private Boolean hidden;
  
  private Boolean locked;
  
  private Boolean enabled;

  private String typeUuid;

  //private List openForums = new UniqueArrayList();
  //private List privateForums = new UniqueArrayList();
  //private List discussionForums = new UniqueArrayList();

  private Set openForumsSet;// = new HashSet();
  private Set privateForumsSet;// = new HashSet();
  private Set discussionForumsSet;// = new HashSet();
  
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
      return "Area.id:" + id;
  }
  
  public Boolean getLocked() {
      return locked;
  }

  public void setLocked(Boolean locked) {
      this.locked = locked;
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

}
