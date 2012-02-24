/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/BaseForum.java $
 * $Id: BaseForum.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;

public interface BaseForum extends MutableEntity {

    public List getAttachments();

    public void setAttachments(List attachments);
    
    public Set getAttachmentsSet();

    public String getExtendedDescription();

    public void setExtendedDescription(String extendedDescription);

    public String getShortDescription();

    public void setShortDescription(String shortDescription);

    public Integer getSortIndex();

    public void setSortIndex(Integer sortIndex);

    public String getTitle();

    public void setTitle(String title);

    public List getTopics();

    public void setTopics(List topics);
    
    public Set getTopicsSet();
      
    public void setTopicsSet(SortedSet topicsSet);
   
    public String getTypeUuid();

    public void setTypeUuid(String typeUuid);

    public Area getArea();

    public void setArea(Area area);
    
    public Boolean getModerated();

    public void setModerated(Boolean moderated);

    public void addTopic(Topic topic);

    public void removeTopic(Topic topic);

    public void addAttachment(Attachment attachment);

    public void removeAttachment(Attachment attachment);
    
    public Set getMembershipItemSet();
			
		public void setMembershipItemSet(Set membershipItemSet);		
		
		public void addMembershipItem(DBMembershipItem item);
		
		public void removeMembershipItem(DBMembershipItem item);
		
	public Boolean getPostFirst();

	public void setPostFirst(Boolean postFirst);

}