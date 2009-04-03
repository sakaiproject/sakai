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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.api.app.messageforums;

import java.util.List;
import java.util.Set;

public interface Area extends MutableEntity {

    public void setVersion(Integer version);

    public String getContextId();

    public void setContextId(String contextId);

    public Boolean getHidden();

    public void setHidden(Boolean hidden);

    public String getName();

    public void setName(String name);

    public Boolean getEnabled();

    public void setEnabled(Boolean enabled);
    
    public Boolean getSendEmailOut();
    
    public void setSendEmailOut(Boolean sendEmailOut);

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
    
    public Set getMembershipItemSet();
			
	public void setMembershipItemSet(Set membershipItemSet);
    
    public void addMembershipItem(DBMembershipItem item);      

    public void removeMembershipItem(DBMembershipItem item);
      
}
