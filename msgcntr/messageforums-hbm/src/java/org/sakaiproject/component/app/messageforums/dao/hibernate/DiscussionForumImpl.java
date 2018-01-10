/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/msgcntr/trunk/messageforums-hbm/src/java/org/sakaiproject/component/app/messageforums/dao/hibernate/DiscussionForumImpl.java $
 * $Id: DiscussionForumImpl.java 9227 2006-05-15 15:02:42Z cwen@iupui.edu $
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
import org.sakaiproject.api.app.messageforums.DiscussionForum;
import org.sakaiproject.api.app.messageforums.Label;
import org.sakaiproject.api.app.messageforums.UniqueArrayList;

@Slf4j
public class DiscussionForumImpl extends OpenForumImpl implements DiscussionForum {

    private List labels = new UniqueArrayList();
    private DateRestrictions dateRestrictions;
    private ActorPermissions actorPermissions;
    private int areaindex;
    private Boolean autoMarkThreadsRead;
    
    public int getAreaindex() {
        try {
            return getArea().getDiscussionForums().indexOf(this);
        } catch (Exception e) {
            return areaindex;
        }
    }

    public void setAreaindex(int areaindex) {
        this.areaindex = areaindex;
    }
    
    public ActorPermissions getActorPermissions() {
        return actorPermissions;
    }

    public void setActorPermissions(ActorPermissions actorPermissions) {
        this.actorPermissions = actorPermissions;
    }

    public DateRestrictions getDateRestrictions() {
        return dateRestrictions;
    }

    public void setDateRestrictions(DateRestrictions dateRestrictions) {
        this.dateRestrictions = dateRestrictions;
    }

    public List getLabels() {
        return labels;
    }

    public void setLabels(List labels) {
        this.labels = labels;
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
        
        label.setDiscussionForum(this);
        labels.add(label);
    }

    public void removeLabel(Label label) {
        if (log.isDebugEnabled()) {
            log.debug("removeLabel(label " + label + ")");
        }
        
        if (label == null) {
            throw new IllegalArgumentException("Illegal topic argument passed!");
        }
        
        label.setDiscussionForum(null);
        labels.remove(label);
    }

	public Boolean getAutoMarkThreadsRead() {
		return autoMarkThreadsRead;
	}

	public void setAutoMarkThreadsRead(Boolean autoMarkThreadsRead) {
		this.autoMarkThreadsRead = autoMarkThreadsRead;
	}

}
