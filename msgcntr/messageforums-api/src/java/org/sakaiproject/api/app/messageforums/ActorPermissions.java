/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/ActorPermissions.java $
 * $Id: ActorPermissions.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import java.util.List;

public interface ActorPermissions {

    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public List getAccessors();

    public void setAccessors(List accessors);

    public List getContributors();

    public void setContributors(java.util.List contributors);

    public java.util.List getModerators();

    public void setModerators(java.util.List moderators);
    
    public void addAccesssor(MessageForumsUser user);
    
    public void removeAccessor(MessageForumsUser user);
    
    public void addModerator(MessageForumsUser user);
    
    public void removeModerator(MessageForumsUser user);
    
    public void addContributor(MessageForumsUser user);
    
    public void removeContributor(MessageForumsUser user);

}