/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/AreaControlPermissionImpl.java $
 * $Id: AreaControlPermissionImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.sakaiproject.api.app.messageforums.AreaControlPermission;

public class AreaControlPermissionImpl implements AreaControlPermission {

    private String role;

    private Boolean postToGradebook;
    
    private Boolean newForum;

    private Boolean newTopic;

    private Boolean newResponse;

    private Boolean responseToResponse;

    private Boolean movePostings;

    private Boolean changeSettings;

    public Boolean getPostToGradebook() {
        return postToGradebook;
    }

    public void setPostToGradebook(Boolean postToGradebook) {
        this.postToGradebook = postToGradebook;
    }

    public Boolean getChangeSettings() {
        return changeSettings;
    }

    public void setChangeSettings(Boolean changeSettings) {
        this.changeSettings = changeSettings;
    }

    public Boolean getMovePostings() {
        return movePostings;
    }

    public void setMovePostings(Boolean movePostings) {
        this.movePostings = movePostings;
    }

    public Boolean getNewForum() {
        return newForum;
    }

    public void setNewForum(Boolean newForum) {
        this.newForum = newForum;
    }

    public Boolean getNewResponse() {
        return newResponse;
    }

    public void setNewResponse(Boolean newResponse) {
        this.newResponse = newResponse;
    }

    public Boolean getNewTopic() {
        return newTopic;
    }

    public void setNewTopic(Boolean newTopic) {
        this.newTopic = newTopic;
    }

    public Boolean getResponseToResponse() {
        return responseToResponse;
    }

    public void setResponseToResponse(Boolean responseToResponse) {
        this.responseToResponse = responseToResponse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
