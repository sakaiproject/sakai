/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-api/src/java/org/sakaiproject/api/app/messageforums/MessageForumsUser.java $
 * $Id: MessageForumsUser.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

public interface MessageForumsUser {
      
    public String getUserId();
    public void setUserId(String userId);
    public String getUuid();
    public void setUuid(String uuid);
    public PrivateMessage getPrivateMessage();
    public void setPrivateMessage(PrivateMessage privateMessage);
    public void setApmoderatorsindex(int apmoderatorsindex);
    public ActorPermissions getApAccessors();
    public void setApAccessors(ActorPermissions apAccessors);
    public ActorPermissions getApContributors();
    public void setApContributors(ActorPermissions apContributors);
    public ActorPermissions getApModerators();
    public void setApModerators(ActorPermissions apModerators);
    public String getTypeUuid();  
    public void setTypeUuid(String typeUuid);
}
