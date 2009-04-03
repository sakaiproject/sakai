/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-component-impl/src/java/org/sakaiproject/component/app/messageforums/PermissionManagerImpl.java $
 * $Id: PermissionManagerImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.api.app.messageforums.Area;
import org.sakaiproject.api.app.messageforums.AreaControlPermission;
import org.sakaiproject.api.app.messageforums.AreaManager;
import org.sakaiproject.api.app.messageforums.BaseForum;
import org.sakaiproject.api.app.messageforums.ControlPermissions;
import org.sakaiproject.api.app.messageforums.DefaultPermissionsManager;
import org.sakaiproject.api.app.messageforums.DiscussionForumService;
import org.sakaiproject.api.app.messageforums.ForumControlPermission;
import org.sakaiproject.api.app.messageforums.MessageForumsTypeManager;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.PermissionManager;
import org.sakaiproject.api.app.messageforums.Topic;
import org.sakaiproject.api.app.messageforums.TopicControlPermission;
import org.sakaiproject.component.app.messageforums.dao.hibernate.AreaControlPermissionImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ControlPermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.ForumControlPermissionImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.MessagePermissionsImpl;
import org.sakaiproject.component.app.messageforums.dao.hibernate.TopicControlPermissionImpl;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class PermissionManagerImpl extends HibernateDaoSupport implements PermissionManager {

    private static final Log LOG = LogFactory.getLog(PermissionManagerImpl.class);

    private static final String QUERY_CP_BY_ROLE = "findAreaControlPermissionByRole";
    private static final String QUERY_CP_BY_FORUM = "findForumControlPermissionByRole";
    private static final String QUERY_CP_BY_TOPIC = "findTopicControlPermissionByRole";
    private static final String QUERY_MP_BY_ROLE = "findAreaMessagePermissionByRole";
    private static final String QUERY_MP_BY_FORUM = "findForumMessagePermissionByRole";
    private static final String QUERY_MP_BY_TOPIC = "findTopicMessagePermissionByRole";

    private IdManager idManager;

    private SessionManager sessionManager;

    private MessageForumsTypeManager typeManager;

    private AreaManager areaManager;
    
    private EventTrackingService eventTrackingService;
    
    private DefaultPermissionsManager defaultPermissionsManager;
    
    public void init() {
       LOG.info("init()");
       ;
    }

    public EventTrackingService getEventTrackingService() {
        return eventTrackingService;
    }

    public void setEventTrackingService(EventTrackingService eventTrackingService) {
        this.eventTrackingService = eventTrackingService;
    }

    public AreaManager getAreaManager() {
        return areaManager;
    }

    public void setAreaManager(AreaManager areaManager) {
        this.areaManager = areaManager;
    }

    public MessageForumsTypeManager getTypeManager() {
        return typeManager;
    }

    public void setTypeManager(MessageForumsTypeManager typeManager) {
        this.typeManager = typeManager;
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
     

    /**
     * @param defaultPermissionsManager The defaultPermissionsManager to set.
     */
    public void setDefaultPermissionsManager(
        DefaultPermissionsManager defaultPermissionManager)
    {
      this.defaultPermissionsManager = defaultPermissionManager;
    }

    public AreaControlPermission getAreaControlPermissionForRole(String role, String typeId) {
        ControlPermissions permissions = getAreaControlPermissionByRoleAndType(role, typeId, false);
        AreaControlPermission cp = new AreaControlPermissionImpl();

        if (permissions == null) {
//            cp.setChangeSettings(Boolean.FALSE);
//            cp.setMovePostings(Boolean.FALSE);
//            cp.setNewForum(Boolean.FALSE);
//            cp.setNewResponse(Boolean.FALSE);
//            cp.setNewTopic(Boolean.FALSE);
//            cp.setResponseToResponse(Boolean.FALSE);
//            cp.setPostToGradebook(Boolean.FALSE);
          return getDefaultAreaControlPermissionForRole(role, typeId);
        } else {
            cp.setPostToGradebook(permissions.getPostToGradebook());
            cp.setChangeSettings(permissions.getChangeSettings());        
            cp.setMovePostings(permissions.getMovePostings());
            cp.setNewForum(permissions.getNewForum());
            cp.setNewResponse(permissions.getNewResponse());
            cp.setNewTopic(permissions.getNewTopic());
            cp.setResponseToResponse(permissions.getResponseToResponse());
        }
        cp.setRole(role);
        return cp;
    }

    public AreaControlPermission getDefaultAreaControlPermissionForRole(String role, String typeId) {
//        ControlPermissions permissions = getAreaControlPermissionByRoleAndType(role, typeId, true);
        AreaControlPermission cp = new AreaControlPermissionImpl();
//        if (permissions == null) {
//            cp.setChangeSettings(Boolean.FALSE);
//            cp.setMovePostings(Boolean.FALSE);
//            cp.setNewForum(Boolean.FALSE);
//            cp.setNewResponse(Boolean.FALSE);
//            cp.setNewTopic(Boolean.FALSE);
//            cp.setResponseToResponse(Boolean.FALSE);
//            cp.setPostToGradebook(Boolean.FALSE);
//        } else {
//            cp.setChangeSettings(permissions.getChangeSettings());
//            cp.setMovePostings(permissions.getMovePostings());
//            cp.setNewForum(permissions.getNewForum());
//            cp.setNewResponse(permissions.getNewResponse());
//            cp.setNewTopic(permissions.getNewTopic());
//            cp.setResponseToResponse(permissions.getResponseToResponse());
//            cp.setPostToGradebook(permissions.getPostToGradebook());
//        }
        cp.setChangeSettings(new Boolean(defaultPermissionsManager.isChangeSettings(role)));
        cp.setMovePostings(new Boolean(defaultPermissionsManager.isMovePostings(role)));
        cp.setNewForum(new Boolean(defaultPermissionsManager.isNewForum(role)));
        cp.setNewResponse(new Boolean(defaultPermissionsManager.isNewResponse(role)));
        cp.setNewTopic(new Boolean(defaultPermissionsManager.isNewTopic(role)));
        cp.setResponseToResponse(new Boolean(defaultPermissionsManager.isResponseToResponse(role)));
        cp.setPostToGradebook(new Boolean(defaultPermissionsManager.isPostToGradebook(role)));
        cp.setRole(role);
        return cp;
    }

    public AreaControlPermission createAreaControlPermissionForRole(String role, String typeId) {
        AreaControlPermission permission = new AreaControlPermissionImpl();
        AreaControlPermission acp = getDefaultAreaControlPermissionForRole(role, typeId);
        permission.setChangeSettings(acp.getChangeSettings());
        permission.setMovePostings(acp.getMovePostings());
        permission.setNewForum(acp.getNewForum());
        permission.setNewResponse(acp.getNewResponse());
        permission.setNewTopic(acp.getNewTopic());
        permission.setResponseToResponse(acp.getResponseToResponse());
        permission.setPostToGradebook(acp.getPostToGradebook());
        permission.setRole(role);
        return permission;
    }

    public void saveAreaControlPermissionForRole(Area area, AreaControlPermission permission, String typeId) {        
        ControlPermissions permissions = getAreaControlPermissionByRoleAndType(permission.getRole(), typeId, false); 
        if (permissions == null) {
            permissions = new ControlPermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setArea(area);
        permissions.setDefaultValue(Boolean.FALSE);
        permissions.setChangeSettings(permission.getChangeSettings());
        permissions.setMovePostings(permission.getMovePostings());
        permissions.setNewForum(permission.getNewForum());
        permissions.setNewResponse(permission.getNewResponse());
        permissions.setNewTopic(permission.getNewTopic());
        permissions.setResponseToResponse(permission.getResponseToResponse());
        permissions.setPostToGradebook(permission.getPostToGradebook());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);                
        
// Commented out when splitting events between Messages tool and Forums tool 
//       if (isNew) {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_ADD, getEventMessage(area, permissions), false));
//        } else {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_WRITE, getEventMessage(area, permissions), false));
//        }
    }

    public void saveDefaultAreaControlPermissionForRole(Area area, AreaControlPermission permission, String typeId) {
        ControlPermissions permissions = getAreaControlPermissionByRoleAndType(permission.getRole(), typeId, true); 
        if (permissions == null) {
            permissions = new ControlPermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;

        permissions.setArea(area);
        permissions.setDefaultValue(Boolean.TRUE);
        permissions.setChangeSettings(permission.getChangeSettings());
        permissions.setMovePostings(permission.getMovePostings());
        permissions.setNewForum(permission.getNewForum());
        permissions.setNewResponse(permission.getNewResponse());
        permissions.setNewTopic(permission.getNewTopic());
        permissions.setResponseToResponse(permission.getResponseToResponse());
        permissions.setPostToGradebook(permission.getPostToGradebook());
        permissions.setRole(permission.getRole());        
        getHibernateTemplate().saveOrUpdate(permissions);

        // Commented out when splitting events between Messages tool and Forums tool
//        if (isNew) {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_ADD, getEventMessage(area, permissions), false));
//        } else {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_WRITE, getEventMessage(area, permissions), false));
//        }        
    }

    public ForumControlPermission getForumControlPermissionForRole(BaseForum forum, String role, String typeId) {
        ControlPermissions permissions = forum == null || forum.getId() == null ? null : getControlPermissionByKeyValue(role, "forumId", forum.getId().toString(), false); 
        ForumControlPermission cp = new ForumControlPermissionImpl();

        if (permissions == null) {
            return null;
        } else {
            cp.setChangeSettings(permissions.getChangeSettings());
            cp.setMovePostings(permissions.getMovePostings());
            cp.setNewResponse(permissions.getNewResponse());
            cp.setNewTopic(permissions.getNewTopic());
            cp.setPostToGradebook(permissions.getPostToGradebook());
            cp.setResponseToResponse(permissions.getResponseToResponse());
        }
        cp.setRole(role);
        return cp;
    }

    public ForumControlPermission getDefaultForumControlPermissionForRole(BaseForum forum, String role, String typeId) {
        ControlPermissions permissions = forum == null || forum.getId() == null ? null : getControlPermissionByKeyValue(role, "forumId", forum.getId().toString(), true); 
        ForumControlPermission cp = new ForumControlPermissionImpl();

        if (permissions == null) {
            return null;
        } else {
            cp.setPostToGradebook(permissions.getPostToGradebook());
            cp.setChangeSettings(permissions.getChangeSettings());
            cp.setMovePostings(permissions.getMovePostings());
            cp.setNewResponse(permissions.getNewResponse());
            cp.setNewTopic(permissions.getNewTopic());
            cp.setResponseToResponse(permissions.getResponseToResponse());
        }
        cp.setRole(role);
        return cp;
    }

    public ForumControlPermission createForumControlPermissionForRole(String role, String typeId) {
        ForumControlPermission permission = new ForumControlPermissionImpl();
        AreaControlPermission acp = getAreaControlPermissionForRole(role, typeId);
        permission.setChangeSettings(acp.getChangeSettings());
        permission.setMovePostings(acp.getMovePostings());
        permission.setNewResponse(acp.getNewResponse());
        permission.setNewTopic(acp.getNewTopic());
        permission.setResponseToResponse(acp.getResponseToResponse());
        permission.setPostToGradebook(acp.getPostToGradebook());
        permission.setRole(role);
        return permission;
    }

    public void saveForumControlPermissionForRole(BaseForum forum, ForumControlPermission permission) {
        ControlPermissions permissions = forum == null || forum.getId() == null ? null : getControlPermissionByKeyValue(permission.getRole(), "forumId", forum.getId().toString(), false); 
        if (permissions == null) {
            permissions = new ControlPermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;

        permissions.setForum(forum);
        permissions.setDefaultValue(Boolean.FALSE);
        permissions.setChangeSettings(permission.getChangeSettings());
        permissions.setMovePostings(permission.getMovePostings());
        permissions.setNewForum(Boolean.FALSE);
        permissions.setNewResponse(permission.getNewResponse());
        permissions.setPostToGradebook(permission.getPostToGradebook());
        permissions.setNewTopic(permission.getNewTopic());
        permissions.setResponseToResponse(permission.getResponseToResponse());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);        
        
        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_ADD, getEventMessage(forum, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REVISE, getEventMessage(forum, permissions), false));
        }        
    }

    public void saveDefaultForumControlPermissionForRole(BaseForum forum, ForumControlPermission permission) {
        ControlPermissions permissions = forum == null || forum.getId() == null ? null : getControlPermissionByKeyValue(permission.getRole(), "forumId", forum.getId().toString(), false); 
        if (permissions == null) {
            permissions = new ControlPermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;

        permissions.setForum(forum);
        permissions.setDefaultValue(Boolean.TRUE);
        permissions.setChangeSettings(permission.getChangeSettings());
        permissions.setMovePostings(permission.getMovePostings());
        permissions.setNewForum(Boolean.FALSE);
        permissions.setNewResponse(permission.getNewResponse());
        permissions.setPostToGradebook(permission.getPostToGradebook());
        permissions.setNewTopic(permission.getNewTopic());
        permissions.setResponseToResponse(permission.getResponseToResponse());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);        

        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_ADD, getEventMessage(forum, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REVISE, getEventMessage(forum, permissions), false));
        }        
    }

    public TopicControlPermission getTopicControlPermissionForRole(Topic topic, String role, String typeId) {
        ControlPermissions permissions = topic == null || topic.getId() == null ? null : getControlPermissionByKeyValue(role, "topicId", topic.getId().toString(), false); 
        TopicControlPermission cp = new TopicControlPermissionImpl();

        if (permissions == null) {
            return null;
        } else {
            cp.setChangeSettings(permissions.getChangeSettings());
            cp.setMovePostings(permissions.getMovePostings());
            cp.setNewResponse(permissions.getNewResponse());
            cp.setResponseToResponse(permissions.getResponseToResponse());
            cp.setPostToGradebook(permissions.getPostToGradebook());
        }
        cp.setRole(role);
        return cp;
    }

    public TopicControlPermission getDefaultTopicControlPermissionForRole(Topic topic, String role, String typeId) {
        ControlPermissions permissions = topic == null || topic.getId() == null ? null : getControlPermissionByKeyValue(role, "topicId", topic.getId().toString(), true); 
        TopicControlPermission cp = new TopicControlPermissionImpl();

        if (permissions == null) {
            return null;
        } else {
            cp.setPostToGradebook(permissions.getPostToGradebook());
            cp.setChangeSettings(permissions.getChangeSettings());
            cp.setMovePostings(permissions.getMovePostings());
            cp.setNewResponse(permissions.getNewResponse());
            cp.setResponseToResponse(permissions.getResponseToResponse());
        }
        cp.setRole(role);
        return cp;
    }

    public TopicControlPermission createTopicControlPermissionForRole(BaseForum forum, String role, String typeId) {
        TopicControlPermission permission = new TopicControlPermissionImpl();
        ForumControlPermission fcp = getForumControlPermissionForRole(forum, role, typeId);
        permission.setChangeSettings(fcp.getChangeSettings());
        permission.setMovePostings(fcp.getMovePostings());
        permission.setNewResponse(fcp.getNewResponse());
        permission.setResponseToResponse(fcp.getResponseToResponse());
        permission.setPostToGradebook(fcp.getPostToGradebook());
        permission.setRole(role);
        return permission;
    }

    public void saveTopicControlPermissionForRole(Topic topic, TopicControlPermission permission) {
        ControlPermissions permissions = topic == null || topic.getId() == null ? null : getControlPermissionByKeyValue(permission.getRole(), "topicId", topic.getId().toString(), false); 
        if (permissions == null) {
            permissions = new ControlPermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;

        permissions.setTopic(topic);
        permissions.setDefaultValue(Boolean.FALSE);
        permissions.setChangeSettings(permission.getChangeSettings());
        permissions.setMovePostings(permission.getMovePostings());
        permissions.setNewForum(Boolean.FALSE);
        permissions.setNewResponse(permission.getNewResponse());
        permissions.setNewTopic(Boolean.FALSE);
        permissions.setPostToGradebook(permission.getPostToGradebook());
        permissions.setResponseToResponse(permission.getResponseToResponse());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);

        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic, permissions), false));
        }
    }

    public void saveDefaultTopicControlPermissionForRole(Topic topic, TopicControlPermission permission) {
        ControlPermissions permissions = topic == null || topic.getId() == null ? null : getControlPermissionByKeyValue(permission.getRole(), "topicId", topic.getId().toString(), false); 
        if (permissions == null) {
            permissions = new ControlPermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;

        permissions.setTopic(topic);
        permissions.setDefaultValue(Boolean.TRUE);
        permissions.setChangeSettings(permission.getChangeSettings());
        permissions.setMovePostings(permission.getMovePostings());
        permissions.setNewForum(Boolean.FALSE);
        permissions.setNewResponse(permission.getNewResponse());
        permissions.setNewTopic(Boolean.FALSE);
        permissions.setResponseToResponse(permission.getResponseToResponse());
        permissions.setPostToGradebook(permission.getPostToGradebook());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);

        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic, permissions), false));
        }        
    }    

    public ControlPermissions getAreaControlPermissionByRoleAndType(final String roleId, final String typeId, final boolean defaultValue) {
        LOG.debug("getAreaControlPermissionByRole executing for current user: " + getCurrentUser());
        final Area area = areaManager.getAreaByContextIdAndTypeId(typeId);
        if (area == null) {
            return null;
        }
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_CP_BY_ROLE);
                q.setParameter("roleId", roleId, Hibernate.STRING);
                q.setParameter("areaId", area.getId().toString(), Hibernate.STRING);
                q.setParameter("defaultValue", new Boolean(defaultValue), Hibernate.BOOLEAN);
                return q.uniqueResult();
            }
        };
        return (ControlPermissions) getHibernateTemplate().execute(hcb);
    }

    private ControlPermissions getControlPermissionByKeyValue(final String roleId, final String key, final String value, final boolean defaultValue) {
        LOG.debug("getAreaControlPermissionByRole executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                String queryString = "forumId".equals(key) ? QUERY_CP_BY_FORUM : QUERY_CP_BY_TOPIC;                
                Query q = session.getNamedQuery(queryString);
                q.setParameter("roleId", roleId, Hibernate.STRING);
                q.setParameter(key, value, Hibernate.STRING);
                q.setParameter("defaultValue", new Boolean(defaultValue), Hibernate.BOOLEAN);
                return q.uniqueResult();
            }
        };
        return (ControlPermissions) getHibernateTemplate().execute(hcb);
    }
    
    private MessagePermissions getMessagePermissionByKeyValue(final String roleId, final String key, final String value, final boolean defaultValue) {
        LOG.debug("getAreaMessagePermissionByRole executing for current user: " + getCurrentUser());
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                String queryString = "forumId".equals(key) ? QUERY_MP_BY_FORUM : QUERY_MP_BY_TOPIC;                
                Query q = session.getNamedQuery(queryString);
                q.setParameter("roleId", roleId, Hibernate.STRING);
                q.setParameter(key, value, Hibernate.STRING);
                q.setParameter("defaultValue", new Boolean(defaultValue), Hibernate.BOOLEAN);
                return q.uniqueResult();
            }
        };
        return (MessagePermissions) getHibernateTemplate().execute(hcb);
    }
       
    
    /**
     * Get the area message permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public MessagePermissions getAreaMessagePermissionForRole(String role, String typeId) {
        MessagePermissions permissions = getAreaMessagePermissionByRoleAndType(role, typeId, false);
        MessagePermissions mp = new MessagePermissionsImpl();

        if (permissions == null) {
//            mp.setDeleteAny(Boolean.FALSE);
//            mp.setDeleteOwn(Boolean.FALSE);
//            mp.setRead(Boolean.FALSE);
//            mp.setReadDrafts(Boolean.FALSE);
//            mp.setReviseAny(Boolean.FALSE);
//            mp.setReviseOwn(Boolean.FALSE);
//            mp.setMarkAsRead(Boolean.FALSE);
          return getDefaultAreaMessagePermissionForRole(role, typeId);
        } else {
            mp.setDeleteAny(permissions.getDeleteAny());
            mp.setDeleteOwn(permissions.getDeleteOwn());
            mp.setRead(permissions.getRead());
            mp.setReadDrafts(permissions.getReadDrafts());
            mp.setReviseAny(permissions.getReviseAny());
            mp.setReviseOwn(permissions.getReviseOwn());
            mp.setMarkAsRead(permissions.getMarkAsRead());
        }
        mp.setRole(role);
        
        return mp;
    }

    /**
     * Get the default area message permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public MessagePermissions getDefaultAreaMessagePermissionForRole(String role, String typeId) {
//        MessagePermissions permissions = getAreaMessagePermissionByRoleAndType(role, typeId, true);
        MessagePermissions mp = new MessagePermissionsImpl();

//        if (permissions == null) {
//            mp.setDeleteAny(Boolean.FALSE);
//            mp.setDeleteOwn(Boolean.FALSE);
//            mp.setRead(Boolean.FALSE);
//            mp.setReadDrafts(Boolean.FALSE);
//            mp.setReviseAny(Boolean.FALSE);
//            mp.setReviseOwn(Boolean.FALSE);
//            mp.setMarkAsRead(Boolean.FALSE);
//        } else {
//            mp.setDeleteAny(permissions.getDeleteAny());
//            mp.setDeleteOwn(permissions.getDeleteOwn());
//            mp.setRead(permissions.getRead());
//            mp.setReadDrafts(permissions.getReadDrafts());
//            mp.setReviseAny(permissions.getReviseAny());
//            mp.setReviseOwn(permissions.getReviseOwn());
//            mp.setMarkAsRead(permissions.getMarkAsRead());
//        }
        mp.setRole(role);        
        mp.setDeleteAny(new Boolean(defaultPermissionsManager.isDeleteAny(role)));
        mp.setDeleteOwn(new Boolean(defaultPermissionsManager.isDeleteOwn(role)));
        mp.setRead(new Boolean(defaultPermissionsManager.isRead(role)));
        mp.setReadDrafts(new Boolean(false));
        mp.setReviseAny(new Boolean(defaultPermissionsManager.isReviseAny(role)));
        mp.setReviseOwn(new Boolean(defaultPermissionsManager.isReviseOwn(role)));
        mp.setMarkAsRead(new Boolean(defaultPermissionsManager.isMarkAsRead(role)));      
        return mp;
    }

    /**
     * Create an empty area message permission with system properties 
     * populated (ie: uuid).
     */
    public MessagePermissions createAreaMessagePermissionForRole(String role, String typeId) {
        MessagePermissions permissions = new MessagePermissionsImpl();
        MessagePermissions mp = getDefaultAreaMessagePermissionForRole(role, typeId);
        if (mp != null) {
            permissions.setDefaultValue(mp.getDefaultValue());
            permissions.setDeleteAny(mp.getDeleteAny());
            permissions.setDeleteOwn(mp.getDeleteOwn());
            permissions.setRead(mp.getRead());
            permissions.setReadDrafts(mp.getReadDrafts());
            permissions.setReviseAny(mp.getReviseAny());
            permissions.setReviseOwn(mp.getReviseOwn());
            permissions.setMarkAsRead(mp.getMarkAsRead());
            permissions.setRole(role);
        }
        return permissions;
    }
    
    /**
     * Save an area message permission.  This is backed in the database by a single
     * message permission (used for areas, forums, and topics).
     */
    public void saveAreaMessagePermissionForRole(Area area, MessagePermissions permission, String typeId) {
        MessagePermissions permissions = getAreaMessagePermissionByRoleAndType(permission.getRole(), typeId, false); 
        if (permissions == null) {
            permissions = new MessagePermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setArea(area);
        permissions.setDefaultValue(Boolean.FALSE);
        permissions.setDeleteAny(permission.getDeleteAny());
        permissions.setDeleteOwn(permission.getDeleteOwn());
        permissions.setRead(permission.getRead());
        permissions.setReadDrafts(permission.getReadDrafts());
        permissions.setReviseAny(permission.getReviseAny());
        permissions.setReviseOwn(permission.getReviseOwn());
        permissions.setRole(permission.getRole());
        permissions.setMarkAsRead(permission.getMarkAsRead());
        getHibernateTemplate().saveOrUpdate(permissions);                
        
// Commented out when splitting events between Messages tool and Forums tool
//        if (isNew) {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_ADD, getEventMessage(area, permissions), false));
//        } else {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_WRITE, getEventMessage(area, permissions), false));
//        }            
    }

    /**
     * Save a default area message permission.  This is backed in the database by a 
     * single message permission (used for areas, forums, and topics).
     */
    public void saveDefaultAreaMessagePermissionForRole(Area area, MessagePermissions permission, String typeId) {
        MessagePermissions permissions = getAreaMessagePermissionByRoleAndType(permission.getRole(), typeId, true); 
        if (permissions == null) {
            permissions = new MessagePermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setArea(area);
        permissions.setDefaultValue(Boolean.TRUE);
        permissions.setDeleteAny(permission.getDeleteAny());
        permissions.setDeleteOwn(permission.getDeleteOwn());
        permissions.setRead(permission.getRead());
        permissions.setReadDrafts(permission.getReadDrafts());
        permissions.setReviseAny(permission.getReviseAny());
        permissions.setReviseOwn(permission.getReviseOwn());
        permissions.setMarkAsRead(permission.getMarkAsRead());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);                
 
// Commented out when splitting events between Messages tool and Forums tool
//        if (isNew) {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_ADD, getEventMessage(area, permissions), false));
//        } else {
//            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_RESOURCE_WRITE, getEventMessage(area, permissions), false));
//        }        
    }

    /**
     * Get the forum message permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public MessagePermissions getForumMessagePermissionForRole(BaseForum forum, String role, String typeId) {
        MessagePermissions permissions = forum == null || forum.getId() == null ? null : getMessagePermissionByKeyValue(role, "forumId", forum.getId().toString(), false);
        MessagePermissions mp = new MessagePermissionsImpl();

        if (permissions == null) {
            return null;
        } else {
            mp.setDeleteAny(permissions.getDeleteAny());
            mp.setDeleteOwn(permissions.getDeleteOwn());
            mp.setRead(permissions.getRead());
            mp.setReadDrafts(permissions.getReadDrafts());
            mp.setReviseAny(permissions.getReviseAny());
            mp.setReviseOwn(permissions.getReviseOwn());
            mp.setMarkAsRead(permissions.getMarkAsRead());
        }
        mp.setRole(role);
        
        return mp;
    }

    /**
     * Get the default forum message permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public MessagePermissions getDefaultForumMessagePermissionForRole(BaseForum forum, String role, String typeId) {
        MessagePermissions permissions = forum == null || forum.getId() == null ? null : getMessagePermissionByKeyValue(role, "forumId", forum.getId().toString(), true);
        MessagePermissions mp = new MessagePermissionsImpl();

        if (permissions == null) {
            return null;
        } else {
            mp.setDeleteAny(permissions.getDeleteAny());
            mp.setDeleteOwn(permissions.getDeleteOwn());
            mp.setRead(permissions.getRead());
            mp.setReadDrafts(permissions.getReadDrafts());
            mp.setReviseAny(permissions.getReviseAny());
            mp.setReviseOwn(permissions.getReviseOwn());
            mp.setMarkAsRead(permissions.getMarkAsRead());
        }
        mp.setRole(role);
        
        return mp;
    }

    /**
     * Create an empty forum message permission with system properties 
     * populated (ie: uuid).
     */
    public MessagePermissions createForumMessagePermissionForRole(String role, String typeId) {
        MessagePermissions permissions = new MessagePermissionsImpl();
        MessagePermissions mp = getAreaMessagePermissionForRole(role, typeId);
        if (mp != null) {
            permissions.setDefaultValue(mp.getDefaultValue());
            permissions.setDeleteAny(mp.getDeleteAny());
            permissions.setDeleteOwn(mp.getDeleteOwn());
            permissions.setRead(mp.getRead());
            permissions.setReadDrafts(mp.getReadDrafts());
            permissions.setReviseAny(mp.getReviseAny());
            permissions.setReviseOwn(mp.getReviseOwn());
            permissions.setMarkAsRead(mp.getMarkAsRead());
            permissions.setRole(role);
        }
        return permissions;
    }
    
    /**
     * Save an forum message permission.  This is backed in the database by a single
     * message permission (used for topics, forums, and topics).
     */
    public void saveForumMessagePermissionForRole(BaseForum forum, MessagePermissions permission) {
        MessagePermissions permissions = forum == null || forum.getId() == null ? null : getMessagePermissionByKeyValue(permission.getRole(), "forumId", forum.getId().toString(), false); 
        if (permissions == null) {
            permissions = new MessagePermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setForum(forum);
        permissions.setDefaultValue(Boolean.FALSE);
        permissions.setDeleteAny(permission.getDeleteAny());
        permissions.setDeleteOwn(permission.getDeleteOwn());
        permissions.setRead(permission.getRead());
        permissions.setReadDrafts(permission.getReadDrafts());
        permissions.setReviseAny(permission.getReviseAny());
        permissions.setReviseOwn(permission.getReviseOwn());
        permissions.setMarkAsRead(permission.getMarkAsRead());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);                
        
        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_ADD, getEventMessage(forum, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REVISE, getEventMessage(forum, permissions), false));
        }         
    }

    /**
     * Save a default forum message permission.  This is backed in the database by a 
     * single message permission (used for topics, forums, and topics).
     */
    public void saveDefaultForumMessagePermissionForRole(BaseForum forum, MessagePermissions permission) {
        MessagePermissions permissions = forum == null || forum.getId() == null ? null : getMessagePermissionByKeyValue(permission.getRole(), "forumId", forum.getId().toString(), true); 
        if (permissions == null) {
            permissions = new MessagePermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setForum(forum);
        permissions.setDefaultValue(Boolean.TRUE);
        permissions.setDeleteAny(permission.getDeleteAny());
        permissions.setDeleteOwn(permission.getDeleteOwn());
        permissions.setRead(permission.getRead());
        permissions.setReadDrafts(permission.getReadDrafts());
        permissions.setReviseAny(permission.getReviseAny());
        permissions.setReviseOwn(permission.getReviseOwn());
        permissions.setMarkAsRead(permission.getMarkAsRead());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);                

        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_ADD, getEventMessage(forum, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_FORUM_REVISE, getEventMessage(forum, permissions), false));
        }                 
    }
    
    /**
     * Get the topic message permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public MessagePermissions getTopicMessagePermissionForRole(Topic topic, String role, String typeId) {
        MessagePermissions permissions = topic == null || topic.getId() == null ? null : getMessagePermissionByKeyValue(role, "topicId", topic.getId().toString(), false);
        MessagePermissions mp = new MessagePermissionsImpl();

        if (permissions == null) {
            return null;
        } else {
            mp.setDeleteAny(permissions.getDeleteAny());
            mp.setDeleteOwn(permissions.getDeleteOwn());
            mp.setRead(permissions.getRead());
            mp.setReadDrafts(permissions.getReadDrafts());
            mp.setReviseAny(permissions.getReviseAny());
            mp.setReviseOwn(permissions.getReviseOwn());
            mp.setMarkAsRead(permissions.getMarkAsRead());
        }
        mp.setRole(role);
        
        return mp;
    }

    /**
     * Get the default topic message permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public MessagePermissions getDefaultTopicMessagePermissionForRole(Topic topic, String role, String typeId) {
        MessagePermissions permissions = topic == null || topic.getId() == null ? null : getMessagePermissionByKeyValue(role, "topicId", topic.getId().toString(), true);
        MessagePermissions mp = new MessagePermissionsImpl();

        if (permissions == null) {
            return null;
        } else {
            mp.setDeleteAny(permissions.getDeleteAny());
            mp.setDeleteOwn(permissions.getDeleteOwn());
            mp.setRead(permissions.getRead());
            mp.setReadDrafts(permissions.getReadDrafts());
            mp.setReviseAny(permissions.getReviseAny());
            mp.setReviseOwn(permissions.getReviseOwn());
            mp.setMarkAsRead(permissions.getMarkAsRead());
        }
        mp.setRole(role);
        
        return mp;
    }

    /**
     * Create an empty topic message permission with system properties 
     * populated (ie: uuid).
     */
    public MessagePermissions createTopicMessagePermissionForRole(BaseForum forum, String role, String typeId) {
        MessagePermissions permissions = new MessagePermissionsImpl();
        MessagePermissions mp = getForumMessagePermissionForRole(forum, role, typeId);
        if (mp != null) {
            permissions.setDefaultValue(mp.getDefaultValue());
            permissions.setDeleteAny(mp.getDeleteAny());
            permissions.setDeleteOwn(mp.getDeleteOwn());
            permissions.setRead(mp.getRead());
            permissions.setReadDrafts(mp.getReadDrafts());
            permissions.setReviseAny(mp.getReviseAny());
            permissions.setReviseOwn(mp.getReviseOwn());
            permissions.setMarkAsRead(mp.getMarkAsRead());
            permissions.setRole(role);
        }
        return permissions;
    }
    
    /**
     * Save an topic message permission.  This is backed in the database by a single
     * message permission (used for areas, forums, and topics).
     */
    public void saveTopicMessagePermissionForRole(Topic topic, MessagePermissions permission) {
        MessagePermissions permissions = topic == null || topic.getId() == null ? null : getMessagePermissionByKeyValue(permission.getRole(), "topicId", topic.getId().toString(), false); 
        if (permissions == null) {
            permissions = new MessagePermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setTopic(topic);
        permissions.setDefaultValue(Boolean.FALSE);
        permissions.setDeleteAny(permission.getDeleteAny());
        permissions.setDeleteOwn(permission.getDeleteOwn());
        permissions.setRead(permission.getRead());
        permissions.setReadDrafts(permission.getReadDrafts());
        permissions.setReviseAny(permission.getReviseAny());
        permissions.setReviseOwn(permission.getReviseOwn());
        permissions.setMarkAsRead(permission.getMarkAsRead());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);
        
        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic, permissions), false));
        }                 
    }
    
    /**
     * Save a default topic message permission.  This is backed in the database by a 
     * single message permission (used for areas, forums, and topics).
     */
    public void saveDefaultTopicMessagePermissionForRole(Topic topic, MessagePermissions permission) {
        MessagePermissions permissions = topic == null || topic.getId() == null ? null : getMessagePermissionByKeyValue(permission.getRole(), "topicId", topic.getId().toString(), true); 
        if (permissions == null) {
            permissions = new MessagePermissionsImpl();
        }
        boolean isNew = permissions.getId() == null;
        
        permissions.setTopic(topic);
        permissions.setDefaultValue(Boolean.TRUE);
        permissions.setDeleteAny(permission.getDeleteAny());
        permissions.setDeleteOwn(permission.getDeleteOwn());
        permissions.setRead(permissions.getRead());
        permissions.setReadDrafts(permission.getReadDrafts());
        permissions.setReviseAny(permission.getReviseAny());
        permissions.setReviseOwn(permission.getReviseOwn());
        permissions.setMarkAsRead(permission.getMarkAsRead());
        permissions.setRole(permission.getRole());
        getHibernateTemplate().saveOrUpdate(permissions);                
        
        if (eventTrackingService == null) // SAK-12988
			throw new Error("eventTrackingService is null!");
        if (isNew) {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_ADD, getEventMessage(topic, permissions), false));
        } else {
            eventTrackingService.post(eventTrackingService.newEvent(DiscussionForumService.EVENT_FORUMS_TOPIC_REVISE, getEventMessage(topic, permissions), false));
        }
    }
    
    public MessagePermissions getAreaMessagePermissionByRoleAndType(final String roleId, final String typeId, final boolean defaultValue) {
        LOG.debug("getAreaMessagePermissionByRole executing for current user: " + getCurrentUser());
        final Area area = areaManager.getAreaByContextIdAndTypeId(typeId);
        if (area == null) {
            return null;
        }
        HibernateCallback hcb = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query q = session.getNamedQuery(QUERY_MP_BY_ROLE);
                q.setParameter("roleId", roleId, Hibernate.STRING);
                q.setParameter("areaId", area.getId().toString(), Hibernate.STRING);
                q.setParameter("defaultValue", new Boolean(defaultValue), Hibernate.BOOLEAN);
                return q.uniqueResult();
            }
        };
        return (MessagePermissions) getHibernateTemplate().execute(hcb);
    }
    
    
    
    // helpers

    private String getCurrentUser() {
        if (TestUtil.isRunningTests()) {
            return "test-user";
        }
        return sessionManager.getCurrentSessionUserId();
    }

    private String getEventMessage(Object parent, Object child) {
    	return "/MessageCenter/site/" + getContextId() + "/" + parent.toString() + "/" + child.toString() + "/" + getCurrentUser();
        //return "MessageCenter::" + getCurrentUser() + "::" + parent.toString() + "::" + child.toString();
    }  
    
    private String getContextId() {
      if (TestUtil.isRunningTests()) {
          return "test-context";
      }
      final Placement placement = ToolManager.getCurrentPlacement();
      final String presentSiteId = placement.getContext();
      return presentSiteId;
    }
}
