/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/Message.java $
 * $Id: Message.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.api.app.messageforums;

import java.util.Date;
import java.util.List;
import java.util.Set;

 

//import org.sakaiproject.component.app.messageforums.dao.hibernate.Type;

// TODO: Needs to be able to get to the MutableEntity stuff
// TODO: Make Type an interface too

public interface Message extends MutableEntity {

	public Boolean getDeleted();
	public void setDeleted(Boolean deleted);
    public Boolean getDraft();
    public void setDraft(Boolean draft);
    public Boolean getApproved();
    public void setApproved(Boolean approved);
    public Boolean getHasAttachments();
    public void setHasAttachments(Boolean hasAttachments);    
    public List getAttachments();
    public void setAttachments(List attachments);
    public Set getAttachmentsSet();
    public String getAuthor();
    public void setAuthor(String author);
    public String getAuthorId();
    public String getBody();
    public void setBody(String body);
    public Message getInReplyTo();
    public void setInReplyTo(Message inReplyTo);
    public String getLabel();
    public void setLabel(String label);
    public String getTitle();
    public void setTitle(String title);
    public String getTypeUuid();
    public void setTypeUuid(String typeUuid); 
    public void setTopic(Topic topic);
    public Topic getTopic();
    public void addAttachment(Attachment attachment);
    public void removeAttachment(Attachment attachment);
    public String getGradeAssignmentName();
    public void setGradeAssignmentName(String gradeAssignmentName);
    
    
    /**
     *  Set the threadId - efectivelly the id of the top level message in this thread
     * @param theadid
     */
    public void setThreadId(Long threadid);
    
    /**
     * the Id of the thread the message is in - null for top level messages
     * @return
     */
    public Long getThreadId();
    
    /**
     * The date that this thread was last updated 
     * @param date
     */
    public void setDateThreadlastUpdated(Date date);
    
    /**
     * 
     * @return
     */
    public Date getDateThreadlastUpdated();
    
    
    /**
     * Set the id of the last message posted in the tread
     * @param messageId
     */
    public void setThreadLastPost(Long messageId);
    
    /**
     * 
     * @return
     */
    public Long getThreadLastPost();
    
    /**
     * Set the number of Unique users who have read the message
     * @param numReaders
     */
    
    public void setNumReaders(Integer numReaders);
    /**
     * 
     * @return the number of unique users who have read the message
     */
    public Integer getNumReaders();
    
    
    
    

}