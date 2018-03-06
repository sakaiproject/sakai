/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/MessagePermissionsImpl.java $
 * $Id: MessagePermissionsImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.Topic;

@Slf4j
public class MessagePermissionsImpl implements MessagePermissions {

    private String role;

    private Boolean read;

    private Boolean reviseAny;

    private Boolean reviseOwn;

    private Boolean deleteAny;

    private Boolean deleteOwn;

    private Boolean readDrafts;

    private Boolean defaultValue;

    private Boolean markAsRead;

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

    public Boolean getDeleteAny() {
        return deleteAny;
    }

    public void setDeleteAny(Boolean deleteAny) {
        this.deleteAny = deleteAny;
    }

    public Boolean getDeleteOwn() {
        return deleteOwn;
    }

    public void setDeleteOwn(Boolean deleteOwn) {
        this.deleteOwn = deleteOwn;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getReadDrafts() {
        return readDrafts;
    }

    public void setReadDrafts(Boolean readDrafts) {
        this.readDrafts = readDrafts;
    }

    public Boolean getReviseAny() {
        return reviseAny;
    }

    public void setReviseAny(Boolean reviseAny) {
        this.reviseAny = reviseAny;
    }

    public Boolean getReviseOwn() {
        return reviseOwn;
    }

    public void setReviseOwn(Boolean reviseOwn) {
        this.reviseOwn = reviseOwn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
    	return "MessagePermissions/" + id;
        //return "MessagePermissions.id:" + id;
    }

    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getMarkAsRead() {
        return this.markAsRead;
    }

    public void setMarkAsRead(Boolean markAsRead) {
        this.markAsRead = markAsRead;
    }

}
