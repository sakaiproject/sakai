/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/MessageForumsUserImpl.java $
 * $Id: MessageForumsUserImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.PrivateMessage;
 

public class MessageForumsUserImpl implements MessageForumsUser {
    private Long id;
    private Integer version; 
    protected String uuid;    
    protected String userId;
    protected String typeUuid;

    // foreign keys for hibernate
    private ActorPermissions apAccessors; 
    private ActorPermissions apContributors; 
    private ActorPermissions apModerators; 
    private PrivateMessage privateMessage;            
     
    // Indices for hibernate
    private int apmoderatorsindex;
    private int apcontributorsindex;
    private int apaccessorsindex;
    
    
    /**
     * 
     */
    public MessageForumsUserImpl()
    {
    
     this.uuid=org.sakaiproject.id.cover.IdManager.createUuid();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getUserId()
    {
      return userId;
    }

    public void setUserId(String userId)
    {
      this.userId = userId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public PrivateMessage getPrivateMessage() {
        return privateMessage;
    }

    public void setPrivateMessage(PrivateMessage privateMessage) {
        this.privateMessage = privateMessage;
    }

//    public int getMesindex() {
//        try {
//            return getPrivateMessage().getRecipients().indexOf(this);
//        } catch (Exception e) {
//            return mesindex;
//        }
//    }
//
//    public void setMesindex(int mesindex) {
//        this.mesindex = mesindex;
//    }

    public int getApaccessorsindex() {
        try {
            return getApAccessors().getAccessors().indexOf(this);
        } catch (Exception e) {
            return apaccessorsindex;
        }
    }

    public void setApaccessorsindex(int apaccessorsindex) {
        this.apaccessorsindex = apaccessorsindex;
    }

    public int getApcontributorsindex() {
        try {
            return getApContributors().getContributors().indexOf(this);
        } catch (Exception e) {
            return apcontributorsindex;
        }
    }

    public void setApcontributorsindex(int apcontributorsindex) {
        this.apcontributorsindex = apcontributorsindex;
    }

    public int getApmoderatorsindex() {
        try {
            return getApModerators().getModerators().indexOf(this);
        } catch (Exception e) {
            return apmoderatorsindex;
        }
    }

    public void setApmoderatorsindex(int apmoderatorsindex) {
        this.apmoderatorsindex = apmoderatorsindex;
    }

    public ActorPermissions getApAccessors() {
        return apAccessors;
    }

    public void setApAccessors(ActorPermissions apAccessors) {
        this.apAccessors = apAccessors;
    }

    public ActorPermissions getApContributors() {
        return apContributors;
    }

    public void setApContributors(ActorPermissions apContributors) {
        this.apContributors = apContributors;
    }

    public ActorPermissions getApModerators() {
        return apModerators;
    }

    public void setApModerators(ActorPermissions apModerators) {
        this.apModerators = apModerators;
    }
    
    public String toString() {
    	return "User/" + id;
        //return "User.id:" + id;
    }

    /**
     * @return Returns the typeUuid.
     */
    public String getTypeUuid()
    {
      return typeUuid;
    }

    /**
     * @param typeUuid The typeUuid to set.
     */
    public void setTypeUuid(String typeUuid)
    {
      this.typeUuid = typeUuid;
    }
    
    
}
