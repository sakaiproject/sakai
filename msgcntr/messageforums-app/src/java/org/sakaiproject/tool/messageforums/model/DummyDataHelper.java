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

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.api.app.messageforums.model.ActorPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.ControlPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.DateRestrictionsModel;
import org.sakaiproject.api.app.messageforums.model.MessagePermissionsModel;

/*
 * This helper provides dummy data for use by interface developers
 * It uses model objects.  Models are hibernate object wrappers, 
 * which are used so that hibernate does not play dirty and try to
 * save objects on the interface.  They also uses List rather than Set,
 * which play nice in JSF tags.
 */

public class DummyDataHelper {
    
    public List getAreas() {
        return null;
    }
    
    public List getForumMessages() {
        return null;
    }
    
    public List getPrivateMessages() {
        return null;
    }
    
    public List getDiscussionForums() {
        return null;
    }
    
    public List getOpenForums() {
        return null;
    }
    
    public List getPrivateForums() {
        return null;
    }
    
    public List getDiscussionTopics() {
        return null;
    }

    public List getOpenTopics() {
        return null;
    }

    // helpers
    
    private List getAttachments() {
        return null;
    }
    
    private List getLabels() {
        return null;
    }
    
    private ActorPermissionsModel getActorPermissions() {
        ActorPermissionsModel apm = new ActorPermissionsModelImpl();
        apm.setAccessors(new ArrayList());
        apm.setContributors(new ArrayList());
        apm.setModerators(new ArrayList());
        apm.setId(new Long(123));
        return null;
    }
    
    private ControlPermissionsModel getControlPermissions() {
        return null;
    }

    private DateRestrictionsModel getDateRestrictions() {
        return null;
    }

    private MessagePermissionsModel getMessgePermissions() {
        return null;
    }
    
}
