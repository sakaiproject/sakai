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

package org.sakaiproject.component.app.messageforums.dao.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

public class ActorPermissionsImpl implements ActorPermissions {

    private static final Log LOG = LogFactory.getLog(ActorPermissionsImpl.class);
    
    private List contributors = new UniqueArrayList();
    private List accessors = new UniqueArrayList();
    private List moderators = new UniqueArrayList();

    private Long id;
    private Integer version; 

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
                
    public List getAccessors() {
        return accessors;
    }

    public void setAccessors(List accessors) {
        this.accessors = accessors;
    }

    public List getContributors() {
        return contributors;
    }

    public void setContributors(List contributors) {
        this.contributors = contributors;
    }

    public List getModerators() {
        return moderators;
    }

    public void setModerators(List moderators) {
        this.moderators = moderators;
    }

    
    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addContributor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addContributor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setApContributors(this);
        contributors.add(user);
    }

    public void removeContributor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeContributor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setApContributors(null);
        contributors.remove(user);
    }
    
    public void addAccesssor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAccesssor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setApAccessors(this);
        accessors.add(user);
    }

    public void removeAccessor(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAccessor(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setApAccessors(null);
        accessors.remove(user);
    }    
    
    public void addModerator(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addModerator(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("user == null");
        }
        
        user.setApModerators(this);
        moderators.add(user);
    }

    public void removeModerator(MessageForumsUser user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeModerator(MessageForumsUser " + user + ")");
        }
        
        if (user == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        user.setApModerators(null);
        moderators.remove(user);
    }
    
}
