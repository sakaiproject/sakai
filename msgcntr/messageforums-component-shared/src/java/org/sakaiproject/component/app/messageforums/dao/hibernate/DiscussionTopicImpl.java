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
 
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.DateRestrictions;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Label;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

public class DiscussionTopicImpl extends OpenTopicImpl implements DiscussionTopic {

    private static final Log LOG = LogFactory.getLog(DiscussionTopicImpl.class);
    
    private Boolean confidentialResponses;
    private Boolean mustRespondBeforeReading;
    private Integer hourBeforeResponsesVisible;
    private DateRestrictions dateRestrictions;
    private ActorPermissions actorPermissions;
    private List labels = new UniqueArrayList();
    private Boolean moderated;
    private String gradebook;
    private String gradebookAssignment;
    
    public ActorPermissions getActorPermissions() {
        return actorPermissions;
    }

    public void setActorPermissions(ActorPermissions actorPermissions) {
        this.actorPermissions = actorPermissions;
    }

    public Boolean getConfidentialResponses() {
        return confidentialResponses;
    }

    public void setConfidentialResponses(Boolean confidentialResponses) {
        this.confidentialResponses = confidentialResponses;
    }

    public DateRestrictions getDateRestrictions() {
        return dateRestrictions;
    }

    public void setDateRestrictions(DateRestrictions dateRestrictions) {
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
    
    ////////////////////////////////////////////////////////////////////////
    // helper methods for collections
    ////////////////////////////////////////////////////////////////////////
    
    public void addLabel(Label label) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addLabel(label " + label + ")");
        }
        
        if (label == null) {
            throw new IllegalArgumentException("topic == null");
        }
        
        label.setDiscussionTopic(this);
        labels.add(label);
    }

    public void removeLabel(Label label) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeLabel(label " + label + ")");
        }
        
        if (label == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        
        label.setDiscussionTopic(null);
        labels.remove(label);
    }
}
