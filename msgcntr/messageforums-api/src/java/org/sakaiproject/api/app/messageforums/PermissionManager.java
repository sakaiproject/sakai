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
    public AreaControlPermission createAreaControlPermissionForRole(String role);
    
    /**
     * Save an area control permission.  This is backed in the database by a single
     * Control permission (used for areas, forums, and topics).
     */
    public void saveAreaControlPermissionForRole(Area area, AreaControlPermission permission, String typeId);

    /**
     * Create an empty default area control permission with system properties 
     * populated (ie: uuid).
     */
    public AreaControlPermission createDefaultAreaControlPermissionForRole(String role);

    /**
     * Save a default area control permission.  This is backed in the database by a 
     * single Control permission (used for areas, forums, and topics).
     */
    public void saveDefaultAreaControlPermissionForRole(Area area, AreaControlPermission permission, String typeId);

    /**
     * Get the forum control permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public ForumControlPermission getForumControlPermissionForRole(String role, String typeId);

    /**
     * Get the default forum control permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public ForumControlPermission getDefaultForumControlPermissionForRole(String role, String typeId);

    /**
     * Create an empty forum control permission with system properties 
     * populated (ie: uuid).
     */
    public ForumControlPermission createForumControlPermissionForRole(String role);
    
    /**
     * Save an forum control permission.  This is backed in the database by a single
     * Control permission (used for topics, forums, and topics).
     */
    public void saveForumControlPermissionForRole(BaseForum forum, ForumControlPermission permission);

    /**
     * Create an empty default forum control permission with system properties 
     * populated (ie: uuid).
     */
    public ForumControlPermission createDefaultForumControlPermissionForRole(String role);

    /**
     * Save a default forum control permission.  This is backed in the database by a 
     * single Control permission (used for topics, forums, and topics).
     */
    public void saveDefaultForumControlPermissionForRole(BaseForum forum, ForumControlPermission permission);
    
    /**
     * Get the topic control permission for a given role.  This provides the permissions
     * that the role currently has.
     */
    public TopicControlPermission getTopicControlPermissionForRole(String role, String typeId);

    /**
     * Get the default topic control permission for a given role.  This provides the 
     * permissions that the role currently has.
     */
    public TopicControlPermission getDefaultTopicControlPermissionForRole(String role, String typeId);

    /**
     * Create an empty topic control permission with system properties 
     * populated (ie: uuid).
     */
    public TopicControlPermission createTopicControlPermissionForRole(String role);
    
    /**
     * Save an topic control permission.  This is backed in the database by a single
     * Control permission (used for areas, forums, and topics).
     */
    public void saveTopicControlPermissionForRole(Topic topic, TopicControlPermission permission);

    /**
     * Create an empty default topic control permission with system properties 
     * populated (ie: uuid).
     */
    public TopicControlPermission createDefaultTopicControlPermissionForRole(String role);

    /**
     * Save a default topic control permission.  This is backed in the database by a 
     * single Control permission (used for areas, forums, and topics).
     */
    public void saveDefaultTopicControlPermissionForRole(Topic topic, TopicControlPermission permission);
    
    public ControlPermissions getAreaControlPermissionByRoleAndType(String roleId, String typeId, boolean defaultValue);

}
