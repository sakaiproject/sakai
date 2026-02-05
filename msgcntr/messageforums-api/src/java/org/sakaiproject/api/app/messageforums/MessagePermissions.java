/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/MessagePermissions.java $
 * $Id: MessagePermissions.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

public interface MessagePermissions {

    public Long getId();

    public void setId(Long id);

    public Integer getVersion();

    public void setVersion(Integer version);

    public Boolean getDeleteAny();

    public void setDeleteAny(Boolean deleteAny);

    public Boolean getDeleteOwn();

    public void setDeleteOwn(Boolean deleteOwn);

    public Boolean getRead();

    public void setRead(Boolean read);

    public Boolean getReadDrafts();

    public void setReadDrafts(Boolean readDrafts);

    public Boolean getReviseAny();

    public void setReviseAny(Boolean reviseAny);

    public Boolean getReviseOwn();

    public Boolean getMarkAsNotRead();

    public void setMarkAsNotRead(Boolean markAsNotRead);
    
    public void setReviseOwn(Boolean reviseOwn);

    public String getRole();

    public void setRole(String role);
    
    public Boolean getDefaultValue();
    
    public void setDefaultValue(Boolean defaultValue);

    public Area getArea();
    
    public void setArea(Area area);
    
    public BaseForum getForum();
    
    public void setForum(BaseForum forum);
    
    public Topic getTopic();

    public void setTopic(Topic topic);
    
}