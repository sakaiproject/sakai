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


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.model.ActorPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.DateRestrictionsModel;
import org.sakaiproject.api.app.messageforums.model.DiscussionForumModel;

public class DiscussionForumModelImpl extends OpenForumModelImpl implements DiscussionForumModel {

    private static final Log LOG = LogFactory.getLog(DiscussionForumModelImpl.class);

    private List labels;
    private DateRestrictionsModel dateRestrictions;
    private ActorPermissionsModel actorPermissions;
    private Boolean moderated;
    
    public ActorPermissionsModel getActorPermissions() {
        return actorPermissions;
    }

    public void setActorPermissions(ActorPermissionsModel actorPermissions) {
        this.actorPermissions = actorPermissions;
    }

    public DateRestrictionsModel getDateRestrictions() {
        return dateRestrictions;
    }

    public void setDateRestrictions(DateRestrictionsModel dateRestrictions) {
        this.dateRestrictions = dateRestrictions;
    }

    public List getLabels() {
        return labels;
    }

    public void setLabels(List labels) {
        this.labels = labels;
    }

    public Boolean getModerated() {
        return moderated;
    }

    public void setModerated(Boolean moderated) {
        this.moderated = moderated;
    }

}
