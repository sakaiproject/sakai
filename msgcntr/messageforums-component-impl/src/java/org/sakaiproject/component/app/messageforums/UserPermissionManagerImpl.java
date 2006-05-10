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

package org.sakaiproject.component.app.messageforums;

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

    public void init() {
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
