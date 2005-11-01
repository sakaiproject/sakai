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

package org.sakaiproject.api.app.messageforums;

import java.util.Set;


public interface DiscussionTopic {

    public ActorPermissions getActorPermissions();

    public void setActorPermissions(ActorPermissions actorPermissions);

    public Boolean getConfidentialResponses();

    public void setConfidentialResponses(Boolean confidentialResponses);

    public DateRestrictions getDateRestrictions();

    public void setDateRestrictions(DateRestrictions dateRestrictions);

    public String getGradebook();

    public void setGradebook(String gradebook);

    public String getGradebookAssignment();

    public void setGradebookAssignment(String gradebookAssignment);

    public Integer getHourBeforeResponsesVisible();

    public void setHourBeforeResponsesVisible(Integer hourBeforeResponsesVisible);

    public Set getLabels();

    public void setLabels(Set labels);

    public Boolean getModerated();

    public void setModerated(Boolean moderated);

    public Boolean getMustRespondBeforeReading();

    public void setMustRespondBeforeReading(Boolean mustRespondBeforeReading);

}