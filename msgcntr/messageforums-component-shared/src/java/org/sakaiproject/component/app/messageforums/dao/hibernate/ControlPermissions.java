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

public class ControlPermissions {

    private static final Log LOG = LogFactory.getLog(ControlPermissions.class);    
  
    private String role;
    private Boolean newTopic;
    private Boolean newResponse;
    private Boolean responseToResponse;
    private Boolean movePostings;
    private Boolean changeSettings;

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
    
    public Boolean getChangeSettings() {
        return changeSettings;
    }

    public void setChangeSettings(Boolean changeSettings) {
        this.changeSettings = changeSettings;
    }

    public Boolean getMovePostings() {
        return movePostings;
    }

    public void setMovePostings(Boolean movePostings) {
        this.movePostings = movePostings;
    }

    public Boolean getNewResponse() {
        return newResponse;
    }

    public void setNewResponse(Boolean newResponse) {
        this.newResponse = newResponse;
    }

    public Boolean getNewTopic() {
        return newTopic;
    }

    public void setNewTopic(Boolean newTopic) {
        this.newTopic = newTopic;
    }

    public Boolean getResponseToResponse() {
        return responseToResponse;
    }

    public void setResponseToResponse(Boolean responseToResponse) {
        this.responseToResponse = responseToResponse;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
        
}
