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

package org.sakaiproject.component.app.messageforums.model;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.model.ActorPermissionsModel;
import org.sakaiproject.api.app.messageforums.model.DateRestrictionsModel;
import org.sakaiproject.api.app.messageforums.model.DiscussionTopicModel;

public class DiscussionTopicModelImpl extends OpenTopicModelImpl implements DiscussionTopicModel {

    private static final Log LOG = LogFactory.getLog(DiscussionTopicModelImpl.class);
    
    private Boolean confidentialResponses;
    private Boolean mustRespondBeforeReading;
    private Integer hourBeforeResponsesVisible;
    private DateRestrictionsModel dateRestrictions;
    private ActorPermissionsModel actorPermissions;
    private List labels;
    private Boolean moderated;
    private String gradebook;
    private String gradebookAssignment;
    
    // package level constructor only used for Testing
    DiscussionTopicModelImpl() {}
    
    public DiscussionTopicModelImpl(DiscussionTopic discussionTopic) {
        // TODO: set up this model based on hibernate object passes
        
    }
    
    public ActorPermissionsModel getActorPermissions() {
        return actorPermissions;
    }

    public void setActorPermissions(ActorPermissionsModel actorPermissions) {
        this.actorPermissions = actorPermissions;
    }

    public Boolean getConfidentialResponses() {
        return confidentialResponses;
    }

    public void setConfidentialResponses(Boolean confidentialResponses) {
        this.confidentialResponses = confidentialResponses;
    }

    public DateRestrictionsModel getDateRestrictions() {
        return dateRestrictions;
    }

    public void setDateRestrictions(DateRestrictionsModel dateRestrictions) {
        this.dateRestrictions = dateRestrictions;
    }

    public String getGradebook() {
        return gradebook;
    }

    public void setGradebook(String gradebook) {
        this.gradebook = gradebook;
    }

    public String getGradebookAssignment() {
        return gradebookAssignment;
    }

    public void setGradebookAssignment(String gradebookAssignment) {
        this.gradebookAssignment = gradebookAssignment;
    }

    public Integer getHourBeforeResponsesVisible() {
        return hourBeforeResponsesVisible;
    }

    public void setHourBeforeResponsesVisible(Integer hourBeforeResponsesVisible) {
        this.hourBeforeResponsesVisible = hourBeforeResponsesVisible;
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

    public Boolean getMustRespondBeforeReading() {
        return mustRespondBeforeReading;
    }

    public void setMustRespondBeforeReading(Boolean mustRespondBeforeReading) {
        this.mustRespondBeforeReading = mustRespondBeforeReading;
    }

}
