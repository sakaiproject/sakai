/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/Area.java $
 * $Id: Area.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

public interface Area extends MutableEntity {
    
    /**
     * setting for {@link #sendToEmail}. A copy of message is never sent
     * to recipients' email addresses
     */
    int EMAIL_COPY_NEVER = 0;
    /**
     * setting for {@link #sendToEmail}. Sender is given the option of sending
     * a copy of message to email addresses 
     */
    int EMAIL_COPY_OPTIONAL = 1;
    /**
     * setting for {@link #sendToEmail}. A copy of message is always sent
     * to recipients' email addresses
     */
    int EMAIL_COPY_ALWAYS = 2;

    void setVersion(Integer version);

    String getContextId();

    void setContextId(String contextId);

    Boolean getHidden();

    void setHidden(Boolean hidden);

    String getName();

    void setName(String name);

    Boolean getEnabled();

    void setEnabled(Boolean enabled);
    
    Boolean getSendEmailOut();
    
    void setSendEmailOut(Boolean sendEmailOut);
    
    /**
     * 
     * @return the site-level setting for sending a copy of the message to recipients'
     * email addresses. This may be {@link #EMAIL_COPY_NEVER}, #{@link #EMAIL_COPY_OPTIONAL},
     * {@link #EMAIL_COPY_ALWAYS}
     */
    int getSendToEmail();
    
    /**
     * set the site-level setting for sending a copy of the message to recipients'
     * email addresses. This may be {@link #EMAIL_COPY_NEVER}, #{@link #EMAIL_COPY_OPTIONAL},
     * {@link #EMAIL_COPY_ALWAYS}
     * @param sendToEmail
     */
    void setSendToEmail(int sendToEmail);

    List<OpenForum> getOpenForums();
    
    Set<OpenForum> getOpenForumsSet();

    void setOpenForums(List<OpenForum> openForums);

    List<PrivateForum> getPrivateForums();
    
    Set<PrivateForum> getPrivateForumsSet();

    void setPrivateForums(List<PrivateForum> discussionForums);

    List<DiscussionForum> getDiscussionForums();

    void setDiscussionForums(List<DiscussionForum> discussionForums);

    String getTypeUuid();

    void setTypeUuid(String typeUuid);

    void addPrivateForum(PrivateForum forum);

    void removePrivateForum(PrivateForum forum);

    void addDiscussionForum(DiscussionForum forum);

    void removeDiscussionForum(DiscussionForum forum);

    void addOpenForum(OpenForum forum);

    void removeOpenForum(OpenForum forum);

    Boolean getLocked();
    
    void setLocked(Boolean locked);
    
    Boolean getModerated();
    
    void setModerated(Boolean moderated);
    
    Boolean getAutoMarkThreadsRead();
    
    void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead);
    
    Set<DBMembershipItem> getMembershipItemSet();
			
	void setMembershipItemSet(Set<DBMembershipItem> membershipItemSet);
    
    void addMembershipItem(DBMembershipItem item);

    void removeMembershipItem(DBMembershipItem item);
    
    Boolean getAvailabilityRestricted();
    
    void setAvailabilityRestricted(Boolean restricted);
      
    Date getOpenDate();

	void setOpenDate(Date openDate);
	
    Date getCloseDate();
    
	void setCloseDate(Date closeDate);
	
	Boolean getAvailability();
    
    void setAvailability(Boolean restricted);
    
    Boolean getPostFirst();
    
    void setPostFirst(Boolean postFirst);
    
    Set<HiddenGroup> getHiddenGroups();
       
    void setHiddenGroups(Set<HiddenGroup> hiddenGroups);
}
