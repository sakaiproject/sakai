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
import org.sakaiproject.api.app.messageforums.PrivateTopic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

public class PrivateTopicImpl extends TopicImpl implements PrivateTopic {

    private static final Log LOG = LogFactory.getLog(PrivateTopicImpl.class);
    
    private String userId;
    private PrivateTopic parentTopic;
    private List childrenFolders = new UniqueArrayList();
    
    private int ptindex;
    
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public PrivateTopic getParentTopic() {
        return parentTopic;
    }

    public void setParentTopic(PrivateTopic parentTopic) {
        this.parentTopic = parentTopic;
    }

    public List getChildrenFolders() {
        return childrenFolders;
    }

    public void setChildrenFolders(List childrenFolders) {
        this.childrenFolders = childrenFolders;
    }

    public int getPtindex() {
        try {
            return getParentTopic().getChildrenFolders().indexOf(this);
        } catch (Exception e) {
            return ptindex;
        }
    }

    public void setPtindex(int ptindex) {
        this.ptindex = ptindex;
    }
    
    public Long getForumId() {
        return new Long(getPfindex());
    }

    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addChildFolder(PrivateTopic folder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addChildFolder(folder " + folder + ")");
        }
        
        if (folder == null) {
            throw new IllegalArgumentException("folder == null");
        }
        
        folder.setParentTopic(this);
        childrenFolders.add(folder);
    }

    public void removeChildFolder(PrivateTopic folder) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeChildFolder(folder " + folder + ")");
        }
        
        if (folder == null) {
            throw new IllegalArgumentException("Illegal folder argument passed!");
        }
        
        folder.setParentTopic(null);
        childrenFolders.remove(folder);
    }
}
