/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/ControlPermissionsImpl.java $
 * $Id: ControlPermissionsImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.ControlPermissions;
import org.sakaiproject.api.app.messageforums.Topic;

public class ControlPermissionsImpl implements ControlPermissions {

    private String role;

    private Boolean postToGradebook;

    private Boolean newForum;

    private Boolean newTopic;

    private Boolean newResponse;

    private Boolean responseToResponse;

    private Boolean movePostings;

    private Boolean changeSettings;

    private Boolean defaultValue;

    private Area area;

    private BaseForum forum;

    private Topic topic;

//    private int areaindex;
//
//    private int forumindex;
//
//    private int topicindex;

    private Long id;

    private Integer version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

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

    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getNewForum() {
        return newForum;
    }

    public void setNewForum(Boolean newForum) {
        this.newForum = newForum;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public BaseForum getForum() {
        return forum;
    }

    public void setForum(BaseForum forum) {
        this.forum = forum;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

//    public int getAreaindex() {
//        try {
//            return getArea().getOpenForums().indexOf(this);
//        } catch (Exception e) {
//            return areaindex;
//        }
//    }
//
//    public void setAreaindex(int areaindex) {
//        this.areaindex = areaindex;
//    }
//
//    public int getForumindex() {
//        try {
//            return getForum().getTopics().indexOf(this);
//        } catch (Exception e) {
//            return forumindex;
//        }
//    }
//
//    public void setForumindex(int forumindex) {
//        this.forumindex = forumindex;
//    }
//
//    public int getTopicindex() {
//        try {
//            return getTopic().getMessages().indexOf(this);
//        } catch (Exception e) {
//            return topicindex;
//        }
//    }
//
//    public void setTopicindex(int topicindex) {
//        this.topicindex = topicindex;
//    }

    public String toString() {
    	return "ControlPermissions/" + id;
        //return "ControlPermissions.id:" + id;
    }

}
