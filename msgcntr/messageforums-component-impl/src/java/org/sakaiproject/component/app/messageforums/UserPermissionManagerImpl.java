/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/UserPermissionManagerImpl.java $
 * $Id: UserPermissionManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
package org.sakaiproject.component.app.messageforums;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.TopicControlPermission;
import org.sakaiproject.api.app.messageforums.UserPermissionManager;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class UserPermissionManagerImpl extends HibernateDaoSupport implements UserPermissionManager {

    private IdManager idManager;

    private SessionManager sessionManager;

    private PermissionManager permissionManager;
    
    private static final Log LOG = LogFactory.getLog(UserPermissionManagerImpl.class);

    public void init() {
       LOG.info("init()");
        ;
    }

    public PermissionManager getPermissionManager() {
        return permissionManager;
    }

    public void setPermissionManager(PermissionManager permissionManager) {
        this.permissionManager = permissionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public IdManager getIdManager() {
        return idManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setIdManager(IdManager idManager) {
        this.idManager = idManager;
    }

    // ------------------ MESSAGE PERMISSIONS

    public boolean canRead(Topic topic, String typeId) {
        MessagePermissions permission = permissionManager.getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getRead().booleanValue();
    }

    public boolean canReviseAny(Topic topic, String typeId) {
        MessagePermissions permission = permissionManager.getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getReviseAny().booleanValue();
    }

    public boolean canReviseOwn(Topic topic, String typeId) {
        MessagePermissions permission = permissionManager.getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getReviseOwn().booleanValue();
    }

    public boolean canDeleteAny(Topic topic, String typeId) {
        MessagePermissions permission = permissionManager.getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getDeleteAny().booleanValue();
    }

    public boolean canDeleteOwn(Topic topic, String typeId) {
        MessagePermissions permission = permissionManager.getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getDeleteOwn().booleanValue();
    }

    public boolean canMarkAsRead(Topic topic, String typeId) {
        MessagePermissions permission = permissionManager.getTopicMessagePermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getMarkAsRead().booleanValue();
    }

    public boolean canRead(BaseForum forum, String typeId) {
        MessagePermissions permission = permissionManager.getForumMessagePermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getRead().booleanValue();
    }

    public boolean canReviseAny(BaseForum forum, String typeId) {
        MessagePermissions permission = permissionManager.getForumMessagePermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getReviseAny().booleanValue();
    }

    public boolean canReviseOwn(BaseForum forum, String typeId) {
        MessagePermissions permission = permissionManager.getForumMessagePermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getReviseOwn().booleanValue();
    }

    public boolean canDeleteAny(BaseForum forum, String typeId) {
        MessagePermissions permission = permissionManager.getForumMessagePermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getDeleteAny().booleanValue();
    }

    public boolean canDeleteOwn(BaseForum forum, String typeId) {
        MessagePermissions permission = permissionManager.getForumMessagePermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getDeleteOwn().booleanValue();
    }

    public boolean canMarkAsRead(BaseForum forum, String typeId) {
        MessagePermissions permission = permissionManager.getForumMessagePermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getMarkAsRead().booleanValue();
    }

    public boolean canRead(Area area, String typeId) {
        MessagePermissions permission = permissionManager.getAreaMessagePermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getRead().booleanValue();
    }

    public boolean canReviseAny(Area area, String typeId) {
        MessagePermissions permission = permissionManager.getAreaMessagePermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getReviseAny().booleanValue();
    }

    public boolean canReviseOwn(Area area, String typeId) {
        MessagePermissions permission = permissionManager.getAreaMessagePermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getReviseOwn().booleanValue();
    }

    public boolean canDeleteAny(Area area, String typeId) {
        MessagePermissions permission = permissionManager.getAreaMessagePermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getDeleteAny().booleanValue();
    }

    public boolean canDeleteOwn(Area area, String typeId) {
        MessagePermissions permission = permissionManager.getAreaMessagePermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getDeleteOwn().booleanValue();
    }

    public boolean canMarkAsRead(Area area, String typeId) {
        MessagePermissions permission = permissionManager.getAreaMessagePermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getMarkAsRead().booleanValue();
    }

    // ------------------ CONTROL PERMISSIONS

    public boolean canNewResponse(Topic topic, String typeId) {
        TopicControlPermission permission = permissionManager.getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getNewResponse().booleanValue();
    }

    public boolean canResponseToResponse(Topic topic, String typeId) {
        TopicControlPermission permission = permissionManager.getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getResponseToResponse().booleanValue();
    }

    public boolean canMovePostings(Topic topic, String typeId) {
        TopicControlPermission permission = permissionManager.getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getMovePostings().booleanValue();
    }

    public boolean canChangeSettings(Topic topic, String typeId) {
        TopicControlPermission permission = permissionManager.getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getChangeSettings().booleanValue();
    }

    public boolean canPostToGradebook(Topic topic, String typeId) {
        TopicControlPermission permission = permissionManager.getTopicControlPermissionForRole(topic, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getPostToGradebook().booleanValue();
    }

    public boolean canNewTopic(BaseForum forum, String typeId) {
        ForumControlPermission permission = permissionManager.getForumControlPermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getNewTopic().booleanValue();
    }

    public boolean canNewResponse(BaseForum forum, String typeId) {
        ForumControlPermission permission = permissionManager.getForumControlPermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getNewResponse().booleanValue();
    }

    public boolean canResponseToResponse(BaseForum forum, String typeId) {
        ForumControlPermission permission = permissionManager.getForumControlPermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getResponseToResponse().booleanValue();
    }

    public boolean canMovePostings(BaseForum forum, String typeId) {
        ForumControlPermission permission = permissionManager.getForumControlPermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getMovePostings().booleanValue();
    }

    public boolean canChangeSettings(BaseForum forum, String typeId) {
        ForumControlPermission permission = permissionManager.getForumControlPermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getChangeSettings().booleanValue();
    }

    public boolean canPostToGradebook(BaseForum forum, String typeId) {
        ForumControlPermission permission = permissionManager.getForumControlPermissionForRole(forum, getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getPostToGradebook().booleanValue();
    }

    public boolean canNewForum(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getNewForum().booleanValue();
    }

    public boolean canNewTopic(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getNewTopic().booleanValue();
    }

    public boolean canNewResponse(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getNewResponse().booleanValue();
    }

    public boolean canResponseToResponse(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getResponseToResponse().booleanValue();
    }

    public boolean canMovePostings(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getMovePostings().booleanValue();
    }

    public boolean canChangeSettings(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getChangeSettings().booleanValue();
    }

    public boolean canPostToGradebook(Area area, String typeId) {
        AreaControlPermission permission = permissionManager.getAreaControlPermissionForRole(getCurrentUserRole(), typeId);
        return permission == null ? false : permission.getPostToGradebook().booleanValue();
    }

    // helpers

    private String getCurrentUser() {
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }

    private String getCurrentUserRole() {
        return AuthzGroupService.getUserRole(getCurrentUser(), "/site/" + getContextId());
    }

    private String getContextId() {
        if (TestUtil.isRunningTests()) {
            return "test-context";
        }
        Placement placement = ToolManager.getCurrentPlacement();
        String presentSiteId = placement.getContext();
        return presentSiteId;
    }
}
