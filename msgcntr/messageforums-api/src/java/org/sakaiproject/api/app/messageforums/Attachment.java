/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/Attachment.java $
 * $Id: Attachment.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums;

public interface Attachment extends MutableEntity {

    public String getAttachmentId();

    public void setAttachmentId(String attachmentId);

    public String getAttachmentName();

    public void setAttachmentName(String attachmentName);

    public String getAttachmentSize();

    public void setAttachmentSize(String attachmentSize);

    public String getAttachmentType();

    public void setAttachmentType(String attachmentType);

    public String getAttachmentUrl();

    public void setAttachmentUrl(String attachmentUrl);

    public Message getMessage();

    public void setMessage(Message parent);

    public BaseForum getForum();

    public void setForum(BaseForum forum);
    
    public Topic getTopic();
    
    public void setTopic(Topic topic);
    
    public OpenForum getOpenForum();
    
    public void setOpenForum(OpenForum openForum);
    
    public PrivateForum getPrivateForum();
    
    public void setPrivateForum(PrivateForum privateForum);    
    
    //Is it required for editing attachment in Pvt Msg????
    public Long getPvtMsgAttachId();
    public void setPvtMsgAttachId(Long pvtMsgAttachId);
    public void setCreatedBy(String createdBy);
    public void setLastModifiedBy(String lastMOdifiedBy);
}