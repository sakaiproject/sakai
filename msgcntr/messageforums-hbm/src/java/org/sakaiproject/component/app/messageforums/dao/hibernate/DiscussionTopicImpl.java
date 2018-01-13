/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/DiscussionTopicImpl.java $
 * $Id: DiscussionTopicImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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
 
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.api.app.messageforums.ActorPermissions;
import org.sakaiproject.api.app.messageforums.DateRestrictions;
import org.sakaiproject.api.app.messageforums.DiscussionTopic;
import org.sakaiproject.api.app.messageforums.Label;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

@Slf4j
public class DiscussionTopicImpl extends OpenTopicImpl implements DiscussionTopic {

    private Boolean confidentialResponses;
    private Boolean mustRespondBeforeReading;
    private Integer hourBeforeResponsesVisible;
    private DateRestrictions dateRestrictions;
    private ActorPermissions actorPermissions;
    private List labels = new UniqueArrayList();
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
        if (log.isDebugEnabled()) {
            log.debug("addLabel(label " + label + ")");
        }
        
        if (label == null) {
            throw new IllegalArgumentException("topic == null");
        }
        
        label.setDiscussionTopic(this);
        labels.add(label);
    }

    public void removeLabel(Label label) {
        if (log.isDebugEnabled()) {
            log.debug("removeLabel(label " + label + ")");
        }
        
        if (label == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        
        label.setDiscussionTopic(null);
        labels.remove(label);
    }
}
