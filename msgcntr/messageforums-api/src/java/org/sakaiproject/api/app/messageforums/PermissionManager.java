/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/PermissionManager.java $
 * $Id: PermissionManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

public interface PermissionManager {

    /**
     * Get the area control permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public AreaControlPermission getAreaControlPermissionForRole(String role, String typeId);

    /**
     * Get the default area control permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public AreaControlPermission getDefaultAreaControlPermissionForRole(String role, String typeId);

    /**
     * Create an empty area control permission with system properties 
     * populated (ie: uuid).
     */
    public AreaControlPermission createAreaControlPermissionForRole(String role, String typeId);
    
    /**
     * Save an area control permission.  This is backed in the database by a single
     * Control permission (used for areas, forums, and topics).
     */
    public void saveAreaControlPermissionForRole(Area area, AreaControlPermission permission, String typeId);

    /**
     * Save a default area control permission.  This is backed in the database by a 
     * single Control permission (used for areas, forums, and topics).
     */
    public void saveDefaultAreaControlPermissionForRole(Area area, AreaControlPermission permission, String typeId);

    /**
     * Get the forum control permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public ForumControlPermission getForumControlPermissionForRole(BaseForum forum, String role, String typeId);

    /**
     * Get the default forum control permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public ForumControlPermission getDefaultForumControlPermissionForRole(BaseForum forum, String role, String typeId);

    /**
     * Create an empty forum control permission with system properties 
     * populated (ie: uuid).
     */
    public ForumControlPermission createForumControlPermissionForRole(String role, String typeId);
    
    /**
     * Save an forum control permission.  This is backed in the database by a single
     * Control permission (used for topics, forums, and topics).
     */
    public void saveForumControlPermissionForRole(BaseForum forum, ForumControlPermission permission);

    /**
     * Save a default forum control permission.  This is backed in the database by a 
     * single Control permission (used for topics, forums, and topics).
     */
    public void saveDefaultForumControlPermissionForRole(BaseForum forum, ForumControlPermission permission);
    
    /**
     * Get the topic control permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public TopicControlPermission getTopicControlPermissionForRole(Topic topic, String role, String typeId);

    /**
     * Get the default topic control permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public TopicControlPermission getDefaultTopicControlPermissionForRole(Topic topic, String role, String typeId);

    /**
     * Create an empty topic control permission with system properties 
     * populated (ie: uuid).
     */
    public TopicControlPermission createTopicControlPermissionForRole(BaseForum forum, String role, String typeId);
    
    /**
     * Save an topic control permission.  This is backed in the database by a single
     * Control permission (used for areas, forums, and topics).
     */
    public void saveTopicControlPermissionForRole(Topic topic, TopicControlPermission permission);

    /**
     * Save a default topic control permission.  This is backed in the database by a 
     * single Control permission (used for areas, forums, and topics).
     */
    public void saveDefaultTopicControlPermissionForRole(Topic topic, TopicControlPermission permission);
    
    public ControlPermissions getAreaControlPermissionByRoleAndType(String roleId, String typeId, boolean defaultValue);

    /**
     * Get the area message permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public MessagePermissions getAreaMessagePermissionForRole(String role, String typeId);

    /**
     * Get the default area message permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public MessagePermissions getDefaultAreaMessagePermissionForRole(String role, String typeId);

    /**
     * Create an empty area message permission with system properties 
     * populated (ie: uuid).
     */
    public MessagePermissions createAreaMessagePermissionForRole(String role, String typeId);
    
    /**
     * Save an area message permission.  This is backed in the database by a single
     * message permission (used for areas, forums, and topics).
     */
    public void saveAreaMessagePermissionForRole(Area area, MessagePermissions permission, String typeId);

    /**
     * Save a default area message permission.  This is backed in the database by a 
     * single message permission (used for areas, forums, and topics).
     */
    public void saveDefaultAreaMessagePermissionForRole(Area area, MessagePermissions permission, String typeId);

    /**
     * Get the forum message permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public MessagePermissions getForumMessagePermissionForRole(BaseForum forum, String role, String typeId);

    /**
     * Get the default forum message permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public MessagePermissions getDefaultForumMessagePermissionForRole(BaseForum forum, String role, String typeId);

    /**
     * Create an empty forum message permission with system properties 
     * populated (ie: uuid).
     */
    public MessagePermissions createForumMessagePermissionForRole(String role, String typeId);
    
    /**
     * Save an forum message permission.  This is backed in the database by a single
     * message permission (used for topics, forums, and topics).
     */
    public void saveForumMessagePermissionForRole(BaseForum forum, MessagePermissions permission);

    /**
     * Save a default forum message permission.  This is backed in the database by a 
     * single message permission (used for topics, forums, and topics).
     */
    public void saveDefaultForumMessagePermissionForRole(BaseForum forum, MessagePermissions permission);
    
    /**
     * Get the topic message permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public MessagePermissions getTopicMessagePermissionForRole(Topic topic, String role, String typeId);

    /**
     * Get the default topic message permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public MessagePermissions getDefaultTopicMessagePermissionForRole(Topic topic, String role, String typeId);

    /**
     * Create an empty topic message permission with system properties 
     * populated (ie: uuid).
     */
    public MessagePermissions createTopicMessagePermissionForRole(BaseForum forum, String role, String typeId);
    
    /**
     * Save an topic message permission.  This is backed in the database by a single
     * message permission (used for areas, forums, and topics).
     */
    public void saveTopicMessagePermissionForRole(Topic topic, MessagePermissions permission);

    /**
     * Save a default topic message permission.  This is backed in the database by a 
     * single message permission (used for areas, forums, and topics).
     */
    public void saveDefaultTopicMessagePermissionForRole(Topic topic, MessagePermissions permission);
    
    public MessagePermissions getAreaMessagePermissionByRoleAndType(String roleId, String typeId, boolean defaultValue);

}
