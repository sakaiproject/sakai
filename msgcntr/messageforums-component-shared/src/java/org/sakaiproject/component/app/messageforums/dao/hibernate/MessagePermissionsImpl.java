/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.messageforums.dao.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.Topic;

public class MessagePermissionsImpl implements MessagePermissions {

    private static final Log LOG = LogFactory.getLog(MessagePermissionsImpl.class);

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

    private int areaindex;

    private int forumindex;

    private int topicindex;

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

    public int getAreaindex() {
        try {
            return getArea().getOpenForums().indexOf(this);
        } catch (Exception e) {
            return areaindex;
        }
    }

    public void setAreaindex(int areaindex) {
        this.areaindex = areaindex;
    }

    public int getForumindex() {
        try {
            return getForum().getTopics().indexOf(this);
        } catch (Exception e) {
            return forumindex;
        }
    }

    public void setForumindex(int forumindex) {
        this.forumindex = forumindex;
    }

    public int getTopicindex() {
        try {
            return getTopic().getMessages().indexOf(this);
        } catch (Exception e) {
            return topicindex;
        }
    }

    public void setTopicindex(int topicindex) {
        this.topicindex = topicindex;
    }

    public String toString() {
        return "MessagePermissions.id:" + id;
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
