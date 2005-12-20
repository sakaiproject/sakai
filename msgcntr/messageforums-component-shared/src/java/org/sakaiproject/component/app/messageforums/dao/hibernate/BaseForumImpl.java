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
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;
 
 
public class BaseForumImpl extends MutableEntityImpl implements BaseForum {

    private static final Log LOG = LogFactory.getLog(BaseForumImpl.class);

    private String title;
    private String shortDescription;
    private String extendedDescription;
    private String typeUuid;
    private List attachments = new UniqueArrayList();
    private List topics = new UniqueArrayList();
    private Area area;
    private Integer sortIndex; 
    
    public List getAttachments() {
        return attachments;
    }

    public void setAttachments(List attachments) {
        this.attachments = attachments;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List getTopics() {
        return topics;
    }

    public void setTopics(List topics) {
        this.topics = topics;
    }

    public String getTypeUuid() {
        return typeUuid;
    }

    public void setTypeUuid(String typeUuid) {
        this.typeUuid = typeUuid;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String toString() {
        return "Forum.id:" + id;
    }
    
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof BaseForum) {
            return getId().equals(((BaseForum)obj).getId());
        }
        return false;
    }

    // needs a better impl
    public int hashCode() {
        return getId().hashCode();
    }

    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addTopic(Topic topic) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addTopic(topic " + topic + ")");
        }
        
        if (topic == null) {
            throw new IllegalArgumentException("topic == null");
        }
        
        topic.setBaseForum(this);
        topics.add(topic);
    }

    public void removeTopic(Topic topic) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeTopic(topic " + topic + ")");
        }
        
        if (topic == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        
        topic.setBaseForum(null);
        topics.remove(topic);
    }
       
    public void addAttachment(Attachment attachment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAttachment(Attachment " + attachment + ")");
        }
        
        if (attachment == null) {
            throw new IllegalArgumentException("attachment == null");
        }
        
        attachment.setForum(this);
        attachments.add(attachment);
    }

    public void removeAttachment(Attachment attachment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAttachment(Attachment " + attachment + ")");
        }
        
        if (attachment == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        attachment.setForum(null);
        attachments.remove(attachment);
    }
    

}
