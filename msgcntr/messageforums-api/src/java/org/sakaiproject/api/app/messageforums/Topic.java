/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/Topic.java $
 * $Id: Topic.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
 
public interface Topic extends MutableEntity {

    /**
     * @return
     */
    public List<Attachment> getAttachments();

    /**
     * @param attachments
     */
    public void setAttachments(List<Attachment> attachments);

    public String getExtendedDescription();

    public void setExtendedDescription(String extendedDescription);

    public Boolean getMutable();

    public void setMutable(Boolean mutable);

    public String getShortDescription();

    public void setShortDescription(String shortDescription);

    public Integer getSortIndex();

    public void setSortIndex(Integer sortIndex);

    public String getTitle();

    public void setTitle(String title);

    public String getTypeUuid();

    public void setTypeUuid(String typeUuid);
    
    public BaseForum getBaseForum();
    
    public void setBaseForum(BaseForum forum);
    
    /**
     * @return List<Message>
     */
    public List<Message> getMessages();
    
    /**
     * @param messages
     */
    public void setMessages(List<Message> messages);

    public void addAttachment(Attachment attachment);
    
    public void removeAttachment(Attachment attachment);
    
    public void addMessage(Message message);
    
    public void removeMessage(Message message);

    public OpenForum getOpenForum();
    
    public void setOpenForum(OpenForum openForum);
    
    public PrivateForum getPrivateForum();
    
    public void setPrivateForum(PrivateForum privateForum);
    
    public Set getMembershipItemSet();

		public void setMembershipItemSet(Set membershipItemSet);			
    
    public void addMembershipItem(DBMembershipItem item); 

    public void removeMembershipItem(DBMembershipItem item);      

    public String getDefaultAssignName();
    
    public void setDefaultAssignName(String defaultAssignName);
    
    public Boolean getModerated();

    public void setModerated(Boolean moderated);

    public Boolean getAutoMarkThreadsRead();
    
    public void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead);
    
    public Boolean getAvailabilityRestricted();
    
    public void setAvailabilityRestricted(Boolean restricted);
      
    public Date getOpenDate();

	public void setOpenDate(Date openDate);
	
    public Date getCloseDate();
    
	public void setCloseDate(Date closeDate);
	
	public Boolean getAvailability();
    
    public void setAvailability(Boolean restricted);
    
    public Boolean getPostFirst();
    
    public void setPostFirst(Boolean postFirst);

    public Boolean getPostAnonymous();

    public void setPostAnonymous(Boolean postAnonymous);

    public Boolean getRevealIDsToRoles();

    public void setRevealIDsToRoles(Boolean revealIDsToRoles);
}
