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

package org.sakaiproject.tool.messageforums.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.MessagePermissions;
import org.sakaiproject.api.app.messageforums.model.MessagePermissionsModel;

public class MessagePermissionsModelImpl implements MessagePermissionsModel {

    private static final Log LOG = LogFactory.getLog(MessagePermissionsModelImpl.class);
     
    private String role;
    private Boolean doNew;
    private Boolean read;
    private Boolean reviseAny;
    private Boolean reviseOwn;
    private Boolean deleteAny;
    private Boolean deleteOwn;
    private Boolean readDrafts;
    private Long id; 

    // package level constructor only used for Testing
    MessagePermissionsModelImpl() {}
    
    public MessagePermissionsModelImpl(MessagePermissions messagePermissions) {
        // TODO: set up this model based on hibernate object passes
        
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getDeleteAny() {
        return deleteAny;
    }

    public void setDeleteAny(Boolean deleteAny) {
        this.deleteAny = deleteAny;
    }

    public Boolean getDeleteOwn() {
        return deleteOwn;
    }

    public void setDeleteOwn(Boolean deleteOwn) {
        this.deleteOwn = deleteOwn;
    }

    public Boolean getDoNew() {
        return doNew;
    }

    public void setDoNew(Boolean doNew) {
        this.doNew = doNew;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getReadDrafts() {
        return readDrafts;
    }

    public void setReadDrafts(Boolean readDrafts) {
        this.readDrafts = readDrafts;
    }

    public Boolean getReviseAny() {
        return reviseAny;
    }

    public void setReviseAny(Boolean reviseAny) {
        this.reviseAny = reviseAny;
    }

    public Boolean getReviseOwn() {
        return reviseOwn;
    }

    public void setReviseOwn(Boolean reviseOwn) {
        this.reviseOwn = reviseOwn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
