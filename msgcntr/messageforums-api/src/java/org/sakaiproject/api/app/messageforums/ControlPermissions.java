/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/ControlPermissions.java $
 * $Id: ControlPermissions.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

public interface ControlPermissions {

    public Long getId();

    public void setId(Long id);

    public Boolean getPostToGradebook();
    
    public void setPostToGradebook(Boolean postToGradebook);
    
    public Integer getVersion();

    public void setVersion(Integer version);

    public Boolean getChangeSettings();

    public void setChangeSettings(Boolean changeSettings);

    public Boolean getMovePostings();

    public void setMovePostings(Boolean movePostings);

    public Boolean getNewResponse();

    public void setNewResponse(Boolean newResponse);

    public Boolean getNewTopic();

    public void setNewTopic(Boolean newTopic);

    public Boolean getResponseToResponse();

    public void setResponseToResponse(Boolean responseToResponse);

    public String getRole();

    public void setRole(String role);

    public Boolean getNewForum();
    
    public void setNewForum(Boolean newForum);

    public Area getArea();
    
    public void setArea(Area area);
    
    public BaseForum getForum();
    
    public void setForum(BaseForum forum);
    
    public Topic getTopic();
    
    public void setTopic(Topic topic);
    
    public Boolean getDefaultValue();

    public void setDefaultValue(Boolean defaultValue);
    
}