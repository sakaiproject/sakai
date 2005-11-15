/**********************************************************************************
 * $URL: $
 * $Id:  $
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
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

public class AreaImpl extends MutableEntityImpl implements Area {

    private static final Log LOG = LogFactory.getLog(AreaImpl.class);

    private String contextId;

    private String name;

    private Boolean hidden;

    private Boolean enabled;

    private String typeUuid; 
    
    private List openForums = new UniqueArrayList();
    
    private List privateForums = new UniqueArrayList();

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeUuid() {
        return typeUuid;
    }

    public void setTypeUuid(String typeUuid) {
        this.typeUuid = typeUuid;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public List getOpenForums() {
        return openForums;
    }

    public void setOpenForums(List openForums) {
        this.openForums = openForums;
    }

    public List getPrivateForums() {
        return privateForums;
    }

    public void setPrivateForums(List privateForums) {
        this.privateForums = privateForums;
    }    


    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////


    public void addOpenForum(OpenForum forum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addOpenForum(Forum " + forum + ")");
        }
        
        if (forum == null) {
            throw new IllegalArgumentException("forum == null");
        }
        
        forum.setArea(this);
        openForums.add(forum);
    }

    public void removeOpenForum(OpenForum forum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeOpenForum(Forum " + forum + ")");
        }
        
        if (forum == null) {
            throw new IllegalArgumentException("Illegal forum argument passed!");
        }
        
        forum.setArea(null);
        openForums.remove(forum);
    }
    
    public void addPrivateForum(PrivateForum forum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addPrivateForum(Forum " + forum + ")");
        }
        
        if (forum == null) {
            throw new IllegalArgumentException("forum == null");
        }
        
        forum.setArea(this);
        privateForums.add(forum);
    }

    public void removePrivateForum(PrivateForum forum) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removePrivateForum(Forum " + forum + ")");
        }
        
        if (forum == null) {
            throw new IllegalArgumentException("Illegal forum argument passed!");
        }
        
        forum.setArea(null);
        privateForums.remove(forum);
    }    

}
