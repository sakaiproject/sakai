/**********************************************************************************
* $URL: $
* $Id:  $
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

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.MessageForumsUser;
import org.sakaiproject.api.app.messageforums.PrivateMessage;

public class MessageForumsUserImpl implements MessageForumsUser {
    private Long id;
    private Integer version; 
    protected String uuid;    
    protected String userId;

    // foreign keys for hibernate
    private ActorPermissions apAccessors; 
    private ActorPermissions apContributors; 
    private ActorPermissions apModerators; 
    private PrivateMessage privateMessage;            
     
    // indecies for hibernate
    private int mesindex;    
    private int apmoderatorsindex;
    private int apcontributorsindex;
    private int apaccessorsindex;
    
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

}
