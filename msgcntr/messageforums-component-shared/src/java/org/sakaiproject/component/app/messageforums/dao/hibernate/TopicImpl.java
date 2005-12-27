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

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.Topic;

public abstract class TopicImpl extends MutableEntityImpl implements Topic {

    private static final Log LOG = LogFactory.getLog(TopicImpl.class);
    
    private String title;
    private String shortDescription;
    private String extendedDescription;
    private Set attachmentsSet;// = new HashSet();
    private Boolean mutable;
    private Integer sortIndex;
    private String typeUuid;
    private BaseForum baseForum;
    private Set messagesSet;// = new HashSet();
    
    // foreign keys for hibernate
    private PrivateForum privateForum;
    private OpenForum openForum;        
    
    // indecies for hibernate
    //private int ofindex;
    //private int pfindex;   

    public List getMessages() {
        return Util.setToList(messagesSet);
    }

    public void setMessages(List messages) {
        this.messagesSet = Util.listToSet(messages);
    }

    public Set getMessagesSet() {
        return messagesSet;
    }

    public void setMessagesSet(Set messagesSet) {
        this.messagesSet = messagesSet;
    }

    public Set getAttachmentsSet() {
        return attachmentsSet;
    }
  
    public void setAttachmentsSet(Set attachmentsSet) {
        this.attachmentsSet = attachmentsSet;
    }
    
    public List getAttachments()
    {
      return Util.setToList(attachmentsSet);
    }
  
    public void setAttachments(List attachments)
    {
      this.attachmentsSet = Util.listToSet(attachments);
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public Boolean getMutable() {
        return mutable;
    }

    public void setMutable(Boolean mutable) {
        this.mutable = mutable;
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

    public String getTypeUuid() {
        return typeUuid;
    }

    public void setTypeUuid(String typeUuid) {
        this.typeUuid = typeUuid;
    }

    public BaseForum getBaseForum() {
        return baseForum;
    }

    public void setBaseForum(BaseForum forum) {
        this.baseForum = forum;        
    }
    
    public OpenForum getOpenForum() {
        return openForum;
    }

    public void setOpenForum(OpenForum openForum) {
        this.openForum = openForum;
    }

    public PrivateForum getPrivateForum() {
        return privateForum;
    }

    public void setPrivateForum(PrivateForum privateForum) {
        this.privateForum = privateForum;
    }

//    public int getOfindex() {
//        try {
//            return getOpenForum().getTopics().indexOf(this);
//        } catch (Exception e) {
//            return ofindex;
//        }
//    }
//
//    public void setOfindex(int ofindex) {
//        this.ofindex = ofindex;
//    }
//
//    public int getPfindex() {
//        try {
//            return getPrivateForum().getTopics().indexOf(this);
//        } catch (Exception e) {
//            return pfindex;
//        }
//    }
//
//    public void setPfindex(int pfindex) {
//        this.pfindex = pfindex;
//    }
    
    public String toString() {
        return "Topic.id:" + id;
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Topic) {
            return getId().equals(((Topic)obj).getId());
        }
        return false;
    }

    // needs a better impl
    public int hashCode() {
        return getId() == null ? 0 : getId().hashCode();
    }
        
    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addMessage(Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addForum(message " + message + ")");
        }
        
        if (message == null) {
            throw new IllegalArgumentException("message == null");
        }
        
        if (messagesSet == null) {
            messagesSet = new HashSet();
        }
        message.setTopic(this);
        messagesSet.add(message);
    }

    public void removeMessage(Message message) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeForum(message " + message + ")");
        }
        
        if (message == null) {
            throw new IllegalArgumentException("Illegal message argument passed!");
        }
        
        message.setTopic(null);
        messagesSet.remove(message);
    }
   
    public void addAttachment(Attachment attachment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addAttachment(Attachment " + attachment + ")");
        }
        
        if (attachment == null) {
            throw new IllegalArgumentException("attachment == null");
        }
        
        if (attachmentsSet == null) {
            attachmentsSet = new HashSet();
        }
        attachment.setTopic(this);
        attachmentsSet.add(attachment);
    }

    public void removeAttachment(Attachment attachment) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAttachment(Attachment " + attachment + ")");
        }
        
        if (attachment == null) {
            throw new IllegalArgumentException("Illegal attachment argument passed!");
        }
        
        attachment.setTopic(null);
        attachmentsSet.remove(attachment);
    }

}
