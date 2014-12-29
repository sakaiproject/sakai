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
    public static final int EMAIL_COPY_NEVER = 0;
    /**
     * setting for {@link #sendToEmail}. Sender is given the option of sending
     * a copy of message to email addresses 
     */
    public static final int EMAIL_COPY_OPTIONAL = 1;
    /**
     * setting for {@link #sendToEmail}. A copy of message is always sent
     * to recipients' email addresses
     */
    public static final int EMAIL_COPY_ALWAYS = 2;

    public void setVersion(Integer version);

    public String getContextId();

    public void setContextId(String contextId);

    public Boolean getHidden();

    public void setHidden(Boolean hidden);

    public String getName();

    public void setName(String name);

    public Boolean getEnabled();

    public void setEnabled(Boolean enabled);
    
    /**
     * {@link Deprecated} This option was replaced by sendToEmail via MSGCNTR-708. DO NOT USE.
     * @return
     */
    public Boolean getSendEmailOut();
    
    /**
     * {@link Deprecated} This option was replaced by sendToEmail via MSGCNTR-708. DO NOT USE.
     * @param sendEmailOut
     */
    public void setSendEmailOut(Boolean sendEmailOut);
    
    /**
     * 
     * @return the site-level setting for sending a copy of the message to recipients'
     * email addresses. This may be {@link #EMAIL_COPY_NEVER}, #{@link #EMAIL_COPY_OPTIONAL},
     * {@link #EMAIL_COPY_ALWAYS}
     */
    public int getSendToEmail();
    
    /**
     * set the site-level setting for sending a copy of the message to recipients'
     * email addresses. This may be {@link #EMAIL_COPY_NEVER}, #{@link #EMAIL_COPY_OPTIONAL},
     * {@link #EMAIL_COPY_ALWAYS}
     * @param sendToEmail
     */
    public void setSendToEmail(int sendToEmail);

    public List getOpenForums();
    
    public Set getOpenForumsSet();

    public void setOpenForums(List openForums);

    public List getPrivateForums();
    
    public Set getPrivateForumsSet();

    public void setPrivateForums(List discussionForums);

    public List getDiscussionForums();

    public void setDiscussionForums(List discussionForums);

    public String getTypeUuid();

    public void setTypeUuid(String typeUuid);

    public void addPrivateForum(BaseForum forum);

    public void removePrivateForum(BaseForum forum);

    public void addDiscussionForum(BaseForum forum);

    public void removeDiscussionForum(BaseForum forum);

    public void addOpenForum(BaseForum forum);

    public void removeOpenForum(BaseForum forum);

    public Boolean getLocked();
    
    public void setLocked(Boolean locked);
    
    public Boolean getModerated();
    
    public void setModerated(Boolean moderated);
    
    public Boolean getAutoMarkThreadsRead();
    
    public void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead);
    
    public Set getMembershipItemSet();
			
	public void setMembershipItemSet(Set membershipItemSet);
    
    public void addMembershipItem(DBMembershipItem item);      

    public void removeMembershipItem(DBMembershipItem item);
    
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
    
    public Set getHiddenGroups();
       
    public void setHiddenGroups(Set hiddenGroups);
}
