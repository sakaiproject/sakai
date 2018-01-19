/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/AttachmentImpl.java $
 * $Id: AttachmentImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Attachment;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.Message;
import org.sakaiproject.api.app.messageforums.OpenForum;
import org.sakaiproject.api.app.messageforums.PrivateForum;
import org.sakaiproject.api.app.messageforums.Topic;

@Slf4j
public class AttachmentImpl extends MutableEntityImpl implements Attachment {

    private String attachmentId;

    private String attachmentUrl;

    private String attachmentName;

    private String attachmentSize;

    private String attachmentType;

    private Long pvtMsgAttachId;
    
    // foreign keys for hibernate
    private Message message;
    private BaseForum forum;
    private Topic topic;    
    private OpenForum openForum;
    private PrivateForum privateForum;
    
    // indecies for hibernate
//    private int mesindex;    
//    private int ofindex;
//    private int pfindex;
//    private int tindex;
   
    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getAttachmentSize() {
        return attachmentSize;
    }

    public void setAttachmentSize(String attachmentSize) {
        this.attachmentSize = attachmentSize;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
    
//    public boolean equals(Object other) {
//        if (!(other instanceof AttachmentImpl)) {
//            return false;
//        }
//
//        Attachment attachment = (Attachment) other;
//        return new EqualsBuilder().append(attachmentId, attachment.getAttachmentId()).isEquals();
//    }
//
//    public int hashCode() {
//        return new HashCodeBuilder().append(attachmentId).toHashCode();
//    }
//
//    public String toString() {
//        return new ToStringBuilder(this).append("attachmentId", attachmentId).append("attachmentUrl", attachmentUrl).append("attachmentName", attachmentName).append("attachmentSize", attachmentSize).append("attachmentType", attachmentType).toString();
//    }

    public BaseForum getForum() {
        return forum;
    }

    public void setForum(BaseForum forum) {
        this.forum = forum;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

//    public int getMesindex() {
//        try {
//            return getMessage().getAttachments().indexOf(this);
//        } catch (Exception e) {
//            return mesindex;
//        }
//    }
//
//    public void setMesindex(int mesindex) {
//        this.mesindex = mesindex;
//    }
//
//    public int getOfindex() {
//        try {
//            return getForum().getAttachments().indexOf(this);
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
//            return getForum().getAttachments().indexOf(this);
//        } catch (Exception e) {
//            return pfindex;
//        }
//    }
//
//    public void setPfindex(int pfindex) {
//        this.pfindex = pfindex;
//    }
//
//    public int getTindex() {
//        try {
//            return getTopic().getAttachments().indexOf(this);
//        } catch (Exception e) {
//            return tindex;
//        }
//    }
//
//    public void setTindex(int tindex) {
//        this.tindex = tindex;
//    }

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

    public Long getPvtMsgAttachId()
    {
      return pvtMsgAttachId;
    }

    public void setPvtMsgAttachId(Long pvtMsgAttachId)
    {
      this.pvtMsgAttachId=pvtMsgAttachId;
    }

    public void setLastModifiedBy(String lastMOdifiedBy)
    {
      // TODO Auto-generated method stub
      
    }
}
