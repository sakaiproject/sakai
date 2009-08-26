/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/UserPermissionManager.java $
 * $Id: UserPermissionManager.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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
package org.sakaiproject.api.app.messageforums;

/**
 *  @deprecated  seems never to have been used
*/

public interface UserPermissionManager {

    public static final String BEAN_NAME = "org.sakaiproject.api.app.messageforums.UserPermissionManager";
    
    public boolean canRead(Topic topic, String typeId);
    public boolean canReviseAny(Topic topic, String typeId);
    public boolean canReviseOwn(Topic topic, String typeId);
    public boolean canDeleteAny(Topic topic, String typeId);
    public boolean canDeleteOwn(Topic topic, String typeId);
    public boolean canMarkAsRead(Topic topic, String typeId);

    public boolean canRead(BaseForum forum, String typeId);
    public boolean canReviseAny(BaseForum forum, String typeId);
    public boolean canReviseOwn(BaseForum forum, String typeId);
    public boolean canDeleteAny(BaseForum forum, String typeId);
    public boolean canDeleteOwn(BaseForum forum, String typeId);
    public boolean canMarkAsRead(BaseForum forum, String typeId);

    public boolean canRead(Area area, String typeId);
    public boolean canReviseAny(Area area, String typeId);
    public boolean canReviseOwn(Area area, String typeId);
    public boolean canDeleteAny(Area area, String typeId);
    public boolean canDeleteOwn(Area area, String typeId);
    public boolean canMarkAsRead(Area area, String typeId);

    
    public boolean canNewResponse(Topic topic, String typeId);
    public boolean canResponseToResponse(Topic topic, String typeId);
    public boolean canMovePostings(Topic topic, String typeId);
    public boolean canChangeSettings(Topic topic, String typeId);
    public boolean canPostToGradebook(Topic topic, String typeId);

    public boolean canNewTopic(BaseForum forum, String typeId);
    public boolean canNewResponse(BaseForum forum, String typeId);
    public boolean canResponseToResponse(BaseForum forum, String typeId);
    public boolean canMovePostings(BaseForum forum, String typeId);
    public boolean canChangeSettings(BaseForum forum, String typeId);
    public boolean canPostToGradebook(BaseForum forum, String typeId);

    public boolean canNewForum(Area area, String typeId);
    public boolean canNewTopic(Area area, String typeId);
    public boolean canNewResponse(Area area, String typeId);
    public boolean canResponseToResponse(Area area, String typeId);
    public boolean canMovePostings(Area area, String typeId);
    public boolean canChangeSettings(Area area, String typeId);
    public boolean canPostToGradebook(Area area, String typeId);

}
